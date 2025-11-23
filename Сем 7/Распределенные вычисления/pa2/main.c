#include <assert.h>
#include <errno.h>
#include <fcntl.h>
#include <getopt.h>
#include <stdarg.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

#include "banking.h"
#include "common.h"
#include "ipc_ext.h"
#include "pa2345.h"

typedef struct {
    IPCContext ipc;
    balance_t balances[MAX_PROCESS_ID + 1];
} SharedContext;

static SharedContext g_ctx;
static FILE *events_fp = NULL;
static FILE *pipes_fp = NULL;

static void log_event(const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    vprintf(fmt, ap);
    va_end(ap);

    if (events_fp) {
        va_start(ap, fmt);
        vfprintf(events_fp, fmt, ap);
        va_end(ap);
        fflush(events_fp);
    }
}

static void set_nonblocking(int fd) {
    int flags = fcntl(fd, F_GETFL);
    if (flags == -1) {
        perror("fcntl(F_GETFL)");
        exit(EXIT_FAILURE);
    }
    if (fcntl(fd, F_SETFL, flags | O_NONBLOCK) == -1) {
        perror("fcntl(F_SETFL)");
        exit(EXIT_FAILURE);
    }
}

static void init_pipes_or_die(SharedContext *ctx) {
    pipes_fp = fopen(pipes_log, "w");
    for (local_id s = 0; s < ctx->ipc.process_count; ++s) {
        for (local_id d = 0; d < ctx->ipc.process_count; ++d) {
            if (s == d) {
                ctx->ipc.pipes[s][d][0] = -1;
                ctx->ipc.pipes[s][d][1] = -1;
                continue;
            }
            int fds[2];
            if (pipe(fds) == -1) {
                perror("pipe");
                exit(EXIT_FAILURE);
            }
            set_nonblocking(fds[0]);
            set_nonblocking(fds[1]);
            ctx->ipc.pipes[s][d][0] = fds[0];
            ctx->ipc.pipes[s][d][1] = fds[1];
            if (pipes_fp) {
                fprintf(pipes_fp, "%d -> %d: read=%d, write=%d\n",
                        s, d, fds[0], fds[1]);
            }
        }
    }
    if (pipes_fp) {
        fflush(pipes_fp);
        fclose(pipes_fp);
        pipes_fp = NULL;
    }
}

static void close_fd(int *fd) {
    if (*fd >= 0) {
        close(*fd);
        *fd = -1;
    }
}

static void close_irrelevant_fds(IPCContext *ipc) {
    for (local_id s = 0; s < ipc->process_count; ++s) {
        for (local_id d = 0; d < ipc->process_count; ++d) {
            if (s == d) {
                continue;
            }
            if (s != ipc->id) {
                close_fd(&ipc->pipes[s][d][1]);
            }
            if (d != ipc->id) {
                close_fd(&ipc->pipes[s][d][0]);
            }
        }
    }
}

static void compose_message(Message *msg, MessageType type,
                            timestamp_t time, const void *payload,
                            uint16_t payload_len) {
    memset(msg, 0, sizeof(*msg));
    msg->s_header.s_magic = MESSAGE_MAGIC;
    msg->s_header.s_type = type;
    msg->s_header.s_local_time = time;
    msg->s_header.s_payload_len = payload_len;
    if (payload_len > 0 && payload != NULL) {
        memcpy(msg->s_payload, payload, payload_len);
    }
}

static void history_append(BalanceHistory *history, timestamp_t time,
                           balance_t balance) {
    uint16_t len = history->s_history_len;
    if (len >= MAX_T) {
        BalanceState *state = &history->s_history[MAX_T - 1];
        state->s_time = time;
        state->s_balance = balance;
        state->s_balance_pending_in = 0;
        return;
    }
    BalanceState *state = &history->s_history[len];
    state->s_time = time;
    state->s_balance = balance;
    state->s_balance_pending_in = 0;
    history->s_history_len = (uint8_t)(len + 1);
}

static void history_update(BalanceHistory *history, timestamp_t time,
                           balance_t balance) {
    if (history->s_history_len == 0) {
        for (timestamp_t t = 0; t <= time; ++t) {
            history_append(history, t, balance);
        }
        return;
    }

    BalanceState last = history->s_history[history->s_history_len - 1];
    if (time < last.s_time) {
        time = last.s_time;
    }

    balance_t last_balance = last.s_balance;
    for (timestamp_t t = last.s_time + 1; t < time; ++t) {
        history_append(history, t, last_balance);
    }

    if (time == last.s_time) {
        history->s_history[history->s_history_len - 1].s_balance = balance;
    } else {
        history_append(history, time, balance);
    }
}

static void child_work(SharedContext *ctx) {
    IPCContext *ipc = &ctx->ipc;
    local_id id = ipc->id;
    balance_t balance = ctx->balances[id];

    BalanceHistory history;
    memset(&history, 0, sizeof(history));
    history.s_id = id;
    history_update(&history, get_physical_time(), balance);

    close_irrelevant_fds(ipc);

    char payload[MAX_PAYLOAD_LEN];

    timestamp_t now = get_physical_time();
    snprintf(payload, sizeof(payload), log_started_fmt, now, id, getpid(),
             getppid(), balance);
    log_event("%s", payload);

    Message msg;
    compose_message(&msg, STARTED, now, payload, (uint16_t)strlen(payload));
    send_multicast(ipc, &msg);

    for (local_id peer = 1; peer < ipc->process_count; ++peer) {
        if (peer == id) {
            continue;
        }
        while (receive(ipc, peer, &msg) != 0 || msg.s_header.s_type != STARTED) {
            // keep receiving until STARTED from peer
        }
    }
    now = get_physical_time();
    log_event(log_received_all_started_fmt, now, id);

    bool sent_done = false;
    int done_expected = ipc->process_count - 2;
    int done_count = 0;
    uint32_t done_mask = 0;

    while (1) {
        if (receive_any(ipc, &msg) != 0) {
            continue;
        }
        local_id from = ipc->last_sender;
        switch (msg.s_header.s_type) {
            case TRANSFER: {
                const TransferOrder *order =
                    (const TransferOrder *)msg.s_payload;
                if (order->s_src == id) {
                    balance -= order->s_amount;
                    now = get_physical_time();
                    history_update(&history, now, balance);
                    log_event(log_transfer_out_fmt, now, id,
                              order->s_amount, order->s_dst);

                    Message forward;
                    compose_message(&forward, TRANSFER, now, order,
                                    sizeof(*order));
                    send(ipc, order->s_dst, &forward);
                } else if (order->s_dst == id) {
                    balance += order->s_amount;
                    now = get_physical_time();
                    history_update(&history, now, balance);
                    log_event(log_transfer_in_fmt, now, id,
                              order->s_amount, order->s_src);

                    Message ack;
                    compose_message(&ack, ACK, now, NULL, 0);
                    send(ipc, PARENT_ID, &ack);
                }
                break;
            }
            case STOP:
                if (!sent_done) {
                    now = get_physical_time();
                    history_update(&history, now, balance);
                    snprintf(payload, sizeof(payload), log_done_fmt, now, id,
                             balance);
                    log_event("%s", payload);

                    compose_message(&msg, DONE, now, payload,
                                    (uint16_t)strlen(payload));
                    send_multicast(ipc, &msg);
                    sent_done = true;
                }
                break;
            case DONE:
                if (from != id && !(done_mask & (1u << from))) {
                    done_mask |= (1u << from);
                    ++done_count;
                }
                break;
            default:
                break;
        }

        if (sent_done && done_count == done_expected) {
            break;
        }
    }

    now = get_physical_time();
    history_update(&history, now, balance);
    log_event(log_received_all_done_fmt, now, id);

    Message history_msg;
    uint16_t history_len =
        (uint16_t)(sizeof(history.s_id) + sizeof(history.s_history_len) +
                   history.s_history_len * sizeof(BalanceState));
    compose_message(&history_msg, BALANCE_HISTORY, now, &history, history_len);
    send(ipc, PARENT_ID, &history_msg);

    close_irrelevant_fds(ipc);
    _exit(EXIT_SUCCESS);
}

static void parent_collect_histories(SharedContext *ctx, int children) {
    IPCContext *ipc = &ctx->ipc;
    AllHistory all;
    memset(&all, 0, sizeof(all));
    all.s_history_len = (uint8_t)children;

    for (local_id child = 1; child <= children; ++child) {
        Message msg;
        receive(ipc, child, &msg);
        if (msg.s_header.s_type != BALANCE_HISTORY) {
            --child;
            continue;
        }
        BalanceHistory *dst = &all.s_history[child - 1];
        memset(dst, 0, sizeof(*dst));
        memcpy(dst, msg.s_payload, msg.s_header.s_payload_len);
    }

    print_history(&all);
}

static void parent_work(SharedContext *ctx, int children) {
    IPCContext *ipc = &ctx->ipc;
    close_irrelevant_fds(ipc);

    Message msg;
    for (local_id child = 1; child <= children; ++child) {
        receive(ipc, child, &msg);
    }
    timestamp_t now = get_physical_time();
    log_event(log_received_all_started_fmt, now, PARENT_ID);

    bank_robbery(ipc, (local_id)children);

    Message stop_msg;
    for (local_id child = 1; child <= children; ++child) {
        now = get_physical_time();
        compose_message(&stop_msg, STOP, now, NULL, 0);
        send(ipc, child, &stop_msg);
    }

    for (local_id child = 1; child <= children; ++child) {
        receive(ipc, child, &msg);
    }
    now = get_physical_time();
    log_event(log_received_all_done_fmt, now, PARENT_ID);

    parent_collect_histories(ctx, children);
}

static void parse_args(int argc, char **argv, SharedContext *ctx,
                       int *children) {
    int opt;
    int count = 0;
    opterr = 0;
    while ((opt = getopt(argc, argv, "p:")) != -1) {
        if (opt == 'p') {
            count = atoi(optarg);
        } else {
            fprintf(stderr, "Usage: %s -p N balance1 ... balanceN\n", argv[0]);
            exit(EXIT_FAILURE);
        }
    }
    if (count <= 0 || count >= MAX_PROCESS_ID) {
        fprintf(stderr, "Invalid process count: %d\n", count);
        exit(EXIT_FAILURE);
    }
    *children = count;
    if (argc - optind < count) {
        fprintf(stderr, "Not enough initial balances provided\n");
        exit(EXIT_FAILURE);
    }
    for (int i = 1; i <= count; ++i) {
        ctx->balances[i] = (balance_t)atoi(argv[optind + i - 1]);
    }
}

void transfer(void *parent_data, local_id src, local_id dst, balance_t amount) {
    IPCContext *ipc = (IPCContext *)parent_data;
    TransferOrder order = {src, dst, amount};

    timestamp_t now = get_physical_time();
    Message msg;
    compose_message(&msg, TRANSFER, now, &order, sizeof(order));
    send(ipc, src, &msg);

    Message ack;
    do {
        receive(ipc, dst, &ack);
    } while (ack.s_header.s_type != ACK);
}

int main(int argc, char **argv) {
    int children = 0;
    memset(&g_ctx, 0, sizeof(g_ctx));

    parse_args(argc, argv, &g_ctx, &children);

    g_ctx.ipc.id = PARENT_ID;
    g_ctx.ipc.process_count = (uint8_t)(children + 1);
    g_ctx.ipc.last_sender = PARENT_ID;

    events_fp = fopen(events_log, "w");
    if (!events_fp) {
        perror("fopen events.log");
        exit(EXIT_FAILURE);
    }

    init_pipes_or_die(&g_ctx);

    for (local_id id = 1; id <= children; ++id) {
        pid_t pid = fork();
        if (pid < 0) {
            perror("fork");
            exit(EXIT_FAILURE);
        }
        if (pid == 0) {
            g_ctx.ipc.id = id;
            child_work(&g_ctx);
        }
    }

    parent_work(&g_ctx, children);

    for (int i = 0; i < children; ++i) {
        wait(NULL);
    }

    if (events_fp) {
        fclose(events_fp);
        events_fp = NULL;
    }
    return EXIT_SUCCESS;
}


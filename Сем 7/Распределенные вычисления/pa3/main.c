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
#include "lamport.h"
#include "pa2345.h"

typedef struct {
    IPCContext ipc;
    balance_t balances[MAX_PROCESS_ID + 1];
} SharedContext;

typedef struct {
    balance_t balance;
    balance_t pending;
    BalanceHistory history;
} AccountState;

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
                            const void *payload, uint16_t payload_len) {
    memset(msg, 0, sizeof(*msg));
    msg->s_header.s_magic = MESSAGE_MAGIC;
    msg->s_header.s_type = type;
    msg->s_header.s_payload_len = payload_len;
    if (payload_len > 0 && payload != NULL) {
        memcpy(msg->s_payload, payload, payload_len);
    }
}

static void history_record(AccountState *state, timestamp_t time) {
    BalanceHistory *hist = &state->history;
    if (hist->s_history_len == 0) {
        BalanceState *slot = &hist->s_history[0];
        slot->s_time = 0;
        slot->s_balance = state->balance;
        slot->s_balance_pending_in = state->pending;
        hist->s_history_len = 1;
    }
    if (time > MAX_T) {
        time = MAX_T;
    }
    if (time >= hist->s_history_len) {
        BalanceState last = hist->s_history[hist->s_history_len - 1];
        for (timestamp_t t = hist->s_history_len; t <= time; ++t) {
            hist->s_history[t] = last;
            hist->s_history[t].s_time = t;
        }
        hist->s_history_len = (uint8_t)(time + 1);
    }
    hist->s_history[time].s_balance = state->balance;
    hist->s_history[time].s_balance_pending_in = state->pending;
}

static void history_add_pending(AccountState *state,
                                timestamp_t from,
                                timestamp_t to,
                                balance_t amount) {
    if (to <= from) {
        return;
    }
    if (from > MAX_T) {
        return;
    }
    if (to - 1 > MAX_T) {
        to = MAX_T + 1;
    }
    BalanceHistory *hist = &state->history;
    if (hist->s_history_len == 0) {
        history_record(state, 0);
    }
    if (hist->s_history_len < to) {
        BalanceState last = hist->s_history[hist->s_history_len - 1];
        for (timestamp_t t = hist->s_history_len; t < to; ++t) {
            if (t > MAX_T) {
                break;
            }
            hist->s_history[t] = last;
            hist->s_history[t].s_time = t;
        }
        hist->s_history_len = (uint8_t)((to - 1 > MAX_T) ? MAX_T + 1 : to);
    }
    for (timestamp_t t = from; t < to; ++t) {
        if (t > MAX_T) {
            break;
        }
        hist->s_history[t].s_balance_pending_in += amount;
    }
}

static void account_init(AccountState *state, local_id id, balance_t initial) {
    state->balance = initial;
    state->pending = 0;
    memset(&state->history, 0, sizeof(state->history));
    state->history.s_id = id;
    history_record(state, 0);
}

static void child_broadcast_started(IPCContext *ipc,
                                    AccountState *account,
                                    local_id id,
                                    balance_t balance) {
    char payload[MAX_PAYLOAD_LEN];
    timestamp_t event_time = lamport_peek_next();
    snprintf(payload, sizeof(payload), log_started_fmt,
             event_time, id, getpid(), getppid(), balance);

    Message msg;
    compose_message(&msg, STARTED, payload, (uint16_t)strlen(payload));
    send_multicast(ipc, &msg);

    history_record(account, get_lamport_time());
    log_event("%s", payload);
}

static void child_send_done(IPCContext *ipc,
                            AccountState *account,
                            local_id id,
                            balance_t balance) {
    char payload[MAX_PAYLOAD_LEN];
    timestamp_t event_time = lamport_peek_next();
    snprintf(payload, sizeof(payload), log_done_fmt,
             event_time, id, balance);

    Message msg;
    compose_message(&msg, DONE, payload, (uint16_t)strlen(payload));
    send_multicast(ipc, &msg);
    history_record(account, get_lamport_time());
    log_event("%s", payload);
}

static void child_work(SharedContext *ctx) {
    IPCContext *ipc = &ctx->ipc;
    local_id id = ipc->id;
    AccountState account;

    lamport_reset();
    account_init(&account, id, ctx->balances[id]);

    close_irrelevant_fds(ipc);

    child_broadcast_started(ipc, &account, id, account.balance);

    Message msg;
    for (local_id peer = 1; peer < ipc->process_count; ++peer) {
        if (peer == id) {
            continue;
        }
        do {
            receive(ipc, peer, &msg);
        } while (msg.s_header.s_type != STARTED);
        history_record(&account, get_lamport_time());
    }
    log_event(log_received_all_started_fmt, get_lamport_time(), id);

    bool done_sent = false;
    int awaited_done = ipc->process_count - 2;
    int done_received = 0;
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
                if (order->s_src == id && from == PARENT_ID) {
                    history_record(&account, get_lamport_time());
                    account.balance -= order->s_amount;
                    Message forward;
                    compose_message(&forward, TRANSFER, order,
                                    sizeof(*order));
                    send(ipc, order->s_dst, &forward);
                    history_record(&account, get_lamport_time());
                    log_event(log_transfer_out_fmt,
                              get_lamport_time(), id,
                              order->s_amount, order->s_dst);
                } else if (order->s_dst == id) {
                    timestamp_t send_time = msg.s_header.s_local_time;
                    timestamp_t recv_time = get_lamport_time();
                    history_add_pending(&account, send_time, recv_time,
                                        order->s_amount);
                    account.balance += order->s_amount;
                    history_record(&account, recv_time);
                    log_event(log_transfer_in_fmt,
                              recv_time, id,
                              order->s_amount, order->s_src);

                    Message ack;
                    compose_message(&ack, ACK, NULL, 0);
                    send(ipc, PARENT_ID, &ack);
                    history_record(&account, get_lamport_time());
                }
                break;
            }
            case STOP:
                if (!done_sent) {
                    history_record(&account, get_lamport_time());
                    child_send_done(ipc, &account, id, account.balance);
                    done_sent = true;
                }
                break;
            case DONE:
                if (from != id && !(done_mask & (1u << from))) {
                    done_mask |= (1u << from);
                    ++done_received;
                }
                history_record(&account, get_lamport_time());
                break;
            default:
                break;
        }

        if (done_sent && done_received == awaited_done) {
            break;
        }
    }

    log_event(log_received_all_done_fmt, get_lamport_time(), id);

    history_record(&account, lamport_peek_next());
    Message history_msg;
    uint16_t payload_len =
        (uint16_t)(sizeof(account.history.s_id) +
                   sizeof(account.history.s_history_len) +
                   account.history.s_history_len *
                       sizeof(BalanceState));
    compose_message(&history_msg, BALANCE_HISTORY,
                    &account.history, payload_len);
    send(ipc, PARENT_ID, &history_msg);

    close_irrelevant_fds(ipc);
    _exit(EXIT_SUCCESS);
}

static void collect_histories(SharedContext *ctx, int children) {
    IPCContext *ipc = &ctx->ipc;
    AllHistory all;
    memset(&all, 0, sizeof(all));
    all.s_history_len = (uint8_t)children;

    timestamp_t global_max = 0;
    for (local_id child = 1; child <= children; ++child) {
        Message msg;
        receive(ipc, child, &msg);
        if (msg.s_header.s_type != BALANCE_HISTORY) {
            --child;
            continue;
        }
        BalanceHistory *dst = &all.s_history[child - 1];
        memcpy(dst, msg.s_payload, msg.s_header.s_payload_len);
        if (dst->s_history_len > 0) {
            BalanceState last = dst->s_history[dst->s_history_len - 1];
            if (last.s_time > global_max) {
                global_max = last.s_time;
            }
        }
    }

    for (local_id child = 0; child < all.s_history_len; ++child) {
        BalanceHistory *hist = &all.s_history[child];
        if (hist->s_history_len == 0) {
            continue;
        }
        BalanceState last = hist->s_history[hist->s_history_len - 1];
        for (timestamp_t t = last.s_time + 1;
             t <= global_max && t <= MAX_T; ++t) {
            if (hist->s_history_len > MAX_T) {
                break;
            }
            BalanceState *state = &hist->s_history[hist->s_history_len++];
            state->s_time = t;
            state->s_balance = last.s_balance;
            state->s_balance_pending_in = last.s_balance_pending_in;
        }
    }

    print_history(&all);
}

static void parent_work(SharedContext *ctx, int children) {
    IPCContext *ipc = &ctx->ipc;
    lamport_reset();
    close_irrelevant_fds(ipc);

    Message msg;
    for (local_id child = 1; child <= children; ++child) {
        receive(ipc, child, &msg);
    }
    log_event(log_received_all_started_fmt, get_lamport_time(), PARENT_ID);

    bank_robbery(ipc, (local_id)children);

    Message stop_msg;
    compose_message(&stop_msg, STOP, NULL, 0);
    for (local_id child = 1; child <= children; ++child) {
        send(ipc, child, &stop_msg);
    }

    for (local_id child = 1; child <= children; ++child) {
        receive(ipc, child, &msg);
    }
    log_event(log_received_all_done_fmt, get_lamport_time(), PARENT_ID);

    collect_histories(ctx, children);
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
            fprintf(stderr,
                    "Usage: %s -p N balance1 ... balanceN\n", argv[0]);
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

    Message msg;
    compose_message(&msg, TRANSFER, &order, sizeof(order));
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

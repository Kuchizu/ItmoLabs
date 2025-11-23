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

#include "common.h"
#include "ipc_ext.h"
#include "lamport.h"
#include "pa2345.h"

typedef struct {
    IPCContext ipc;
    bool use_mutex;
    int child_count;
} GlobalState;

static GlobalState g_state;
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

static void init_pipes_or_die(GlobalState *state) {
    pipes_fp = fopen(pipes_log, "w");
    IPCContext *ipc = &state->ipc;
    for (local_id s = 0; s < ipc->process_count; ++s) {
        for (local_id d = 0; d < ipc->process_count; ++d) {
            if (s == d) {
                ipc->pipes[s][d][0] = -1;
                ipc->pipes[s][d][1] = -1;
                continue;
            }
            int fds[2];
            if (pipe(fds) == -1) {
                perror("pipe");
                exit(EXIT_FAILURE);
            }
            set_nonblocking(fds[0]);
            set_nonblocking(fds[1]);
            ipc->pipes[s][d][0] = fds[0];
            ipc->pipes[s][d][1] = fds[1];
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

static bool fetch_message(IPCContext *ipc, Message *msg, local_id *from) {
    if (lamport_pop_pending(msg, from)) {
        return true;
    }
    if (receive_any(ipc, msg) != 0) {
        return false;
    }
    *from = ipc->last_sender;
    return true;
}

static void wait_for_started_children(GlobalState *state) {
    IPCContext *ipc = &state->ipc;
    int remaining = state->child_count - 1;
    bool seen[MAX_PROCESS_ID + 1] = {false};
    Message msg;
    local_id from = PARENT_ID;

    while (remaining > 0) {
        if (!fetch_message(ipc, &msg, &from)) {
            continue;
        }
        if (lamport_handle_message(ipc, &msg, from)) {
            continue;
        }
        if (msg.s_header.s_type == STARTED && from != ipc->id) {
            if (!seen[from]) {
                seen[from] = true;
                --remaining;
            }
            continue;
        }
        lamport_store_message(&msg, from);
    }
}

static void wait_for_done_children(GlobalState *state) {
    IPCContext *ipc = &state->ipc;
    int remaining = state->child_count - 1;
    bool seen[MAX_PROCESS_ID + 1] = {false};
    Message msg;
    local_id from = PARENT_ID;

    while (remaining > 0) {
        if (!fetch_message(ipc, &msg, &from)) {
            continue;
        }
        if (lamport_handle_message(ipc, &msg, from)) {
            continue;
        }
        if (msg.s_header.s_type == DONE && from != ipc->id) {
            if (!seen[from]) {
                seen[from] = true;
                --remaining;
            }
            continue;
        }
        lamport_store_message(&msg, from);
    }
}

static void wait_for_children_messages(GlobalState *state, MessageType type) {
    IPCContext *ipc = &state->ipc;
    Message msg;
    for (local_id child = 1; child <= state->child_count; ++child) {
        while (receive(ipc, child, &msg) != 0 ||
               (MessageType)msg.s_header.s_type != type) {
            /* keep receiving until expected type */
        }
    }
}

static void child_broadcast_started(GlobalState *state) {
    IPCContext *ipc = &state->ipc;
    local_id id = ipc->id;
    char payload[MAX_PAYLOAD_LEN];
    timestamp_t time = lamport_peek_next();
    snprintf(payload, sizeof(payload), log_started_fmt,
             time, id, getpid(), getppid(), id);

    Message msg;
    compose_message(&msg, STARTED, payload, (uint16_t)strlen(payload));
    send_multicast(ipc, &msg);
    log_event("%s", payload);
}

static void child_broadcast_done(GlobalState *state) {
    IPCContext *ipc = &state->ipc;
    local_id id = ipc->id;
    char payload[MAX_PAYLOAD_LEN];
    timestamp_t time = lamport_peek_next();
    snprintf(payload, sizeof(payload), log_done_fmt,
             time, id, id);

    Message msg;
    compose_message(&msg, DONE, payload, (uint16_t)strlen(payload));
    send_multicast(ipc, &msg);
    log_event("%s", payload);
}

static void child_main(GlobalState *state) {
    IPCContext *ipc = &state->ipc;
    local_id id = ipc->id;

    lamport_reset();
    lamport_cs_init(id, ipc->process_count);
    close_irrelevant_fds(ipc);

    child_broadcast_started(state);
    wait_for_started_children(state);
    log_event(log_received_all_started_fmt, get_lamport_time(), id);

    int iterations = id * 5;
    char buffer[256];
    for (int i = 1; i <= iterations; ++i) {
        if (state->use_mutex) {
            request_cs(ipc);
        }
        snprintf(buffer, sizeof(buffer), log_loop_operation_fmt,
                 id, i, iterations);
        print(buffer);
        if (state->use_mutex) {
            release_cs(ipc);
        }
    }

    child_broadcast_done(state);
    wait_for_done_children(state);
    log_event(log_received_all_done_fmt, get_lamport_time(), id);

    close_irrelevant_fds(ipc);
    if (events_fp) {
        fclose(events_fp);
        events_fp = NULL;
    }
    _exit(EXIT_SUCCESS);
}

static void parent_main(GlobalState *state) {
    IPCContext *ipc = &state->ipc;
    lamport_reset();
    close_irrelevant_fds(ipc);

    wait_for_children_messages(state, STARTED);
    log_event(log_received_all_started_fmt, get_lamport_time(), PARENT_ID);

    wait_for_children_messages(state, DONE);
    log_event(log_received_all_done_fmt, get_lamport_time(), PARENT_ID);
}

static void parse_args(int argc, char **argv, GlobalState *state) {
    int option;
    static struct option long_options[] = {
        {"mutexl", no_argument, NULL, 1},
        {0, 0, 0, 0}
    };
    state->use_mutex = false;
    state->child_count = 0;

    opterr = 0;
    while ((option = getopt_long(argc, argv, "+p:", long_options, NULL)) != -1) {
        switch (option) {
            case 'p':
                state->child_count = atoi(optarg);
                break;
            case 1:
                state->use_mutex = true;
                break;
            default:
                fprintf(stderr, "Usage: %s -p N [--mutexl]\n", argv[0]);
                exit(EXIT_FAILURE);
        }
    }

    if (state->child_count <= 0 || state->child_count >= MAX_PROCESS_ID) {
        fprintf(stderr, "Invalid number of child processes: %d\n",
                state->child_count);
        exit(EXIT_FAILURE);
    }
}

int main(int argc, char **argv) {
    memset(&g_state, 0, sizeof(g_state));
    parse_args(argc, argv, &g_state);

    g_state.ipc.id = PARENT_ID;
    g_state.ipc.process_count = (uint8_t)(g_state.child_count + 1);
    g_state.ipc.last_sender = PARENT_ID;

    events_fp = fopen(events_log, "w");
    if (!events_fp) {
        perror("fopen events.log");
        return EXIT_FAILURE;
    }

    init_pipes_or_die(&g_state);

    for (local_id id = 1; id <= g_state.child_count; ++id) {
        pid_t pid = fork();
        if (pid < 0) {
            perror("fork");
            return EXIT_FAILURE;
        }
        if (pid == 0) {
            g_state.ipc.id = id;
            child_main(&g_state);
        }
    }

    parent_main(&g_state);

    for (int i = 0; i < g_state.child_count; ++i) {
        wait(NULL);
    }

    if (events_fp) {
        fclose(events_fp);
        events_fp = NULL;
    }
    return EXIT_SUCCESS;
}


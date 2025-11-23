#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <getopt.h>

#include "common.h"
#include "pa1.h"
#include "ipc.h"

typedef struct {
    local_id id;
    int      total;
    int      channels[MAX_PROCESS_ID][MAX_PROCESS_ID][2];
} ProcessData;

static ProcessData g;
static FILE *fp_events = NULL;
static FILE *fp_pipes  = NULL;

static void safe_fclose(FILE **pf) {
    if (pf && *pf) { fclose(*pf); *pf = NULL; }
}

static void elog(const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    vprintf(fmt, ap);
    va_end(ap);

    if (fp_events) {
        va_start(ap, fmt);
        vfprintf(fp_events, fmt, ap);
        va_end(ap);
        fflush(fp_events);
    }
}

static void init_pipes_or_die(void) {
    fp_pipes = fopen(pipes_log, "w");
    for (int s = 0; s < g.total; ++s) {
        for (int d = 0; d < g.total; ++d) {
            if (s == d) continue;
            if (pipe(g.channels[s][d]) == -1) {
                perror("pipe");
                exit(EXIT_FAILURE);
            }
            if (fp_pipes) {
                fprintf(fp_pipes, "%d -> %d: read=%d, write=%d\n",
                        s, d, g.channels[s][d][0], g.channels[s][d][1]);
            }
        }
    }
    safe_fclose(&fp_pipes);
}

static void close_irrelevant_fds(void) {
    for (int s = 0; s < g.total; ++s) {
        for (int d = 0; d < g.total; ++d) {
            if (s == d) continue;
            if (s != g.id) close(g.channels[s][d][0]);
            if (d != g.id) close(g.channels[s][d][1]);
        }
    }
}

static void make_msg(Message *m, MessageType type, const char *payload) {
    memset(m, 0, sizeof(*m));
    m->s_header.s_magic = MESSAGE_MAGIC;
    m->s_header.s_type  = type;
    m->s_header.s_local_time = 0;
    if (payload) {
        size_t n = strlen(payload);
        if (n > MAX_PAYLOAD_LEN) n = MAX_PAYLOAD_LEN;
        memcpy(m->s_payload, payload, n);
        m->s_header.s_payload_len = (uint16_t)n;
    } else {
        m->s_header.s_payload_len = 0;
    }
}

static void recv_from_all_peers(void *ctx) {
    Message tmp;
    for (local_id i = 1; i < (local_id)g.total; ++i) {
        if (i == g.id) continue;
        (void)receive(ctx, i, &tmp);
    }
}

static void child_flow(void) {
    Message m;
    char buf[MAX_PAYLOAD_LEN];

    snprintf(buf, sizeof(buf), log_started_fmt, g.id, getpid(), getppid());
    make_msg(&m, STARTED, buf);
    elog("%s", m.s_payload);
    send_multicast(&g, &m);
    recv_from_all_peers(&g);
    elog(log_received_all_started_fmt, g.id);

    snprintf(buf, sizeof(buf), log_done_fmt, g.id);
    make_msg(&m, DONE, buf);
    elog("%s", m.s_payload);
    send_multicast(&g, &m);
    recv_from_all_peers(&g);
    elog(log_received_all_done_fmt, g.id);
}

static void parent_flow(void) {
    Message m;
    for (local_id i = 1; i < (local_id)g.total; ++i) {
        (void)receive(&g, i, &m);
    }
    elog(log_received_all_started_fmt, PARENT_ID);

    for (local_id i = 1; i < (local_id)g.total; ++i) {
        (void)receive(&g, i, &m);
    }
    elog(log_received_all_done_fmt, PARENT_ID);

    for (local_id i = 1; i < (local_id)g.total; ++i) wait(NULL);
}

int main(int argc, char **argv) {
    int n = 0;

    int opt;
    while ((opt = getopt(argc, argv, "p:")) != -1) {
        switch (opt) {
            case 'p': n = atoi(optarg); break;
            default:
                fprintf(stderr, "Usage: %s -p X\n", argv[0]);
                return EXIT_FAILURE;
        }
    }

    if (n <= 0 || n >= MAX_PROCESS_ID) return EXIT_FAILURE;

    g.total = n + 1;
    fp_events = fopen(events_log, "w");

    init_pipes_or_die();

    for (local_id i = 1; i <= (local_id)n; ++i) {
        pid_t pid = fork();
        if (pid < 0) {
            perror("fork");
            return EXIT_FAILURE;
        }
        if (pid == 0) {
            g.id = i;
            close_irrelevant_fds();
            child_flow();
            safe_fclose(&fp_events);
            _exit(0);
        }
    }

    g.id = PARENT_ID;
    close_irrelevant_fds();
    parent_flow();

    safe_fclose(&fp_events);
    return EXIT_SUCCESS;
}

#include <errno.h>
#include <sched.h>
#include <string.h>
#include <unistd.h>

#include "ipc.h"
#include "ipc_ext.h"
#include "lamport.h"

#define HEADER_SIZE ((size_t)sizeof(MessageHeader))

static int write_full(int fd, const void *buf, size_t len) {
    const char *ptr = (const char *)buf;
    size_t left = len;
    while (left > 0) {
        ssize_t w = write(fd, ptr, left);
        if (w > 0) {
            ptr += (size_t)w;
            left -= (size_t)w;
            continue;
        }
        if (w == 0) {
            sched_yield();
            continue;
        }
        if (errno == EINTR) {
            continue;
        }
        if (errno == EAGAIN || errno == EWOULDBLOCK) {
            sched_yield();
            continue;
        }
        return -1;
    }
    return 0;
}

static int read_full(int fd, void *buf, size_t len) {
    char *ptr = (char *)buf;
    size_t got = 0;
    while (got < len) {
        ssize_t r = read(fd, ptr + got, len - got);
        if (r > 0) {
            got += (size_t)r;
            continue;
        }
        if (r == 0) {
            errno = EPIPE;
            return -1;
        }
        if (errno == EINTR) {
            continue;
        }
        if (errno == EAGAIN || errno == EWOULDBLOCK) {
            sched_yield();
            continue;
        }
        return -1;
    }
    return 0;
}

static int send_raw(IPCContext *ctx, local_id dst, const Message *msg) {
    int fd = ctx->pipes[ctx->id][dst][1];
    if (fd < 0) {
        errno = EBADF;
        return -1;
    }
    if (write_full(fd, &msg->s_header, HEADER_SIZE) != 0) {
        return -1;
    }
    if (msg->s_header.s_payload_len > 0) {
        if (write_full(fd, msg->s_payload, msg->s_header.s_payload_len) != 0) {
            return -1;
        }
    }
    return 0;
}

int send(void *self, local_id dst, const Message *msg) {
    IPCContext *ctx = (IPCContext *)self;
    Message copy = *msg;
    copy.s_header.s_local_time = lamport_time_inc();
    return send_raw(ctx, dst, &copy);
}

int send_multicast(void *self, const Message *msg) {
    IPCContext *ctx = (IPCContext *)self;
    Message copy = *msg;
    timestamp_t timestamp = lamport_time_inc();
    copy.s_header.s_local_time = timestamp;
    for (local_id i = 0; i < ctx->process_count; ++i) {
        if (i == ctx->id) {
            continue;
        }
        if (send_raw(ctx, i, &copy) != 0) {
            return -1;
        }
    }
    return 0;
}

int receive(void *self, local_id from, Message *msg) {
    IPCContext *ctx = (IPCContext *)self;
    int fd = ctx->pipes[from][ctx->id][0];
    if (fd < 0) {
        errno = EBADF;
        return -1;
    }
    if (read_full(fd, &msg->s_header, HEADER_SIZE) != 0) {
        return -1;
    }
    uint16_t len = msg->s_header.s_payload_len;
    if (len > 0) {
        if (read_full(fd, msg->s_payload, len) != 0) {
            return -1;
        }
    }
    lamport_update_on_receive(msg->s_header.s_local_time);
    ctx->last_sender = from;
    return 0;
}

static int read_remaining_header(int fd, MessageHeader *hdr, size_t already) {
    char *ptr = (char *)hdr;
    return read_full(fd, ptr + already, HEADER_SIZE - already);
}

int receive_any(void *self, Message *msg) {
    IPCContext *ctx = (IPCContext *)self;
    while (1) {
        for (local_id from = 0; from < ctx->process_count; ++from) {
            if (from == ctx->id) {
                continue;
            }
            int fd = ctx->pipes[from][ctx->id][0];
            if (fd < 0) {
                continue;
            }

            errno = 0;
            ssize_t r = read(fd, &msg->s_header, HEADER_SIZE);
            if (r < 0) {
                if (errno == EINTR) {
                    --from;
                    continue;
                }
                if (errno == EAGAIN || errno == EWOULDBLOCK) {
                    continue;
                }
                return -1;
            }
            if (r == 0) {
                continue;
            }
            if ((size_t)r < HEADER_SIZE) {
                if (read_remaining_header(fd, &msg->s_header, (size_t)r) != 0) {
                    return -1;
                }
            }
            uint16_t len = msg->s_header.s_payload_len;
            if (len > 0) {
                if (read_full(fd, msg->s_payload, len) != 0) {
                    return -1;
                }
            }
            lamport_update_on_receive(msg->s_header.s_local_time);
            ctx->last_sender = from;
            return 0;
        }
        sched_yield();
    }
}

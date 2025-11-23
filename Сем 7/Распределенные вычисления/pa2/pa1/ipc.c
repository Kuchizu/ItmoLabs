#include <unistd.h>
#include <errno.h>
#include <string.h>
#include "ipc.h"
#include "common.h"

#define HDR_SIZE ((size_t)sizeof(MessageHeader))

typedef struct {
    local_id id;
    int      total;
    int      channels[MAX_PROCESS_ID][MAX_PROCESS_ID][2];
} ProcessData;

static ssize_t write_all(int fd, const void *buf, size_t n) {
    const char *p = (const char *)buf;
    size_t left = n;
    while (left > 0) {
        ssize_t w = write(fd, p, left);
        if (w < 0) {
            if (errno == EINTR) continue;
            return -1;
        }
        if (w == 0) {
            errno = EPIPE;
            return -1;
        }
        p += (size_t)w;
        left -= (size_t)w;
    }
    return (ssize_t)n;
}

static ssize_t read_exact(int fd, void *buf, size_t n) {
    char *p = (char *)buf;
    size_t left = n;
    while (left > 0) {
        ssize_t r = read(fd, p, left);
        if (r < 0) {
            if (errno == EINTR) continue;
            return -1;
        }
        if (r == 0) {
            errno = EPIPE;
            return -1;
        }
        p += (size_t)r;
        left -= (size_t)r;
    }
    return (ssize_t)n;
}

int send(void *self, local_id dst, const Message *msg) {
    ProcessData *ctx = (ProcessData *)self;
    const int wr = ctx->channels[ctx->id][dst][1];
    // const size_t full = HDR_SIZE + msg->s_header.s_payload_len;

    if (write_all(wr, &msg->s_header, HDR_SIZE) < 0) return -1;
    if (msg->s_header.s_payload_len > 0) {
        if (write_all(wr, msg->s_payload, msg->s_header.s_payload_len) < 0) return -1;
    }
    return 0;
}

int send_multicast(void *self, const Message *msg) {
    ProcessData *ctx = (ProcessData *)self;
    for (local_id i = 0; i < (local_id)ctx->total; ++i) {
        if (i == ctx->id) continue;
        if (send(self, i, msg) != 0) return -1;
    }
    return 0;
}

int receive(void *self, local_id from, Message *msg) {
    ProcessData *ctx = (ProcessData *)self;
    const int rd = ctx->channels[from][ctx->id][0];

    if (read_exact(rd, &msg->s_header, HDR_SIZE) < 0) return -1;
    const uint32_t len = msg->s_header.s_payload_len;
    if (len > 0) {
        if (read_exact(rd, msg->s_payload, len) < 0) return -1;
    }
    return 0;
}

int receive_any(void *self, Message *msg) {
    ProcessData *ctx = (ProcessData *)self;
    for (local_id from = 0; from < (local_id)ctx->total; ++from) {
        if (from == ctx->id) continue;
        int rd = ctx->channels[from][ctx->id][0];

        if (read(rd, &msg->s_header, HDR_SIZE) == (ssize_t)HDR_SIZE) {
            const uint32_t len = msg->s_header.s_payload_len;
            if (len > 0) {
                if (read_exact(rd, msg->s_payload, len) < 0) return -1;
            }
            return 0;
        }
    }
    return -1;
}

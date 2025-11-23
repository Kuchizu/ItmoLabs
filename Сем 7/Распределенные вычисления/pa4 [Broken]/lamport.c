#include <limits.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

#include "lamport.h"
#include "pa2345.h"

#define PENDING_QUEUE_CAPACITY 512

typedef struct {
    Message msg;
    local_id from;
} PendingEntry;

static timestamp_t lamport_clock = 0;
static const timestamp_t LAMPORT_MAX = INT16_MAX;

static bool cs_initialized = false;
static local_id self_id = PARENT_ID;
static uint8_t total_processes = 0;
static uint32_t child_mask = 0;
static timestamp_t request_ts[MAX_PROCESS_ID + 1];
static timestamp_t current_request_time = 0;
static uint32_t expected_reply_mask = 0;
static uint32_t reply_bitmap = 0;
static uint32_t deferred_reply_mask = 0;
static bool in_cs = false;

static PendingEntry pending_queue[PENDING_QUEUE_CAPACITY];
static size_t pending_head = 0;
static size_t pending_tail = 0;
static size_t pending_size = 0;

static void queue_push(const Message *msg, local_id from) {
    if (pending_size >= PENDING_QUEUE_CAPACITY) {
        abort();
    }
    pending_queue[pending_tail] = (PendingEntry){ .msg = *msg, .from = from };
    pending_tail = (pending_tail + 1u) % PENDING_QUEUE_CAPACITY;
    ++pending_size;
}

static bool queue_pop(Message *msg, local_id *from) {
    if (pending_size == 0) {
        return false;
    }
    PendingEntry entry = pending_queue[pending_head];
    pending_head = (pending_head + 1u) % PENDING_QUEUE_CAPACITY;
    --pending_size;
    if (msg) {
        *msg = entry.msg;
    }
    if (from) {
        *from = entry.from;
    }
    return true;
}

static void compose_empty_message(Message *msg, MessageType type) {
    memset(msg, 0, sizeof(*msg));
    msg->s_header.s_magic = MESSAGE_MAGIC;
    msg->s_header.s_type = type;
    msg->s_header.s_payload_len = 0;
}

static bool has_queue_priority(void) {
    if (!cs_initialized) {
        return true;
    }
    for (local_id id = 1; id < total_processes; ++id) {
        if (id == self_id) {
            continue;
        }
        timestamp_t peer_time = request_ts[id];
        if (peer_time == 0) {
            continue;
        }
        if (peer_time < current_request_time) {
            return false;
        }
        if (peer_time == current_request_time && id < self_id) {
            return false;
        }
    }
    return true;
}

timestamp_t get_lamport_time(void) {
    return lamport_clock;
}

timestamp_t lamport_time_inc(void) {
    if (lamport_clock < LAMPORT_MAX) {
        ++lamport_clock;
    }
    return lamport_clock;
}

void lamport_update_on_receive(timestamp_t received_time) {
    if (lamport_clock < received_time) {
        lamport_clock = received_time;
    }
    if (lamport_clock < LAMPORT_MAX) {
        ++lamport_clock;
    }
}

void lamport_reset(void) {
    lamport_clock = 0;
    cs_initialized = false;
    self_id = PARENT_ID;
    total_processes = 0;
    child_mask = 0;
    memset(request_ts, 0, sizeof(request_ts));
    current_request_time = 0;
    expected_reply_mask = 0;
    reply_bitmap = 0;
    pending_head = pending_tail = pending_size = 0;
    deferred_reply_mask = 0;
    in_cs = false;
}

timestamp_t lamport_peek_next(void) {
    if (lamport_clock < LAMPORT_MAX) {
        return lamport_clock + 1;
    }
    return lamport_clock;
}

void lamport_cs_init(local_id id, uint8_t process_count) {
    cs_initialized = true;
    self_id = id;
    total_processes = process_count;
    child_mask = 0;
    for (local_id proc = 1; proc < total_processes; ++proc) {
        child_mask |= (1u << proc);
    }
    memset(request_ts, 0, sizeof(request_ts));
    current_request_time = 0;
    expected_reply_mask = 0;
    reply_bitmap = 0;
    deferred_reply_mask = 0;
    in_cs = false;
}

bool lamport_pop_pending(Message *msg, local_id *from) {
    return queue_pop(msg, from);
}

void lamport_store_message(const Message *msg, local_id from) {
    queue_push(msg, from);
}

bool lamport_handle_message(void *self, const Message *msg, local_id from) {
    if (!cs_initialized) {
        return false;
    }
    IPCContext *ipc = (IPCContext *)self;
    switch (msg->s_header.s_type) {
        case CS_REQUEST: {
            request_ts[from] = msg->s_header.s_local_time;
            bool can_grant = (current_request_time == 0 && !in_cs);
            if (!can_grant) {
                timestamp_t their_time = msg->s_header.s_local_time;
                if (their_time < current_request_time ||
                    (their_time == current_request_time && from < self_id)) {
                    can_grant = true;
                }
            }
            if (can_grant) {
                Message reply;
                compose_empty_message(&reply, CS_REPLY);
                send(ipc, from, &reply);
            } else {
                deferred_reply_mask |= (1u << from);
            }
            return true;
        }
        case CS_REPLY:
            reply_bitmap |= (1u << from);
            return true;
        case CS_RELEASE:
            request_ts[from] = 0;
            return true;
        default:
            return false;
    }
}

int request_cs(const void *self) {
    IPCContext *ipc = (IPCContext *)self;
    if (!cs_initialized) {
        lamport_cs_init(ipc->id, ipc->process_count);
    }
    timestamp_t request_time = lamport_peek_next();
    current_request_time = request_time;
    request_ts[self_id] = current_request_time;
    expected_reply_mask = child_mask & ~(1u << self_id);
    reply_bitmap = 0;
    deferred_reply_mask &= ~(1u << self_id);

    Message request;
    compose_empty_message(&request, CS_REQUEST);
    for (local_id peer = 1; peer < total_processes; ++peer) {
        if (peer == self_id) {
            continue;
        }
        lamport_clock = request_time - 1;
        send(ipc, peer, &request);
    }
    lamport_clock = request_time;

    while (true) {
        if ((reply_bitmap & expected_reply_mask) == expected_reply_mask &&
            has_queue_priority()) {
            break;
        }
        Message incoming;
        if (receive_any(ipc, &incoming) != 0) {
            continue;
        }
        local_id from = ipc->last_sender;
        if (lamport_handle_message(ipc, &incoming, from)) {
            continue;
        }
        queue_push(&incoming, from);
    }
    in_cs = true;
    return 0;
}

int release_cs(const void *self) {
    IPCContext *ipc = (IPCContext *)self;
    if (!cs_initialized) {
        return 0;
    }
    request_ts[self_id] = 0;
    current_request_time = 0;
    expected_reply_mask = 0;
    reply_bitmap = 0;
    in_cs = false;

    Message release;
    compose_empty_message(&release, CS_RELEASE);
    if (total_processes > 1) {
        timestamp_t release_time = lamport_peek_next();
        for (local_id peer = 1; peer < total_processes; ++peer) {
            if (peer == self_id) {
                continue;
            }
            lamport_clock = release_time - 1;
            send(ipc, peer, &release);
        }
        lamport_clock = release_time;
    }

    if (deferred_reply_mask != 0) {
        Message reply;
        compose_empty_message(&reply, CS_REPLY);
        for (local_id peer = 1; peer < total_processes; ++peer) {
            if (peer == self_id) {
                continue;
            }
            if (deferred_reply_mask & (1u << peer)) {
                send(ipc, peer, &reply);
            }
        }
        deferred_reply_mask = 0;
    }
    return 0;
}

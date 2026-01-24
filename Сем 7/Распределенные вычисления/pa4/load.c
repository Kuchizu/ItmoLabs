#include "load.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "pa2345.h"
#include "message_handler.h"
#include "pipe_manager.h"

extern int mutexl;
extern FILE *eventlog;
extern timestamp_t lamport_time;
extern int8_t num_processes;

char buff[100];
request_queue_t rq[11];
int wait_flag = 0;
int8_t num_running_processes;
int8_t num_future_processes;
timestamp_t request_time;

static void initialize_queue_entry(request_queue_t *entry, local_id id, local_id req_id, timestamp_t req_time) {
    entry->id = id;
    entry->req_id = req_id;
    entry->req_time = req_time;
}

static void init_request_queue(void) {
    int idx;
    for (idx = 0; idx < 11; idx++) {
        initialize_queue_entry(&rq[idx], 0, 0, 9999);
    }
}

static int compare_requests(timestamp_t time1, local_id id1, timestamp_t time2, local_id id2) {
    if (time1 < time2) {
        return -1;
    }
    if (time1 > time2) {
        return 1;
    }
    return (id1 < id2) ? -1 : ((id1 > id2) ? 1 : 0);
}

static int find_insert_position(timestamp_t req_time, local_id req_id, int8_t total_procs) {
    int pos;
    for (pos = 0; pos < total_procs; pos++) {
        int cmp = compare_requests(req_time, req_id, rq[pos].req_time, rq[pos].req_id);
        if (cmp < 0) {
            return pos;
        }
    }
    return pos;
}

static void shift_queue_right(int start_pos, int8_t total_procs) {
    int idx;
    for (idx = total_procs - 1; idx >= start_pos; idx--) {
        rq[idx + 1] = rq[idx];
    }
}

static void insert_request(request_queue_t *request, int8_t total_procs) {
    int insert_pos = find_insert_position(request->req_time, request->req_id, total_procs);
    shift_queue_right(insert_pos, total_procs);
    rq[insert_pos] = *request;
}

int request_cs(const void *self)
{
    const request_queue_t *incoming_request = (const request_queue_t *)self;
    local_id process_id = incoming_request->id;
    local_id requester_id = incoming_request->req_id;

    insert_request((request_queue_t *)incoming_request, num_processes);

    if (process_id != requester_id) {
        process_send(process_id, requester_id, CS_REPLY);
    }

    int is_queue_head = (process_id == rq[0].req_id && rq[0].id != 0);

    return is_queue_head ? 0 : 1;
}

static void shift_queue_left(int8_t total_procs) {
    int idx;
    for (idx = 0; idx < total_procs; idx++) {
        rq[idx] = rq[idx + 1];
    }
}

int release_cs(const void *self)
{
    shift_queue_left(num_processes);
    return 0;
}

static void execute_critical_section(local_id process_id, int iteration, int total_iterations) {
    request_queue_t *cs_request = malloc(sizeof(request_queue_t));

    initialize_queue_entry(cs_request, process_id, process_id, 0);

    process_send_multicast(process_id, CS_REQUEST);

    cs_request->req_time = get_lamport_time();
    request_cs(cs_request);

    wait_flag = 1;
    num_future_processes = 1;
    request_time = cs_request->req_time;

    while (num_future_processes != num_running_processes && wait_flag) {
        process_recieve_any(process_id);
    }

    sprintf(buff, log_loop_operation_fmt, process_id, iteration, total_iterations);
    print(buff);

    process_send_multicast(process_id, CS_RELEASE);
    release_cs(cs_request);

    free(cs_request);
}

static void execute_without_mutex(local_id process_id, int iteration, int total_iterations) {
    sprintf(buff, log_loop_operation_fmt, process_id, iteration, total_iterations);
    print(buff);
}

void load(local_id id)
{
    static int queue_initialized = 0;
    if (!queue_initialized) {
        init_request_queue();
        queue_initialized = 1;
    }

    int total_iterations = id * 5;
    int current_iteration;

    for (current_iteration = 1; current_iteration <= total_iterations; current_iteration++) {
        if (mutexl) {
            execute_critical_section(id, current_iteration, total_iterations);
        } else {
            execute_without_mutex(id, current_iteration, total_iterations);
        }
    }
}

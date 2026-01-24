#include "message_handler.h"

#include <malloc.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <unistd.h>

#include "pa2345.h"
#include "load.h"

extern FILE *eventlog;
extern int8_t num_running_processes;
extern int8_t num_processes;
extern int8_t num_future_processes;
extern timestamp_t request_time;
extern int wait_flag;

static int message_counters_started[11] = {0};
static int message_counters_done[11] = {0};
static int8_t total_started_received = 0;
static int8_t total_done_received = 0;

int started_len = 0;
int done_len = 0;

timestamp_t lamport_time = 0;

timestamp_t get_lamport_time(void)
{
    return lamport_time;
}

static void increment_logical_clock(void) {
    lamport_time++;
}

static void synchronize_clock(timestamp_t received_time) {
    increment_logical_clock();
    while (lamport_time <= received_time) {
        increment_logical_clock();
    }
}

static void handle_started_message(Message *message)
{
    int process_id, pid_val, ppid_val, timestamp_val, balance_val;

    message->s_payload[message->s_header.s_payload_len] = '\0';

    sscanf(message->s_payload, log_started_fmt,
           &timestamp_val, &process_id, &pid_val, &ppid_val, &balance_val);

    if (message_counters_started[process_id] == 0) {
        total_started_received++;
    }

    message_counters_started[process_id]++;
}

static void handle_done_message(Message *message)
{
    int process_id, timestamp_val, balance_val;

    message->s_payload[message->s_header.s_payload_len] = '\0';

    sscanf(message->s_payload, log_done_fmt,
           &timestamp_val, &process_id, &balance_val);

    if (message_counters_done[process_id] == 0) {
        total_done_received++;
    }

    num_running_processes--;
    message_counters_done[process_id]++;
}

static void handle_cs_request(Message *message, local_id current_id, local_id sender_id)
{
    if (current_id == PARENT_ID || message_counters_done[current_id] > 0) {
        return;
    }

    request_queue_t *queue_entry = malloc(sizeof(request_queue_t));
    queue_entry->id = current_id;
    queue_entry->req_id = sender_id;
    queue_entry->req_time = message->s_header.s_local_time;

    timestamp_t msg_time = message->s_header.s_local_time;

    if (request_time < msg_time ||
        (request_time == msg_time && current_id < sender_id)) {
        num_future_processes++;
    }

    wait_flag = request_cs(queue_entry);
    free(queue_entry);
}

static void handle_cs_release(Message *message, local_id current_id, local_id sender_id)
{
    if (current_id == PARENT_ID || message_counters_done[current_id] > 0) {
        return;
    }

    request_queue_t *queue_entry = malloc(sizeof(request_queue_t));
    queue_entry->id = current_id;
    queue_entry->req_id = sender_id;
    queue_entry->req_time = message->s_header.s_local_time;

    release_cs(queue_entry);
    free(queue_entry);
}

void process_msg(Message *msg, local_id id, local_id dst)
{
    synchronize_clock(msg->s_header.s_local_time);

    int16_t msg_type = msg->s_header.s_type;

    switch (msg_type) {
        case STARTED:
            handle_started_message(msg);
            break;

        case DONE:
            handle_done_message(msg);
            break;

        case CS_REQUEST:
            handle_cs_request(msg, id, dst);
            break;

        case CS_REPLY:
            break;

        case CS_RELEASE:
            handle_cs_release(msg, id, dst);
            break;

        default:
            fclose(eventlog);
            free(msg);
            perror("Unknown msg type");
            exit(EXIT_FAILURE);
    }
}

static int determine_payload_size(int16_t message_type)
{
    switch (message_type) {
        case STARTED:
            return started_len;

        case DONE:
            return done_len;

        case CS_RELEASE:
        case CS_REPLY:
        case CS_REQUEST:
            return 0;

        default:
            return MAX_PAYLOAD_LEN;
    }
}

static void fill_started_payload(local_id process_id, char *buffer) {
    sprintf(buffer, log_started_fmt,
            get_lamport_time() + 1,
            process_id,
            getpid(),
            getppid(),
            0);
}

static void fill_done_payload(local_id process_id, char *buffer) {
    sprintf(buffer, log_done_fmt,
            get_lamport_time() + 1,
            process_id,
            0);
}

static void fill_payload_by_type(int16_t message_type, local_id process_id, char *buffer)
{
    switch (message_type) {
        case STARTED:
            fill_started_payload(process_id, buffer);
            break;

        case DONE:
            fill_done_payload(process_id, buffer);
            break;
    }
}

char *create_payload(int16_t type, local_id id)
{
    int size = determine_payload_size(type);
    char *buffer = NULL;

    if (size != 0) {
        buffer = malloc(size);
    }

    if (buffer == NULL && size != 0) {
        fclose(eventlog);
        perror("create_payload");
        exit(EXIT_FAILURE);
    }

    fill_payload_by_type(type, id, buffer);
    return buffer;
}

static void copy_payload_to_message(Message *message, char *payload, uint16_t length) {
    uint16_t idx;
    for (idx = 0; idx < length; idx++) {
        message->s_payload[idx] = payload[idx];
    }
}

Message *create_msg(int16_t type, char *payload)
{
    uint16_t payload_size = determine_payload_size(type);

    if (payload == NULL && payload_size != 0) {
        fclose(eventlog);
        perror("create_msg");
        exit(EXIT_FAILURE);
    }

    Message *new_message = malloc(sizeof(MessageHeader) + payload_size);

    if (new_message == NULL) {
        fclose(eventlog);
        if (payload_size != 0) {
            free(payload);
        }
        perror("create_msg");
        exit(EXIT_FAILURE);
    }

    new_message->s_header.s_magic = MESSAGE_MAGIC;
    new_message->s_header.s_type = type;
    new_message->s_header.s_payload_len = payload_size;

    if (type >= 0) {
        increment_logical_clock();
    }

    new_message->s_header.s_local_time = get_lamport_time();

    copy_payload_to_message(new_message, payload, payload_size);

    return new_message;
}

static const char *get_format_string_for_type(int16_t message_type)
{
    switch (message_type) {
        case STARTED:
            return log_started_fmt;

        case DONE:
            return log_done_fmt;

        default:
            fclose(eventlog);
            perror("Unknown msg type");
            exit(EXIT_FAILURE);
    }
}

const char *log_fmt_type(int16_t type)
{
    return get_format_string_for_type(type);
}

static void increment_started_counter(local_id process_id) {
    message_counters_started[process_id]++;
    total_started_received++;
}

static void increment_done_counter(local_id process_id) {
    message_counters_done[process_id]++;
    total_done_received++;
}

void count_sent_num(local_id id, int16_t type)
{
    switch (type) {
        case STARTED:
            increment_started_counter(id);
            break;

        case DONE:
            increment_done_counter(id);
            break;
    }
}

int8_t *get_rcvd_num(int16_t type)
{
    switch (type) {
        case STARTED:
            return &total_started_received;

        case DONE:
            return &total_done_received;

        default:
            fclose(eventlog);
            perror("Unknown msg type");
            exit(EXIT_FAILURE);
    }
}

int *get_rcvd(int16_t type)
{
    switch (type) {
        case STARTED:
            return message_counters_started;

        case DONE:
            return message_counters_done;

        default:
            fclose(eventlog);
            perror("Unknown msg type");
            exit(EXIT_FAILURE);
    }
}

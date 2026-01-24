#include "pipe_manager.h"

#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include "load.h"
#include "message_handler.h"

extern FILE *eventlog;
extern local_id recieve_any_dst;

char free_payload[MAX_PAYLOAD_LEN];
static int write_pipes[12][12];
static int read_pipes[12][12];

static void initialize_pipe_pair(int source, int destination, FILE *log_file)
{
    int pipe_fds[2];

    if (pipe2(pipe_fds, O_NONBLOCK) == -1) {
        fclose(log_file);
        fclose(eventlog);
        perror("pipe");
        exit(EXIT_FAILURE);
    }

    fprintf(log_file, "%i %i\n", pipe_fds[0], pipe_fds[1]);

    read_pipes[destination][source] = pipe_fds[0];
    write_pipes[source][destination] = pipe_fds[1];
}

static void mark_pipe_as_unused(int source, int destination) {
    read_pipes[destination][source] = -1;
    write_pipes[source][destination] = -1;
}

static void set_pipe_terminators(int process_count) {
    local_id idx;
    for (idx = 0; idx < process_count + 1; idx++) {
        read_pipes[idx][process_count + 1] = -2;
        write_pipes[idx][process_count + 1] = -2;
        read_pipes[process_count + 1][idx] = -2;
        write_pipes[process_count + 1][idx] = -2;
    }
}

void create_pipe_topology(int8_t num_processes, FILE *pipelog)
{
    local_id src, dst;

    for (src = 0; src < num_processes + 1; src++) {
        for (dst = 0; dst < num_processes + 1; dst++) {
            if (src == dst) {
                mark_pipe_as_unused(src, dst);
                continue;
            }

            initialize_pipe_pair(src, dst, pipelog);
        }
    }

    set_pipe_terminators(num_processes);
    fclose(pipelog);
}

static void safe_close_pipe(int fd, const char *error_msg) {
    if (close(fd) != 0) {
        fclose(eventlog);
        perror(error_msg);
        exit(EXIT_FAILURE);
    }
}

static void close_pipe_if_valid(int *pipe_fd, const char *error_msg) {
    if (*pipe_fd != -1) {
        safe_close_pipe(*pipe_fd, error_msg);
        *pipe_fd = -1;
    }
}

void close_unused_pipes(int8_t num_processes, local_id id)
{
    local_id src, dst;

    for (src = 0; src < num_processes + 1; src++) {
        if (src == id) {
            continue;
        }

        for (dst = 0; dst < num_processes + 1; dst++) {
            close_pipe_if_valid(&read_pipes[src][dst], "close_unused_pipes");
            close_pipe_if_valid(&write_pipes[src][dst], "close_unused_pipes");
        }
    }
}

void close_used_pipes(int8_t num_processes, local_id id)
{
    local_id target;

    for (target = 0; target < num_processes + 1; target++) {
        close_pipe_if_valid(&read_pipes[id][target], "close_used_pipes");
        close_pipe_if_valid(&write_pipes[id][target], "close_used_pipes");
    }
}

static void send_message_with_cleanup(local_id sender, int16_t msg_type,
                                      char *payload_data, Message *message,
                                      int use_multicast, local_id receiver)
{
    int send_status;

    if (use_multicast) {
        send_status = send_multicast(write_pipes[sender], message);
    } else {
        send_status = send(write_pipes[sender], receiver, message);
    }

    if (send_status != 0) {
        fclose(eventlog);
        free(message);
        perror(use_multicast ? "send_multicast" : "send");
        exit(EXIT_FAILURE);
    }

    count_sent_num(sender, msg_type);
    free(message);
}

void process_send_multicast(local_id id, int16_t type)
{
    char *payload_data = create_payload(type, id);
    Message *message = create_msg(type, payload_data);

    free(payload_data);

    send_message_with_cleanup(id, type, payload_data, message, 1, 0);
}

void process_send(local_id from, local_id to, int16_t type)
{
    char *payload_data = create_payload(type, from);
    Message *message = create_msg(type, payload_data);

    free(payload_data);

    send_message_with_cleanup(from, type, payload_data, message, 0, to);
}

static int try_receive_from_process(local_id receiver, local_id sender, Message *msg_buffer)
{
    int receive_status = receive(read_pipes[receiver], sender, msg_buffer);

    if (receive_status == 0) {
        process_msg(msg_buffer, receiver, sender);
        return 1;
    }

    if (receive_status == -1) {
        fclose(eventlog);
        free(msg_buffer);
        perror("receive");
        exit(EXIT_FAILURE);
    }

    return 0;
}

void process_recieve_all(int8_t num_processes, local_id id, int16_t type)
{
    int *received_from = get_rcvd(type);
    int8_t *total_received = get_rcvd_num(type);
    int watchdog_counter = 0;
    const int MAX_WATCHDOG = 100000000;

    while (*total_received < num_processes && watchdog_counter < MAX_WATCHDOG) {
        local_id sender_id;

        for (sender_id = 1; sender_id < num_processes + 1; sender_id++) {
            if (received_from[sender_id] != 0) {
                continue;
            }

            Message *msg_buffer = create_msg(-1, free_payload);
            try_receive_from_process(id, sender_id, msg_buffer);
            free(msg_buffer);
        }

        watchdog_counter++;
    }
}

void process_recieve_any(local_id id)
{
    Message *msg_buffer = create_msg(-1, free_payload);

    int receive_status = receive_any(read_pipes[id], msg_buffer);

    if (receive_status == -1) {
        fclose(eventlog);
        free(msg_buffer);
        perror("receive_any");
        exit(EXIT_FAILURE);
    }

    if (receive_status == 0) {
        process_msg(msg_buffer, id, recieve_any_dst);
    }

    free(msg_buffer);
}

void process_load(local_id id)
{
    load(id);
}

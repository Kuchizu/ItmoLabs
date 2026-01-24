#include "ipc.h"
#include <unistd.h>

local_id recieve_any_dst = -1;

static int validate_params(void *pipe_set, const Message *message) {
    return (pipe_set != NULL && message != NULL) ? 0 : -1;
}

static size_t calculate_message_size(const Message *message) {
    return sizeof(MessageHeader) + message->s_header.s_payload_len;
}

int send(void *self, local_id dst, const Message *msg)
{
    if (validate_params(self, msg) != 0) {
        return -1;
    }

    int *pipe_descriptors = (int *)self;
    int destination_fd = pipe_descriptors[dst];
    size_t bytes_to_write = calculate_message_size(msg);

    ssize_t written_bytes = write(destination_fd, msg, bytes_to_write);

    if (written_bytes == -1) {
        return -1;
    }

    return 0;
}

int send_multicast(void *self, const Message *msg)
{
    if (validate_params(self, msg) != 0) {
        return -1;
    }

    int *pipe_array = (int *)self;
    local_id current_id = 0;

    while (pipe_array[current_id] != -2) {
        int fd = pipe_array[current_id];

        if (fd != -1) {
            int send_result = send(self, current_id, msg);
            if (send_result != 0) {
                return -1;
            }
        }

        current_id++;
    }

    return 0;
}

static int read_header(int fd, MessageHeader *header) {
    ssize_t bytes_read = read(fd, header, sizeof(MessageHeader));

    if (bytes_read == sizeof(MessageHeader)) {
        return 0;
    }

    if (bytes_read == -1 || bytes_read == 0) {
        return 1;
    }

    return -1;
}

static int read_payload(int fd, Message *msg, uint16_t payload_length) {
    if (payload_length == 0) {
        return 0;
    }

    ssize_t bytes_read = read(fd, msg->s_payload, payload_length);

    if (bytes_read == payload_length) {
        return 0;
    }

    return -1;
}

int receive(void *self, local_id from, Message *msg)
{
    if (validate_params(self, msg) != 0) {
        return -1;
    }

    int *pipe_descriptors = (int *)self;
    int source_fd = pipe_descriptors[from];

    MessageHeader received_header;
    int header_status = read_header(source_fd, &received_header);

    if (header_status != 0) {
        return header_status;
    }

    msg->s_header = received_header;

    int payload_status = read_payload(source_fd, msg, received_header.s_payload_len);

    if (payload_status != 0) {
        return -1;
    }

    return 0;
}

int receive_any(void *self, Message *msg)
{
    if (validate_params(self, msg) != 0) {
        return -1;
    }

    int *pipe_array = (int *)self;
    local_id process_id = 0;

    while (pipe_array[process_id] != -2) {
        int fd = pipe_array[process_id];

        if (fd != -1) {
            recieve_any_dst = process_id;
            int recv_status = receive(self, process_id, msg);

            if (recv_status != 1) {
                return recv_status;
            }
        }

        process_id++;
    }

    return 1;
}

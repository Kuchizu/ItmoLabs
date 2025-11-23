#include "lamport.h"

static timestamp_t lamport_clock = 0;

timestamp_t get_lamport_time(void) {
    return lamport_clock;
}

timestamp_t lamport_time_inc(void) {
    if (lamport_clock < MAX_T) {
        ++lamport_clock;
    }
    return lamport_clock;
}

void lamport_update_on_receive(timestamp_t received_time) {
    if (lamport_clock < received_time) {
        lamport_clock = received_time;
    }
    if (lamport_clock < MAX_T) {
        ++lamport_clock;
    }
}

void lamport_reset(void) {
    lamport_clock = 0;
}

timestamp_t lamport_peek_next(void) {
    if (lamport_clock < MAX_T) {
        return lamport_clock + 1;
    }
    return lamport_clock;
}


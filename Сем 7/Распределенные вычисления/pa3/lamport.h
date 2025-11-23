#ifndef LAMPORT_H
#define LAMPORT_H

#include "banking.h"

timestamp_t lamport_time_inc(void);
void lamport_update_on_receive(timestamp_t received_time);
void lamport_reset(void);
timestamp_t lamport_peek_next(void);

#endif /* LAMPORT_H */


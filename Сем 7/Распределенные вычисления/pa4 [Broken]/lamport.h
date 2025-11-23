#ifndef LAMPORT_H
#define LAMPORT_H

#include <stdbool.h>

#include "ipc_ext.h"

timestamp_t get_lamport_time(void);
timestamp_t lamport_time_inc(void);
void lamport_update_on_receive(timestamp_t received_time);
void lamport_reset(void);
timestamp_t lamport_peek_next(void);

void lamport_cs_init(local_id self_id, uint8_t process_count);
bool lamport_handle_message(void *self, const Message *msg, local_id from);
bool lamport_pop_pending(Message *msg, local_id *from);
void lamport_store_message(const Message *msg, local_id from);

#endif /* LAMPORT_H */


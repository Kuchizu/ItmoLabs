#ifndef __IFMO_DISTRIBUTED_CLASS_MESSAGE_HANDLER__H
#define __IFMO_DISTRIBUTED_CLASS_MESSAGE_HANDLER__H

#include <stdint.h>

#include "ipc.h"

//------------------------------------------------------------------------------

/** Processes a message given.
 *
 * @param msg             Message
 * @param id              Id of the process
 *
 */
void process_msg(Message *msg, local_id id, local_id dst);

//------------------------------------------------------------------------------

/** Creates a typed message with a given payload.
 *
 * @param type            Type of a message
 * @param payload_len     Length of payload
 * @param payload         Message payload
 *
 * @return message pointer on success, EXIT_FAILURE on error
 */
Message *create_msg(int16_t type, char *payload);

/** Creates a payload with a given type.
 *
 * @param type            Type of a message
 * @param id              Id of the process
 *
 * @return payload pointer on success, EXIT_FAILURE on error
 */
char *create_payload(int16_t type, local_id id);

/** Counts types of messages sent by this process.
 *
 * @param id              Id of the process
 * @param type            Type of a message
 *
 */
void count_sent_num(local_id id, int16_t type);

/** Returns a number of messages recieved with a given type.
 *
 * @param type            Type of a message
 *
 * @return number pointer on success, EXIT_FAILURE on error
 */
int8_t *get_rcvd_num(int16_t type);

/** Returns an array of numbers of messages recieved by process with a given type.
 *
 * @param type            Type of a message
 *
 * @return array on success, EXIT_FAILURE on error
 */
int *get_rcvd(int16_t type);

/**
 * Returs the value of Lamport's clock.
 */
timestamp_t get_lamport_time(void);

//------------------------------------------------------------------------------

#endif // __IFMO_DISTRIBUTED_CLASS_MESSAGE_HANDLER__H

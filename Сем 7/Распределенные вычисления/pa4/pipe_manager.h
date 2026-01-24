#ifndef __IFMO_DISTRIBUTED_CLASS_PIPE_MANAGER__H
#define __IFMO_DISTRIBUTED_CLASS_PIPE_MANAGER__H

#define _GNU_SOURCE

#include <stdint.h>
#include <stdio.h>

#include "ipc.h"

//------------------------------------------------------------------------------

/** Creates a pipe topology for full-mesh interprocess communication.
 *
 * @param num_processes    Number of child processes
 * @param pipelog          File descriptor of pipe topology log
 *
 */
void create_pipe_topology(int8_t num_processes, FILE *pipelog);

//------------------------------------------------------------------------------

/** Closes pipe ends that have no use for that process.
 *
 * @param num_processes    Number of child processes
 * @param id               Id of the process
 *
 */
void close_unused_pipes(int8_t num_processes, local_id id);

//------------------------------------------------------------------------------

/** Closes pipe ends that have been used for that process.
 *
 * @param num_processes    Number of child processes
 * @param id               Id of the process
 *
 */
void close_used_pipes(int8_t num_processes, local_id id);

//------------------------------------------------------------------------------

/** Sends typed messages towards all the processes.
 *
 * @param id               Id of the process
 * @param type             Type of a message
 *
 */
void process_send_multicast(local_id id, int16_t type);

/** Sends typed messages towards the process.
 *
 * @param from             Id of the process
 * @param to               Id of recieving process
 * @param type             Type of a message
 *
 */
void process_send(local_id from, local_id to, int16_t type);

//------------------------------------------------------------------------------

/** Recieves typed messages from all the processes.
 *
 * @param num_processes    Number of child processes
 * @param id               Id of the process
 * @param type             Type of a message
 *
 */
void process_recieve_all(int8_t num_processes, local_id id, int16_t type);

//------------------------------------------------------------------------------

/** Recieves any messages without processing.
 *
 * @param id               Id of the process
 *
 */
void process_recieve_any(local_id id);

//------------------------------------------------------------------------------

/** Performs workload after all syncronisation procedures. **/
void process_load(local_id id);

//------------------------------------------------------------------------------

#endif // __IFMO_DISTRIBUTED_CLASS_PIPE_MANAGER__H

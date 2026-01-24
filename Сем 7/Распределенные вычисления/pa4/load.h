#ifndef __IFMO_DISTRIBUTED_CLASS_LOAD__H
#define __IFMO_DISTRIBUTED_CLASS_LOAD__H

#include "ipc.h"

typedef struct
{
	local_id id;
	local_id req_id;
	timestamp_t req_time;
} request_queue_t;

//------------------------------------------------------------------------------

/** Performs workload after all syncronisation procedures. **/
void load(local_id id);

//------------------------------------------------------------------------------

#endif // __IFMO_DISTRIBUTED_CLASS_LOAD__H

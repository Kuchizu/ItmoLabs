#ifndef IPC_EXT_H
#define IPC_EXT_H

#include "ipc.h"

typedef struct {
    local_id id;
    uint8_t process_count;
    int pipes[MAX_PROCESS_ID + 1][MAX_PROCESS_ID + 1][2];
    local_id last_sender;
} IPCContext;

#endif /* IPC_EXT_H */

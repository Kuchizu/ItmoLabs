#ifndef LAB2_H
#define LAB2_H

#include <sys/types.h>

int lab2_open(const char *path);

int lab2_close(int fd);

ssize_t lab2_read(int fd, void *buf, size_t count);

ssize_t lab2_write(int fd, const void *buf, size_t count);

off_t lab2_lseek(int fd, off_t offset, int whence);

int lab2_fsync(int fd);

#endif

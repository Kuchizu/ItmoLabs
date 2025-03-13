#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <errno.h>

#include "lab2.h"

#define BLOCK_SIZE 4096

#define CACHE_CAPACITY 1024

#define MAX_FILES 128

// Структура для одного блока кеша
typedef struct {
    off_t block_number;  // Номер блока в файле (block = offset / BLOCK_SIZE)
    char *data;          // Указатель на память под блок (выравненный буфер)
    int valid;           // 1, если блок валиден; 0 — нет
    int dirty;           // 1, если блок был изменён и не сброшен на диск
    unsigned int age;    // Счётчик "старения"
    unsigned int refbit; // Референс-бит (для обновления age)
} cache_block_t;

// Структура для учёта файла
typedef struct {
    int real_fd;     // Настоящий файловый дескриптор ОС (от open)
    off_t offset;    // Текущая позиция для данного файла
    int in_use;      // 1, если fd используется; 0, если свободен
} file_info_t;


static cache_block_t g_cache[CACHE_CAPACITY];

static file_info_t g_fd_table[MAX_FILES];

static int g_cache_initialized = 0;

static void *aligned_alloc_block(size_t size) {
    void *ptr = NULL;
    int ret = posix_memalign(&ptr, BLOCK_SIZE, size);
    if (ret != 0) {
        return NULL;
    }
    memset(ptr, 0, size);
    return ptr;
}

static void init_cache_if_needed() {
    if (g_cache_initialized) return;

    for (int i = 0; i < CACHE_CAPACITY; i++) {
        g_cache[i].block_number = -1;
        g_cache[i].valid = 0;
        g_cache[i].dirty = 0;
        g_cache[i].age = 0;
        g_cache[i].refbit = 0;
        g_cache[i].data = (char *)aligned_alloc_block(BLOCK_SIZE);
        if (!g_cache[i].data) {
            fprintf(stderr, "Failed to allocate aligned memory for cache block\n");
        }
    }

    for (int i = 0; i < MAX_FILES; i++) {
        g_fd_table[i].real_fd = -1;
        g_fd_table[i].offset  = 0;
        g_fd_table[i].in_use  = 0;
    }

    g_cache_initialized = 1;
}

static int find_block_in_cache(int fd_idx, off_t block_num) {
    (void)fd_idx;
    for (int i = 0; i < CACHE_CAPACITY; i++) {
        if (g_cache[i].valid && g_cache[i].block_number == block_num) {
            return i;
        }
    }
    return -1;
}

// Выполним "aging tick" для всех блоков, когда собираемся кого-то вытеснять.
// Идея: age = (age >> 1) | (refbit << (какой-то старший бит)), refbit сбрасывается.
static void perform_aging() {
    for (int i = 0; i < CACHE_CAPACITY; i++) {
        g_cache[i].age >>= 1;
        if (g_cache[i].refbit) {
            g_cache[i].age |= (1U << 31);
        }
        g_cache[i].refbit = 0;
    }
}

static int find_victim_block() {
    perform_aging();

    unsigned int min_age = (unsigned int)-1;
    int victim_idx = -1;

    for (int i = 0; i < CACHE_CAPACITY; i++) {
        if (!g_cache[i].valid) {
            return i;
        }
        if (g_cache[i].age < min_age) {
            min_age = g_cache[i].age;
            victim_idx = i;
        }
    }
    return victim_idx;
}

static void flush_block_to_disk(int real_fd, cache_block_t *block) {
    if (!block->valid || !block->dirty) return;

    off_t offset = block->block_number * BLOCK_SIZE;
    if (lseek(real_fd, offset, SEEK_SET) == (off_t)-1) {
        perror("lseek (flush_block_to_disk)");
        return;
    }

    ssize_t written = write(real_fd, block->data, BLOCK_SIZE);
    if (written < 0) {
        perror("write (flush_block_to_disk)");
    } else if (written != BLOCK_SIZE) {
        fprintf(stderr, "Partial write in flush_block_to_disk: %zd\n", written);
    }

    block->dirty = 0;
}

static void read_block_from_disk(int real_fd, cache_block_t *block, off_t block_num) {
    off_t offset = block_num * BLOCK_SIZE;
    if (lseek(real_fd, offset, SEEK_SET) == (off_t)-1) {
        perror("lseek (read_block_from_disk)");
    }

    ssize_t bytes_read = read(real_fd, block->data, BLOCK_SIZE);
    if (bytes_read < 0) {
        perror("read (read_block_from_disk)");
    } else if (bytes_read < BLOCK_SIZE) {
        memset(block->data + bytes_read, 0, BLOCK_SIZE - bytes_read);
    }

    block->block_number = block_num;
    block->valid = 1;
    block->dirty = 0;
    block->age = 0;
    block->refbit = 1; // Посвежел
}

// Достаём блок из кеша. Если нет — вытесняем жертву, сбрасываем её, читаем нужный блок.
static int get_block_for_read(int fd_idx, off_t block_num) {
    int cache_idx = find_block_in_cache(fd_idx, block_num);
    if (cache_idx >= 0) {
        g_cache[cache_idx].refbit = 1; // Access
        return cache_idx;
    }

    // Not in cache ? Find victim
    cache_idx = find_victim_block();
    if (cache_idx < 0) {
        // Maybe ?
        return -1;
    }

    flush_block_to_disk(g_fd_table[fd_idx].real_fd, &g_cache[cache_idx]);

    read_block_from_disk(g_fd_table[fd_idx].real_fd, &g_cache[cache_idx], block_num);

    return cache_idx;
}

static int get_block_for_write(int fd_idx, off_t block_num) {
    int cache_idx = get_block_for_read(fd_idx, block_num);
    return cache_idx;
}

int lab2_open(const char *path) {
    init_cache_if_needed();

    int fd_idx = -1;
    for (int i = 0; i < MAX_FILES; i++) {
        if (!g_fd_table[i].in_use) {
            fd_idx = i;
            break;
        }
    }
    if (fd_idx < 0) {
        errno = EMFILE; // слишком много открытых
        return -1;
    }

    int real_fd = open(path, O_RDWR | O_CREAT | O_DIRECT | O_SYNC, 0666);
    if (real_fd < 0) {
        perror("open");
        return -1;
    }

    g_fd_table[fd_idx].real_fd = real_fd;
    g_fd_table[fd_idx].offset  = 0;
    g_fd_table[fd_idx].in_use  = 1;

    return fd_idx;
}

int lab2_close(int fd) {
    if (fd < 0 || fd >= MAX_FILES || !g_fd_table[fd].in_use) {
        errno = EBADF;
        return -1;
    }

    lab2_fsync(fd);

    int real_fd = g_fd_table[fd].real_fd;
    close(real_fd);

    g_fd_table[fd].real_fd = -1;
    g_fd_table[fd].offset  = 0;
    g_fd_table[fd].in_use  = 0;

    return 0;
}

ssize_t lab2_read(int fd, void *buf, size_t count) {
    if (fd < 0 || fd >= MAX_FILES || !g_fd_table[fd].in_use) {
        errno = EBADF;
        return -1;
    }

    file_info_t *finfo = &g_fd_table[fd];
    size_t bytes_read_total = 0;

    while (count > 0) {
        off_t block_num = finfo->offset / BLOCK_SIZE;
        size_t block_offset = finfo->offset % BLOCK_SIZE;

        size_t bytes_in_block = BLOCK_SIZE - block_offset;
        size_t to_read = (count < bytes_in_block) ? count : bytes_in_block;

        int cache_idx = get_block_for_read(fd, block_num);
        if (cache_idx < 0) {
            if (bytes_read_total == 0) {
                return -1;
            }
            break; // Return what read
        }

        memcpy((char *)buf + bytes_read_total,
               g_cache[cache_idx].data + block_offset,
               to_read);

        bytes_read_total += to_read;
        count -= to_read;
        finfo->offset += to_read;

    }

    return bytes_read_total;
}

ssize_t lab2_write(int fd, const void *buf, size_t count) {
    if (fd < 0 || fd >= MAX_FILES || !g_fd_table[fd].in_use) {
        errno = EBADF;
        return -1;
    }

    file_info_t *finfo = &g_fd_table[fd];
    size_t bytes_written_total = 0;

    while (count > 0) {
        off_t block_num = finfo->offset / BLOCK_SIZE;
        size_t block_offset = finfo->offset % BLOCK_SIZE;

        size_t bytes_in_block = BLOCK_SIZE - block_offset;
        size_t to_write = (count < bytes_in_block) ? count : bytes_in_block;

        int cache_idx = get_block_for_write(fd, block_num);
        if (cache_idx < 0) {
            if (bytes_written_total == 0) {
                return -1;
            }
            break;
        }

        memcpy(g_cache[cache_idx].data + block_offset,
               (char *)buf + bytes_written_total,
               to_write);

        g_cache[cache_idx].dirty = 1;
        g_cache[cache_idx].refbit = 1;

        bytes_written_total += to_write;
        count -= to_write;
        finfo->offset += to_write;
    }

    return bytes_written_total;
}

off_t lab2_lseek(int fd, off_t offset, int whence) {
    if (fd < 0 || fd >= MAX_FILES || !g_fd_table[fd].in_use) {
        errno = EBADF;
        return (off_t)-1;
    }

    if (whence != SEEK_SET) {
        errno = EINVAL;
        return (off_t)-1;
    }

    g_fd_table[fd].offset = offset;
    return offset;
}

int lab2_fsync(int fd) {
    if (fd < 0 || fd >= MAX_FILES || !g_fd_table[fd].in_use) {
        errno = EBADF;
        return -1;
    }

    int real_fd = g_fd_table[fd].real_fd;

    for (int i = 0; i < CACHE_CAPACITY; i++) {
        if (g_cache[i].valid && g_cache[i].dirty) {
            flush_block_to_disk(real_fd, &g_cache[i]);
        }
    }

    if (fsync(real_fd) < 0) {
        perror("fsync");
        return -1;
    }

    return 0;
}

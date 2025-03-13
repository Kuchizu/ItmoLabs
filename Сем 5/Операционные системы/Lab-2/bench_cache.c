// bench_cache.c
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <unistd.h>
#include "lab2.h"

#define BLOCK_SIZE 4096

long bench1_cached(long num_writes) {
    if (!num_writes) {
        return -1;
    }

    int fd = lab2_open("io_lat_write_test_cache.dat");
    if (fd == -1) {
        perror("lab2_open");
        return -1;
    }

    char buffer[BLOCK_SIZE];
    memset(buffer, 'A', BLOCK_SIZE);

    struct timeval start, end;
    gettimeofday(&start, NULL);

    for (long i = 0; i < num_writes; i++) {
        long random_offset = (rand() % 64) * BLOCK_SIZE;
        if (lab2_lseek(fd, random_offset, SEEK_SET) == (off_t)-1) {
            perror("lab2_lseek");
            lab2_close(fd);
            return -1;
        }

        ssize_t bytes_written = lab2_write(fd, buffer, BLOCK_SIZE);
        if (bytes_written != BLOCK_SIZE) {
            perror("lab2_write");
            lab2_close(fd);
            return -1;
        }
    }

    if (lab2_fsync(fd) == -1) {
        perror("lab2_fsync");
    }

    lab2_close(fd);

    gettimeofday(&end, NULL);
    long elapsed = (end.tv_sec - start.tv_sec) * 1000000L
                 + (end.tv_usec - start.tv_usec);

    return elapsed;
}

int main(int argc, char* argv[]) {
    if (argc != 2) {
        fprintf(stderr, "Usage: %s <number_of_writes>\n", argv[0]);
        exit(EXIT_FAILURE);
    }
    long num_writes = atol(argv[1]);
    if (num_writes <= 0) {
        fprintf(stderr, "Number of writes must be positive\n");
        exit(EXIT_FAILURE);
    }

    long elapsed = bench1_cached(num_writes);
    if (elapsed >= 0) {
        printf("Total time: %ld microseconds\n", elapsed);
    } else {
        fprintf(stderr, "Benchmark failed.\n");
    }

    return 0;
}

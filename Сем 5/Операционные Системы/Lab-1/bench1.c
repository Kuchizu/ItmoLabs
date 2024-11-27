#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/time.h>
#include <string.h>

#define BLOCK_SIZE 1024

int main(int argc, char *argv[]) {
    if (argc != 2) {
        fprintf(stderr, "Usage: %s <number_of_writes>\n", argv[0]);
        exit(EXIT_FAILURE);
    }
    long num_writes = atol(argv[1]);
    if (num_writes <= 0) {
        fprintf(stderr, "Number of writes must be positive\n");
        exit(EXIT_FAILURE);
    }
    int fd = open("io_lat_write_test.dat", O_WRONLY | O_CREAT | O_TRUNC, 0666);
    if (fd == -1) {
        perror("Failed to open file");
        exit(EXIT_FAILURE);
    }

    char buffer[BLOCK_SIZE];
    memset(buffer, 'A', BLOCK_SIZE);
    struct timeval start, end;
    gettimeofday(&start, NULL);

    for (long i = 0; i < num_writes; i++) {
        ssize_t bytes_written = write(fd, buffer, BLOCK_SIZE);
        if (bytes_written != BLOCK_SIZE) {
            perror("Failed to write to file");
            close(fd);
            exit(EXIT_FAILURE);
        }
    }

    fsync(fd);
    close(fd);
    gettimeofday(&end, NULL);
    long elapsed = (end.tv_sec - start.tv_sec) * 1000000;
    elapsed += (end.tv_usec - start.tv_usec);

    printf("Total time: %ld microseconds\n", elapsed);

    return 0;
}

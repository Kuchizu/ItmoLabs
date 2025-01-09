// bench1.c
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <unistd.h>

#define BLOCK_SIZE 1024

long bench1(long num_writes) {
  if (!num_writes) {
    return -1;
  }

  int fd = open("io_lat_write_test.dat", O_WRONLY | O_CREAT | O_TRUNC, 0666);
  if (fd == -1) {
    perror("Failed to open file");
    return -1;
  }

  char buffer[BLOCK_SIZE];
  memset(buffer, 'A', BLOCK_SIZE);
  struct timeval start, end;
  gettimeofday(&start, NULL);

  for (long i = 0; i < num_writes; i++) {
    long random_offset = (rand() % 1024) * BLOCK_SIZE;
    if (lseek(fd, random_offset, SEEK_SET) == -1) {
      perror("Failed to seek file");
      close(fd);
      return -1;
    }

    ssize_t bytes_written = write(fd, buffer, BLOCK_SIZE);
    if (bytes_written != BLOCK_SIZE) {
      perror("Failed to write to file");
      close(fd);
      return -1;
    }
  }

  fsync(fd);
  close(fd);
  gettimeofday(&end, NULL);
  long elapsed = (end.tv_sec - start.tv_sec) * 1000000;
  elapsed += (end.tv_usec - start.tv_usec);

  return elapsed;
}

int main_bench1(int argc, char* argv[]) {
  if (argc != 2) {
    fprintf(stderr, "Usage: %s <number_of_writes>\n", argv[0]);
    exit(EXIT_FAILURE);
  }
  long num_writes = atol(argv[1]);
  if (num_writes <= 0) {
    fprintf(stderr, "Number of writes must be positive\n");
    exit(EXIT_FAILURE);
  }

  long elapsed = bench1(num_writes);
  if (elapsed >= 0) {
    printf("Total time: %ld microseconds\n", elapsed);
  } else {
    fprintf(stderr, "Benchmark failed.\n");
  }

  return 0;
}

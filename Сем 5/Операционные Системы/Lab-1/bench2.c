#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>

int main(int argc, char *argv[]) {
    if (argc != 2) {
        fprintf(stderr, "Usage: %s <number_of_iterations>\n", argv[0]);
        exit(EXIT_FAILURE);
    }

    long num_iterations = atol(argv[1]);
    if (num_iterations <= 0) {
        fprintf(stderr, "Number of iterations must be positive\n");
        exit(EXIT_FAILURE);
    }

    volatile int dummy = 0;
    struct timeval start, end;
    gettimeofday(&start, NULL);

    for (long i = 0; i < num_iterations; i++) {
        dummy += 1;
    }

    gettimeofday(&end, NULL);

    long elapsed = (end.tv_sec - start.tv_sec) * 1000000;
    elapsed += (end.tv_usec - start.tv_usec);

    printf("Total time: %ld microseconds\n", elapsed);
    printf("Result: %d\n", dummy);  // Чтобы избежать оптимизации

    return 0;
}

#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <sys/time.h>

volatile int dummy = 0;

void *thread_func(void *arg) {
    long num_iterations = *(long *)arg;
    for (long i = 0; i < num_iterations; i++) {
        dummy += 1;
    }
    return NULL;
}

int main(int argc, char *argv[]) {
    if (argc != 3) {
        fprintf(stderr, "Usage: %s <number_of_threads> <iterations_per_thread>\n", argv[0]);
        exit(EXIT_FAILURE);
    }
    int num_threads = atoi(argv[1]);
    long iterations_per_thread = atol(argv[2]);
    pthread_t *threads = malloc(num_threads * sizeof(pthread_t));
    if (threads == NULL) {
        perror("Failed to allocate memory for threads");
        exit(EXIT_FAILURE);
    }
    struct timeval start, end;
    gettimeofday(&start, NULL);
    for (int i = 0; i < num_threads; i++) {
        if (pthread_create(&threads[i], NULL, thread_func, &iterations_per_thread) != 0) {
            perror("Failed to create thread");
            free(threads);
            exit(EXIT_FAILURE);
        }
    }
    for (int i = 0; i < num_threads; i++) {
        pthread_join(threads[i], NULL);
    }
    gettimeofday(&end, NULL);
    free(threads);
    long elapsed = (end.tv_sec - start.tv_sec) * 1000000;
    elapsed += (end.tv_usec - start.tv_usec);
    printf("Total time: %ld microseconds\n", elapsed);
    printf("Result: %d\n", dummy);

    return 0;
}

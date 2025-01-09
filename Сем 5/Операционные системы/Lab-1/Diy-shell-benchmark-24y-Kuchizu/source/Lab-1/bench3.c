// bench3.c
#include <limits.h>
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>

volatile int count = 0;

int minDistance3(int dist[], int sptSet[], int V) {
  int min = INT_MAX, min_index = -1;

  for (int v = 0; v < V; v++) {
    if (sptSet[v] == 0 && dist[v] <= min) {
      min = dist[v];
      min_index = v;
    }
  }

  return min_index;
}

void dijkstra3(int** graph, int V, int src) {
  int* dist = malloc(V * sizeof(int));
  int* sptSet = malloc(V * sizeof(int));

  if (!dist || !sptSet) {
    perror("Failed to allocate memory for Dijkstra's algorithm");
    exit(EXIT_FAILURE);
  }

  for (int i = 0; i < V; i++) {
    dist[i] = INT_MAX;
    sptSet[i] = 0;
  }
  dist[src] = 0;

  for (int count = 0; count < V - 1; count++) {
    int u = minDistance3(dist, sptSet, V);
    if (u == -1) {
      break;
    }
    sptSet[u] = 1;

    for (int v = 0; v < V; v++) {
      if (!sptSet[v] && graph[u][v] && dist[u] != INT_MAX && dist[u] + graph[u][v] < dist[v]) {
        dist[v] = dist[u] + graph[u][v];
      }
    }
  }

  free(dist);
  free(sptSet);

  count++;
}

int** generateRandomGraph(int V) {
  int** graph = malloc(V * sizeof(int*));
  if (!graph) {
    perror("Failed to allocate memory for graph");
    exit(EXIT_FAILURE);
  }
  for (int i = 0; i < V; i++) {
    graph[i] = malloc(V * sizeof(int));
    if (!graph[i]) {
      perror("Failed to allocate memory for graph row");
      for (int j = 0; j < i; j++) {
        free(graph[j]);
      }
      free(graph);
      exit(EXIT_FAILURE);
    }
    for (int j = 0; j < V; j++) {
      graph[i][j] = (i != j) ? (rand() % 20 + 1) : 0;
    }
  }
  return graph;
}

void freeGraph(int** graph, int V) {
  for (int i = 0; i < V; i++) {
    free(graph[i]);
  }
  free(graph);
}

void* thread_func(void* arg) {
  long* args = (long*)arg;
  long num_iterations = args[0];
  int V = args[1];

  for (long i = 0; i < num_iterations; i++) {
    int** graph = generateRandomGraph(V);
    dijkstra3(graph, V, 0);
    freeGraph(graph, V);
  }
  return NULL;
}

long bench3(int num_threads, long iterations_per_thread, int V, int* result) {
  if (num_threads <= 0 || iterations_per_thread <= 0) {
    return -1;
  }

  pthread_t* threads = malloc(num_threads * sizeof(pthread_t));
  if (!threads) {
    perror("Failed to allocate memory for threads");
    return -1;
  }

  long** thread_args = malloc(num_threads * sizeof(long*));
  if (!thread_args) {
    perror("Failed to allocate memory for thread arguments");
    free(threads);
    return -1;
  }

  for (int i = 0; i < num_threads; i++) {
    thread_args[i] = malloc(2 * sizeof(long));
    if (!thread_args[i]) {
      perror("Failed to allocate memory for thread argument row");
      for (int j = 0; j < i; j++) {
        free(thread_args[j]);
      }
      free(thread_args);
      free(threads);
      return -1;
    }
    thread_args[i][0] = iterations_per_thread;
    thread_args[i][1] = V;
  }

  struct timeval start, end;
  gettimeofday(&start, NULL);
  for (int i = 0; i < num_threads; i++) {
    if (pthread_create(&threads[i], NULL, thread_func, thread_args[i]) != 0) {
      perror("Failed to create thread");
      for (int j = 0; j <= i; j++) {
        free(thread_args[j]);
      }
      free(thread_args);
      free(threads);
      return -1;
    }
  }
  for (int i = 0; i < num_threads; i++) {
    pthread_join(threads[i], NULL);
  }
  gettimeofday(&end, NULL);

  for (int i = 0; i < num_threads; i++) {
    free(thread_args[i]);
  }
  free(thread_args);
  free(threads);

  long elapsed = (end.tv_sec - start.tv_sec) * 1000000;
  elapsed += (end.tv_usec - start.tv_usec);
  *result = count;

  return elapsed;
}

int main_bench3(int argc, char* argv[]) {
  if (argc != 3) {
    fprintf(stderr, "Usage: %s <number_of_threads> <iterations_per_thread>\n", argv[0]);
    exit(EXIT_FAILURE);
  }

  int num_threads = atoi(argv[1]);
  long iterations_per_thread = atol(argv[2]);
  int V = 10;
  int result = 0;
  long elapsed = bench3(num_threads, iterations_per_thread, V, &result);
  if (elapsed >= 0) {
    printf("Total time: %ld microseconds\n", elapsed);
    printf("Result: %d\n", result);
  } else {
    fprintf(stderr, "Benchmark failed.\n");
  }

  return 0;
}

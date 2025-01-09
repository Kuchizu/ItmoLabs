// bench2.c
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>

#define V 5

int minDistance(int dist[], int sptSet[]) {
  int min = INT_MAX, min_index = -1;

  for (int v = 0; v < V; v++) {
    if (sptSet[v] == 0 && dist[v] <= min) {
      min = dist[v];
      min_index = v;
    }
  }

  return min_index;
}

void dijkstra(int graph[V][V], int src) {
  int dist[V];
  int sptSet[V];

  for (int i = 0; i < V; i++) {
    dist[i] = INT_MAX;
    sptSet[i] = 0;
  }
  dist[src] = 0;

  for (int count = 0; count < V - 1; count++) {
    int u = minDistance(dist, sptSet);
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
}

void genGraph(int graph[V][V]) {
  for (int i = 0; i < V; i++) {
    for (int j = 0; j < V; j++) {
      if (i == j) {
        graph[i][j] = 0;
      } else {
        graph[i][j] = rand() % 20 + 1;
      }
    }
  }
}

long bench2(long num_iterations, int* result) {
  if (num_iterations < 1) {
    return -1;
  }

  volatile int count = 0;

  int graph[V][V];
  int source = 0;

  struct timeval start, end;
  gettimeofday(&start, NULL);

  for (long i = 0; i < num_iterations; i++) {
    genGraph(graph);
    dijkstra(graph, source);
    count++;
  }

  gettimeofday(&end, NULL);

  long elapsed = (end.tv_sec - start.tv_sec) * 1000000;
  elapsed += (end.tv_usec - start.tv_usec);
  *result = count;

  return elapsed;
}

int main_bench2(int argc, char* argv[]) {
  if (argc != 2) {
    fprintf(stderr, "Usage: %s <number_of_iterations>\n", argv[0]);
    exit(EXIT_FAILURE);
  }

  long num_iterations = atol(argv[1]);
  int result = 0;
  long elapsed = bench2(num_iterations, &result);

  if (elapsed >= 0) {
    printf("Total time: %ld microseconds\n", elapsed);
    printf("Result: %d\n", result);
  } else {
    fprintf(stderr, "Benchmark failed.\n");
  }

  return 0;
}

#include <benchmark/benchmark.h>

#include "monolith/HelloWorld.hpp"

void Sample(benchmark::State& state) {
  for (auto _ : state) {
    auto result = monolith::HelloWorld();
    benchmark::DoNotOptimize(result);
  }
}

BENCHMARK(Sample);
BENCHMARK_MAIN();

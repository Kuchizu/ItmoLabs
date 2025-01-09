// test_bench3.cpp
#include <gtest/gtest.h>

extern "C" {
long bench3(int num_threads, long iterations_per_thread, int V, int* result);
}

TEST(Bench3Test, PositiveThreadsAndIterations) {
  int result = 0;
  long elapsed = bench3(1, 1, 5, &result);
  EXPECT_GT(elapsed, 0);
  EXPECT_GT(result, 0);
}

TEST(Bench3Test, ZeroThreads) {
  int result = 0;
  long elapsed = bench3(0, 1000, 5, &result);
  EXPECT_EQ(elapsed, -1);
}

TEST(Bench3Test, SingleThread) {
  int result = 0;
  long elapsed = bench3(1, 1000, 5, &result);
  EXPECT_GT(elapsed, 0);
  EXPECT_GT(result, 100);
}

TEST(Bench3Test, MultipleThreadsEqualWork) {
  int result = 0;
  long elapsed = bench3(4, 1000, 5, &result);
  EXPECT_GT(elapsed, 0);
  EXPECT_GT(result, 100);
}

TEST(Bench3Test, HighThreadCount) {
  int result = 0;
  long elapsed = bench3(4, 10, 5, &result);
  EXPECT_GT(elapsed, 0);
  EXPECT_GT(result, 100);
}

TEST(Bench3Test, LargeIterationsPerThread) {
  int result = 0;
  long elapsed = bench3(2, 100000, 5, &result);
  EXPECT_GT(elapsed, 0);
  EXPECT_GT(result, 100);
}

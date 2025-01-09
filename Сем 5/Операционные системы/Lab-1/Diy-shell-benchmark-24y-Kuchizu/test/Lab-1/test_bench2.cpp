// test_bench2.cpp
#include <gtest/gtest.h>

extern "C" {
long bench2(long num_iterations, int* result);
}

TEST(Bench2Test, PositiveIterations) {
  int result = 0;
  long elapsed = bench2(1000000, &result);
  EXPECT_GT(elapsed, 0);
  EXPECT_EQ(result, 1000000);
}

TEST(Bench2Test, ZeroIterations) {
  int result = 0;
  long elapsed = bench2(0, &result);
  EXPECT_EQ(elapsed, -1);
}

TEST(Bench2Test, NegativeIterations) {
  int result = 0;
  long elapsed = bench2(-100, &result);
  EXPECT_EQ(elapsed, -1);
}

TEST(Bench2Test, LargeIterations) {
  int result = 0;
  long elapsed = bench2(10000000, &result);
  EXPECT_GT(elapsed, 0);
  EXPECT_EQ(result, 10000000);
}

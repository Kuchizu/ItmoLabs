// test_bench1.cpp
#include <gtest/gtest.h>

extern "C" {
long bench1(long num_writes);
}

TEST(Bench1Test, PositiveWrites) {
  long elapsed = bench1(10);
  EXPECT_GT(elapsed, 0);
}

TEST(Bench1Test, ZeroWrites) {
  long elapsed = bench1(0);
  EXPECT_EQ(elapsed, -1);
}

TEST(Bench1Test, SingleWrite) {
  long elapsed = bench1(1);
  EXPECT_GT(elapsed, 0);
}

TEST(Bench1Test, LargeNumberOfWrites) {
  long elapsed = bench1(100000);
  EXPECT_GT(elapsed, 0);
}

TEST(Bench1Test, FileCreation) {
  remove("io_lat_write_test.dat");
  long elapsed = bench1(1);
  EXPECT_GT(elapsed, 0);
  FILE* file = fopen("io_lat_write_test.dat", "r");
  ASSERT_NE(file, nullptr);
  fclose(file);
}

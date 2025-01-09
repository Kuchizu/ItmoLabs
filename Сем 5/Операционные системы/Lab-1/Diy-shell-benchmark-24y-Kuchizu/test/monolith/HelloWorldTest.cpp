#include <gtest/gtest.h>

#include "monolith/HelloWorld.hpp"

namespace monolith {

TEST(HelloWorld, ExactMatch) {
  ASSERT_EQ(HelloWorld(), "Hello, World!");
}

}  // namespace monolith

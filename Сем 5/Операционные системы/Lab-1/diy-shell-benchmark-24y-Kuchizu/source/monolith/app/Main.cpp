#include <iostream>

#include "monolith/HelloWorld.hpp"

namespace monolith::app {

namespace {

void Main() {
  std::cout << HelloWorld() << '\n';
}

}  // namespace

}  // namespace monolith::app

int main() {
  monolith::app::Main();
}

#!/bin/bash

set -e

cd "$(dirname "$0")"/..

. ci/commons.bash

MODE=$1
if [ -z "$MODE" ]; then
  MODE="check"
fi

STYLE=$2
if [ -z "$STYLE" ]; then
  STYLE="--style"
fi

if [ "$MODE" != "fix" ] && [ "$MODE" != "check" ]; then
  error "Invalid argument. Must be either 'fix' or 'check'."
  exit 1
fi

if [ "$STYLE" != "--style" ] && [ "$STYLE" != "--no-style" ]; then
  error "Invalid argument. Must be either '--style' or '--no-style'."
  exit 1
fi

message "Got parameters: '$MODE', '$STYLE'."

message "Building the project..."
(cd build && make)

message "Testing the project..."
(cd build && ./test/monolith-test)

if [ "$MODE" = "fix" ]; then
  message "Formatting code..."
  find source test -iname '*.hpp' -o -iname '*.cpp' \
  | xargs clang-format -i --fallback-style=Google --verbose
fi

message "Checking code format..."
find source test -iname '*.hpp' -o -iname '*.cpp' \
| xargs clang-format -Werror --dry-run --fallback-style=Google --verbose

if [ "$STYLE" = "--style" ]; then
  message "Checking code style..."
  find source -iname '*.hpp' -o -iname '*.cpp' \
  | xargs clang-tidy -p build/compile_commands.json
fi


message "Precommit checks was passed!"

#!/bin/sh

set -e

cd "$(dirname "$0")"

LLVM_VERSION=18

for tool in clang clang++ clang-format clang-tidy; do
    ln -s $(which $tool-$LLVM_VERSION) /usr/local/bin/$tool
done

apt update
apt install -y cmake git

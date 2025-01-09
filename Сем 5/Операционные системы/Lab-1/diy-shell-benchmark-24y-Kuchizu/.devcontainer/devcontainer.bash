#!/bin/bash

set -e

cd "$(dirname "$0")"

apt update
apt install -y \
    cmake \
    git \
    clang \
    clangd \
    clang-tidy \
    clang-format \
    lldb

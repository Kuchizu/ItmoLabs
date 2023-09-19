#!/bin/bash

if [[ -z $1 ]]; then
	echo '[ERROR] .asm file not specified.'
	exit
fi

path=$1

echo [PWD] $(pwd)
echo [PATH] $path
echo [1] nasm -g $1 -felf64 -o temp.o
echo [2] ld -o "${path%%.*}" temp.o

nasm -g $1 -felf64 -o temp.o
ld -o "${path%%.*}" temp.o

rm temp.o

echo 'Done.'
echo

#!/bin/bash

output_file="threads_stat.csv"
echo "Threads,ContextSwitches" > $output_file

for threads in {1..10}; do
  cmd=""
  for ((i=1; i<=threads; i++)); do
    cmd+=" ./bench1 1000 &"
  done
  cmd=${cmd%&}

  cs=$(bash -c "perf stat -e context-switches $cmd wait" 2>&1 | grep "context-switches" | awk '{print $1}')
  echo "$threads,$cs" >> $output_file
done

#!/bin/bash

output_file="processes_stat_alternative.csv"
echo "Processes,ContextSwitches" > $output_file

for processes in {1..10}; do
  total_cs=0

  for ((i=1; i<=processes; i++)); do
    cs=$(perf stat -e context-switches ./bench3 1000000 2>&1 | grep "context-switches" | awk '{print $1}')
    cs=${cs//,/}
    total_cs=$((total_cs + cs))
  done

  echo "$processes,$total_cs" >> $output_file
done

echo "Done"

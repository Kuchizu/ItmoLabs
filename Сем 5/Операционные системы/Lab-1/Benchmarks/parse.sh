#!/bin/bash

output_file="context_switches_data.csv"
echo "Iterations,ContextSwitches" > $output_file

for iterations in 100 250 500 750 1000 1500 2000 3000 4000 5000 10000 20000 50000 100000 250000 500000 1000000; do
  cs=$(perf stat -e context-switches ./bench1 $iterations 2>&1 | grep "context-switches" | awk '{print $1}')
  echo "$iterations,$cs" >> $output_file
done

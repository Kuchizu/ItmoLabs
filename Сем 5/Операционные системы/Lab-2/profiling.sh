perf stat -o perf_stat.txt ./bench 10000
perf stat -o perf_stat_cache.txt ./bench_cache 10000

strace -c -o strace.txt ./bench 10000
strace -c -o strace_cache.txt ./bench_cache 10000
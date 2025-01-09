get_filename_component(MONOLITH_BENCH_SOURCE_PATH "./${PROJECT_NAME}" ABSOLUTE)

set(MONOLITH_BENCH_TARGET_BASE "${PROJECT_NAME}-bench")

macro(MONOLITH_BENCH_DECLARE suffix file)
    add_executable(
        ${MONOLITH_BENCH_TARGET_BASE}-${suffix}
        ${MONOLITH_BENCH_SOURCE_PATH}/${file}
    )

    target_link_libraries(
        ${MONOLITH_BENCH_TARGET_BASE}-${suffix}
        ${PROJECT_NAME}
        benchmark::benchmark
    )
endmacro()

MONOLITH_BENCH_DECLARE(hello-world HelloWorldBench.cpp)

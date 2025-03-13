cmake_minimum_required(VERSION 3.12)

project(elf64-loader LANGUAGES C)


set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

if(CMAKE_C_COMPILER_ID STREQUAL GNU)
    set(CMAKE_CONFIGURATION_TYPES Debug Release)
elseif(CMAKE_C_COMPILER_ID MATCHES Clang)
    set(CMAKE_CONFIGURATION_TYPES Debug Release)
endif()

if(CMAKE_BUILD_TYPE AND NOT CMAKE_BUILD_TYPE IN_LIST CMAKE_CONFIGURATION_TYPES)
    message(FATAL_ERROR "Unexpected build type ${CMAKE_BUILD_TYPE}, possible values: ${CMAKE_CONFIGURATION_TYPES}")
endif()

if(CMAKE_C_COMPILER_ID STREQUAL GNU OR CMAKE_C_COMPILER_ID MATCHES Clang)
    set(CMAKE_C_FLAGS       "-pedantic -Wall -Werror -Wno-unused -ggdb")
endif()

find_program(CLANG_TIDY clang-tidy)
message(STATUS "Clang-tidy: ${CLANG_TIDY}")
if(CLANG_TIDY)
    file(STRINGS clang-tidy-checks.txt clang_tidy_checks)
    list(JOIN clang_tidy_checks "," clang_tidy_checks_str)
    set(CMAKE_C_CLANG_TIDY
        ${CLANG_TIDY}
        -header-filter=${CMAKE_SOURCE_DIR}
        -checks=${clang_tidy_checks_str}
        -warnings-as-errors=*
    )
endif()

add_subdirectory(solution)

option(BUILD_TESTING "Enable tests" ON)
if(BUILD_TESTING)
    enable_testing()
    add_subdirectory(tester)
endif()

file(GLOB_RECURSE sources CONFIGURE_DEPENDS
    src/*.c
    src/*.h
    include/*.h
)

add_executable(elf64-loader ${sources})
target_include_directories(elf64-loader PRIVATE src include)

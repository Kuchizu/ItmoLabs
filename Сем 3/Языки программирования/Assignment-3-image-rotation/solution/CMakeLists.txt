file(GLOB_RECURSE sources CONFIGURE_DEPENDS
    src/*.c
    src/*.h
    include/*.h
)

add_executable(image-transformer ${sources}
        include/bmp.h
        src/bmp.c
        src/transformations.c
        include/transformations.h)
target_include_directories(image-transformer PRIVATE src include)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

add_compile_options(-Wall -Wextra -Wpedantic)

if(MONOLITH_DEVELOPER)
    add_compile_options(-Werror)
    set(CMAKE_EXPORT_COMPILE_COMMANDS ON)
endif()

#source/CMakeLists.txt

get_filename_component(MONOLITH_INCLUDE_PATH "." ABSOLUTE)
get_filename_component(MONOLITH_SOURCE_PATH "./${PROJECT_NAME}" ABSOLUTE)

file(GLOB_RECURSE MONOLITH_SOURCES CONFIGURE_DEPENDS *.hpp *.cpp *.c)

set(MONOLITH_LIB_SOURCES ${MONOLITH_SOURCES})

set(MONOLITH_APP_SOURCES monolith/app/Main.cpp)

add_library(${PROJECT_NAME} STATIC ${MONOLITH_LIB_SOURCES})
target_include_directories(${PROJECT_NAME} PUBLIC ${MONOLITH_INCLUDE_PATH})

add_executable(${PROJECT_NAME}-app ${MONOLITH_APP_SOURCES})
set_property(TARGET ${PROJECT_NAME}-app PROPERTY OUTPUT_NAME ${PROJECT_NAME})
target_link_libraries(
    ${PROJECT_NAME}-app PRIVATE
    ${PROJECT_NAME}
)

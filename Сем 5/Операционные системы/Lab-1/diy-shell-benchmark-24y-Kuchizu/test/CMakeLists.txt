get_filename_component(MONOLITH_TEST_INCLUDE_PATH "." ABSOLUTE)
get_filename_component(MONOLITH_TEST_SOURCE_PATH "./${PROJECT_NAME}" ABSOLUTE)

set(MONOLITH_TEST_TARGET "${PROJECT_NAME}-test")

file(GLOB_RECURSE MONOLITH_TEST_SOURCES CONFIGURE_DEPENDS *.hpp *.cpp)

add_executable(${MONOLITH_TEST_TARGET} ${MONOLITH_TEST_SOURCES})

target_include_directories(${MONOLITH_TEST_TARGET} PUBLIC ${MONOLITH_TEST_INCLUDE_PATH})

target_link_libraries(
    ${MONOLITH_TEST_TARGET} PRIVATE
    ${PROJECT_NAME}
    GTest::gtest
    GTest::gmock
)

gtest_discover_tests(${MONOLITH_TEST_TARGET})

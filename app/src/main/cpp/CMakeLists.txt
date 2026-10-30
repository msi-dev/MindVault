cmake_minimum_required(VERSION 3.22.1)

project("mindvault")

add_library(
    mindvault
    SHARED
    native-lib.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    mindvault
    ${log-lib}
)

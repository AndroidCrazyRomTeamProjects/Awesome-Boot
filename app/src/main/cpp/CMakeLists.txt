cmake_minimum_required(VERSION 3.22.1)

project("qmgplayer")

add_library(
        qmgplayer
        SHARED
        qmgplayer.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        qmgplayer
        ${log-lib}
)

cmake_minimum_required(VERSION 3.18.1)

project("BreakExample")

#引入c/c++编译源文件
file(GLOB source_file app_main.cpp)

#引入头文件
include_directories(breakpad/src breakpad/src/common/android/include)

add_subdirectory(breakpad)

add_library(
        BreakExample
        SHARED
        ${source_file})

find_library(
        log-lib
        log)

#连接动态库
target_link_libraries(
        BreakExample
        breakpad
        ${log-lib})
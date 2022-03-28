cmake_minimum_required(VERSION 3.4.1)

# 以从Makefile构建中获得更详细的输出，并在启动时显示每个命令行。
set(CMAKE_VERBOSE_MAKEFILE on)

set(lib_src_DIR ${CMAKE_CURRENT_SOURCE_DIR})

set(lib_build_DIR $ENV{HOME}/tmp/${ANDROID_ABI})
file(MAKE_DIRECTORY ${lib_build_DIR})

# 打印
message("lib_src_DIR=$lib_src_DIR")
message("lib_build_DIR=$lib_build_DIR")

add_subdirectory(${lib_src_DIR}/gmath ${lib_build_DIR}/gmath)
add_subdirectory(${lib_src_DIR}/gperf ${lib_build_DIR}/gperf)


cmake_minimum_required(VERSION 3.18.1)

project("BreakExample")

# 引入c/c++编译源文件
file(GLOB source_file app_main.cpp)

# 引入头文件
include_directories(breakpad/src breakpad/src/common/android/include)

add_subdirectory(breakpad)

# configure import libs
set(distribution_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution)

add_library(lib_gmath STATIC IMPORTED)
set_target_properties(lib_gmath PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/gmath/lib/${ANDROID_ABI}/libgmath.a)

# 将库导入为静态或共享（使用add_library）
add_library(lib_gperf STATIC IMPORTED)

# 配置每个库二进制位置（使用set_target_properties）
set_target_properties(lib_gperf PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/gperf/lib/${ANDROID_ABI}/libgperf.a)

# build application's shared lib
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

include_directories(${distribution_DIR}/gmath/include)
include_directories(${distribution_DIR}/gperf/include)

# PUBLIC PRIVATE INTERFACE
# target_include_directories(BreakExample INTERFACE
#        ${distribution_DIR}/gmath/include
#        ${distribution_DIR}/gperf/include)

add_library(
        BreakExample
        SHARED
        ${source_file})

find_library(
        log-lib
        log)

# 连接动态库
target_link_libraries(
        BreakExample
        lib_gmath
        lib_gperf
        android
        breakpad
        ${log-lib})
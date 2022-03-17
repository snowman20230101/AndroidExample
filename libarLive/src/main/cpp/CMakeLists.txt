cmake_minimum_required(VERSION 3.18.1)

project("libarlive")

# 引入c/c++编译源文件
file(GLOB source_file *.cpp CommonInclude.cpp app_main.cpp)

# 引入头文件
include_directories(${CMAKE_SOURCE_DIR}/ffmpeg/include)

include_directories(${CMAKE_SOURCE_DIR})

# 定义 rtmp总目录的变量
set(RTMP ${CMAKE_SOURCE_DIR}/rtmp)

# 指定rtmp里面的库文件目录（静态库）
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${RTMP}/libs/${CMAKE_ANDROID_ARCH_ABI}")

# 设置静态库路径
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/ffmpeg/libs/${CMAKE_ANDROID_ARCH_ABI}")

message("${CMAKE_SOURCE_DIR}")

add_library(
        libarlive
        SHARED
        ${source_file})

find_library(
        log-lib
        log)

# 连接动态库
target_link_libraries(
        libarlive
        avformat avcodec avfilter avutil swresample swscale
        z
        rtmp
        android
        jnigraphics
        OpenSLES
        ${log-lib})
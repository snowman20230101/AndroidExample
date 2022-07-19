cmake_minimum_required(VERSION 3.18.1)

project("libralive")

# 定义 rtmp,ffmpeg 总目录的变量
set(RTMP ${CMAKE_SOURCE_DIR}/rtmp)
set(ffmpeg ${CMAKE_SOURCE_DIR}/ffmpeg)

# 引入c/c++编译源文件
file(GLOB source_file *.cpp
        ffmpeg/examples/decode_video.c
        ffmpeg/examples/*.c
        NiubiPlayer.cpp)

# 引入头文件
include_directories(${CMAKE_SOURCE_DIR})
include_directories(${RTMP}/include)
include_directories(${ffmpeg}/include)

# 指定rtmp里面的库文件目录（静态库）
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${RTMP}/libs/${CMAKE_ANDROID_ARCH_ABI}")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${ffmpeg}/libs/${CMAKE_ANDROID_ARCH_ABI}")

message("RTMP=${RTMP}  ${CMAKE_ANDROID_ARCH_ABI}")
message("ffmpeg=${ffmpeg}")

add_library(
        libralive
        SHARED
        ${source_file})

find_library(
        log-lib
        log)

# 连接动态库
target_link_libraries(
        libralive
        avformat avcodec avfilter avutil swresample swscale
        z
        rtmp
        android
        jnigraphics
        OpenSLES
        ${log-lib})
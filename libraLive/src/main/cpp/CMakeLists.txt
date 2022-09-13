cmake_minimum_required(VERSION 3.18.1)

project("libralive")

add_subdirectory(breakpad)

# 定义 rtmp, ffmpeg 总目录的变量
set(RTMP ${CMAKE_SOURCE_DIR}/rtmp)
set(ffmpeg ${CMAKE_SOURCE_DIR}/ffmpeg)

# 引入c/c++编译源文件
file(GLOB source_file *.cpp
#        ffmpeg/examples/decode_video.c
#        ffmpeg/examples/filtering_video.c
#        ffmpeg/examples/filtering_audio.c
#        ffmpeg/examples/filter_audio.c
        player/*.cpp
        player/VideoChannel.cpp
        player/render/*.cpp
        player/filter/ScaleFilter.cpp
        player/filter/AudioFilter.cpp
        platform/*.cpp
        common/*.cpp
        util/*.cpp
        egl/*.cpp
        looper/*.cpp
#        egl/OffscreenSurface.cpp
        )

# 引入头文件
include_directories(
        ${CMAKE_SOURCE_DIR}/breakpad/src/common/android/include
        ${CMAKE_SOURCE_DIR}/breakpad/src
        ${ffmpeg}/include
        ${RTMP}/include
        ${CMAKE_SOURCE_DIR}/platform
        ${CMAKE_SOURCE_DIR}/common
        ${CMAKE_SOURCE_DIR}/util
        ${CMAKE_SOURCE_DIR}/player
        ${CMAKE_SOURCE_DIR}/player/filter
        ${CMAKE_SOURCE_DIR}/player/render
        ${CMAKE_SOURCE_DIR}/glm
        ${CMAKE_SOURCE_DIR}/egl
        ${CMAKE_SOURCE_DIR}/looper
)

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
        -Wl,--start-group
        avformat avcodec avutil avfilter swresample swscale
        -Wl,--end-group
        z
        rtmp
        android
        jnigraphics
        OpenSLES
        EGL
        GLESv3
        breakpad
        ${log-lib})
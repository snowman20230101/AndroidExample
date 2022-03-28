cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE on)

add_library(gmath STATIC src/gmath.c)

# copy out the lib binary... need to leave the static lib around to pass gradle check
set(distribution_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../../distribution)
set_target_properties(gmath
        PROPERTIES
        ARCHIVE_OUTPUT_DIRECTORY
        "${distribution_DIR}/gmath/lib/${ANDROID_ABI}")

# copy out lib header file...
add_custom_command(TARGET gmath POST_BUILD
        COMMAND "${CMAKE_COMMAND}" -E
        copy "${CMAKE_CURRENT_SOURCE_DIR}/src/gmath.h"
        "${distribution_DIR}/gmath/include/gmath.h"
        #                   **** the following 2 lines are for potential future debug purpose ****
        #                   COMMAND "${CMAKE_COMMAND}" -E
        #                   remove_directory "${CMAKE_CURRENT_BINARY_DIR}"
        COMMENT "Copying gmath to output directory")
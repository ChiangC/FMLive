cmake_minimum_required(VERSION 3.4.1)

file(GLOB C_SOURCE src/main/cpp/*.cpp src/main/cpp/*.c)

set(DIR ${CMAKE_SOURCE_DIR}/libs)

add_subdirectory(src/main/cpp/librtmp)

include_directories(libs/include)
MESSAGE("-------DIR:  " ${DIR})


add_library(x264
            STATIC
            IMPORTED)

set_target_properties(x264
                      PROPERTIES IMPORTED_LOCATION
                      ${DIR}/armeabi/libx264.a)

add_library(faac
            STATIC
            IMPORTED)

set_target_properties(faac
                      PROPERTIES IMPORTED_LOCATION
                      ${DIR}/armeabi/libfaac.a)



add_library( native-lib
             SHARED
             src/main/cpp/native-lib.cpp )

find_library(log-lib
              log )

target_link_libraries(
                        native-lib
                        ${log-lib}
                        faac
                        x264
                        rtmp
                      )
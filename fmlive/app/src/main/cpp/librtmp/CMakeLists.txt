#close openssl not surport rtmps
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO"  )

file(GLOB rtmp_source *.c)

#static library
add_library(
             rtmp
             STATIC
            ${rtmp_source} )
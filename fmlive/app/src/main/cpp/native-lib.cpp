#include <jni.h>
#include <string>

extern "C"{
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
}
extern "C" JNIEXPORT jstring

JNICALL
Java_com_fmtech_fmlive_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    av_register_all();
    return env->NewStringUTF(hello.c_str());
}

#include <jni.h>
#include <string>

#define LOGI(FORMAT,...) __android_log_print(ANDROID_LOG_INFO,"FMLive",FORMAT,##__VA_ARGS__)
#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR,"FMLive",FORMAT,##__VA_ARGS__)


extern "C"{
#include "x264.h"
#include "faac.h"
#include "librtmp/rtmp.h"
}

#include <queue>

extern "C"
JNIEXPORT void JNICALL
Java_com_fmtech_fmlive_push_PushNative_startPush(JNIEnv *env, jobject instance, jstring url_) {
    const char *url = env->GetStringUTFChars(url_, 0);

    // TODO

    env->ReleaseStringUTFChars(url_, url);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_fmtech_fmlive_push_PushNative_setVideoOptions(JNIEnv *env, jobject instance, jint width,
                                                       jint height, jint bitRate, jint fps) {

    // TODO

}extern "C"
JNIEXPORT void JNICALL
Java_com_fmtech_fmlive_push_PushNative_setAudioOptions(JNIEnv *env, jobject instance,
                                                       jint sampleRate, jint channel) {

    // TODO

}extern "C"
JNIEXPORT void JNICALL
Java_com_fmtech_fmlive_push_PushNative_pushAudio(JNIEnv *env, jobject instance, jbyteArray data_,
                                                 jint len) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);

    // TODO

    env->ReleaseByteArrayElements(data_, data, 0);
}extern "C"
JNIEXPORT void JNICALL
Java_com_fmtech_fmlive_push_PushNative_pushVideo(JNIEnv *env, jobject instance, jbyteArray data_) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);

    // TODO

    env->ReleaseByteArrayElements(data_, data, 0);
}
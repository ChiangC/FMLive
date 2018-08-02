#include <jni.h>
#include <string>
#include <queue>
#include <malloc.h>
#include <android/log.h>
#include <x264.h>
#include <pthread.h>

#define LOGI(FORMAT,...) __android_log_print(ANDROID_LOG_INFO,"FMLive",FORMAT,##__VA_ARGS__)
#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR,"FMLive",FORMAT,##__VA_ARGS__)


extern "C"{
#include "x264.h"
#include "faac.h"
#include "librtmp/rtmp.h"
}



char *path;
pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t cond = PTHREAD_COND_INITIALIZER;

int publishing = 0;
long start_time = 0;

std::queue<RTMPPacket *> queue;
faacEncHandle audioHandle;
unsigned long inputSamples;
unsigned long maxOutputBytes;

int y_len;
int u_len;
int v_len;
x264_picture_t *pic;
x264_picture_t *pic_out;
x264_t *video_encoder;

RTMPPacket* getRtmpPacket(){
    RTMPPacket *packet;
    pthread_mutex_lock(&mutex);
    if(queue.empty()){
        pthread_cond_wait(&cond, &mutex);
    }
    packet = queue.front();
    queue.pop();
    pthread_mutex_unlock(&mutex);
    return packet;
}

void *push_thread_routine(void *arg){
    publishing = 1;
    RTMP *rtmp = RTMP_Alloc();
    RTMP_Init(rtmp);
    rtmp->Link.timeout = 5;
    RTMP_SetupURL(rtmp, path);
    RTMP_EnableWrite(rtmp);
    LOGI("RTMP Connect Begin.");
    if(!RTMP_Connect(rtmp, NULL)){
        LOGE("RTMP Connect failed.");
        goto END;
    }
    LOGI("RTMP Connect success.");

    RTMP_ConnectStream(rtmp, 0);
    while(publishing){
        RTMPPacket *packet = getRtmpPacket();
        packet->m_nInfoField2 = rtmp->m_stream_id;
        int result = RTMP_SendPacket(rtmp, packet, 1);
        if(!result){
            LOGE("Send Packet failed.");
        }
        RTMPPacket_Free(packet);
        free(packet);
//        LOGI("Send Packet success.");
    }
    publishing = 0;
    free(path);

END:
    RTMP_Close(rtmp);
    RTMP_Free(rtmp);
    pthread_exit(NULL);
}

void enqueuePacket(RTMPPacket *packet){
    pthread_mutex_lock(&mutex);
    if(publishing){
        queue.push(packet);
    }

    pthread_cond_signal(&cond);
    pthread_mutex_unlock(&mutex);
}

void add_aac_body(unsigned char *bitBuf, int length){
    int body_size = length + 2;
    RTMPPacket *packet = (RTMPPacket *)malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(packet, body_size);
    char *body = packet->m_body;
    body[0] = 0xAF;
    body[1] = 0x01;
    memcpy(&body[2], bitBuf, length);
    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    packet->m_nBodySize = body_size;
    packet->m_hasAbsTimestamp = 0;
    packet->m_nChannel = 0x04;
    packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
    packet->m_nTimeStamp = RTMP_GetTime() - start_time;
    enqueuePacket(packet);
}

void add_264_body(unsigned char *data, int length){
    /*去掉帧界定符 00 00 00 01*/
    if(data[2] == 0x00){
        data += 4;
        length -= 4;
    }else if(data[2] == 0x01){// 00 00 01
        data += 3;
        length -= 3;
    }

    int body_size = length + 9;
    RTMPPacket *packet = (RTMPPacket *)malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(packet, length + 9);
    char *body = packet->m_body;
    int type = data[0] & 0x1f;
    body[0] = 0x27;
    if(type == NAL_SLICE_IDR){
        body[0] = 0x17;
    }
    body[1] = 0x01; /*nal unit*/
    body[2] = 0x00;
    body[3] = 0x00;
    body[4] = 0x00;

    body[5] = (length >> 24) & 0xff;
    body[6] = (length >> 16) & 0xff;
    body[7] = (length >> 8) & 0xff;
    body[8] = (length) & 0xff;

    /*copy data*/
    memcpy(&body[9], data, length);

    packet->m_hasAbsTimestamp = 0;
    packet->m_nBodySize = body_size;
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nChannel = 0x04;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nTimeStamp = RTMP_GetTime() - start_time;
    enqueuePacket(packet);
}

void add_264_sequence_header(unsigned char *pps, unsigned char *sps, int pps_len, int sps_len){
    int body_size = 13 + sps_len + 3 + pps_len;
    RTMPPacket *packet = (RTMPPacket *) malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(packet, body_size);
    RTMPPacket_Reset(packet);
    char *body = packet->m_body;
    int i = 0;
    body[i++] = 0x17;
    body[i++] = 0x00;
    //composition time 0x000000
    body[i++] = 0x00;
    body[i++] = 0x00;
    body[i++] = 0x00;

    /*AVCDecoderConfigurationRecord*/
    body[i++] = 0x01;
    body[i++] = sps[1];
    body[i++] = sps[2];
    body[i++] = sps[3];
    body[i++] = 0xFF;

    /*sps*/
    body[i++] = 0xE1;
    body[i++] = (pps_len >> 8) & 0xff;
    body[i++] = (pps_len) & 0xff;
    memcpy(&body[i], sps, sps_len);
    i += sps_len;

    /*pps*/
    body[i++] = 0x01;
    body[i++] = (pps_len >> 8) & 0xff;
    body[i++] = (pps_len) & 0xff;
    memcpy(&body[i], pps, pps_len);
    i += pps_len;

    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = body_size;
    packet->m_nChannel = 0x04;
    packet->m_nTimeStamp = 0;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
    enqueuePacket(packet);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_fmtech_fmlive_push_PushNative_startPush(JNIEnv *env, jobject instance, jstring url_) {
    const char *url = env->GetStringUTFChars(url_, 0);

    path = (char*)malloc(strlen(url) + 1);
    memset(path, 0, strlen(url) + 1);
    memcpy(path, url, strlen(url));

    /*pthread_cond_init(&cond, NULL);
    pthread_mutex_init(&mutex, NULL);*/
    pthread_t push_thread_id;
    start_time = RTMP_GetTime();
    pthread_create(&push_thread_id, NULL, push_thread_routine, NULL);

    env->ReleaseStringUTFChars(url_, url);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_fmtech_fmlive_push_PushNative_setVideoOptions(JNIEnv *env, jobject instance, jint width,
                                                       jint height, jint bitRate, jint fps) {

    x264_param_t param;
    y_len = width * height;
    u_len = y_len/4;
    v_len = u_len;
    x264_param_default_preset(&param, "ultrafast", "zerolatency");
    param.i_level_idc = 51;
    param.i_csp = X264_CSP_I420;
    param.i_width = width;
    param.i_height = height;
    param.i_threads = 1;

    param.i_fps_num = fps;
    param.i_fps_den = 1;
    param.i_timebase_num = param.i_fps_num;
    param.i_timebase_den = param.i_fps_den;
    //码率相关设置 关键帧间隔时间的帧率
    param.i_keyint_max = fps * 2;
    param.rc.i_rc_method = X264_RC_ABR;
    param.rc.i_bitrate = bitRate/1000;
    param.rc.i_vbv_max_bitrate = bitRate/1000*1.2;
    param.rc.i_vbv_buffer_size = bitRate/1000;

    //pts
    param.b_vfr_input = 0;
    //sps pps
    param.b_repeat_headers = 1;
    //设置画面质量
    x264_param_apply_profile(&param, "baseline");

    video_encoder = x264_encoder_open(&param);
    if(!video_encoder){
        LOGE("x264_encoder_open failed");
        return;
    }
    LOGI("x264_encoder_open success");

    pic = (x264_picture_t *)malloc(sizeof(x264_picture_t));
    pic_out = (x264_picture_t *)malloc(sizeof(x264_picture_t));
    x264_picture_alloc(pic, X264_CSP_I420, width, height);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_fmtech_fmlive_push_PushNative_setAudioOptions(JNIEnv *env, jobject instance,
                                                       jint sampleRate, jint channel) {

    int nSampleRate = sampleRate;
    audioHandle = faacEncOpen(nSampleRate, channel, &inputSamples, &maxOutputBytes);
    if(!audioHandle){
        LOGE("faacEncOpen failed.");
    }else{
        LOGI("faacEncOpen success.");
    }

    faacEncConfigurationPtr  configurationPtr = faacEncGetCurrentConfiguration(audioHandle);
    configurationPtr->mpegVersion = MPEG4;
    configurationPtr->allowMidside = 1;
    configurationPtr->aacObjectType = LOW;
    configurationPtr->outputFormat = 0;
    configurationPtr->useTns = 1;
    configurationPtr->useLfe = 0;
    configurationPtr->shortctl = SHORTCTL_NOSHORT;

    if(!faacEncSetConfiguration(audioHandle, configurationPtr)){
        LOGE("faacEncSetConfiguration failed.");
    }
    LOGI("faacEncSetConfiguration success.");

}


extern "C"
JNIEXPORT void JNICALL
Java_com_fmtech_fmlive_push_PushNative_pushAudio(JNIEnv *env, jobject instance, jbyteArray data_,
                                                 jint len) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);

    size_t byteCount = sizeof(unsigned char) * maxOutputBytes;
    unsigned char *bitBuf = (unsigned char *)malloc(byteCount);
    memset(bitBuf, 0, byteCount);
    int byteLength = faacEncEncode(audioHandle, (int32_t *)data, len, bitBuf, maxOutputBytes);
    LOGI("Got audio data length %d", byteLength);

    if(byteLength > 0){
        add_aac_body(bitBuf, byteLength);
//        LOGI("add_aac_body");
    }

    env->ReleaseByteArrayElements(data_, data, 0);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_fmtech_fmlive_push_PushNative_pushVideo(JNIEnv *env, jobject instance, jbyteArray data_) {
    //nv21 data
    jbyte *data = env->GetByteArrayElements(data_, NULL);

    jbyte *u = (jbyte *)pic->img.plane[1];
    jbyte *v = (jbyte *)pic->img.plane[2];
    memcpy(pic->img.plane[0], data, y_len);
    for(int i = 0; i < u_len; i++){
        *(u + i) =*(data + y_len + i*2 + 1);
        *(v + i) =*(data + y_len + i*2);
    }

    x264_nal_t *nal = NULL;
    int n_nal  = -1;
    if(x264_encoder_encode(video_encoder, &nal, &n_nal, pic, pic_out) < 0){
        LOGE("x264_encoder_encode failed.");
    }else{
//        LOGI("x264_encoder_encode success.");
    }

    unsigned char sps[100];
    unsigned char pps[100];
    int sps_len;
    int pps_len;
    for(int i = 0; i < n_nal; ++i){
        if(nal[i].i_type == NAL_SPS){
            sps_len = nal[i].i_payload - 4;
            memcpy(sps, nal[i].p_payload + 4, sps_len);
        }else if(nal[i].i_type == NAL_PPS){
            pps_len = nal[i].i_payload - 4;
            memcpy(pps, nal[i].p_payload + 4, pps_len);
            add_264_sequence_header(pps, sps, pps_len, sps_len);
        }else{
            add_264_body(nal[i].p_payload, nal[i].i_payload);
        }

    }
    env->ReleaseByteArrayElements(data_, data, 0);
}
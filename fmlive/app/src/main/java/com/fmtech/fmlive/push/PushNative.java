package com.fmtech.fmlive.push;

/**
 * ==================================================================
 * Copyright (C) 2018 FMTech Limited All Rights Reserved.
 *
 * Created on 18/7/27 17:11
 *
 * @author Drew.Chiang
 * @version v1.0.0
 *
 * ==================================================================
 */


public class PushNative {

    static {
        System.loadLibrary("native-lib");
    }

    public native void setVideoOptions(int width, int height, int bitRate, int fps);

    public native void setAudioOptions(int sampleRate, int channel);

    public native void pushAudio(byte[] data, int len);

    public native void pushVideo(byte[] data);

    public native void startPush(String url);

}

package com.fmtech.fmlive.push;

import android.app.Activity;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.fmtech.fmlive.params.AudioParam;
import com.fmtech.fmlive.params.VideoParam;

/**
 * ==================================================================
 * Copyright (C) 2018 FMTech Limited All Rights Reserved.
 *
 * Created on 18/7/27 17:09
 *
 * @author Drew.Chiang
 * @version v1.0.0
 *
 * ==================================================================
 */


public class LivePusher {
    private SurfaceHolder mSurfaceHolder;
    private AudioPusher mAudioPusher;
    private VideoPusher mVideoPusher;
    private PushNative mPushNative;

    public LivePusher(Activity activity, SurfaceHolder surfaceHolder, String url){
        mSurfaceHolder = surfaceHolder;
        init(activity, url);
    }

    private void init(Activity activity, String url){
        mPushNative = new PushNative();
        mAudioPusher = new AudioPusher(mPushNative, new AudioParam(44100, 1));
        mVideoPusher = new VideoPusher(activity, url, new VideoParam(960,720, Camera.CameraInfo.CAMERA_FACING_BACK), mSurfaceHolder);
        mVideoPusher.setPushNative(mPushNative);
        mPushNative.startPush(url);
    }

    public void startPush(){
        mVideoPusher.startPush();
        mAudioPusher.startPush();
    }

    public void stopPush(){
        mVideoPusher.stopPush();
        mAudioPusher.stopPush();
    }

    public void switchCamera(){
        mVideoPusher.switchCamera();
    }

}

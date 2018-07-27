package com.fmtech.fmlive.push;

import android.app.Activity;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.fmtech.fmlive.params.VideoParam;

/**
 * ==================================================================
 * Copyright (C) 2018 FMTech Limited All Rights Reserved.
 *
 * Created on 18/7/27 17:07
 *
 * @author Drew.Chiang
 * @version v1.0.0
 *
 * ==================================================================
 */


public class VideoPusher extends Pusher implements SurfaceHolder.Callback, Camera.PreviewCallback{
    private Camera mCamera;
    private VideoParam mVideoParam;
    private SurfaceHolder mSurfaceHolder;
    private byte[] mBuffers;
    private PushNative mPushNative;
    private Activity mActivity;
    private int mScreen;

    private byte[] mRaw;
    private static final int SCREEN_PORTRAIT = 0;
    private static final int SCREEN_LANDSCAPE_LEFT = 90;
    private static final int SCREEN_LANDSCAPE_RIGHT = 270;

    private String mUrl;

    public VideoPusher(Activity activity, String url, VideoParam videoParam, SurfaceHolder surfaceHolder){
        mActivity = activity;
        mVideoParam = videoParam;
        mSurfaceHolder = surfaceHolder;
        mSurfaceHolder.addCallback(this);
        mUrl = url;
    }

    public void setPushNative(PushNative pushNative){
        mPushNative = pushNative;
    }

    @Override
    public void startPush() {
        startPreview();
        isPushing = true;
    }

    @Override
    public void stopPush() {

    }

    @Override
    public void release() {

    }

    public void switchCamera(){
        if(Camera.CameraInfo.CAMERA_FACING_BACK == mVideoParam.getCameraId()){
            mVideoParam.setCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }else{
            mVideoParam.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
        }

        stopPreview();
        startPreview();

    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }


    private void startPreview(){

    }

    private void stopPreview(){

    }

}

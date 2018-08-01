package com.fmtech.fmlive.push;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.fmtech.fmlive.params.VideoParam;

import java.util.Iterator;
import java.util.List;

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
        mCamera.stopPreview();
        isPushing = false;
    }

    @Override
    public void release() {
        mCamera.release();
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
        if(isPushing){
            switch (mScreen){
                case SCREEN_PORTRAIT:
                    portraitData2Raw(mBuffers);
                    break;
                case SCREEN_LANDSCAPE_LEFT:
                    mRaw = mBuffers;
                    break;
                case SCREEN_LANDSCAPE_RIGHT:
                    landscapeData2Raw(mBuffers);
                    break;
            }

            //very important
            if(null != mCamera){
                mCamera.addCallbackBuffer(mBuffers);
            }

            mPushNative.pushVideo(mRaw);
        }

    }

    private void portraitData2Raw(byte[] data){
        int width = mVideoParam.getWidth(), height = mVideoParam.getHeight();
        int y_len = width * height;
        int uvHeight = height >> 1;// uv is half of y
        int k = 0;
        if(mVideoParam.getCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK){
            for(int j = 0; j < width; j++){
                for(int i = height - 1; i >= 0; i--){
                    mRaw[k++] = data[width * i + j];
                }
            }

            for(int j = 0; j < width; j += 2){
                for(int i = uvHeight - 1; i >= 0; i--){
                    mRaw[k++] = data[y_len + width * i + j];
                    mRaw[k++] = data[y_len + width * i + j + 1];
                }
            }
        }else{
            for(int i = 0; i < width; i++){
                int pos = width - 1;
                for(int j = 0; j < height; j++){
                    mRaw[k] = data[pos - i];
                    k++;
                    pos += width;
                }
            }

            for(int i = 0; i < width; i += 2){
                int pos = y_len + width - 1;
                for(int j = 0; j < uvHeight; j++){
                    mRaw[k] = data[pos - i - 1];
                    mRaw[k + 1] = data[pos - i];
                    k += 2;
                    pos += width;
                }
            }
        }
    }

    private void landscapeData2Raw(byte[] data){
        int width = mVideoParam.getWidth();
        int height = mVideoParam.getHeight();
        int y_len = width * height;
        int k = 0;

        for(int i = y_len - 1; i >= 0; i-- ){
            mRaw[k] = data[i];
            k++;
        }

        // v1 u1 v2 u2
        // v3 u3 v4 u4
        // change to:
        // v4 u4 v3 u3
        // v2 u2 v1 u1

        int maxPos = data.length - 1;
        int uv_len = y_len >> 2;//y:u:v  4:1:1
        for(int i = 0; i < uv_len; i++){
            int pos  = i << 1;
            mRaw[y_len + i * 2] = data[maxPos - pos -1];
            mRaw[y_len + i * 2] = data[maxPos - pos];
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        stopPreview();
        startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }


    private void startPreview(){
        try {
            mPushNative.setVideoOptions(mVideoParam.getWidth(), mVideoParam.getHeight(), mVideoParam.getBitrate(),
                    mVideoParam.getFps());

            mCamera = Camera.open(mVideoParam.getCameraId());
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewFormat(ImageFormat.NV21);

            setPreviewSize(parameters);

            setPreviewOrientation(parameters);

            mCamera.setParameters(parameters);

//            parameters.setPreviewFpsRange(mVideoParam.getFps() - 1, mVideoParam.getFps());
            mCamera.setPreviewDisplay(mSurfaceHolder);

            int bitsPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.NV21);
            mBuffers = new byte[mVideoParam.getWidth() * mVideoParam.getHeight() * bitsPerPixel/8];
            mRaw = new byte[mVideoParam.getWidth() * mVideoParam.getHeight()*bitsPerPixel/8];

            mCamera.addCallbackBuffer(mBuffers);
            mCamera.setPreviewCallbackWithBuffer(this);
            mCamera.startPreview();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPreviewSize(Camera.Parameters parameters){
        List<Integer> supportedPreviewFormats = parameters.getSupportedPreviewFormats();
        for(Integer integer : supportedPreviewFormats){
            System.out.println("support:"+integer);
        }

        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size size = supportedPreviewSizes.get(0);
        int m = Math.abs(size.width * size.height - mVideoParam.getWidth()*mVideoParam.getHeight());
        supportedPreviewSizes.remove(0);

        Iterator<Camera.Size> iterator = supportedPreviewSizes.iterator();
        while(iterator.hasNext()){
            Camera.Size next = iterator.next();
            System.out.println("support: "+next.width +"x" + next.height);
            int n = Math.abs(next.width*next.height - mVideoParam.getWidth()*mVideoParam.getHeight());
            if(n < m){
                m = n;
                size = next;
            }

        }

        mVideoParam.setWidth(size.width);
        mVideoParam.setHeight(size.height);
        parameters.setPreviewSize(mVideoParam.getWidth(), mVideoParam.getHeight());
        System.out.println("setPreviewSize: "+size.width +"x" + size.height);
    }

    private void setPreviewOrientation(Camera.Parameters parameters){
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mVideoParam.getCameraId(), cameraInfo);
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        mScreen = 0;
        switch (rotation){
            case Surface.ROTATION_0:
                mScreen = SCREEN_PORTRAIT;
                mPushNative.setVideoOptions(mVideoParam.getHeight(), mVideoParam.getWidth(), mVideoParam.getBitrate(), mVideoParam.getFps());
                break;
            case Surface.ROTATION_90:
                mScreen = SCREEN_LANDSCAPE_LEFT;
                mPushNative.setVideoOptions(mVideoParam.getWidth(), mVideoParam.getHeight(), mVideoParam.getBitrate(), mVideoParam.getFps());
                break;
            case Surface.ROTATION_180:
                mScreen = 180;
                break;
            case Surface.ROTATION_270:
                mScreen = SCREEN_LANDSCAPE_RIGHT;
                mPushNative.setVideoOptions(mVideoParam.getWidth(), mVideoParam.getHeight(), mVideoParam.getBitrate(), mVideoParam.getFps());
                break;
        }

        int result;
        if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
            result = (cameraInfo.orientation + mScreen)%360;
            result = (360 - result) % 360; // compensate the mirror
        }else { // back-facing
            result = (cameraInfo.orientation - mScreen + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);
    }

    private void stopPreview(){
        if(null != mCamera){
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

}

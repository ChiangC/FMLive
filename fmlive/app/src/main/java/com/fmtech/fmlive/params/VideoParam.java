package com.fmtech.fmlive.params;

/**
 * ==================================================================
 * Copyright (C) 2018 FMTech Limited All Rights Reserved.
 *
 * Created on 18/7/27 17:03
 *
 * @author Drew.Chiang
 * @version v1.0.0
 *
 * ==================================================================
 */


public class VideoParam {
    private int width;
    private int height;
    private int bitrate = 480000;
    private int fps = 25;
    private int cameraId;

    public VideoParam(int width, int height, int cameraId) {
        this.width = width;
        this.height = height;
        this.cameraId = cameraId;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public int getCameraId() {
        return cameraId;
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }
}

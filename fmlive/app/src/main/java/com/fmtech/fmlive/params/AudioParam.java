package com.fmtech.fmlive.params;

/**
 * ==================================================================
 * Copyright (C) 2018 FMTech Limited All Rights Reserved.
 *
 * Created on 18/7/27 17:01
 *
 * @author Drew.Chiang
 * @version v1.0.0
 *
 * ==================================================================
 */


public class AudioParam {
    private int sampleRateInHz = 44100;

    private int channel = 1;

    public AudioParam(int sampleRateInHz, int channel){
        this.sampleRateInHz = sampleRateInHz;
        this.channel = channel;
    }

    public int getSampleRateInHz() {
        return sampleRateInHz;
    }

    public void setSampleRateInHz(int sampleRateInHz) {
        this.sampleRateInHz = sampleRateInHz;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

}

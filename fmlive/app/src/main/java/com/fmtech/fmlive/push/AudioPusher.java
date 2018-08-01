package com.fmtech.fmlive.push;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.fmtech.fmlive.params.AudioParam;

/**
 * ==================================================================
 * Copyright (C) 2018 FMTech Limited All Rights Reserved.
 *
 * Created on 18/7/27 17:08
 *
 * @author Drew.Chiang
 * @version v1.0.0
 *
 * ==================================================================
 */


public class AudioPusher extends Pusher {
    private AudioParam mAudioParam;
    private AudioRecord mAudioRecord;
    private int mMinBufferSize;
    private PushNative mPushNative;

    public AudioPusher(PushNative pushNative, AudioParam audioParam){
        mPushNative = pushNative;
        mAudioParam = audioParam;

        mPushNative.setAudioOptions(mAudioParam.getSampleRateInHz(), mAudioParam.getChannel());

        int channelConfig = audioParam.getChannel() == 1? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
        mMinBufferSize = AudioRecord.getMinBufferSize(audioParam.getSampleRateInHz(),
                channelConfig, AudioFormat.ENCODING_PCM_16BIT);

        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                audioParam.getSampleRateInHz(),
                channelConfig,
                AudioFormat.ENCODING_PCM_16BIT, mMinBufferSize);
    }

    @Override
    public void startPush() {
        isPushing = true;
        new Thread(new AudioRecordTask()).start();
    }

    @Override
    public void stopPush() {
        mAudioRecord.stop();
        isPushing = false;
    }

    @Override
    public void release() {
        mAudioRecord.release();
    }

    class AudioRecordTask implements Runnable{

        @Override
        public void run() {
            mAudioRecord.startRecording();
            while (isPushing){
                byte[] buffer = new byte[mMinBufferSize];
                int len = mAudioRecord.read(buffer, 0, buffer.length);
                mPushNative.pushAudio(buffer, len);
            }
        }
    }



}

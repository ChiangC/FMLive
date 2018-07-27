package com.fmtech.fmlive.push;

/**
 * ==================================================================
 * Copyright (C) 2018 MTel Limited All Rights Reserved.
 *
 * Created on 18/7/27 17:06
 *
 * @author Drew.Chiang
 * @version v1.0.0
 *
 * ==================================================================
 */


public abstract class Pusher {

    protected boolean isPushing = false;

    public abstract void startPush();

    public abstract void stopPush();

    public abstract void release();


}

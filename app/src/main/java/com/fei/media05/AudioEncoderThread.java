package com.fei.media05;

import android.util.Log;

/**
 * author : huangjf
 * date   : 2021/11/2921:58
 * desc   :
 */
public class AudioEncoderThread extends Thread{

    private static final String TAG = AudioEncoderThread.class.getSimpleName();
    private volatile boolean mIsExit;
    private final Object object = new Object();

    @Override
    public void run() {
        while(!mIsExit) {

        }
        Log.i("")
    }

    /**
     * 退出
     */
    public void exit(){
        mIsExit = true;

    }


}

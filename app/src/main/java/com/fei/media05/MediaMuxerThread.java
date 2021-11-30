package com.fei.media05;

import android.util.Log;

/**
 * author : huangjf
 * date   : 2021/11/2921:57
 * desc   :
 */
public class MediaMuxerThread extends Thread {

    private volatile boolean mIsExit;
    private boolean mIsStart;
    private static MediaMuxerThread mSelf;
    private AudioEncoderThread mAudioThread;
    private VideoEncoderThread mVideoThread;
    private final Object object = new Object();

    /**
     * 开始
     */
    public static void startMuxer() {
        if (mSelf == null) {
            synchronized (MediaMuxerThread.class) {
                if (mSelf == null) {
                    mSelf = new MediaMuxerThread();
                    Log.e("111", "mediaMuxerThread.start();");
                    mSelf.start();
                }
            }
        }
    }

    /**
     * 停止
     */
    public static void stopMuxer() {
        if (mSelf != null) {
            mSelf.exit();
        }
    }

    /**
     * 退出
     */
    private void exit() {
        if (mAudioThread != null) {
            mAudioThread.exit();
        }


    }


    @Override
    public void run() {
        super.run();
    }
}

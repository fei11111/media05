
package com.fei.media05

import android.util.Log
import java.lang.ref.WeakReference

/**
 * author : huangjf
 * date   : 2021/11/2921:58
 * desc   :
 */
class AudioEncoderThread(private val mReference: WeakReference<MediaMuxerThread>) : Thread() {

    private val `object` = java.lang.Object()
    @Volatile
    private var mIsExit = false
    @Volatile
    var mMuxerReady = false
    set(value) {
        synchronized(`object`) {
            Log.i(TAG, "${Thread.currentThread().name} audio -- setMuxerRead + $mMuxerReady")
            mMuxerReady = value
            `object`.notify()
        }
    }


    companion object {
        private val TAG = AudioEncoderThread::class.java.simpleName
    }


    override fun run() {
        while (!mIsExit) {
        }
    }

    /**
     * 退出
     */
    fun exit() {
        mIsExit = true
    }

    /**
     * 重启
     */
    fun restart() {

    }

}
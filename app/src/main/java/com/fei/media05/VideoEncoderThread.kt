package com.fei.media05

import android.util.Log
import java.lang.ref.WeakReference

/**
 * author : huangjf
 * date   : 2021/11/2921:59
 * desc   :
 */
class VideoEncoderThread(
    width: Int,
    height: Int,
    weakReference: WeakReference<MediaMuxerThread>
) : Thread() {
    private val `object` = java.lang.Object()
    @Volatile
    private var mIsExit = false
    @Volatile
    var mMuxerReady: Boolean? = null
        set(value) {
            synchronized(`object`) {
                Log.i(TAG, "${Thread.currentThread().name} video -- setMuxerRead + $mMuxerReady")
                mMuxerReady = value
                `object`.notify()
            }
        }

    companion object {
        private val TAG = VideoEncoderThread.javaClass.simpleName
    }

    override fun run() {
        super.run()
    }

    /**
     * 退出
     */
    fun exit() {}

    /**
     * 重启
     */
    fun restart() {


    }
}
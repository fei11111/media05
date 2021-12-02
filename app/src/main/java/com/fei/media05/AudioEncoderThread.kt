package com.fei.media05

import java.lang.ref.WeakReference

/**
 * author : huangjf
 * date   : 2021/11/2921:58
 * desc   :
 */
class AudioEncoderThread(private val mReference: WeakReference<MediaMuxerThread>) : Thread() {
    @Volatile
    private var mIsExit = false
    private val `object` = Any()
    private fun init() {}
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

    companion object {
        private val TAG = AudioEncoderThread::class.java.simpleName
    }

    init {
        init()
    }
}
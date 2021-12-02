package com.fei.media05

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

    private val mIsExit = false

    override fun run() {
        super.run()
    }

    /**
     * 退出
     */
    fun exit() {}
}
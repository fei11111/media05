package com.fei.media05

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.util.Log
import com.fei.media05.MediaMuxerThread.Companion.TAG
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.util.*

/**
 * author : huangjf
 * date   : 2021/11/2921:57
 * desc   :
 */
class MediaMuxerThread : Thread() {
    @Volatile
    private var mIsExit = false
    @Volatile
    private var mIsStart = false
    private val mAudioThread: AudioEncoderThread?
    private val mVideoThread: VideoEncoderThread?
    private val `object` = java.lang.Object()
    private val mMediaCodecInfo //编码器信息
            : MediaCodecInfo? = null
    private val mMediaCodec: MediaCodec? = null
    private val mBufferInfo //缓冲
            : MediaCodec.BufferInfo
    private var mMuxerDatas: Vector<MuxerData>? = null

    /**
     * 封装数据
     */
    class MuxerData {
        var trackIndex = 0
        var byteBuffer: ByteBuffer? = null
        var bufferInfo: MediaCodec.BufferInfo? = null
    }

    /**
     * 退出
     */
    private fun exit() {
        //录音线程关闭
        mAudioThread?.exit()

        //视频线程关闭
        mVideoThread?.exit()
        mIsExit = true
        synchronized(`object`) { `object`.notify() }
    }

    override fun run() {
        while (!mIsExit) {

        }
    }

    companion object {
        private var mSelf: MediaMuxerThread? = null
        private val TAG = MediaMuxerThread.javaClass.simpleName
        /**
         * 开始
         */
        fun startMuxer() {
            if (mSelf == null) {
                synchronized(MediaMuxerThread::class.java) {
                    if (mSelf == null) {
                        mSelf = com.fei.media05.MediaMuxerThread()
                        Log.e(TAG, "mediaMuxerThread.start();")
                        mSelf!!.start()
                    }
                }
            }
        }

        /**
         * 停止
         */
        fun stopMuxer() {
            if (mSelf != null) {
                mSelf!!.exit()
            }
        }
    }

    init {
        //初始化数据
        mMuxerDatas = Vector() //合并的数据
        mBufferInfo = MediaCodec.BufferInfo() //缓冲
        mAudioThread = AudioEncoderThread(WeakReference<MediaMuxerThread>(this))
        mVideoThread = VideoEncoderThread(1920, 1082, WeakReference<MediaMuxerThread>(this))
    }
}
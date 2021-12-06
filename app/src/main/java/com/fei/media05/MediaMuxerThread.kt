package com.fei.media05

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.util.*

/**
 * author : huangjf
 * date   : 2021/11/2921:57
 * desc   : 1.创建 2.添加addTrack 3.start 4.writeSampleData
 */
class MediaMuxerThread : Thread() {
    @Volatile
    private var mIsExit = false

    @Volatile
    private var mIsStart = false

    @Volatile
    private var mIsAddVideo = false //视频线程准备是否准备好了

    @Volatile
    private var mIsAddAudio = false //音频线程是否准备好了

    private var mVideoTrackId = -1
    private var mAudioTrackId = -1
    private var mAudioThread: AudioEncoderThread? = null
    private var mVideoThread: VideoEncoderThread? = null
    private val `object` = java.lang.Object()
    private val mMediaCodecInfo //编码器信息
            : MediaCodecInfo? = null
    private val mMediaCodec: MediaCodec? = null
    private var mBufferInfo //缓冲
            : MediaCodec.BufferInfo? = null
    private var mMuxerDatas: Vector<MuxerData>? = null  //这个是线程安全
    private var mMediaMuxer: MediaMuxer? = null

    companion object {
        private var mSelf: MediaMuxerThread? = null
        private val TAG = MediaMuxerThread.javaClass.simpleName
        const val TRACK_VIDEO = 0
        const val TRACK_AUDIO = 1

        /**
         * 开始
         */
        fun startMuxer() {
            if (mSelf == null) {
                synchronized(MediaMuxerThread::class.java) {
                    if (mSelf == null) {
                        mSelf = MediaMuxerThread()
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


    /**
     * 添加数据
     */
    fun addMuxerData(data: MuxerData) {
        if (mIsAddAudio && mIsAddVideo) {
            mMuxerDatas?.add(data)
            synchronized(`object`) {
                `object`.notify()
            }
        }
    }

    override fun run() {
        initMuxer()
        while (!mIsExit) {

            //如果为空，说明没有添加数据，等待视频音频
            //如果不为空，判断音视频，是否都添加了
            if (!mMuxerDatas!!.isEmpty() && mIsAddAudio && mIsAddVideo) {
                if (FileUtil.requestSwapFile()) {
                    Log.i(TAG, "重启muxer")
                    restartMuxer()
                } else {
                    Log.i(TAG, "开始混合")
                    var data = mMuxerDatas?.removeAt(0)
                    var trackId = -1;
                    if (data != null) {
                        trackId =
                            if (data.trackIndex == TRACK_VIDEO) mVideoTrackId else mAudioTrackId
                        Log.i(TAG, "写入混合数据：${data.bufferInfo!!.size}}")
                        mMediaMuxer?.writeSampleData(trackId, data.byteBuffer!!, data.bufferInfo!!)
                    }

                }
            } else {
                synchronized(`object`) {
                    Log.i(TAG, "等待数据混合...")
                    `object`.wait()
                }
            }

        }
        Log.i(TAG, "退出muxer")
    }

    /**
     * 重启muxer
     */
    private fun restartMuxer() {
        mIsAddVideo = false
        mVideoTrackId = -1
        mAudioThread?.mMuxerReady = false
        mAudioThread?.restart()
        mIsAddVideo = false
        mAudioTrackId = -1
        mVideoThread?.mMuxerReady = false
        mVideoThread?.restart()

        mMediaMuxer?.stop()
        mMediaMuxer?.release()
        mMediaMuxer = null
        readStart(FileUtil.nextFile!!)
    }

    /**
     * 初始化muxer
     */
    private fun initMuxer() {
        //初始化数据
        mMuxerDatas = Vector() //合并的数据
        mBufferInfo = MediaCodec.BufferInfo() //缓冲
        mAudioThread = AudioEncoderThread(WeakReference<MediaMuxerThread>(this)) //录音线程
        mAudioThread?.name = "audio"
        mVideoThread = VideoEncoderThread(1920, 1082, WeakReference<MediaMuxerThread>(this)) //视频线程
        mVideoThread?.name = "video"

        //开启线程
        mAudioThread?.start()
        mVideoThread?.start()

        //开始时，需要建立文件
        FileUtil.requestSwapFile(true)
        //准备开始
        readStart(FileUtil.nextFile!!)
    }

    /**
     * 添加音视频轨道
     */
    fun addTrackId(index: Int, mediaFormat: MediaFormat) {
        synchronized(MediaMuxerThread::class.java) {
            Log.i(TAG, "当前线程${currentThread().name}")
            if (mIsAddVideo && mIsAddAudio) return
            if ((mIsAddAudio && index == TRACK_AUDIO) || (mIsAddVideo && index == TRACK_VIDEO)) return

            if (index == TRACK_VIDEO) {
                mVideoTrackId = mMediaMuxer!!.addTrack(mediaFormat)
                mIsAddVideo = true
            }

            if (index == TRACK_AUDIO) {
                mAudioTrackId = mMediaMuxer!!.addTrack(mediaFormat)
                mIsAddAudio = true
            }

            synchronized(`object`) {
                if (isMuxerStart()) {
                    mMediaMuxer?.start()
                    Log.i(TAG,"音视频轨道已添加，混合器已开启...")
                    `object`.notify()
                }
            }
        }
    }

    /**
     * 当前音视频合成器是否运行了
     *
     * @return
     */
    fun isMuxerStart(): Boolean {
        return mIsAddVideo && mIsAddAudio
    }

    /**
     * 开始
     */
    private fun readStart(nextFile: String) {
        mIsExit = false
        mIsAddAudio = false
        mIsAddVideo = false

        //清楚数据
        mMuxerDatas?.clear()

        //初始化muxer
        mMediaMuxer = MediaMuxer(nextFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        mVideoThread?.mMuxerReady = true
        mAudioThread?.mMuxerReady = true

        Log.i(TAG, "muxer 保存至 $nextFile")
    }
}
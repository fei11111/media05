package com.fei.media05

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.Camera
import android.hardware.Camera.PreviewCallback
import android.os.Build
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import java.io.IOException

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback, PreviewCallback {
    private var mStartStopBtn: AppCompatButton? = null
    private var mSurfaceView: SurfaceView? = null
    private var mCamera: Camera? = null
    private var mHolder: SurfaceHolder? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //权限申请
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), 100
                )
            }
        }
        mStartStopBtn = findViewById(R.id.start)
        mSurfaceView = findViewById(R.id.surface_view)
        mSurfaceView?.holder?.addCallback(this)
        mStartStopBtn?.setOnClickListener(View.OnClickListener { v: View ->
            if (v.tag.toString().equals("stop", ignoreCase = true)) {
                stopCamera()
                mStartStopBtn?.setText("开始")
                v.tag = "start"
                MediaMuxerThread.stopMuxer()
            } else {
                startCamera()
                mStartStopBtn?.setText("停止")
                v.tag = "stop"
                MediaMuxerThread.startMuxer()
            }
        })
    }

    /**
     * 开启摄像头
     */
    private fun startCamera() {
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
        if (mCamera != null) {
            mCamera!!.setDisplayOrientation(90)
            try {
                mCamera!!.setPreviewDisplay(mHolder)
                val parameters = mCamera!!.getParameters()
                parameters.previewFormat = ImageFormat.NV21
                parameters.setPreviewSize(1920, 1080)
                mCamera!!.setParameters(parameters)
                mCamera!!.setPreviewCallback(this)
                mCamera!!.startPreview()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun stopCamera() {
        if (mCamera != null) {
            mCamera!!.stopPreview()
            mCamera!!.setPreviewCallback(null)
            mCamera = null
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mHolder = holder
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopCamera()
        MediaMuxerThread.Companion.stopMuxer()
    }

    override fun onPreviewFrame(data: ByteArray, camera: Camera) {
        //摄像头回调数据
    }
}
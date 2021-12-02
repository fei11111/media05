package com.fei.media05

import android.os.Environment
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat

/**
 *    author : huangjf
 *    date   : 2021/12/221:39
 *    desc   :
 */
object FileUtil {

    private val TAG by lazy { FileUtil.javaClass.simpleName }
    private const val MAIN_DIR = "/android_records"
    private const val BASE_VIDEO = "/video/"
    private const val BASE_EXT = ".mp4"

    private val simpleFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm")
    private var currentFileName = "-"
    private var nextFile: String? = null

    fun requestSwapFile(): Boolean {
        return requestSwapFile(false)
    }

    fun requestSwapFile(force: Boolean): Boolean {
        var fileName = getFileName()
        var isChange = false
        if (currentFileName != fileName) {
            isChange = true
        }
        if (force || isChange) {
            nextFile = getSaveFile(fileName)
            Log.i(TAG, "文件地址:$fileName")
            return true;
        }
        return false;
    }

    private fun getSaveFile(fileName: String): String? {
        currentFileName = fileName
        val fullPath = StringBuilder()
        fullPath.append(getExternalStorageDirectory())
        fullPath.append(MAIN_DIR)
        fullPath.append(BASE_VIDEO)
        //检查内置卡剩余空间容量,并清理
        checkSpace(fullPath)
        fullPath.append(fileName)
        fullPath.append(BASE_EXT)

        val string = fullPath.toString()
        val file = File(string)
        val parentFile = file.parentFile
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }

        return string
    }

    /**
     * 没空间会清空数据
     */
    private fun checkSpace(fullPath: StringBuilder) {
        val dir = File(fullPath.toString())
        val checkCardSpace = checkCardSpace(dir)
        if (checkCardSpace) {
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val list = dir.list();
            if (list.isEmpty()) return
            list.forEach {
                val file = File(it)
                file.deleteOnExit()
                Log.i(TAG, "删除：${file.absoluteFile}")
                if (!checkCardSpace(dir)) return
            }
        }
    }

    /**
     * 判断空间是否不足，小于总空间0.2
     */
    private fun checkCardSpace(dir: File): Boolean {
        val totalSpace = dir.totalSpace
        val freeSpace = dir.freeSpace
        return totalSpace * 0.2 > freeSpace
    }

    private fun getExternalStorageDirectory(): String {
        return Environment.getExternalStorageDirectory().absolutePath;
    }

    private fun getFileName(): String {
        return simpleFormat.format(System.currentTimeMillis());
    }

}
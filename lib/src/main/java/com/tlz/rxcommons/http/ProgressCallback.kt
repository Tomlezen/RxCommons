package com.tlz.rxcommons.http

/**
 * Created by LeiShao.
 * Data 2017/5/24.
 * Time 15:51.
 * Email t.nainshang@foxmail.com.
 */
interface ProgressCallback {

    fun onProgress(progress: Int)

    fun onFileSize(size: Long)

}
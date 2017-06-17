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

fun <T> T.ProgressCallback(block1: (size: Long)-> Unit, block2: (progress: Int) ->Unit): ProgressCallback{
    return object : ProgressCallback{
        override fun onFileSize(size: Long) {
            block1(size)
        }

        override fun onProgress(progress: Int) {
            block2(progress)
        }
    }
}
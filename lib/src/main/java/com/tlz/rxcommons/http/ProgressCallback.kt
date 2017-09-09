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

fun <T> T.ProgressCallback(totalSize: (size: Long) -> Unit,
    progress: (progress: Int) -> Unit): ProgressCallback {
  return object : ProgressCallback {
    override fun onFileSize(size: Long) {
      totalSize(size)
    }

    override fun onProgress(progress: Int) {
      progress(progress)
    }
  }
}
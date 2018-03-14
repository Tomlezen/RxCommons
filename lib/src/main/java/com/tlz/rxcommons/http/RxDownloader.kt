package com.tlz.rxcommons.http

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.android.MainThreadDisposable
import okhttp3.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Created by LeiShao.
 * Data 2017/5/24.
 * Time 15:37.
 * Email t.nainshang@foxmail.com.
 */
class RxDownloader private constructor(private val httpClient: OkHttpClient) {

  fun download(downloadUrl: String, saveFileDir: String): Flowable<File> =
      download(downloadUrl, saveFileDir, downloadUrl.getFileName(), null)

  fun download(downloadUrl: String, saveFileDir: String, saveFileName: String): Flowable<File> =
      download(downloadUrl, saveFileDir, saveFileName, null)

  fun download(downloadUrl: String, saveFileDir: String, progressCallback: ProgressCallback): Flowable<File> =
      download(downloadUrl, saveFileDir, downloadUrl.getFileName(), progressCallback)

  fun download(downloadUrl: String, saveFileDir: String, saveFileName: String?, progressCallback: ProgressCallback?): Flowable<File> =
      Flowable.create({
        val request = Request.Builder()
            .url(downloadUrl)
            .tag(downloadUrl)
            .addHeader("Accept-Encoding", "identity")
            .build()
        val call = httpClient.newCall(request)
        call.enqueue(ResponseCallback(it, saveFileDir, saveFileName, progressCallback))
        it.setDisposable(object : MainThreadDisposable() {
          override fun onDispose() {
            call?.cancel()
          }
        })
      }, BackpressureStrategy.LATEST)

  private inner class ResponseCallback(
      private val emitter: FlowableEmitter<File>,
      private val saveFileDir: String,
      private val saveFileName: String?,
      private val progressCallback: ProgressCallback?
  ) : Callback {
    override fun onFailure(call: Call?, e: IOException?) {
      e?.let { emitter.onError(it) }
    }

    override fun onResponse(call: Call?, response: Response?) {
      response?.body()?.let { body ->
        try {
          if (emitter.isCancelled) {
            call?.cancel()
          } else {
            val totalSize = body.contentLength()
            body.source()
            body.byteStream()?.use {
              progressCallback?.sendFileSize(totalSize)
              val saveDir = File(saveFileDir).apply {
                if (!exists()) {
                  mkdirs()
                }
              }
              val fileName = "$saveFileName.temp"
              val file = File(saveDir, fileName)
              file.outputStream().use { out ->
                var bytesCopied: Long = 0
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytes = it.read(buffer)
                while (bytes >= 0 && !emitter.isCancelled) {
                  out.write(buffer, 0, bytes)
                  bytesCopied += bytes
                  progressCallback?.sendProgress((bytesCopied * 1.0f / totalSize * 100).toInt())
                  bytes = it.read(buffer)
                }

                if (bytesCopied == totalSize) {
                  val saveFile = File(saveFileDir, saveFileName)
                  if (file.renameTo(saveFile)) {
                    if (!emitter.isCancelled) {
                      emitter.onNext(saveFile)
                      emitter.onComplete()
                    }
                  } else if (!emitter.isCancelled) {
                    emitter.onError(FileNotFoundException())
                  }
                } else if (!emitter.isCancelled) {
                  emitter.onError(FileNotFoundException())
                }
              }
            } ?: emitter.onError(NullPointerException("InputStream is null!"))
          }
        } catch (e: Exception) {
          emitter.onError(e)
        }
      } ?: emitter.onError(NullPointerException("response body is null"))
    }
  }

  companion object {
    fun newInstance(okHttpClient: OkHttpClient) = RxDownloader(okHttpClient)
  }

}

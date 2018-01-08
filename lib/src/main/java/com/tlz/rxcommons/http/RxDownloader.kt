package com.tlz.rxcommons.http

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.MainThreadDisposable
import okhttp3.*
import java.io.*

/**
 * Created by LeiShao.
 * Data 2017/5/24.
 * Time 15:37.
 * Email t.nainshang@foxmail.com.
 */
class RxDownloader(val httpClient: OkHttpClient) {

  fun download(downloadUrl: String, saveFileDir: String): Flowable<File> {
    return download(downloadUrl, saveFileDir, downloadUrl.getFileName(), null)
  }

  fun download(downloadUrl: String, saveFileDir: String, saveFileName: String): Flowable<File> {
    return download(downloadUrl, saveFileDir, saveFileName, null)
  }

  fun download(downloadUrl: String, saveFileDir: String,
      progressCallback: ProgressCallback): Flowable<File> {
    return download(downloadUrl, saveFileDir, downloadUrl.getFileName(), progressCallback)
  }

  fun download(downloadUrl: String, saveFileDir: String, saveFileName: String?,
      progressCallback: ProgressCallback?): Flowable<File> {
    return Flowable.create({ emitter ->
      val request = Request.Builder()
          .url(downloadUrl)
          .tag(downloadUrl)
          .build()
      val call = httpClient.newCall(request)
      call.enqueue(object : Callback {
        override fun onFailure(c: Call, e: IOException) {
          emitter.onError(e)
        }

        @Throws(IOException::class)
        override fun onResponse(c: Call, response: Response) {
          response.body()?.let {
            emitter.setDisposable(object : MainThreadDisposable() {
              override fun onDispose() {
                call.cancel()
              }
            })
            var ins: InputStream? = null
            val buffer = ByteArray(2048)
            var length: Int //已读大小
            var os: FileOutputStream? = null
            var progress: Long = 0 //进度
            val totalSize = it.contentLength()
            try {
              it.source()
              ins = it.byteStream()
              if (emitter.isCancelled) {
                call.cancel()
                emitter.onComplete()
              } else if (ins == null) {
                call.cancel()
                emitter.onError(NullPointerException("InputStream is null!"))
              } else {
                progressCallback?.sendFileSize(totalSize)
                val dir = File(saveFileDir)
                if (!dir.exists()) {
                  dir.mkdirs()
                }
                val fileName = "$saveFileName.temp"
                val file = File(dir, fileName)
                os = file.outputStream()
                length = ins.read(buffer)
                while (length != -1 && !call.isCanceled) {
                  os.write(buffer, 0, length)
                  progress += length.toLong()
                  if (!emitter.isCancelled) {
                    progressCallback?.sendProgress((progress * 1.0f / totalSize * 100).toInt())
                  } else {
                    call.cancel()
                    emitter.onComplete()
                    break
                  }
                  length = ins.read(buffer)
                }
                os.flush()

                if (progress == totalSize) {
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
            } catch (e: Exception) {
              emitter.onError(e)
            } finally {
              try {
                ins?.close()
              } catch (e: IOException) {
                e.printStackTrace()
              }

              try {
                os?.close()
              } catch (e: IOException) {
                e.printStackTrace()
              }
            }
          } ?: emitter.onError(NullPointerException("response body is null"))
        }
      })
    }, BackpressureStrategy.LATEST)
  }
}

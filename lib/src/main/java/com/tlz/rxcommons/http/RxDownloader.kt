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

    fun download(downloadUrl: String, saveFileDir: String, savaFileName: String): Flowable<File> {
        return download(downloadUrl, saveFileDir, savaFileName, null)
    }

    fun download(downloadUrl: String, saveFileDir: String, progressCallback: ProgressCallback): Flowable<File> {
        return download(downloadUrl, saveFileDir, downloadUrl.getFileName(), progressCallback)
    }

    fun download(downloadUrl: String, saveFileDir: String, savaFileName: String?, progressCallback: ProgressCallback?): Flowable<File> {
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
                    if(response.body() == null){
                        emitter.onError(NullPointerException("response body is null"))
                        return
                    }
                    var ins: InputStream? = null
                    val buffer = ByteArray(2048)//设置缓冲区大小
                    var length: Int //已读大小
                    var os: FileOutputStream? = null
                    var progress: Long = 0 //进度
                    val fileSize = response.body()!!.contentLength()
                    emitter.setDisposable(object : MainThreadDisposable() {
                        override fun onDispose() {
                            call.cancel()
                        }
                    })
                    try {
                        response.body()?.source()
                        ins = response.body()?.byteStream()
                        if (emitter.isCancelled) {
                            call.cancel()
                            emitter.onComplete()
                        } else if (ins == null) {
                            call.cancel()
                            emitter.onError(NullPointerException("InputStream is null!"))
                        } else {
                            progressCallback?.sendFileSize(fileSize)
                            val dir = File(saveFileDir)
                            if (!dir.exists()) {
                                dir.mkdirs()
                            }
                            val fileName = "$savaFileName.temp"
                            val file = File(dir, fileName)
                            os = file.outputStream()
                            length = ins.read(buffer)
                            while (length != -1 && !call.isCanceled) {
                                os.write(buffer, 0, length)
                                progress += length.toLong()
                                if (!emitter.isCancelled) {
                                    progressCallback?.sendProgress((progress * 1.0f / fileSize * 100).toInt())
                                } else {
                                    call.cancel()
                                    emitter.onComplete()
                                    break
                                }
                                length = ins.read(buffer)
                            }
                            os.flush()

                            if (progress == fileSize) {
                                val saveFile = File(saveFileDir, savaFileName)
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
                }
            })
        }, BackpressureStrategy.LATEST)
    }
}

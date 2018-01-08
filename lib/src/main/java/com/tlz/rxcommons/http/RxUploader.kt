package com.tlz.rxcommons.http

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.MainThreadDisposable
import okhttp3.*
import java.io.File
import java.io.IOException

/**
 * Created by LeiShao.
 * Data 2017/5/24.
 * Time 15:37.
 * Email t.nainshang@foxmail.com.
 */
class RxUploader constructor(private val httpClient: OkHttpClient) {

  fun upload(serviceUrl: String, fileKey: String, fileValue: String): Flowable<String> =
      upload(serviceUrl, arrayOf(Param(fileKey, fileValue)), null, null)

  fun upload(serviceUrl: String, fileKey: String, fileValue: String,
      params: Array<Param>?): Flowable<String> =
      upload(serviceUrl, arrayOf(Param(fileKey, fileValue)), params, null)

  fun upload(serviceUrl: String, fileKey: String, fileValue: String, params: Array<Param>,
      progressCallback: ProgressCallback): Flowable<String> =
      upload(serviceUrl, arrayOf(Param(fileKey, fileValue)), params, progressCallback)

  fun upload(serviceUrl: String, files: Array<Param>, params: Array<Param>?,
      progressCallback: ProgressCallback?): Flowable<String> {
    return Flowable.create<String>({ subscriber ->
      val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
      val headersBuilder = Headers.Builder()
      params?.forEach {
        headersBuilder.add(it.key, it.value)
      }
      val headers = headersBuilder.build()
      if(headers.size() > 0){
        builder.addPart(headers, RequestBody.create(null, ""))
      }
      files.filter { File(it.value).exists() }.forEach {
        val file = File(it.value)
        val fileName = file.name
        builder.addFormDataPart(it.key, fileName, RequestBody.create(MediaType.parse(fileName.guessMimeType()), file))
      }
      try {
        val requestBody = builder.build()
        val request = Request.Builder()
            .url(serviceUrl)
            .post(if(progressCallback == null) requestBody else ProgressRequestBody(requestBody, progressCallback))
            .tag(serviceUrl)
            .build()
        val call = httpClient.newCall(request)
        call.enqueue(object : Callback {
          override fun onFailure(call: Call, e: IOException) {
            if (!subscriber.isCancelled) {
              subscriber.onError(e)
            }
          }

          @Throws(IOException::class)
          override fun onResponse(call: Call, response: Response) {
            try {
              if (!subscriber.isCancelled) {
                if (response.isSuccessful) {
                  val content = response.body()!!.string()
                  subscriber.onNext(content)
                  subscriber.onComplete()
                } else {
                  subscriber.onError(Exception("code = ${response.code()}"))
                }
              }
            } catch (e: Exception) {
              if (!subscriber.isCancelled) {
                subscriber.onError(e)
              }
            }

          }
        })
        subscriber.setDisposable(object : MainThreadDisposable() {
          override fun onDispose() {
            if (!call.isCanceled) {
              call.cancel()
            }
          }
        })
      } catch (e: Exception) {
        e.printStackTrace()
        if (!subscriber.isCancelled) {
          subscriber.onError(e)
        }
      }
    }, BackpressureStrategy.BUFFER)
  }

}
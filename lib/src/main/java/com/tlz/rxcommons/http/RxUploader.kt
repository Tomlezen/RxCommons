package com.tlz.rxcommons.http

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import okhttp3.*
import java.io.File
import java.io.IOException

/**
 * Created by LeiShao.
 * Data 2017/5/24.
 * Time 15:37.
 * Email t.nainshang@foxmail.com.
 */
class RxUploader private constructor(private val httpClient: OkHttpClient) {

  fun upload(serviceUrl: String, fileKey: String, fileValue: String) =
      upload(serviceUrl, arrayOf(Param(fileKey, fileValue)), null, null)

  fun upload(serviceUrl: String, fileKey: String, fileValue: String, params: Array<Param>) =
      upload(serviceUrl, arrayOf(Param(fileKey, fileValue)), params, null)

  fun upload(serviceUrl: String, fileKey: String, fileValue: String, params: Array<Param>, progressCallback: ProgressCallback) =
      upload(serviceUrl, arrayOf(Param(fileKey, fileValue)), params, progressCallback)

  fun upload(serviceUrl: String, files: Array<Param>, params: Array<Param>?, progressCallback: ProgressCallback?) =
     Flowable.create<String>({
      val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
      val headersBuilder = Headers.Builder()
      params?.forEach {
        headersBuilder.add(it.key, it.value)
      }
      val headers = headersBuilder.build()
      if(headers.size() > 0){
        builder.addPart(headers, RequestBody.create(null, ""))
      }
      files.filter { File(it.value).exists() }
          .forEach {
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
        call.enqueue(ResponseCallback(it))
      }catch (e: Exception){
        it.onError(e)
      }
    }, BackpressureStrategy.BUFFER)

  private inner class ResponseCallback(private val emitter: FlowableEmitter<String>): Callback{
    override fun onFailure(call: Call?, e: IOException?) {
      e?.apply { emitter.onError(this) }
    }

    override fun onResponse(call: Call?, response: Response?) {
      try {
        if (!emitter.isCancelled) {
          if (response?.isSuccessful == true) {
            val content = response.body()?.string() ?: throw NullPointerException("response body is null")
            emitter.onNext(content)
            emitter.onComplete()
          } else {
            emitter.onError(Exception("code = ${response?.code() ?: "null"}"))
          }
        }
      } catch (e: Exception) {
        emitter.onError(e)
      }
    }
  }

  companion object {
    fun newInstance(okHttpClient: OkHttpClient) = RxUploader(okHttpClient)
  }

}
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
class RxUploader constructor(val httpClient: OkHttpClient) {

    fun upload(serviceUrl: String, fileKey: String, fileValue: String): Flowable<String> {
        return upload(serviceUrl, arrayOf(Param(fileKey, fileValue)), null, null)
    }

    fun upload(serviceUrl: String, fileKey: String, fileValue: String, params: Array<Param>?): Flowable<String> {
        return upload(serviceUrl, arrayOf(Param(fileKey, fileValue)), params, null)
    }

    fun upload(serviceUrl: String, fileKey: String, fileValue: String, params: Array<Param>, progressCallback: ProgressCallback): Flowable<String> {
        return upload(serviceUrl, arrayOf(Param(fileKey, fileValue)), params, progressCallback)
    }

    fun upload(serviceUrl: String, files: Array<Param>, params: Array<Param>?, progressCallback: ProgressCallback?): Flowable<String> {
        return Flowable.create<String>({ subscriber ->
            val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
            if (params != null) {
                Flowable.fromArray(*params)
                        .subscribe { (key, value) ->
                            builder.addPart(
                                    Headers.of("Content-Disposition", "form-data; name=\"" + key + "\""),
                                    RequestBody.create(null, value))
                        }
            }
            Flowable.fromArray(*files)
                    .subscribe { (key, value) ->
                        val file = File(value)
                        if (file.exists()) {
                            val fileBody: RequestBody?
                            val fileName = file.name
                            fileBody = RequestBody.create(MediaType.parse(fileName.guessMimeType()), file)
                            builder.addPart(Headers.of("Content-Disposition",
                                    "form-data; name=\"$key\"; filename=\"$fileName\""),
                                    fileBody!!)
                        }
                    }
            try {
                val requestBody = builder.build()
                val request = Request.Builder()
                        .url(serviceUrl)
                        .post(ProgressRequestBody(requestBody, progressCallback))
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
                            val content = response.body()!!.string()
                            if (!subscriber.isCancelled) {
                                subscriber.onNext(content)
                                subscriber.onComplete()
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
        }, BackpressureStrategy.LATEST)
    }

}
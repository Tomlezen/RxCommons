package com.tlz.rxcommons.http

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import java.io.IOException

/**
 * Created by LeiShao.
 * Data 2017/5/24.
 * Time 17:20.
 * Email t.nainshang@foxmail.com.
 */
internal class ProgressRequestBody(val requestBody: RequestBody, val progressCallback: ProgressCallback?): RequestBody(){

    private var bufferedSink: BufferedSink? = null


    override fun contentType(): MediaType? {
        return requestBody.contentType()
    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        try {
            return requestBody.contentLength()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        try {
            if (bufferedSink == null) {
                //包装
                bufferedSink = Okio.buffer(sink(sink))
            }
            //写入
            requestBody.writeTo(bufferedSink!!)
            //必须调用flush，否则最后一部分数据可能不会被写入
            bufferedSink?.flush()
        } catch (e: Exception) {

        }

    }

    private fun sink(sink: Sink): Sink {
        return object : ForwardingSink(sink) {
            //当前写入字节大小
            internal var bytesWritten = 0L
            //总字节长度，避免多次调用contentLength()方法
            internal var contentLength = 0L

            @Throws(IOException::class)
            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                if (contentLength == 0L) {
                    contentLength = contentLength()
                    progressCallback?.sendFileSize(contentLength)
                }
                //增加当前写入的字节数
                bytesWritten += byteCount
                progressCallback?.sendPogress((bytesWritten * 1.0f / contentLength * 100).toInt())
            }
        }
    }

}
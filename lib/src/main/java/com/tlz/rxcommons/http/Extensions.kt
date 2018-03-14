package com.tlz.rxcommons.http

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.net.URLConnection

/**
 * Created by LeiShao.
 * Data 2017/5/24.
 * Time 16:26.
 * Email t.nainshang@foxmail.com.
 */

internal fun String.getFileName(): String {
  val separatorIndex = lastIndexOf("/")
  var name = if (separatorIndex < 0) this else substring(separatorIndex + 1, length)
  name = name.replace("/", "_").replace("?", "_").replace("-", "_")
  if (name.length > 30) {
    name = name.substring(0, 30)
  }
  return "$name.temp"
}

internal fun String.guessMimeType(): String {
  val fileNameMap = URLConnection.getFileNameMap()
  var contentTypeFor: String? = fileNameMap.getContentTypeFor(this)
  if (contentTypeFor == null) {
    contentTypeFor = "application/octet-stream"
  }
  return contentTypeFor
}

internal fun ProgressCallback.sendProgress(progress: Int) =
  Observable.just(progress)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { onProgress(progress) }


internal fun ProgressCallback.sendFileSize(size: Long) =
  Observable.just(size)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { onFileSize(size) }

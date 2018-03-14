package com.tlz.rxcommons

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Created by tomlezen.
 * Date: 2017/5/20.
 * Time: 下午4:13
 */
private val subjectMapper = ConcurrentHashMap<Any, ArrayList<PublishSubject<Any>>>()

fun <T> onEvent(observable: Observable<T>, onNext: (T) -> Unit, onError: (Throwable) -> Unit): Disposable =
    observable.observeOn(AndroidSchedulers.mainThread()).subscribe(onNext, onError)

//fun <T> T.onEvent(observable: Observable<T>, onNext: (T) -> Unit): Disposable =
//    onEvent(observable, onNext, { it.printStackTrace() })

fun <T> onEvent(tag: Any, onNext: (T) -> Unit): Disposable =
    onEvent(register(tag), onNext, { it.printStackTrace() })

fun <T> onEvent(tag: Any, onNext: (T) -> Unit, onError: (Throwable) -> Unit): Disposable =
    onEvent(register(tag), onNext, onError)

fun <T> register(tag: Any): Observable<T> {
  var subjectList = subjectMapper[tag]
  if (null == subjectList) {
    subjectList = ArrayList()
    subjectMapper[tag] = subjectList
  }
  val subject = PublishSubject.create<T>()
  subjectList.add(subject as PublishSubject<Any>)
  return subject
}

fun unregister(tag: Any) {
  if (subjectMapper.containsKey(tag)) {
    subjectMapper.remove(tag)
  }
}

fun unregister(tag: Any, observable: Observable<*>) =
    subjectMapper[tag]?.let {
      it.remove(observable)
      if (it.isEmpty()) {
        subjectMapper.remove(tag)
      }
    }


fun post(content: Any) {
  post(content::class, content)
}

fun post(tag: Any, content: Any) {
  postDelay(tag, content, 0L)
}

fun postDelay(content: Any, millis: Long): Disposable? =
    postDelay(content, millis, MILLISECONDS)

fun postDelay(content: Any, delay: Long, unit: TimeUnit): Disposable? =
    postDelay(content::class, content, delay, unit)

fun postDelay(tag: Any, content: Any, millis: Long): Disposable? =
    postDelay(tag, content, millis, MILLISECONDS)

fun postDelay(tag: Any, content: Any, delay: Long, unit: TimeUnit): Disposable? =
    if (delay == 0L) {
      subjectMapper[tag]?.filter { it.hasObservers() }?.forEach { it.onNext(content) }
      null
    } else {
      delay(delay, unit) {
        subjectMapper[tag]?.filter { it.hasObservers() }?.forEach { it.onNext(content) }
      }
    }

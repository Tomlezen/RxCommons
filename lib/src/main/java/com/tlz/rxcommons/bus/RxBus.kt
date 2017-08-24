package com.tlz.rxcommons.bus

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
object RxBus: RxBusI{

    override fun <T> onEvent(observable: Observable<T>, onNext: (T) -> Unit, onError: (Throwable) -> Unit): Disposable {
        return observable.observeOn(AndroidSchedulers.mainThread()).subscribe(onNext, onError)
    }

    override fun <T> onEvent(observable: Observable<T>, onNext: (T) -> Unit): Disposable {
        return onEvent(observable, onNext, { it.printStackTrace() } )
    }

    override fun <T> onEvent(tag: Any, onNext: (T) -> Unit): Disposable {
        return onEvent(register(tag), onNext)
    }

    override fun <T> onEvent(tag: Any, onNext: (T) -> Unit, onError: (Throwable) -> Unit): Disposable {
        return onEvent(register(tag), onNext, onError)
    }

    override fun <T> register(tag: Any): Observable<T> {
        var subjectList = subjectMapper[tag]
        if (null == subjectList) {
            subjectList = ArrayList<PublishSubject<Any>>()
            subjectMapper.put(tag, subjectList)
        }
        val subject = PublishSubject.create<T>()
        subjectList.add(subject as PublishSubject<Any>)
        return subject
    }

    override fun unregister(tag: Any) {
        val subjects = subjectMapper[tag]
        if (null != subjects) {
            subjectMapper.remove(tag)
        }
    }

    override fun unregister(tag: Any, observable: Observable<*>) {
        val subjectList = subjectMapper[tag]
        if (null != subjectList) {
            subjectList.remove(observable)
            if (subjectList.isEmpty()) {
                subjectMapper.remove(tag)
            }
        }
    }

    override fun post(content: Any) {
        post(content.javaClass, content)
    }

    override fun post(tag: Any, content: Any) {
        postDelay(tag, content, 0L)
    }

    override fun postDelay(content: Any, millis: Long) {
        post(content.javaClass, content)
    }

    override fun postDelay(content: Any, delay: Long, unit: TimeUnit) {
        postDelay(content.javaClass, content, delay, unit)
    }

    override fun postDelay(tag: Any, content: Any, millis: Long) {
        postDelay(tag, content, millis, MILLISECONDS)
    }

    override fun postDelay(tag: Any, content: Any, delay: Long, unit: TimeUnit) {
        if(delay == 0L){
            val subjectsList = subjectMapper[tag]
            if (subjectsList?.isNotEmpty() ?: false) {
                subjectsList?.filter { it.hasObservers() }
                    ?.forEach { it.onNext(content) }
            }
        }else{
            com.tlz.rxcommons.delay(delay, unit){
                val subjectsList = subjectMapper[tag]
                if (subjectsList?.isNotEmpty() ?: false) {
                    subjectsList?.filter { it.hasObservers() }?.forEach { it.onNext(content) }
                }
            }
        }

    }

    private val subjectMapper = ConcurrentHashMap<Any, ArrayList<PublishSubject<Any>>>()

}
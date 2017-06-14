package com.tlz.rxcommons.bus

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by tomlezen.
 * Date: 2017/5/20.
 * Time: 下午4:13
 */
object RxBus: RxBusI{

    override fun <T> onEvent(observable: Observable<T>, onNext: Consumer<T>, onError: Consumer<Throwable>): Disposable {
        return observable.observeOn(AndroidSchedulers.mainThread()).subscribe(onNext, onError)
    }

    override fun <T> onEvent(observable: Observable<T>, onNext: Consumer<T>): Disposable {
        return onEvent(observable, onNext, Consumer { it.printStackTrace() })
    }

    override fun <T> onEvent(tag: Any, onNext: Consumer<T>): Disposable {
        return onEvent(register(tag), onNext)
    }

    override fun <T> onEvent(tag: Any, onNext: Consumer<T>, onError: Consumer<Throwable>): Disposable {
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
        val subjectsList = subjectMapper[tag]
        if (subjectsList?.isNotEmpty() ?: false) {
            subjectsList?.filter { it.hasObservers() }
                    ?.forEach { it.onNext(content) }
        }
    }

    private val subjectMapper = ConcurrentHashMap<Any, ArrayList<PublishSubject<Any>>>()

}
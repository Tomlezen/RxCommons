package com.tlz.rxcommons.bus

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by tomlezen.
 * Date: 2017/5/20.
 * Time: 下午4:13
 */
object RxBus: RxBusI{

    override fun <T> onEvent(observable: Observable<T>, onNext: Consumer<T>, onError: Consumer<Throwable>) {
        observable.observeOn(AndroidSchedulers.mainThread()).subscribe(onNext, onError)
    }

    override fun <T> onEvent(observable: Observable<T>, onNext: Consumer<T>) {
        onEvent(observable, onNext, Consumer { it.printStackTrace() })
    }

    override fun <T> onEvent(tag: Any, onNext: Consumer<T>) {
        onEvent(register(tag), onNext)
    }

    override fun <T> onEvent(tag: Any, onNext: Consumer<T>, onError: Consumer<Throwable>) {
        onEvent(register(tag), onNext, onError)
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
            if (isEmpty(subjectList)) {
                subjectMapper.remove(tag)
            }
        }
    }

    override fun post(content: Any) {
        post(content.javaClass, content)
    }

    override fun post(tag: Any, content: Any) {
        val subjectsList = subjectMapper[tag]
        if (!isEmpty(subjectsList)) {
            for (subject in subjectsList!!) {
                subject.onNext(content)
            }
        }
    }

    private fun isEmpty(collection: Collection<PublishSubject<*>>?): Boolean {
        return null == collection || collection.isEmpty()
    }

    private val subjectMapper = ConcurrentHashMap<Any, ArrayList<PublishSubject<Any>>>()

}
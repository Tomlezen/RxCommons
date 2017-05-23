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

    override fun <T> onEvent(observable: Observable<T>, consumer: Consumer<T>) {
        observable.observeOn(AndroidSchedulers.mainThread()).subscribe(consumer, Consumer { it.printStackTrace() })
    }

    private val subjectMap = ConcurrentHashMap<Any, MutableList<PublishSubject<Any>>>()

}
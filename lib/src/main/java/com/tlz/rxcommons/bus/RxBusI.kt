package com.tlz.rxcommons.bus

import io.reactivex.Observable
import io.reactivex.functions.Consumer

/**
 * Created by tomlezen.
 * Date: 2017/5/22.
 * Time: 下午10:17
 */
internal interface RxBusI {

    fun <T> onEvent(observable: Observable<T>, onNext: Consumer<T>)

    fun <T> onEvent(observable: Observable<T>, onNext: Consumer<T>, onError: Consumer<Throwable>)

    fun <T> onEvent(tag: Any, onNext: Consumer<T>)

    fun <T> onEvent(tag: Any, onNext: Consumer<T>, onError: Consumer<Throwable>)

    fun <T> register(tag: Any): Observable<T>

    fun unregister(tag: Any)

    fun unregister(tag: Any, observable: Observable<*>)

    fun post(content: Any)

    fun post(tag: Any, content: Any)

}
package com.tlz.rxcommons.bus

import io.reactivex.Observable
import io.reactivex.functions.Consumer

/**
 * Created by tomlezen.
 * Date: 2017/5/22.
 * Time: 下午10:17
 */
internal interface RxBusI {

    fun <T> onEvent(observable: Observable<T>, consumer: Consumer<T>)

}
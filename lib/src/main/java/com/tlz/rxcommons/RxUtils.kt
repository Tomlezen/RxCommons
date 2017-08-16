package com.tlz.rxcommons

import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.ObservableTransformer
import io.reactivex.SingleTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Created by tomlezen.
 * Date: 2017/5/20.
 * Time: 上午10:31
 */
object RxUtils {

    fun disposeIfNotNull(disposable: Disposable?): Boolean {
        if (disposable != null && !disposable.isDisposed) {
            disposable.dispose()
            return true
        } else
            return false
    }

    fun runOnUi(): Flowable<Boolean> {
        return runOnUi(true)
    }

    fun <T> runOnUi(value: T): Flowable<T> {
        return Flowable.just(value!!).observeOn(AndroidSchedulers.mainThread())
    }

    fun runOnThread(): Flowable<Boolean> {
        return runOnThread(true)
    }

    fun <T> runOnThread(value: T): Flowable<T> {
        return Flowable.just(value).subscribeOn(Schedulers.newThread())
    }

    fun <T> applyMainThread(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream -> upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()) }
    }

    fun <T> applyMainThreadForFlowable(): FlowableTransformer<T, T> {
        return FlowableTransformer { upstream -> upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()) }
    }

    fun <T> applyMainThreadForSingle(): SingleTransformer<T, T> {
        return SingleTransformer { upstream -> upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()) }
    }

    fun countdown(time: Int, unit: TimeUnit): Flowable<Int> {
        val countTime = if (time < 0) 0 else time
        return Flowable.interval(0, 1, unit)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map { increaseTime -> countTime - increaseTime.toInt() }
                .take((countTime + 1).toLong())
    }

}

fun delay(millisDelayTime: Long, block: ()->Unit){
    delay(millisDelayTime, TimeUnit.MILLISECONDS, block)
}

fun delay(delayTime: Long, timeUnit: TimeUnit, block: ()->Unit){
    Flowable.timer(delayTime, timeUnit)
            .subscribe { block() }
}

fun delayOnMainThread(millisDelayTime: Long, block: ()->Unit){
    delayOnMainThread(millisDelayTime, TimeUnit.MILLISECONDS, block)
}

fun delayOnMainThread(delayTime: Long, timeUnit: TimeUnit, block: ()->Unit){
    Flowable.timer(delayTime, timeUnit)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { block() }
}

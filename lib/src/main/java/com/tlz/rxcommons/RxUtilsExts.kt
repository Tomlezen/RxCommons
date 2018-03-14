package com.tlz.rxcommons

import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.ObservableTransformer
import io.reactivex.SingleTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit

/**
 * Created by tomlezen.
 * Date: 2017/5/20.
 * Time: 上午10:31
 */
fun <T> applyMainThreadForObservable(): ObservableTransformer<T, T> {
  return ObservableTransformer {
    it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
  }
}

fun <T> applyMainThreadForFlowable(): FlowableTransformer<T, T> {
  return FlowableTransformer {
    it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
  }
}

fun <T> applyMainThreadForSingle(): SingleTransformer<T, T> {
  return SingleTransformer {
    it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
  }
}

fun countdown(time: Int, unit: TimeUnit): Flowable<Int> {
  val countTime = if (time < 0) 0 else time
  return Flowable.interval(0, 1, unit)
      .map { increaseTime -> countTime - increaseTime.toInt() }
      .take((countTime + 1).toLong())
      .observeOn(AndroidSchedulers.mainThread())
}

fun delay(millisDelayTime: Long, block: () -> Unit): Disposable =
    delay(millisDelayTime, TimeUnit.MILLISECONDS, block)

fun delay(delayTime: Long, timeUnit: TimeUnit, block: () -> Unit): Disposable =
   Flowable.timer(delayTime, timeUnit)
      .subscribe { block() }

fun delayOnMainThread(millisDelayTime: Long, block: () -> Unit): Disposable =
    delayOnMainThread(millisDelayTime, TimeUnit.MILLISECONDS, block)

fun delayOnMainThread(delayTime: Long, timeUnit: TimeUnit, block: () -> Unit): Disposable =
   Flowable.timer(delayTime, timeUnit)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { block() }

fun <T> async(start: CoroutineStart = CoroutineStart.DEFAULT, block: suspend CoroutineScope.() -> T) =
    kotlinx.coroutines.experimental.async(CommonPool, start, block)

fun ui(start: CoroutineStart = CoroutineStart.DEFAULT, block: suspend CoroutineScope.() -> Unit) =
    launch(UI, start, block)


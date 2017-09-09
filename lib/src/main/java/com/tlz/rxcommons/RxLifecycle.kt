package com.tlz.rxcommons

import com.tlz.rxcommons.bus.RxBus
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject

/**
 * Created by LeiShao.
 * Data 2017/5/24.
 * Time 15:03.
 * Email t.nainshang@foxmail.com.
 */
class RxLifecycle {

  private enum class Event {
    CREATE, ATTACH, CREATE_VIEW, RESTART, START, RESUME,
    PAUSE, STOP, DESTROY_VIEW, DETACH, DESTROY
  }

  private val lifecycleBehavior = BehaviorSubject.create<Event>()

  fun onCreate() {
    lifecycleBehavior.onNext(Event.CREATE)
  }

  fun onAttach() {
    lifecycleBehavior.onNext(Event.ATTACH)
  }

  fun onCreateView() {
    lifecycleBehavior.onNext(Event.CREATE_VIEW)
  }

  fun onRestart() {
    lifecycleBehavior.onNext(Event.RESTART)
  }

  fun onStart() {
    lifecycleBehavior.onNext(Event.START)
  }

  fun onResume() {
    lifecycleBehavior.onNext(Event.RESUME)
  }

  fun onPause() {
    lifecycleBehavior.onNext(Event.PAUSE)
  }

  fun onStop() {
    lifecycleBehavior.onNext(Event.STOP)
  }

  fun onDestroyView() {
    lifecycleBehavior.onNext(Event.DESTROY_VIEW)
  }

  fun onDetach() {
    lifecycleBehavior.onNext(Event.DETACH)
  }

  fun onDestroy() {
    lifecycleBehavior.onNext(Event.DESTROY)
  }

  class CheckUIDestroiedTransformer<T>(
      private val lifecycle: RxLifecycle) : ObservableTransformer<T, T> {

    override fun apply(upstream: Observable<T>): ObservableSource<T> {
      return upstream.takeUntil(lifecycle.lifecycleBehavior
          .skipWhile { event -> event != RxLifecycle.Event.DESTROY_VIEW && event != RxLifecycle.Event.DESTROY && event != RxLifecycle.Event.DETACH })
          .observeOn(AndroidSchedulers.mainThread())
    }
  }

  class CheckUIDestroiedTransformerForRxBus<T>(private val lifecycle: RxLifecycle,
      private val tag: Any, private val all: Boolean) : ObservableTransformer<T, T> {

    override fun apply(upstream: Observable<T>): ObservableSource<T> {
      return upstream.takeUntil(lifecycle.lifecycleBehavior.skipWhile { event ->
        val ok = event != RxLifecycle.Event.DESTROY_VIEW && event != RxLifecycle.Event.DESTROY && event != RxLifecycle.Event.DETACH
        if (!ok) {
          if (all)
            RxBus.unregister(tag)
          else
            RxBus.unregister(tag, upstream)
        }
        ok
      }).observeOn(AndroidSchedulers.mainThread())
    }
  }

}
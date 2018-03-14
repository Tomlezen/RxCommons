package com.tlz.rxcommons

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.reactivex.Observable
import io.reactivex.Observable.create
import io.reactivex.android.MainThreadDisposable

/**
 * Created by LeiShao.
 * Data 2017/5/24.
 * Time 17:37.
 * Email t.nainshang@foxmail.com.
 */
fun Context.observeBroadcast(vararg filters: String): Observable<Intent> {
  return observeBroadcast(IntentFilter().apply { filters.forEach { addAction(it) } })
}

fun Context.observeBroadcast(filters: IntentFilter): Observable<Intent> {
  return create({
    run {
      val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
          it.onNext(intent)
        }
      }
      it.setDisposable(object : MainThreadDisposable() {
        override fun onDispose() {
          this@observeBroadcast.unregisterReceiver(receiver)
        }
      })
      this@observeBroadcast.registerReceiver(receiver, filters)
    }
  })
}
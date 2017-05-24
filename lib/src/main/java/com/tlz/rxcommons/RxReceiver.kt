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
object RxReceiver {

    fun  observeBroadcast(context: Context, filter: IntentFilter): Observable<Intent> {
        return create({ emitter ->
            run {
                val receiver = object: BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        emitter.onNext(intent)
                    }
                }
                emitter.setDisposable(object :MainThreadDisposable(){
                    override fun onDispose() {
                        context.unregisterReceiver(receiver)
                    }
                })
                context.registerReceiver(receiver, filter)
            }
        })
    }

}
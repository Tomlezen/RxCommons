package com.tlz.rxcommons

import android.annotation.SuppressLint
import android.content.SharedPreferences
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableOnSubscribe
import io.reactivex.android.MainThreadDisposable

/**
 * Created by LeiShao.
 * Data 2017/5/24.
 * Time 15:14.
 * Email t.nainshang@foxmail.com.
 */
class RxPreferences(val sharedPreferences: SharedPreferences) {

  private val preferenceFlowable: Flowable<String>

  init {
    preferenceFlowable = Flowable.create(FlowableOnSubscribe<String> { e ->
      val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key -> e.onNext(key) }

      sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

      e.setDisposable(object : MainThreadDisposable() {
        override fun onDispose() {
          sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
      })
    }, BackpressureStrategy.LATEST).share()
  }

  fun <T> get(key: String, defValue: T): T = with(sharedPreferences) {
    val res: Any = when (defValue) {
      is Int -> getInt(key, defValue)
      is Float -> getFloat(key, defValue)
      is Long -> getLong(key, defValue)
      is String -> getString(key, defValue)
      is Boolean -> getBoolean(key, defValue)
      else -> throw  IllegalArgumentException("This type cant be saved")
    }
    res as T
  }

  @SuppressLint("CommitPrefEdits")
  fun <T> put(key: String, value: T) = with(sharedPreferences.edit()) {
    when (value) {
      is Int -> putInt(key, value)
      is Float -> putFloat(key, value)
      is Long -> putLong(key, value)
      is String -> putString(key, value)
      is Boolean -> putBoolean(key, value)
      else -> throw  IllegalArgumentException("This type can be saved")
    }.apply()
  }

  private fun observe(key: String, emitOnStart: Boolean): Flowable<String> {
    return preferenceFlowable
        .startWith(Flowable.just(key, key)
            .filter { emitOnStart })
        .filter { s -> key == s }
  }

  fun observeBoolean(key: String, defValue: Boolean, emitOnStart: Boolean): Flowable<Boolean> {
    return observe(key, emitOnStart)
        .map { sharedPreferences.getBoolean(key, defValue) }
  }

  fun observeString(key: String, defValue: String, emitOnStart: Boolean): Flowable<String> {
    return observe(key, emitOnStart)
        .map { sharedPreferences.getString(key, defValue) }
  }

  fun observeInt(key: String, defValue: Int, emitOnStart: Boolean): Flowable<Int> {
    return observe(key, emitOnStart)
        .map { sharedPreferences.getInt(key, defValue) }
  }

  fun observeLong(key: String, defValue: Long, emitOnStart: Boolean): Flowable<Long> {
    return observe(key, emitOnStart)
        .map { sharedPreferences.getLong(key, defValue) }
  }

  fun observeFloat(key: String, defValue: Float, emitOnStart: Boolean): Flowable<Float> {
    return observe(key, emitOnStart)
        .map { sharedPreferences.getFloat(key, defValue) }
  }

  fun observeStringSet(key: String, defValue: Set<String>,
      emitOnStart: Boolean): Flowable<Set<String>> {
    return observe(key, emitOnStart)
        .map { sharedPreferences.getStringSet(key, defValue) }
  }

}
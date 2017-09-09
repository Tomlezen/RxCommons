package com.tlz.rxcommons

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.support.annotation.RequiresPermission
import android.telephony.TelephonyManager
import android.text.TextUtils
import io.reactivex.Observable
import io.reactivex.android.MainThreadDisposable
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by LeiShao.
 * Data 2017/5/24.
 * Time 10:06.
 * Email t.nainshang@foxmail.com.
 */
object RxNetwork {

  @RequiresPermission(value = Manifest.permission.ACCESS_NETWORK_STATE)
  fun observeNetworkAvailable(context: Context): Observable<Boolean> {
    return Observable.create { e ->
      try {
        val connectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // 获取链接网络信息
        val networkInfo = connectivityManager.activeNetworkInfo
        e.onNext(networkInfo != null && networkInfo.isAvailable)
      } catch (exception: Exception) {
        e.onNext(false)
      }

      e.onComplete()
    }
  }

  fun observeNetworkState(context: Context): Observable<NetworkInfo.State> {
    return Observable.create<NetworkInfo.State>({ e ->
      val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
          val connectivityManager = context.getSystemService(
              Context.CONNECTIVITY_SERVICE) as ConnectivityManager
          val info = connectivityManager.activeNetworkInfo
          if (info != null) {
            e.onNext(info.state)
          } else {
            e.onNext(NetworkInfo.State.UNKNOWN)
          }
        }
      }

      e.setDisposable(object : MainThreadDisposable() {
        override fun onDispose() {
          context.unregisterReceiver(receiver)
        }
      })

      context.registerReceiver(receiver, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
    }).observeOn(AndroidSchedulers.mainThread())
  }

  fun observeNeworkType(context: Context): Observable<Int> {
    return observeNetworkState(context)
        .map {
          val manager = context.getSystemService(
              Context.CONNECTIVITY_SERVICE) as ConnectivityManager
          val networkInfo = manager.activeNetworkInfo
          var networktype = NetworkType.INVALID
          if (networkInfo != null && networkInfo.isConnected) {
            val type = networkInfo.typeName
            if (type.equals("WIFI", ignoreCase = true)) {
              networktype = NetworkType.WIFI
            } else if (type.equals("MOBILE", ignoreCase = true)) {
              val proxyHost = android.net.Proxy.getDefaultHost()
              networktype = if (TextUtils.isEmpty(proxyHost)) if (isFastMobileNetwork(
                  context)) NetworkType._3G else NetworkType._2G else NetworkType.WAP
            }
          }
          networktype
        }
  }

  private fun isFastMobileNetwork(context: Context): Boolean {
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    when (telephonyManager.networkType) {
      TelephonyManager.NETWORK_TYPE_1xRTT -> return false // ~ 50-100 kbps
      TelephonyManager.NETWORK_TYPE_CDMA -> return false // ~ 14-64 kbps
      TelephonyManager.NETWORK_TYPE_EDGE -> return false // ~ 50-100 kbps
      TelephonyManager.NETWORK_TYPE_EVDO_0 -> return true // ~ 400-1000 kbps
      TelephonyManager.NETWORK_TYPE_EVDO_A -> return true // ~ 600-1400 kbps
      TelephonyManager.NETWORK_TYPE_GPRS -> return false // ~ 100 kbps
      TelephonyManager.NETWORK_TYPE_HSDPA -> return true // ~ 2-14 Mbps
      TelephonyManager.NETWORK_TYPE_HSPA -> return true // ~ 700-1700 kbps
      TelephonyManager.NETWORK_TYPE_HSUPA -> return true // ~ 1-23 Mbps
      TelephonyManager.NETWORK_TYPE_UMTS -> return true // ~ 400-7000 kbps
      TelephonyManager.NETWORK_TYPE_EHRPD -> return true // ~ 1-2 Mbps
      TelephonyManager.NETWORK_TYPE_EVDO_B -> return true // ~ 5 Mbps
      TelephonyManager.NETWORK_TYPE_HSPAP -> return true // ~ 10-20 Mbps
      TelephonyManager.NETWORK_TYPE_IDEN -> return false // ~25 kbps
      TelephonyManager.NETWORK_TYPE_LTE -> return true // ~ 10+ Mbps
      TelephonyManager.NETWORK_TYPE_UNKNOWN -> return false
      else -> return false
    }
  }

  object NetworkType {

    /** 没有网络 */
    val INVALID = 0
    /** wap网络 */
    val WAP = 1
    /** 2G网络 */
    val _2G = 2
    /** 3G和3G以上网络，或统称为快速网络 */
    val _3G = 3
    /** wifi网络 */
    val WIFI = 4
  }

}
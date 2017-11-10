package com.tlz.test

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.tlz.rxcommons.bus.RxBus
import com.tlz.rxcommons.delay
import com.tlz.rxcommons.delayOnMainThread
import com.tlz.rxcommons.http.RxDownloader
import com.tlz.rxcommons.permission.RxPermissions
import com.tlz.rxcommons.test.R
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import android.support.v4.content.ContextCompat.startActivity
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import io.reactivex.android.schedulers.AndroidSchedulers
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    private lateinit var rxPermissons: RxPermissions

    private var isRequestting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dir = (externalCacheDir ?: Environment.getExternalStorageDirectory()).absolutePath
        val fileName = "algorithm_temp.txt"
        setContentView(R.layout.activity_main)
//        val okhttpClient = OkHttpClient.Builder().build()
//        RxDownloader(okhttpClient).download("http://thermometer.fanmicloud.com/download/sf/a/20170703-1-4.txt", dir, fileName)
//            .subscribe({
//                Log.e("MainActivity", "download algorithm update file successful")
//            }, {
//                Log.e("MainActivity", "download algorithm file failed")
//            })
        RxBus.onEvent<String>(MainActivity::class) {
            Toast.makeText(this, "我接受到消息啦$it", Toast.LENGTH_LONG).show()
        }
        rxPermissons = RxPermissions.with(this)
        RxBus.postDelay("cesh1", 2000)
    }

    private fun requestPermission(){
        Log.d("MainActivity", "do requestPermission")
        isRequestting = true
        rxPermissons.request(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .delay(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate { isRequestting = false }
                .subscribe({
                    if(it){
                        Log.d("MainActivity", "permission request successful")
                        delay(2000){
                            RxBus.post(MainActivity::class, "this is a test msg")
                        }
                    }else{
                        Toast.makeText(this@MainActivity, "您已拒绝了权限，2s后将开启权限设置界面", Toast.LENGTH_LONG).show()
                        delayOnMainThread(2, TimeUnit.SECONDS){
                            openPermissionSettingPage()
                        }
                    }
                })
    }

    private fun openPermissionSettingPage(){
        try {
            val intent = Intent()
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", packageName, null)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "do onResume")
        if(isRequestting == false){
            requestPermission()
        }
    }

}

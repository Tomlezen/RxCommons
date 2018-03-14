package com.tlz.test

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.tlz.rxcommons.*
import com.tlz.rxcommons.http.ProgressCallback
import com.tlz.rxcommons.http.RxDownloader
import com.tlz.rxcommons.permission.RxPermissions
import com.tlz.rxcommons.test.R
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.OkHttpClient
import java.lang.Exception
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var rxPermissons: RxPermissions

    private var isRequesting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dir = (externalCacheDir ?: Environment.getExternalStorageDirectory()).absolutePath
        val fileName = "algorithm_temp.txt"
        setContentView(R.layout.activity_main)
        val okhttpClient = OkHttpClient.Builder().build()
//        RxDownloader.newInstance(okhttpClient).download("https://downloads.atlassian.com/software/sourcetree/windows/ga/SourceTreeSetup-2.4.8.0.exe", dir, fileName, ProgressCallback({
//            Log.e("MainActivity", "total size = $it")
//        }, {
//            Log.e("MainActivity", "download progress = $it")
//        }))
//            .subscribe({
//                Log.e("MainActivity", "download file successful")
//            }, {
//                it.printStackTrace()
//                Log.e("MainActivity", "download file failed")
//            })
       onEvent<String>(MainActivity::class) {
            Toast.makeText(this, "我接受到消息啦$it", Toast.LENGTH_LONG).show()
        }
        rxPermissons = RxPermissions.with(this)
        postDelay("cesh1", 2000)
    }

    private fun requestPermission(){
        Log.d("MainActivity", "do requestPermission")
        isRequesting = true
        rxPermissons.request(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .delay(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate { isRequesting = false }
                .subscribe({
                    if(it){
                        Log.d("MainActivity", "permission request successful")
                        delay(2000){
                            post(MainActivity::class, "this is a test msg")
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
        if(!isRequesting){
            requestPermission()
        }
    }

}

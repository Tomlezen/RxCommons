package com.tlz.test

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.tlz.rxcommons.http.RxDownloader
import com.tlz.rxcommons.test.R
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dir = (externalCacheDir ?: Environment.getExternalStorageDirectory()).absolutePath
        val fileName = "algorithm_temp.txt"
        setContentView(R.layout.activity_main)
        val okhttpClient = OkHttpClient.Builder().build()
        RxDownloader(okhttpClient).download("http://thermometer.fanmicloud.com/download/sf/a/20170703-1-4.txt", dir, fileName)
            .subscribe({
                Log.e("MainActivity", "download algorithm update file successful")
            }, {
                Log.e("MainActivity", "download algorithm file failed")
            })
    }
}

package com.tlz.rxcommons

import android.content.Intent
import android.content.IntentFilter
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Created by LeiShao.
 * Data 2017/5/24.
 * Time 19:30.
 * Email t.nainshang@foxmail.com.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class RxReceiverTest{

    @Before
    fun setUp() {

    }

    @After
    fun tearDown() {

    }

    @Test
    fun observeBroadcast() {
        val filter = IntentFilter()
        filter.addAction("test1")
        filter.addAction("test2")
        filter.addAction("test3")
        var action: String? = null
        RxReceiverExts.observeBroadcast(RuntimeEnvironment.application, filter)
                .subscribe { action = it.action }
        RuntimeEnvironment.application.sendBroadcast(Intent("test1"))
        Assert.assertEquals(action ,"test1")
        RuntimeEnvironment.application.sendBroadcast(Intent("test11"))
        Assert.assertEquals(action ,"test1")
        RuntimeEnvironment.application.sendBroadcast(Intent("test2"))
        Assert.assertEquals(action ,"test2")
    }

}
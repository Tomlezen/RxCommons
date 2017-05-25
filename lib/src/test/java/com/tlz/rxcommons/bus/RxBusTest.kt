package com.tlz.rxcommons.bus

import com.sys1yagi.mastodon.android.testtool.RxTestSchedulerRule
import io.reactivex.functions.Consumer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Created by LeiShao.
 * Data 2017/5/25.
 * Time 9:46.
 * Email t.nainshang@foxmail.com.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class RxBusTest {

    @get:Rule
    val rule = RxTestSchedulerRule()

    @Before
    fun setUp() {

    }

    @Test
    fun onEvent() {
        val tag = "test"
        var value = "123"
        RxBus.onEvent(tag, Consumer<String> {
            value = it
        })
        RxBus.post(tag, "test")
        rule.triggerActions()
        Assert.assertEquals(value, tag)
    }

    @Test
    fun onEvent1() {

    }

    @Test
    fun onEvent2() {

    }

    @Test
    fun onEvent3() {

    }

    @Test
    fun register() {

    }

    @Test
    fun unregister() {

    }

    @Test
    fun unregister1() {

    }

    @Test
    fun post() {

    }

    @Test
    fun post1() {

    }

}
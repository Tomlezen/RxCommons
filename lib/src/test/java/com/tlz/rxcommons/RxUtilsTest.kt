package com.tlz.rxcommons

import android.os.Looper
import com.sys1yagi.mastodon.android.testtool.RxTestSchedulerRule
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Created by LeiShao.
 * Data 2017/5/24.
 * Time 9:41.
 * Email t.nainshang@foxmail.com.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class RxUtilsTest {

    @get:Rule
    val rule = RxTestSchedulerRule()

    @Test
    fun disposeIfNotNull() {
        val disposable = Flowable.just(1).subscribe()
        RxUtils.disposeIfNotNull(disposable)
        Assert.assertTrue(disposable.isDisposed)
    }

    @Test
    fun runOnUi() {
        var looper: Looper? = null
        Flowable.just(true)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    RxUtils.runOnUi()
                            .subscribe {
                                looper = Looper.myLooper()
                            }
                })
        rule.triggerActions()
        Assert.assertTrue(RuntimeEnvironment.application.mainLooper == looper)
    }

    @Test
    fun runOnUi1() {

    }

    @Test
    fun runOnThread() {
        var value = 0
        RxUtils.runOnThread()
                .subscribe {
                    Thread.sleep(5000)
                    value = 1
                }
        rule.triggerActions()
        Assert.assertTrue(value == 0)
    }

    @Test
    fun runOnThread1() {

    }

    @Test
    fun applyMainThread() {

    }

    @Test
    fun applyMainThreadForFlowable() {

    }

    @Test
    fun applyMainThreadForSingle() {

    }

    @Test
    fun countdown() {

    }

}
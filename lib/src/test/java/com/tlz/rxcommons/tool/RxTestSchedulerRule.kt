package com.sys1yagi.mastodon.android.testtool

import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.rules.ExternalResource

class RxTestSchedulerRule : ExternalResource() {
    val testScheduler: TestScheduler = TestScheduler()

    override fun before() {
        RxJavaPlugins.reset()
        RxJavaPlugins.setInitIoSchedulerHandler { testScheduler }
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }

        RxAndroidPlugins.reset()
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { testScheduler }
        RxAndroidPlugins.setMainThreadSchedulerHandler { testScheduler }
    }

    override fun after() {
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
    }

    fun triggerActions(){
        testScheduler.triggerActions()
    }
}

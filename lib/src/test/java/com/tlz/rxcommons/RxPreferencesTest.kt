package com.tlz.rxcommons

import android.content.Context
import android.content.SharedPreferences
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
 * Time 19:54.
 * Email t.nainshang@foxmail.com.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class RxPreferencesTest {

    lateinit var mPreference: RxPreferences

    lateinit var mSharePreference: SharedPreferences

    @Before
    fun setup(){
        mSharePreference = RuntimeEnvironment.application.getSharedPreferences("test", Context.MODE_PRIVATE)
        mPreference = RxPreferences(mSharePreference)
    }

    @Test
    fun observeBoolean() {
        var value = false
        mPreference.observeBoolean("testBoolean", false ,false)
                .subscribe({
                    value = it
                })
        mSharePreference.edit().putBoolean("testBoolean", true).apply()
        Assert.assertTrue(value)
    }

    @Test
    fun observeString() {
        var value = "test"
        mPreference.observeString("testString", "" , false)
                .subscribe({
                    value = it
                })
        mSharePreference.edit().putString("testString", "test").apply()
        Assert.assertEquals(value, "test")
    }

    @Test
    fun observeInt() {

    }

    @Test
    fun observeLong() {

    }

    @Test
    fun observeFloat() {

    }

    @Test
    fun observeStringSet() {

    }

    @Test
    fun getSharedPreferences() {

    }

}
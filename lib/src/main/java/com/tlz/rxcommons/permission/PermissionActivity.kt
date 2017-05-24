package com.tlz.rxcommons.permission

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi

/**
 * Created by LeiShao.
 * Data 2017/5/24.
 * Time 10:56.
 * Email t.nainshang@foxmail.com.
 */
class PermissionActivity : Activity() {


    companion object {
        val REQUEST_CODE = 0x10101

        private val PERMISSIONS = "permissions"

        fun startActivity(context: Context, permissions: Array<String>) {
            val intent = Intent(context, PermissionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(PERMISSIONS, permissions)
            context.startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            handleIntent(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onNewIntent(intent: Intent) {
        handleIntent(intent)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun handleIntent(intent: Intent) {
        val permissions = intent.getStringArrayExtra("permissions")
        requestPermissions(permissions, REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE)
            RxPermissions.getInstance(this)?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        finish()
    }

}

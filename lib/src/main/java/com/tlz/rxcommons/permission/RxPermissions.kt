package com.tlz.rxcommons.permission

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.Function
import io.reactivex.functions.Predicate
import io.reactivex.subjects.PublishSubject
import java.util.*

/**
 * Created by LeiShao.
 * Data 2017/5/24.
 * Time 10:57.
 * Email t.nainshang@foxmail.com.
 */
class RxPermissions private constructor(val context: Context) {

    private val mSubjectMap = HashMap<String, PublishSubject<Permission>>()

    companion object{

        private var mInstance: RxPermissions? = null

        fun getInstance(context: Context): RxPermissions?{
            if(mInstance == null){
                mInstance = RxPermissions(context)
            }
            return mInstance
        }
    }

    fun request(vararg permissions: String): Observable<Boolean> {
        return Observable.just(Any()).compose { upstream -> request_(upstream, *permissions) }
    }

    private fun request_(observable: Observable<Any>, vararg permissions: String): Observable<Boolean> {
        return doRequest(observable, *permissions)
                .buffer(permissions.size) //等待所有数据
                .flatMap(Function<List<Permission>, ObservableSource<Boolean>> { permissions ->
                    if (permissions.isEmpty()) {
                        return@Function Observable.empty<Boolean>()
                    }
                    //只要有一个被取消就返回false
                    for ((_, granted) in permissions) {
                        if (!granted) {
                            return@Function Observable.just(false)
                        }
                    }
                    Observable.just(true)
                })
    }

    fun requestEach(vararg permissions: String): Observable<Permission> {
        return Observable.just(true).compose { upstream -> doRequest(upstream, *permissions) }
    }

    private fun doRequest(observable: Observable<*>, vararg permissions: String): Observable<Permission> {
        if (permissions.isEmpty()) {
            throw IllegalArgumentException("Requires at least one input permission")
        }
        return observable
                .flatMap { doRequest_(*permissions) }
    }

    private fun doRequest_(vararg permissions: String): Observable<Permission> {

        /** 所有的请求权限  */
        val list = ArrayList<Observable<Permission>>(permissions.size)
        /** 需要请求的权限  */
        val unrequestedPermissions = ArrayList<String>()

        //过滤
        Observable.fromArray(*permissions)
                .filter(Predicate<String> { s ->
                    if (isGranted(s)) {
                        list.add(Observable.just(Permission(s, true)))
                        return@Predicate false
                    }
                    if (isRevoked(s)) {
                        list.add(Observable.just(Permission(s, false)))
                        return@Predicate false
                    }
                    true
                })
                .subscribe({ s ->
                    var subject: PublishSubject<Permission>? = mSubjectMap[s]
                    if (subject == null) {
                        unrequestedPermissions.add(s)
                        subject = PublishSubject.create<Permission>()
                        mSubjectMap.put(s, subject)
                    }
                    list.add(subject!!)
                })

        if (!unrequestedPermissions.isEmpty()) {
            PermissionActivity.startActivity(context, unrequestedPermissions.toTypedArray())
        }
        return Observable.concat(Observable.fromIterable(list))
    }

    internal fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if(requestCode == PermissionActivity.REQUEST_CODE){
            var i = 0
            val size = permissions.size
            while (i < size) {
                val subject = mSubjectMap[permissions[i]] ?: throw IllegalStateException("didn't find the corresponding permission request.")
                //移除权限请求
                mSubjectMap.remove(permissions[i])
                val granted = grantResults[i] == PackageManager.PERMISSION_GRANTED
                //请求发布结果
                subject.onNext(Permission(permissions[i], granted))
                subject.onComplete()
                i++
            }
        }
    }

    fun shouldShowRequestPermissionRationale(activity: Activity, vararg permissions: String): Observable<Boolean> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return Observable.just(false)
        }
        return Observable.just(getInstance(activity)?.shouldShowRequestPermissionRationale_(activity, *permissions))
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun shouldShowRequestPermissionRationale_(activity: Activity, vararg permissions: String): Boolean {
        return permissions.none { !isGranted(it) && !activity.shouldShowRequestPermissionRationale(it) }
    }

    fun isGranted(permission: String): Boolean {
        return isGranted_(permission)
    }

    fun isRevoked(permission: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return isM() && isRevoked_(permission)
        }
        return false
    }

    private fun isM(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    private fun isGranted_(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun isRevoked_(permission: String): Boolean {
        return context.packageManager.isPermissionRevokedByPolicy(permission, context.packageName)
    }

}
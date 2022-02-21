package com.mnfst.saas.test.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build


class RequestPermissionResultItem(val permission: String, val granted: Boolean)
typealias RequestPermissionResult = List<RequestPermissionResultItem>
typealias RequestPermissionsResultProc = (result: RequestPermissionResult) -> Unit

fun RequestPermissionResult?.hasGranted(permission: String) =
    this?.firstOrNull { it.permission == permission }?.granted == true


class PermissionManager(private val context: Context) {
  private var continuation: RequestPermissionsResultProc? = null

  private fun lackingPermissions(permissions: Array<String>) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
      permissions.filterNot {
        (context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED)
      }
    else
      emptyList()

  fun requestPermissions(permissions: Array<String>, activity: Activity, proc: RequestPermissionsResultProc) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
      return proc.invoke(permissions.map {
        RequestPermissionResultItem(it, true)
      })

    if (lackingPermissions(permissions).isEmpty())
      return proc.invoke(permissions.map { RequestPermissionResultItem(it, true) })

    continuation = proc
    activity.requestPermissions(permissions, 1)
  }

  fun onRequestPermissionsResult(permissions: Array<String>, grantResults: IntArray) {
    val result = permissions.mapIndexed { idx, p ->
      RequestPermissionResultItem(p, grantResults[idx] == PackageManager.PERMISSION_GRANTED)
    }

    val proc = continuation
    continuation = null
    proc?.invoke(result)
  }
}

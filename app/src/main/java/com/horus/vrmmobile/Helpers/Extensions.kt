package com.horus.vrmmobile.Helpers

import android.view.View
import com.horus.vrmmobile.Config.SharedConfig

fun View.permissionsOnly(permission: String) {
    visibility = if (SharedConfig.getPermissions().contains(permission)) View.VISIBLE else View.GONE
}
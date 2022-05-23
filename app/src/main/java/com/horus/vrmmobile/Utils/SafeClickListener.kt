package com.horus.vrmmobile.Utils

import android.os.SystemClock
import android.util.Log
import android.view.View

private class SafeClickListener(
        private var defaultInterval: Int = 1000,
        private val onSafeCLick: (View) -> Unit
) : View.OnClickListener {
    private var lastTimeClicked: Long = 0
    override fun onClick(v: View) {
        Log.e("click button", "click" + (SystemClock.elapsedRealtime() - lastTimeClicked) + " - " + defaultInterval + (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval))
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeCLick(v)
    }
}

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}

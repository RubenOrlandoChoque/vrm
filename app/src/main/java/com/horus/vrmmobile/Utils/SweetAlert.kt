package com.horus.vrmmobile.Utils

import android.content.Context
import android.os.Handler
import android.view.View
import cn.pedant.SweetAlert.SweetAlertDialog

abstract class SweetAlert(context: Context, type: Int, title: String, message: String, val timeCloseMillis: Long = 2000): SweetAlertDialog(context, type) {

    init {
        titleText = title
        contentText = message
        setOnShowListener { (getButton(SweetAlertDialog.BUTTON_CONFIRM).parent as View).visibility = View.GONE }
        setCancelable(false)
    }

    override fun show() {
        super.show()
        Handler().postDelayed({
            this@SweetAlert.dismiss()
            onClose()
        }, timeCloseMillis)
    }

    abstract fun onClose()
}
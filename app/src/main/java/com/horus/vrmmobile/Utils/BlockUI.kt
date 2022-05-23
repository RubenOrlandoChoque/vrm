package com.horus.vrmmobile.Utils

import android.app.Activity
import android.app.Dialog
import android.view.Window
import android.widget.TextView
import com.horus.vrmmobile.R

class BlockUI(val context: Activity): Dialog(context) {
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.loading)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        window.setBackgroundDrawable(null)
    }

    fun start(text: String = ""){
        val txtMessage = findViewById<TextView>(R.id.txt_message_dialog)
        txtMessage.text = text
        show()
    }

    fun stop(){
//        if (isShowing) {
            dismiss()
//        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        context.finish()
    }
}
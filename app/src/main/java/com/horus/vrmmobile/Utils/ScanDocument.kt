package com.horus.vrmmobile.Utils

import android.app.Activity
import android.util.Log
import android.view.Window
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.zxing.BarcodeFormat
import com.horus.vrmmobile.Models.Person
import com.horus.vrmmobile.R
import android.view.WindowManager

abstract class ScanDocument(val context: Activity) {

    private var codeScanner: CodeScanner? = null
    private var dialogScan: MaterialDialog? = null

    fun show(){
        showScanDialog()
    }

    fun close(){
        codeScanner!!.stopPreview()
        codeScanner!!.releaseResources()
        dialogScan!!.dismiss()
    }

    private fun showScanDialog(){
        dialogScan = MaterialDialog(context).show {
            customView(viewRes = R.layout.dialog_scan, scrollable = false)
            title(text = "Escanear documento")
            message(text = "Escanee el codigo de barra que figura en su documento de identidad.")
            negativeButton(R.string.cancel)
            cancelOnTouchOutside(false)
            noAutoDismiss()
            negativeButton {
                close()
            }
        }

//        val title = dialogScan!!.findViewById<TextView>(R.id.md_text_title)
//        title.setTextColor(context.getColor(R.color.white))
//        dialogScan!!.window.setBackgroundDrawable(context.getDrawable(R.drawable.bg_gradient))

        val scanView = dialogScan!!.findViewById<CodeScannerView>(R.id.scanner_view)
        codeScanner = CodeScanner(context, scanView)
        codeScanner!!.camera = CodeScanner.CAMERA_BACK
        codeScanner!!.formats =listOf(BarcodeFormat.PDF_417)
        codeScanner!!.autoFocusMode = AutoFocusMode.SAFE
        codeScanner!!.scanMode = ScanMode.SINGLE
        codeScanner!!.isAutoFocusEnabled = true
        codeScanner!!.isFlashEnabled = false
        codeScanner!!.decodeCallback = DecodeCallback {
            context.runOnUiThread {
                codeScanner!!.stopPreview()
                setResultScan(it.text)
            }
        }
        codeScanner!!.errorCallback = ErrorCallback {
            context.runOnUiThread {
                Log.e("codeScanner", "Camera initialization error: ${it.message}")
            }
        }
        codeScanner!!.startPreview()
    }

    private fun setResultScan(result: String){
        if(result.split("@").size < 7){
            Log.i("Scan Error", result)
            codeScanner!!.startPreview()
            return;
        }
        val person = ModelSyncBuilder.create(Person(result))
        onResult(person)
        close()
    }

    abstract fun onResult(person: Person)
}
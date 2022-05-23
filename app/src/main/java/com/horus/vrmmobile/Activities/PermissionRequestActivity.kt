package com.horus.vrmmobile.Activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.horus.vrmmobile.R
import com.horus.vrmmobile.services.DownloadService
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import kotlinx.android.synthetic.main.activity_permission.*

class PermissionRequestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        request_camera.setOnClickListener {
            val permissions = arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            )
            Permissions.check(this, permissions, null, null, object : PermissionHandler() {
                override fun onGranted() {
                    DownloadService.downloadCatalogs(this@PermissionRequestActivity) { goNextPage() }
                }
            })
        }
    }

    private fun goNextPage() {
        val i = Intent(this@PermissionRequestActivity, MainActivity::class.java)
        i.putExtra("origen", "login")
        startActivity(i)
        finish()
    }
}
package com.horus.vrmmobile.Activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Helpers.CommonHelper
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.ActionRepository
import com.horus.vrmmobile.Repositories.ConfigRepository
import com.horus.vrmmobile.Repositories.EventZoneRepository
import com.horus.vrmmobile.Utils.Task
import com.horus.vrmmobile.Utils.Utils
import com.horus.vrmmobile.services.DownloadService
import com.horus.vrmmobile.services.SyncServiceTimer
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import java.util.ArrayList

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setSyncServiceTimer()

        object : Task<String>() {
            override fun task(): String {
                try {
                    Thread.sleep(1700)
                    goNextPage()
                } catch (e: Exception) {
                    Log.e("SplashActivity", e.message)
                }
                return ""
            }
        }.run()
    }

    private fun setSyncServiceTimer() {
        val isTrackingServiceRunning = CommonHelper.isServiceRunning(this, SyncServiceTimer::class.java)
        if (!isTrackingServiceRunning) {
            val syncServiceTimer = Intent(this, SyncServiceTimer::class.java)
            startService(syncServiceTimer)
        }
    }

    private fun goNextPage() {
        if (SharedConfig.getToken().isEmpty()) {
            val i = Intent(applicationContext, PhoneActivity::class.java)
            startActivity(i)
            finish()
            return
        }

        if (SharedConfig.isTracking()) {
            val action = ActionRepository.instance.getById(SharedConfig.getActionIdTtracking())
            val ez = EventZoneRepository.instance.getById(action!!.EventZonesId)
            val i = Intent(this, InfoEventActivity::class.java)
            i.putExtra("actionId", SharedConfig.getActionIdTtracking())
            i.putExtra("eventId", ez!!.EventId)
            i.putExtra("fromSplash", true)
            startActivity(i)
            finish()
            return
        }
        var config = ConfigRepository.instance.getConfig()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                if (config != null){
                    gotoMainActivity()
                }else{
                    DownloadService.downloadCatalogs(this@SplashActivity) {
                        val i = Intent(this@SplashActivity, MainActivity::class.java)
                        i.putExtra("origen","login")
                        startActivity(i)
                        finish()
                    }
                }
//          Verificacion de Mensaje Firebase
            }  else if(!SharedConfig.isPhoneVerified()){
                val i = Intent(applicationContext, PhoneActivity::class.java)
                startActivity(i)
                finish()
                return
            } else {
                val i = Intent(applicationContext, PermissionRequestActivity::class.java)
                startActivity(i)
                finish()
            }
        } else {
            if(SharedConfig.isFirstDownloadFinish() && config != null){
                gotoMainActivity()
            }else {
                DownloadService.downloadCatalogs(this@SplashActivity) {
                    val i = Intent(this@SplashActivity, MainActivity::class.java)
                    i.putExtra("origen","login")
                    startActivity(i)
                    finish()
                }
            }
        }
    }

    private fun gotoMainActivity() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        finish()
    }
}

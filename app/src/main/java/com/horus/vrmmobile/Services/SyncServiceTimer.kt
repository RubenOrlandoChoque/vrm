package com.horus.vrmmobile.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.horus.vrmmobile.Repositories.MessageRepository
import com.horus.vrmmobile.Repositories.MultimediaRepository
import com.horus.vrmmobile.Utils.Utils
import java.util.*

class SyncServiceTimer : Service() {
    private val timer = Timer()
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (Utils.isNetworkAvailable()) {
                    object : Thread() {
                        override fun run() {
                            MessageRepository.instance.syncReadMessages()
                            MultimediaRepository.instance.uploadPending()
                            SyncService.push()
                        }
                    }.run()
                }
            }
        }, 0, 1 * 60 * 1000)
    }
}
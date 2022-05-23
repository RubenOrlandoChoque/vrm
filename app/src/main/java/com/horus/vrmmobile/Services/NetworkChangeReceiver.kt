package com.horus.vrmmobile.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Repositories.MessageRepository
import com.horus.vrmmobile.Repositories.MultimediaRepository
import com.horus.vrmmobile.Utils.Utils

/**
 * Created by USUARIO 004 on 27/8/2018.
 */
class NetworkChangeReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val status = Utils.isNetworkAvailable()
        if (status) {
            object : Thread() {
                override fun run() {
                    if(!SharedConfig.getToken().isEmpty()){
                        MessageRepository.instance.syncReadMessages()
                        MultimediaRepository.instance.uploadPending()
                        SyncService.push()
                    }
                }
            }.run()
        }
    }
}

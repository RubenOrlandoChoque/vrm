package com.horus.vrmmobile.services

/**
 * Created by USUARIO 004 on 16/3/2018.
 */

import android.os.Bundle
import android.util.Log

import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.horus.vrmmobile.Utils.Utils

class NotificationFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {

        if (remoteMessage!!.data != null && remoteMessage.data.size > 0) {
            if (/* Check if data needs to be processed by long running job */ true) {
                val params = Utils.convertToBundle(remoteMessage.data)
                scheduleJob(params)
            } else {
                handleNow()
            }
        }

        if (remoteMessage.notification != null) {
            Log.d(TAG, "Message Message Body: " + remoteMessage.notification!!.body!!)
        }
    }


    private fun scheduleJob(params: Bundle) {
        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))
        var notificationType = params.getString("NotificationType");
        if (null == notificationType){notificationType="1"}
        val myJob = dispatcher.newJobBuilder()
                .setService(NotificationJobService::class.java)
                .setExtras(params)
                .setTag(notificationType)
                .build()
        dispatcher.schedule(myJob)
    }


    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    companion object {

        private val TAG = "FirebaseMsgService"
    }
}
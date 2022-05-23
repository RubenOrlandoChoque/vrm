package com.horus.vrmmobile.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import android.util.Log

import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.firebase.iid.FirebaseInstanceId
import com.horus.vrmmobile.Activities.MainActivity
import com.horus.vrmmobile.Activities.NotificationsListActivity
import com.horus.vrmmobile.Config.Constant
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.Message
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.ProjectRepository
import com.horus.vrmmobile.Utils.Event
import com.horus.vrmmobile.VRMApplication
import org.greenrobot.eventbus.EventBus

import org.json.JSONException
import org.json.JSONObject

/**
 * Created by USUARIO 004 on 23/3/2018.
 */

object NotificationService {

    private val TAG = "Notification2Manager"
    private var countNotify = 0
    private var countNewEvent = 0

    fun join(topics: String) {
        val urlServer = Constant.urlVRMServer
        val token = FirebaseInstanceId.getInstance().token
        val url = "$urlServer/api/notification?token=$token&topics=$topics&ZUMO-API-VERSION=2.0.0"
        AndroidNetworking.get(url)
                .addHeaders("Content-Type", "application/json")
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        try {
                            if (response.getInt("ResultCode") == 1) {
                                Log.i("join", response.getString("ResultMsg") + "-" + response.getString("Result"))
                            } else {
                                Log.e("join", response.getString("ResultMsg"))
                            }
                        } catch (e: JSONException) {
                            Log.e("join", e.message)
                        }
                    }

                    override fun onError(anError: ANError) {
                        Log.e("join", anError.errorDetail)
                    }
                })
    }

    fun send(notification: Message) {
        val urlServer = Constant.urlVRMServer
        AndroidNetworking.post(urlServer + "api/notification")
                .addHeaders("Content-Type", "application/json")
                .addBodyParameter(notification)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        try {
                            if (response.getInt("resultCode") == 1) {
                                Log.i("send", response.getString("result"))
                            } else {
                                Log.e("send", response.getString("resultMsg"))
                            }
                        } catch (e: JSONException) {
                            Log.e("send", e.message)
                        }

                    }

                    override fun onError(anError: ANError) {
                        Log.e("send", anError.errorDetail)
                    }
                })
    }

    fun sendNotification(title: String, messageBody: String, notificationType: String, intent: Intent, projectId: String?) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(VRMApplication.context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(VRMApplication.context!!, notificationType)
                .setSmallIcon(R.drawable.icon_logo)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = VRMApplication.context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel(notificationType,
                            "Channel human readable title",
                            NotificationManager.IMPORTANCE_DEFAULT)
            } else {
                null
            }
            notificationManager.createNotificationChannel(channel!!)
        }
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
        if(notificationType.equals("OnNewEvent")){
            countNewEvent = SharedConfig.getCountEvent()
            countNewEvent++
            SharedConfig.setCountEvent(countNewEvent)
            EventBus.getDefault().post(Event("countEvent"))
        }else {
            countNotify = SharedConfig.getCountNotify()
            countNotify++
            SharedConfig.setCountNotify(countNotify)
            EventBus.getDefault().post(Event("countNotify"))
        }
    }
}

package com.horus.vrmmobile.services

/**
 * Created by USUARIO 004 on 16/3/2018.
 */

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.horus.vrmmobile.Activities.EventsActivity
import com.horus.vrmmobile.Activities.NotificationsListActivity
import com.horus.vrmmobile.Config.Constant
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.Message
import com.horus.vrmmobile.Models.MessageMultimedias
import com.horus.vrmmobile.Models.Multimedia
import com.horus.vrmmobile.Repositories.*
import com.horus.vrmmobile.Utils.*
import com.horus.vrmmobile.VRMApplication
import io.realm.Realm
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.util.*

class NotificationJobService : JobService() {
    override fun onStartJob(jobParameters: JobParameters): Boolean {
        val params2 = jobParameters.extras
        if (params2 != null && params2.containsKey("NotificationType")) {
            object : Task<String>() {
                override fun task(): String {
                    try {
                        val title = params2.getString("Title", "Sin Titulo")
                        val messageBody = params2.getString("MessageOld", "")
                        val notificationType = params2.getString("NotificationType", "1")
                        if (notificationType == "OnNewEvent") {
                            download(title, messageBody, notificationType, params2)
                        } else {
                            val mailerId = params2.getString("MailerId", "-1")
                            val mailerName = params2.getString("MailerName", "Sin Nombre")
                            val messageId = params2.getString("MessageId", "Sin MessageId")

                            val intent = Intent(VRMApplication.context, NotificationsListActivity::class.java)
                            intent.putExtra("tittle",title)
                            intent.putExtra("messageBody",messageBody)
                            NotificationService.sendNotification(title, messageBody, notificationType, intent,"")
                            val notification = Message()
                            notification.Id = messageId
                            notification.Sender = mailerName
                            notification.SenderId = mailerId
                            notification.Title = title
                            notification.Text = messageBody
                            notification.NotificationType = notificationType
                            notification.SendDate = DateUtils.convertDateToString(Date())
                            notification.CompleteDownload = false

                            val m = MessageRepository.instance.getByField(mailerId, "SenderId")
                            if (m == null) {
                                notification.Color = Utils.getRandomMaterialColor("400", applicationContext)
                            } else {
                                notification.Color = m.Color
                            }

                            MessageRepository.instance.addOrUpdate(notification, false)
                            EventBus.getDefault().post(Event("new_message_incoming", messageId))
                            downloadMessage(messageId)
                        }
                    } catch (ex: java.lang.Exception) {
                        Log.e("onStartJob", ex.message)
                    }
                    return ""
                }
            }.run()
        }
        return false
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean {
        return false
    }

    private fun download(title: String, messageBody: String, notificationType: String, params: Bundle) {
        Log.e("neuevo evento", "nuevo evento")
        val projectId = params.getString("ProjectId")
        val contadorService = object : TaskIterator<RepositoryInterface<*>>(DownloadService.getListTablesEventDownload()) {
            override fun actionEnd() {
                val intent = Intent(VRMApplication.context, EventsActivity::class.java)
                intent.putExtra("tittle", title)
                intent.putExtra("messageBody", messageBody)
                intent.putExtra("projectId", params.getString("ProjectId"))
                intent.putExtra("eventId", params.getString("EventId"))
                intent.putExtra("fromNotification", true)
                NotificationService.sendNotification(title, messageBody, notificationType, intent, projectId)
            }

            override fun actionItem(item: RepositoryInterface<*>) {
                item.download()
            }
        }
        contadorService.simultaneousWork = 3
        contadorService.eventNext = "download_finish"
        contadorService.start()
    }

    companion object {
        private val TAG = "MyJobService"
        fun downloadMessage(messageId: String, onSuccess: (() -> Unit)? = null, onError: (() -> Unit)? = null) {
            val url = "${Constant.urlVRMServer}api/messages/$messageId"
            AndroidNetworking.get(url)
                    .addHeaders("Content-Type", "application/json")
                    .addHeaders("Authorization", "Bearer " + SharedConfig.getToken())
                    .addHeaders("UserApp", SharedConfig.getUserId())
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject) {
                            val realm: Realm? = null
                            try {
                                var msj = MessageRepository.instance.getById(response.getString("MessageId"))
                                val messageMultimedia = response.getJSONArray("Multimedias")
                                if (msj == null) {
                                    msj = Message()
                                    msj.Id = response.getString("MessageId")
                                }
                                msj.Sender = response.getString("MailerName")
                                msj.SenderId = response.getString("MailerId")
                                msj.Text = response.getString("Message")
                                msj.Title = response.getString("Title")
                                msj.CompleteDownload = true
                                msj.HasAttachments = messageMultimedia.length() > 0
                                MessageRepository.instance.addOrUpdate(msj, false)

                                for (i in 0 until messageMultimedia.length()) {
                                    val msjMultimediasJson = messageMultimedia.getJSONObject(i)
                                    var multimedia = MultimediaRepository.instance.getById(msjMultimediasJson.getString("MultimediaId"))
                                    if (multimedia == null) {
                                        multimedia = Multimedia()
                                        multimedia.Id = msjMultimediasJson.getString("MultimediaId")
                                    }
                                    multimedia.Path = msjMultimediasJson.getString("Path")
                                    multimedia.MultimediaTypeId = msjMultimediasJson.getString("MultimediaTypeId")
                                    MultimediaRepository.instance.addOrUpdate(multimedia, false)

                                    var msjMultimedia = MessageMultimediaRepository.instance.getById(msjMultimediasJson.getString("MessageMultimediaId"))
                                    if (msjMultimedia == null) {
                                        msjMultimedia = MessageMultimedias()
                                        msjMultimedia.Id = msjMultimediasJson.getString("MessageMultimediaId")
                                    }
                                    msjMultimedia.MultimediaId = multimedia.Id
                                    msjMultimedia.MessageId = msj.Id
                                    MessageMultimediaRepository.instance.addOrUpdate(msjMultimedia, false)
                                }
                                EventBus.getDefault().post(Event("download_message_finish", messageId))
                                onSuccess?.invoke()
                            } catch (e: Exception) {
                                onError?.invoke()
//                            Log.e(TAG, e.message)
                            } finally {
                                if (realm != null && !realm.isClosed) {
                                    realm.close()
                                }
                            }
                        }

                        override fun onError(anError: ANError) {
                            onError?.invoke()
                            var messageError = Utils.getMessageError(anError)
                            messageError = if (messageError.isEmpty()) "Error al conectar con el servidor $url" else messageError
                            Log.e(TAG, messageError)
                        }
                    })
        }
    }

}
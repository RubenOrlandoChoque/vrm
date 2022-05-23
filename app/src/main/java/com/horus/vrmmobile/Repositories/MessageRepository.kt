package com.horus.vrmmobile.Repositories

import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.horus.vrmmobile.Config.Constant
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.Dtos.MessageDtoUpload
import com.horus.vrmmobile.Models.HistoyChange
import com.horus.vrmmobile.Models.Message
import com.horus.vrmmobile.Utils.Utils
import io.realm.Realm
import org.json.JSONObject

/**
 * Created by mgarzon on 24/06/2019.
 */

open class MessageRepository private constructor(): RepositoryInterface<Message>(Message::class.java) {

    private object Holder { val INSTANCE = MessageRepository() }
    companion object {
        val instance: MessageRepository by lazy { Holder.INSTANCE }
    }

    override fun getByIdAsJSONObject(id: String): JSONObject? {
        var item: JSONObject? = null
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            val result = realm.where(getInterfaceClass()).equalTo("Id", id).findFirst()

            if(result != null){
                val gson = Utils.gson()
                val dto = MessageDtoUpload(realm.copyFromRealm(result))
                val resultString = gson.toJson(dto)
                item = JSONObject(resultString)
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        } finally {
            if (realm != null && !realm.isClosed) {
                realm.close()
            }
        }
        return item
    }

    fun syncReadMessages () {
        val messagesTo = getPendingMessagesTo()
        messagesTo.forEach { upload(it.ModelId, SharedConfig.getUserId()) }
    }

    fun upload(messageId: String, personId: String) {
        val url = Constant.urlVRMServer + "api/SyncMobile/SyncMessagesTo"
        val   body =  JSONObject()
        body.put("MessageId",messageId)
        body.put("PersonId",personId)
        AndroidNetworking.post(url)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Authorization","Bearer " + SharedConfig.getToken())
                .addJSONObjectBody(body)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        HistoryChangeRepository.removeItem(messageId)
                    }

                    override fun onError(error: ANError) {
                        Log.e("url", error.errorDetail)
                    }
                })
    }

    fun getPendingMessagesTo(): ArrayList<HistoyChange> {
        val items: ArrayList<HistoyChange> = ArrayList()
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val results = realm.where(HistoyChange::class.java).equalTo("ModelName", "MessagesTo").findAll()
            if(results != null){
                items.addAll(realm.copyFromRealm(results))
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        } finally {
            if (realm != null && !realm.isClosed) {
                realm.close()
            }
        }
        return items
    }
}
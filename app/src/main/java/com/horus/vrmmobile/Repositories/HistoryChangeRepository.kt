package com.horus.vrmmobile.Repositories
import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.horus.vrmmobile.Config.Constant
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.HistoyChange
import com.horus.vrmmobile.Utils.Event
import io.realm.Realm
import io.realm.Sort
import org.greenrobot.eventbus.EventBus
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.Semaphore

/**
 * Created by USUARIO 004 on 27/8/2018.
 */
open class HistoryChangeRepository {

    private object Holder { val INSTANCE = HistoryChangeRepository() }
    companion object {
        private val semaphore = Semaphore(1)
        var TAG: String = "HistoryChangeRepository"
        val instance: HistoryChangeRepository by lazy { Holder.INSTANCE }

        fun getPending():List<HistoyChange> {
            var historyChanges: List<HistoyChange> = ArrayList()
            var realm: Realm? = null
            try {
                realm = Realm.getDefaultInstance()
                val items = realm.where(HistoyChange::class.java).findAll().sort("RegisterDate", Sort.DESCENDING)
                historyChanges = realm.copyFromRealm(items)
            } catch (e: Exception) {
                Log.e("HistoryChangeService", e.message)
            } finally {
                if (realm != null && !realm.isClosed) {
                    realm.close()
                }
            }
            return historyChanges
        }

        fun add(modelId: String,modelName: String, action: String, realm: Realm) {
            val histoyChange = HistoyChange()
            histoyChange.Id = UUID.randomUUID().toString()
            histoyChange.Action = action
            histoyChange.ModelId = modelId
            histoyChange.ModelName = modelName
            histoyChange.RegisterDate = Date()
            realm.insertOrUpdate(histoyChange)
        }

        fun existisItem(modelId: String):Boolean {
            var result = false
            var realm: Realm? = null
            try {
                realm = Realm.getDefaultInstance()
                val item = realm.where(HistoyChange::class.java).equalTo("ModelId", modelId).findFirst()
                if(item != null){
                    result = true
                }
            } catch (e: Exception) {
                Log.e("HistoryChangeService", e.message)
            } finally {
                if (realm != null && !realm.isClosed) {
                    realm.close()
                }
            }
            return result
        }

        fun removeItem(modelId: String) {
            var realm: Realm? = null
            try {
                realm = Realm.getDefaultInstance()
                realm!!.refresh()
                realm.beginTransaction()
                val results = realm.where(HistoyChange::class.java).equalTo("ModelId", modelId).findAll()
                results.deleteAllFromRealm()
                realm.commitTransaction()
            } catch (e: JSONException) {
                Log.e("removeItem", e.message)
            } finally {
                if (realm != null && !realm.isClosed) {
                    realm.close()
                }
            }
        }

        fun updateVersionData(){
            AndroidNetworking.get(Constant.urlVRMServer + "api/history-changes/GetLastVersion")
                    .addHeaders("Content-Type", "application/json")
                    .addHeaders("Authorization","Bearer " + SharedConfig.getToken())
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject) {
                            try {
                                if (response.getInt("ResultCode") == 1) {
                                    val lastVersion = response.getString("Result")
                                    SharedConfig.setVersionData(lastVersion)

                                } else {
                                    EventBus.getDefault().post(Event("pull_finish", response.getString("ResultMsg")))
                                    Log.e("updateVersionData", response.getString("ResultMsg"))
                                }
                            } catch (e: JSONException) {
                                EventBus.getDefault().post(Event("pull_finish", e.message))
                                Log.e("updateVersionData", e.message)
                            }
                        }

                        override fun onError(anError: ANError) {
                            EventBus.getDefault().post(Event("pull_finish", "Hubo un problema al conectarse con el servidor."))
                            Log.e("updateVersionData", anError.errorBody)
                        }
                    })
        }

        fun add (modelId: String,modelName: String, action: String) {
            var realm: Realm? = null
            try {
                realm = Realm.getDefaultInstance()
                realm!!.refresh()
                realm.executeTransaction(Realm.Transaction { realm ->
                    try {
                        semaphore.acquire()
                        add(modelId, modelName, action, realm)
                        semaphore.release()
                    } catch (e: JSONException) {
                        Log.e(TAG, e.message)
                    }
                })
            } catch (e: JSONException) {
                Log.e(TAG, e.message)
            } finally {
                if (realm != null && !realm.isClosed) {
                    realm.close()
                }
            }
        }
    }
}
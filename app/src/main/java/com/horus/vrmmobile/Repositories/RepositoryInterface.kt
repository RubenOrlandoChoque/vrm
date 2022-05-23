package com.horus.vrmmobile.Repositories

import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.horus.vrmmobile.Config.Constant
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.HistoyChange
import com.horus.vrmmobile.Models.RealmModelAudit
import com.horus.vrmmobile.Models.RealmModelSync
import com.horus.vrmmobile.Utils.DateUtils
import com.horus.vrmmobile.Utils.Event
import com.horus.vrmmobile.Utils.Utils
import io.realm.Realm
import io.realm.RealmModel
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.Semaphore
import kotlin.collections.ArrayList

/**
 * Created by mparraga on 29/8/2018.
 */
abstract class RepositoryInterface<T: RealmModel>(var clazz: Class<T>) {
    var apiMethodDownload: String = "api/SyncMobile/${getInterfaceClass().simpleName}"
    var apiMethodDownloadById: String = "api/SyncMobile/${getInterfaceClass().simpleName}/&id&"
    var apiMethodUpload: String = "api/SyncMobile/${getInterfaceClass().simpleName}/&id&/&action&"
    var TAG: String = ""
    private val semaphore = Semaphore(1)

    init{
        TAG = getInterfaceClass().simpleName + "Interface"
    }

    fun getInterfaceClass(): Class<T> {
        return clazz
    }

    open fun getAll(): ArrayList<T> {
        val items: ArrayList<T> = ArrayList()
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val results = realm.where(getInterfaceClass()).findAll()
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

    fun getById(id: String?): T? {
        var item: T? = null
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val result = realm.where(getInterfaceClass()).equalTo("Id", id).findFirst()
            if(result != null){
                item = realm.copyFromRealm(result)
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

    fun getByField(value: String, fieldName: String): T? {
        var item: T? = null
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val result = realm.where(getInterfaceClass()).equalTo(fieldName, value).findFirst()
            if(result != null){
                item = realm.copyFromRealm(result)
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

    fun getAllByField(fieldName: String, value: String): ArrayList<T> {
        val items: ArrayList<T> = ArrayList()
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val results = realm.where(getInterfaceClass()).equalTo(fieldName, value).findAll()
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

    fun getByIds(ids: List<String>): List<T> {
        var item: List<T> = ArrayList()
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val result = realm.where(getInterfaceClass()).`in`("Id", ids.toTypedArray()).findAll()
            if(result != null){
                item = realm.copyFromRealm(result)
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

//    open fun convertJSONObject(item: T): JSONObject? {
//        var result: JSONObject? = null
//        try {
//            if(item != null){
//                val gson = Utils.gson()
//                val resultString = gson.toJson(item)
//                result = JSONObject(resultString)
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, e.message)
//        }
//        return result
//    }

    open fun getByIdAsJSONObject(id: String): JSONObject? {
        var item: JSONObject? = null
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            val result = realm.where(getInterfaceClass()).equalTo("Id", id).findFirst()

            if(result != null){
                val gson = Utils.gson()
                val resultString = gson.toJson(realm.copyFromRealm(result))
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

    fun removeById(id: String) {
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm!!.refresh()
            realm.beginTransaction()
            val result =realm.where(getInterfaceClass()).equalTo("Id", id).findAll()
            result.deleteAllFromRealm()
            realm.commitTransaction()
        } catch (e: JSONException) {
            Log.e("removeById", e.message)
        } finally {
            if (realm != null && !realm.isClosed) {
                realm.close()
            }
        }
    }

    fun softDelete(id: String): Boolean {
        var result = false
        val item = getById(id)
        if (item is RealmModelAudit) {
            HistoryChangeRepository.removeItem(id)
            var realm: Realm? = null
            try {
                realm = Realm.getDefaultInstance()
                realm!!.refresh()
                realm.executeTransaction(Realm.Transaction { realm ->
                    try {
                        item.IsDeleted = true
                        item.DeletedBy = SharedConfig.getUserId()
                        item.DeletedDate = DateUtils.convertDateToString(Date())
                        realm.insertOrUpdate(item)
                        result = true
                        semaphore.acquire()
                            HistoryChangeRepository.add(id, getInterfaceClass().simpleName, "DELETE", realm)
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
        return result
    }

    fun <T>  addOrUpdate  (item: T, sync: Boolean = true) : Boolean where T : RealmModel, T: RealmModelSync {
        var result = false
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm!!.refresh()
            realm.executeTransaction(Realm.Transaction { realm ->
                try {
                    realm.insertOrUpdate(item)
                    result = true
                    semaphore.acquire()
                    if (sync && !HistoryChangeRepository.existisItem(item.Id)) {
                        HistoryChangeRepository.add(item.Id, getInterfaceClass().simpleName, "INSERT", realm)
                    } else {
                    }
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
        return result
    }

    fun download(){
        val url = Constant.urlVRMServer + apiMethodDownload.replace("&user&", SharedConfig.getUserId())
        AndroidNetworking.get(url)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Authorization","Bearer " + SharedConfig.getToken())
                .addHeaders("UserApp",SharedConfig.getUserId())
                .build()
                .getAsJSONArray(object : JSONArrayRequestListener{
                    override fun onResponse(response: JSONArray?) {
                        var realm: Realm? = null
                        try {
                            if (response != null) {
                                val items = response
                                realm = Realm.getDefaultInstance()
                                realm!!.refresh()
                                realm.executeTransactionAsync(Realm.Transaction { realm ->
                                    try {
                                        // copiar los datos a la base de datos
                                        realm.createOrUpdateAllFromJson(getInterfaceClass(), items)

                                        // obtener los ids de los datos a insertar
                                        val ids = ArrayList<String>()
                                        for (i in 0 until items.length()) {
                                            val ob =  items.getJSONObject(i)
                                            ids.add(ob.getString("Id"))
                                        }

                                        //obtener los datos que existen actualmente
                                        val listCurrentData: ArrayList<T> = ArrayList()
                                        try {
                                            val results = realm.where(getInterfaceClass()).findAll()
                                            if(results != null){
                                                listCurrentData.addAll(realm.copyFromRealm(results))
                                            }
                                        } catch (e: Exception) {
                                            Log.e(TAG, e.message)
                                        }

                                        // recorrer los datos existentes y verificar que este en la lista descargada
                                        listCurrentData.forEach {
                                            if (it is RealmModelSync) {
                                                // si el objeto no se encuentra en la lista descargada
                                                // verificamos que no este pendiente de subida
                                                if (!ids.contains(it.Id)) {
                                                    var hasPending = false
                                                    try {
                                                        hasPending = realm.where(HistoyChange::class.java).equalTo("ModelId", it.Id).findAll().size > 0
                                                    } catch (e: Exception) {
                                                        Log.e(TAG, e.message)
                                                    }
                                                    // si el objeto no esta pendiente de subida
                                                    // se pone el objeto en estado deleted (IdDeleted = true)
                                                    if(!hasPending) {
                                                        val result = realm.where(getInterfaceClass()).equalTo("Id", it.Id).findFirst()
                                                        if (result != null) {
                                                            val item = realm.copyFromRealm(result)
                                                            if (item is RealmModelAudit) {

                                                                // eliminarlo
                                                                val toDelete =realm.where(getInterfaceClass()).equalTo("Id",  it.Id).findAll()
                                                                toDelete.deleteAllFromRealm()

                                                                // softdelete
//                                                                try {
//                                                                    item.IsDeleted = true
//                                                                    item.DeletedBy = SharedConfig.getUserId()
//                                                                    item.DeletedDate = DateUtils.convertDateToString(Date())
//                                                                    realm.insertOrUpdate(item)
//                                                                } catch (e: JSONException) {
//                                                                    Log.e(TAG, e.message)
//                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } catch (e: JSONException) {
                                        Log.e(TAG, e.message)
                                    }
                                }, Realm.Transaction.OnSuccess {
                                    EventBus.getDefault().post(Event("download_finish"))
                                }, Realm.Transaction.OnError { error ->
                                    EventBus.getDefault().post(Event("download_finish", error.message))
                                    Log.e(TAG, error.message)
                                })
                            } else {
                                EventBus.getDefault().post(Event("download_finish", "Respuesta nula"))
                                Log.e(TAG,"Respuesta nula")
                            }
                        } catch (e: Exception) {
                            EventBus.getDefault().post(Event("download_finish", e.message))
                            Log.e(TAG, e.message)
                        } finally {
                            if (realm != null && !realm.isClosed) {
                                realm.close()
                            }
                        }
                    }

                    override fun onError(anError: ANError?) {
                        EventBus.getDefault().post(Event("download_finish", "Error al conectarse con el servidor"))
                        var messageError = Utils.getMessageError(anError!!)
                        messageError = if(messageError.isEmpty()) "Error al conectar con el servidor $url" else messageError
                        Log.e(TAG, messageError)
                    }
                })
    }

    fun downloadById(id: String){
        val url = Constant.urlVRMServer + apiMethodDownloadById.replace("&id&", id).replace("&user&", SharedConfig.getUserId())
        AndroidNetworking.get(url)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Authorization","Bearer " + SharedConfig.getToken())
                .addHeaders("UserApp",SharedConfig.getUserId())
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        var realm: Realm? = null
                        try {
                            if (response.getInt("ResultCode") == 1) {
                                val item = response.getJSONObject("Result")
                                realm = Realm.getDefaultInstance()
                                realm!!.refresh()
                                realm.executeTransactionAsync(Realm.Transaction { realm ->
                                    try {
                                        realm.createOrUpdateObjectFromJson(getInterfaceClass(), item)
                                    } catch (e: JSONException) {
                                        Log.e(TAG, e.message)
                                    }
                                }, Realm.Transaction.OnSuccess {
                                    Log.e("Descargo item", "$apiMethodDownloadById - $id")
                                    EventBus.getDefault().post(Event("download_item_finish"))
                                }, Realm.Transaction.OnError { error ->
                                    EventBus.getDefault().post(Event("download_item_finish"))
                                    Log.e(TAG, error.message)
                                })
                            } else {
                                EventBus.getDefault().post(Event("download_item_finish"))
                                Log.e(TAG, response.getString("ResultMsg"))
                            }
                        } catch (e: Exception) {
                            EventBus.getDefault().post(Event("download_item_finish"))
                            Log.e(TAG, e.message)
                        } finally {
                            if (realm != null && !realm.isClosed) {
                                realm.close()
                            }
                        }
                    }

                    override fun onError(anError: ANError) {
                        EventBus.getDefault().post(Event("download_item_finish"))
                        var messageError = Utils.getMessageError(anError)
                        messageError = if(messageError.isEmpty()) "Error al conectar con el servidor $url" else messageError
                        Log.e(TAG, messageError)
                    }
                })
    }

    fun getByParentId(parentId: String): ArrayList<com.horus.vrmmobile.Models.Event> {
        val items: ArrayList<com.horus.vrmmobile.Models.Event> = ArrayList()
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val results = realm.where(com.horus.vrmmobile.Models.Event::class.java).equalTo("ParentId", parentId).findAll()
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


    fun uploadById(item: JSONObject, id: String, action: String){
        val url = Constant.urlVRMServer + apiMethodUpload.replace("&id&", id).replace("&action&", action)
        AndroidNetworking.post(url)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Authorization","Bearer " + SharedConfig.getToken())
                .addJSONObjectBody(item)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        try {
                            if (response.getInt("ResultCode") == 1) {
                                HistoryChangeRepository.removeItem(id)
                                EventBus.getDefault().post(Event("upload_finish"))
                            } else {
                                EventBus.getDefault().post(Event("upload_finish"))
                                Log.e(TAG, response.getString("ResultMsg"))
                            }
                        } catch (e: Exception) {
                            EventBus.getDefault().post(Event("upload_finish"))
                            Log.e(TAG, e.message)
                        }
                    }

                    override fun onError(anError: ANError) {
                        EventBus.getDefault().post(Event("upload_finish"))
                        var messageError = Utils.getMessageError(anError)
                        messageError = if(messageError.isEmpty()) "Error al conectar con el servidor $url" else messageError
                        Log.e(TAG, messageError)
                    }
                })
    }
}


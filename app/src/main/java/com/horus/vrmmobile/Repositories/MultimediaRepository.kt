package com.horus.vrmmobile.Repositories

import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.horus.vrmmobile.Config.Constant
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.Multimedia
import com.horus.vrmmobile.Utils.Event
import com.horus.vrmmobile.Utils.Task
import com.horus.vrmmobile.services.SyncService
import io.realm.Realm
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.io.File

/**
 * Created by mgarzon on 24/04/2019.
 */
open class MultimediaRepository private constructor(): RepositoryInterface<Multimedia>(Multimedia::class.java) {

    private object Holder { val INSTANCE = MultimediaRepository() }
    companion object {
        val instance: MultimediaRepository by lazy { Holder.INSTANCE }
    }

    fun getByPath(pathLocal: String?): Multimedia? {
        var item: Multimedia? = null
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val result = realm.where(getInterfaceClass()).equalTo("PathLocal", pathLocal).findFirst()
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

    fun getPending(): ArrayList<Multimedia> {
        val items: ArrayList<Multimedia> = ArrayList()
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val results = realm.where(getInterfaceClass()).isEmpty("Path").findAll()
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

    fun uploadPending () {
        val multimedias = getPending()
        multimedias.forEach { m -> upload(m.Id, File(m.PathLocal), m.Id, SharedConfig.getPoliticalFrontId() ) }
        SyncService.push()
    }

    fun upload(id: String, file: File, fileName: String, politicalFrontId: String) {
        var url = Constant.urlVRMServer + "api/Multimedias/Upload"
        AndroidNetworking.upload(url)
                .addMultipartFile("File", file)
                .addMultipartParameter("FileName", fileName)
                .addMultipartParameter("PoliticalFrontId", politicalFrontId)
                .setTag("uploadMultimedia")
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener { bytesUploaded, totalBytes ->
                    // do anything with progress
                }
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        try {
                            if(response.getString("FullUrl").isNotEmpty()){
                                val m = MultimediaRepository.instance.getById(id)
                                m!!.Path = response.getString("FullUrl")
                                MultimediaRepository.instance.addOrUpdate(m)
                                val task = object : Task<String>() {
                                    override fun task(): String {
                                        SyncService.synchronizing.set(false)
                                        return ""
                                    }
                                }
                                SyncService.push(task)
                            }else {
                                EventBus.getDefault().post(Event("did_not_upload"))
                                MultimediaRepository.instance.softDelete(id)
                            }
                        } catch (e: Exception) {
                            EventBus.getDefault().post(Event("did_not_upload"))
                            MultimediaRepository.instance.softDelete(id)
                            Log.e("setValues", e.message)
                        }
                    }

                    override fun onError(error: ANError) {
                        // handle error
                        Log.e("url", error.errorDetail)
                    }
                })
    }
}
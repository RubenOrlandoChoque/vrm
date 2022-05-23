package com.horus.vrmmobile.Repositories

import android.util.Log
import com.horus.vrmmobile.Models.Dtos.PartakerDtoUpload
import com.horus.vrmmobile.Models.Partaker
import com.horus.vrmmobile.Utils.Utils
import io.realm.Realm
import org.json.JSONObject

/**
 * Created by USUARIO 004 on 27/8/2018.
 */
open class PartakerRepository private constructor() : RepositoryInterface<Partaker>(Partaker::class.java) {

    private object Holder {
        val INSTANCE = PartakerRepository()
    }

    companion object {
        val instance: PartakerRepository by lazy { Holder.INSTANCE }
    }

    override fun getByIdAsJSONObject(id: String): JSONObject? {
        var item: JSONObject? = null
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            val result = realm.where(getInterfaceClass()).equalTo("Id", id).findFirst()
            if (result != null) {
                val gson = Utils.gson()
                val dto = PartakerDtoUpload(realm.copyFromRealm(result))
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


    fun getByActionId(actionId: String): ArrayList<Partaker> {
        val items: ArrayList<Partaker> = ArrayList()
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val results = realm.where(Partaker::class.java).equalTo("ActionId", actionId).findAll()
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


    fun getByEventIdAndPersonId(eventId: String, personId: String): Partaker? {
        var item: Partaker? = null
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val result = realm.where(Partaker::class.java).equalTo("ActionId", eventId).and().equalTo("PersonId", personId).findFirst()
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
}
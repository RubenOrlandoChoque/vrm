package com.horus.vrmmobile.Repositories

import android.util.Log
import com.horus.vrmmobile.Models.Dtos.MessageMultimediaDtoUpload
import com.horus.vrmmobile.Models.MessageMultimedias
import com.horus.vrmmobile.Utils.Utils
import io.realm.Realm
import org.json.JSONObject
/**
 * Created by mgarzon on 24/04/2019.
 */
open class MessageMultimediaRepository private constructor() : RepositoryInterface<MessageMultimedias>(MessageMultimedias::class.java) {

    private object Holder {
        val INSTANCE = MessageMultimediaRepository()
    }

    companion object {
        val instance: MessageMultimediaRepository by lazy { Holder.INSTANCE }
    }

    override fun getByIdAsJSONObject(id: String): JSONObject? {
        var item: JSONObject? = null
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            val result = realm.where(getInterfaceClass()).equalTo("Id", id).findFirst()

            if(result != null){
                val gson = Utils.gson()
                val dto = MessageMultimediaDtoUpload(realm.copyFromRealm(result))
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
}
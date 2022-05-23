package com.horus.vrmmobile.Repositories

import android.util.Log
import com.horus.vrmmobile.Models.Dtos.EventDtoUpload
import com.horus.vrmmobile.Models.Event
import com.horus.vrmmobile.Utils.Utils
import io.realm.Realm
import org.json.JSONObject


/**
 * Created by USUARIO 004 on 27/8/2018.
 */
open class EventRepository private constructor() : RepositoryInterface<Event>(Event::class.java) {

    private object Holder {
        val INSTANCE = EventRepository()
    }

    companion object {
        val instance: EventRepository by lazy { Holder.INSTANCE }
    }

    fun getCompleteById(id: String): Event? {
        var item: Event? = null
        try {
            item = super.getById(id)
            if (item != null) {
                item.Notes.clear()
                item.Notes.addAll(NoteRepository.instance.getByActionId(item.Id))

                item.Instances.clear()
                item.Instances.addAll(EventRepository.instance.getByParentId(item.Id))

                item.Partakers.clear()
                item.Partakers.addAll(PartakerRepository.instance.getByActionId(item.Id))
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }
        return item
    }
    
    override fun getByIdAsJSONObject(id: String): JSONObject? {
        var item: JSONObject? = null
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            val result = realm.where(getInterfaceClass()).equalTo("Id", id).findFirst()

            if(result != null){
                val gson = Utils.gson()
                val dto = EventDtoUpload(realm.copyFromRealm(result))
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
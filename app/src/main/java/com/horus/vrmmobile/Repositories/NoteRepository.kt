package com.horus.vrmmobile.Repositories

import android.util.Log
import com.horus.vrmmobile.Models.Multimedia
import com.horus.vrmmobile.Models.Note
import io.realm.Realm


/**
 * Created by USUARIO 004 on 27/8/2018.
 */
open class NoteRepository private constructor(): RepositoryInterface<Note>(Note::class.java) {

    private object Holder { val INSTANCE = NoteRepository() }
    companion object {
        val instance: NoteRepository by lazy { Holder.INSTANCE }
    }

    fun getByActionId(actionId: String): ArrayList<Note> {
        val items: ArrayList<Note> = ArrayList()
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val results = realm.where(Note::class.java).equalTo("ActionId", actionId).and().equalTo("IsDeleted", false).findAll()
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

    fun getByMultimediaId(multimediaId: String?): Note? {
        var item: Note? = null
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val result = realm.where(getInterfaceClass()).equalTo("MultimediaId", multimediaId).findFirst()
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
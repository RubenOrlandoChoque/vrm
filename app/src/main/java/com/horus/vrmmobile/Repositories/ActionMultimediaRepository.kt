package com.horus.vrmmobile.Repositories

import android.util.Log
import com.horus.vrmmobile.Models.ActionMultimedia
import io.realm.Realm

/**
 * Created by mgarzon on 24/04/2019.
 */
open class ActionMultimediaRepository private constructor(): RepositoryInterface<ActionMultimedia>(ActionMultimedia::class.java) {

    private object Holder { val INSTANCE = ActionMultimediaRepository() }
    companion object {
        val instance: ActionMultimediaRepository by lazy { Holder.INSTANCE }
    }

    fun getByActionId(actionId: String): ArrayList<ActionMultimedia> {
        val items: ArrayList<ActionMultimedia> = ArrayList()
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val results = realm.where(ActionMultimedia::class.java).equalTo("ActionId", actionId).and().equalTo("IsDeleted", false).findAll()
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

    fun getAllByActionIdAndMultimediaId(actionId: String, multimediaId: String): ArrayList<ActionMultimedia> {
        val items: ArrayList<ActionMultimedia> = ArrayList()
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val results = realm.where(getInterfaceClass()).equalTo("ActionId", actionId).and().equalTo("MultimediaId", multimediaId).findAll()
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
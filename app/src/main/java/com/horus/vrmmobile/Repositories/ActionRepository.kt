package com.horus.vrmmobile.Repositories

import android.util.Log
import com.horus.vrmmobile.Models.Action
import io.realm.Realm

/**
 * Created by smorales on 03/06/2019.
 */
open class ActionRepository private constructor() : RepositoryInterface<Action>(Action::class.java) {

    private object Holder {
        val INSTANCE = ActionRepository()
    }

    companion object {
        val instance: ActionRepository by lazy { Holder.INSTANCE }
    }

    fun getByEventId(eventId: String): ArrayList<Action> {
        val items: ArrayList<Action> = ArrayList()
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()

            val eventZone = EventZoneRepository.instance.getAll().filter { it.EventId.equals(eventId) }.distinct().firstOrNull()
            if (eventZone != null) {
                val results = realm.where(Action::class.java).equalTo("EventZonesId", eventZone.Id).and().equalTo("IsDeleted", false).findAll()
                if (results != null) {
                    items.addAll(realm.copyFromRealm(results))
                }
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

    fun getByEventZonesId(eventZonesId: String): ArrayList<Action> {
        val items: ArrayList<Action> = ArrayList()
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val results = realm.where(Action::class.java).equalTo("EventZonesId", eventZonesId).and().equalTo("IsDeleted", false).findAll()
            if (results != null) {
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

    fun getCompleteById(id: String): Action? {
        var item: Action? = null
        try {
            item = super.getById(id)
            if (item != null) {
                item.Notes.clear()
                item.Notes.addAll(NoteRepository.instance.getByActionId(item.Id))

                item.Partakers.clear()
                item.Partakers.addAll(PartakerRepository.instance.getByActionId(item.Id))
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }
        return item
    }
}
package com.horus.vrmmobile.Repositories
import android.util.Log
import com.horus.vrmmobile.Models.Zone
import io.realm.Realm

/**
 * Created by mgarzon on 13/05/2019.
 */
open class ZoneRepository private constructor(): RepositoryInterface<Zone>(Zone::class.java) {

    private object Holder { val INSTANCE = ZoneRepository() }
    companion object {
        val instance: ZoneRepository by lazy { Holder.INSTANCE }
    }

    fun getBeginWith(id: String): ArrayList<Zone> ? {
        val items: ArrayList<Zone> = ArrayList()
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val result = realm.where(Zone::class.java).beginsWith("FullId", id).findAll()
            if (result != null) {
                items.addAll(realm.copyFromRealm(result))
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
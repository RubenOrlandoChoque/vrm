package com.horus.vrmmobile.Repositories
import android.util.Log
import com.horus.vrmmobile.Models.ZonePoliticalFront
import io.realm.Realm

/**
 * Created by mgarzon on 13/05/2019.
 */
open class ZonePoliticalFrontRepository private constructor(): RepositoryInterface<ZonePoliticalFront>(ZonePoliticalFront::class.java) {

    private object Holder { val INSTANCE = ZonePoliticalFrontRepository() }
    companion object {
        val instance: ZonePoliticalFrontRepository by lazy { Holder.INSTANCE }
    }

    fun getCompleteById(id: String): ZonePoliticalFront? {
        var item: ZonePoliticalFront? = null
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val result = realm.where(ZonePoliticalFront::class.java).equalTo("IsDeleted", false).equalTo("Id", id).findFirst()
            if (result != null) {
                item = realm.copyFromRealm(result)
                if(!item!!.ZoneId.isNullOrEmpty()){
                    item.Zone = ZoneRepository.instance.getById(item.ZoneId)
                }
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
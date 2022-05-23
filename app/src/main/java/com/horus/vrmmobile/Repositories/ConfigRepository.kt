package com.horus.vrmmobile.Repositories

import android.util.Log
import com.horus.vrmmobile.Models.Config
import io.realm.Realm

/**
 * Created by fsoria on 03/06/2019.
 */
open class ConfigRepository private constructor() : RepositoryInterface<Config>(Config::class.java) {

    private object Holder {
        val INSTANCE = ConfigRepository()
    }

    companion object {
        val instance: ConfigRepository by lazy { Holder.INSTANCE }
    }

    fun getConfig(): Config? {
        var item: Config? = null
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val result = realm.where(getInterfaceClass()).findFirst()
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
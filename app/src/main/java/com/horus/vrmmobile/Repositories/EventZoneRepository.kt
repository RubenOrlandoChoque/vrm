package com.horus.vrmmobile.Repositories

import android.util.Log
import com.horus.vrmmobile.Models.Dtos.PartakerDtoUpload
import com.horus.vrmmobile.Models.EventZone
import com.horus.vrmmobile.Models.Partaker
import com.horus.vrmmobile.Utils.Utils
import io.realm.Realm
import org.json.JSONObject

/**
 * Created by smorales on 03/06/2019.
 */
open class EventZoneRepository private constructor() : RepositoryInterface<EventZone>(EventZone::class.java) {

    private object Holder {
        val INSTANCE = EventZoneRepository()
    }

    companion object {
        val instance: EventZoneRepository by lazy { Holder.INSTANCE }
    }
}
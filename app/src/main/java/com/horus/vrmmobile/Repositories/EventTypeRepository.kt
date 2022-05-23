package com.horus.vrmmobile.Repositories

import com.horus.vrmmobile.Models.EventType


/**
 * Created by USUARIO 004 on 27/8/2018.
 */
open class EventTypeRepository private constructor(): RepositoryInterface<EventType>(EventType::class.java) {

    private object Holder { val INSTANCE = EventTypeRepository() }
    companion object {
        val instance: EventTypeRepository by lazy { Holder.INSTANCE }
    }
}
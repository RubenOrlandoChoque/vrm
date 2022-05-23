package com.horus.vrmmobile.Repositories
import com.horus.vrmmobile.Models.EventState


/**
 * Created by USUARIO 004 on 27/8/2018.
 */
open class EventStateRepository private constructor(): RepositoryInterface<EventState>(EventState::class.java) {

    private object Holder { val INSTANCE = EventStateRepository() }
    companion object {
        val instance: EventStateRepository by lazy { Holder.INSTANCE }
    }
}
package com.horus.vrmmobile.Repositories

import com.horus.vrmmobile.Models.Track

/**
 * Created by smorales on 03/06/2019.
 */
open class TrackRepository private constructor() : RepositoryInterface<Track>(Track::class.java) {

    private object Holder {
        val INSTANCE = TrackRepository()
    }

    companion object {
        val instance: TrackRepository by lazy { Holder.INSTANCE }
    }
}
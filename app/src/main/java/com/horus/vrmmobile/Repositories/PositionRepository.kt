package com.horus.vrmmobile.Repositories
import com.horus.vrmmobile.Models.Position

/**
 * Created by USUARIO 004 on 27/8/2018.
 */
open class PositionRepository private constructor(): RepositoryInterface<Position>(Position::class.java) {

    private object Holder { val INSTANCE = PositionRepository() }
    companion object {
        val instance: PositionRepository by lazy { Holder.INSTANCE }
    }
}
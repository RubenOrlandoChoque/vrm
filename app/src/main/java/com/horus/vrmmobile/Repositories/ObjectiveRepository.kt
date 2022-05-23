package com.horus.vrmmobile.Repositories
import com.horus.vrmmobile.Models.Objective


/**
 * Created by smorales on 13/06/2019.
 */
open class ObjectiveRepository private constructor(): RepositoryInterface<Objective>(Objective::class.java) {

    private object Holder { val INSTANCE = ObjectiveRepository() }
    companion object {
        val instance: ObjectiveRepository by lazy { Holder.INSTANCE }
    }
}
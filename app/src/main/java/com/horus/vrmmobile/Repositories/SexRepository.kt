package com.horus.vrmmobile.Repositories
import com.horus.vrmmobile.Models.Sex


/**
 * Created by USUARIO 004 on 27/8/2018.
 */
open class SexRepository private constructor(): RepositoryInterface<Sex>(Sex::class.java) {

    private object Holder { val INSTANCE = SexRepository() }
    companion object {
        val instance: SexRepository by lazy { Holder.INSTANCE }
    }
}
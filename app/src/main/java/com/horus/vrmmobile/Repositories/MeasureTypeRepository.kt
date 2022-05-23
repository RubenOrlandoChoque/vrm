package com.horus.vrmmobile.Repositories

import com.horus.vrmmobile.Models.MeasureType


/**
 * Created by USUARIO 004 on 27/8/2018.
 */
open class MeasureTypeRepository private constructor(): RepositoryInterface<MeasureType>(MeasureType::class.java) {

    private object Holder { val INSTANCE = MeasureTypeRepository() }
    companion object {
        val instance: MeasureTypeRepository by lazy { Holder.INSTANCE }
    }
}
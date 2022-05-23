package com.horus.vrmmobile.Repositories

import com.horus.vrmmobile.Models.PartakerType


/**
 * Created by mgarzon on 31/05/2019.
 */
open class PartakerTypeRepository private constructor(): RepositoryInterface<PartakerType>(PartakerType::class.java) {

    private object Holder { val INSTANCE = PartakerTypeRepository() }
    companion object {
        val instance: PartakerTypeRepository by lazy { Holder.INSTANCE }
    }
}
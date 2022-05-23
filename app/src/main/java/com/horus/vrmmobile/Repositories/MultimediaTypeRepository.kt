package com.horus.vrmmobile.Repositories

import com.horus.vrmmobile.Models.MultimediaType

/**
 * Created by mgarzon on 23/04/2019.
 */

open class MultimediaTypeRepository private constructor(): RepositoryInterface<MultimediaType>(MultimediaType::class.java) {

    private object Holder { val INSTANCE = MultimediaTypeRepository() }
    companion object {
        val instance: MultimediaTypeRepository by lazy { Holder.INSTANCE }
    }
}
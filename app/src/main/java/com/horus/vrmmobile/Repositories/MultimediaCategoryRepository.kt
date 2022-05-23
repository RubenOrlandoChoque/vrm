package com.horus.vrmmobile.Repositories

import com.horus.vrmmobile.Models.MultimediaCategory

/**
 * Created by mgarzon on 23/04/2019.
 */

open class MultimediaCategoryRepository private constructor(): RepositoryInterface<MultimediaCategory>(MultimediaCategory::class.java) {

    private object Holder { val INSTANCE = MultimediaCategoryRepository() }
    companion object {
        val instance: MultimediaCategoryRepository by lazy { Holder.INSTANCE }
    }
}
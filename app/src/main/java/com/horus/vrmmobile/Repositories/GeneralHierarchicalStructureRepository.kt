package com.horus.vrmmobile.Repositories

import com.horus.vrmmobile.Models.GeneralHierarchicalStructure


/**
 * Created by USUARIO 004 on 27/8/2018.
 */
open class GeneralHierarchicalStructureRepository private constructor() : RepositoryInterface<GeneralHierarchicalStructure>(GeneralHierarchicalStructure::class.java) {

    private object Holder { val INSTANCE = GeneralHierarchicalStructureRepository() }

    companion object {
        val instance: GeneralHierarchicalStructureRepository by lazy { Holder.INSTANCE }
    }
}
package com.horus.vrmmobile.Repositories

import com.horus.vrmmobile.Models.ContributionToTheObjective

/**
 * Created by smorales on 13/06/2019.
 */
open class ContributionToTheObjectiveRepository private constructor() : RepositoryInterface<ContributionToTheObjective>(ContributionToTheObjective::class.java) {

    private object Holder {
        val INSTANCE = ContributionToTheObjectiveRepository()
    }

    companion object {
        val instance: ContributionToTheObjectiveRepository by lazy { Holder.INSTANCE }
    }
}
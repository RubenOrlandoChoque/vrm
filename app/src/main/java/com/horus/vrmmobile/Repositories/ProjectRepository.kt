package com.horus.vrmmobile.Repositories

import android.util.Log
import com.horus.vrmmobile.Models.Dtos.PartakerDtoUpload
import com.horus.vrmmobile.Models.Partaker
import com.horus.vrmmobile.Models.Project
import com.horus.vrmmobile.Utils.Utils
import io.realm.Realm
import org.json.JSONObject

/**
 * Created by smorales on 03/06/2019.
 */
open class ProjectRepository private constructor() : RepositoryInterface<Project>(Project::class.java) {

    private object Holder {
        val INSTANCE = ProjectRepository()
    }

    companion object {
        val instance: ProjectRepository by lazy { Holder.INSTANCE }
    }
}
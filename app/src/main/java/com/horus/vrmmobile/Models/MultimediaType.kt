package com.horus.vrmmobile.Models

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by mgarzon on 23/04/2019.
 */
@RealmClass
open class MultimediaType: RealmModel, RealmModelSync {

    @PrimaryKey
    override var Id: String = UUID.randomUUID().toString()
    var Name: String = ""
    var Description: String? = ""


    override fun toString(): String {
        return Name
    }

    companion object {
        const val file = "75111490-4263-46f6-8827-34f1adb6c1f6"
        const val audio = "ef8a2004-0f91-4e2a-9cd7-d75fe1c4c637"
        const val photo = "bc09c30b-73fb-4e01-885d-741cd2b493e5"
        const val link = "E0D61F07-B1AF-454D-AF5C-CEBC6642CBC8"
        const val video = "b3b9daca-c55b-486b-9ba6-65af8de1ce4f"
    }
}
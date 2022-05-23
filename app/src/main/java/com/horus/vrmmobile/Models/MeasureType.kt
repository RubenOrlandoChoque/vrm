package com.horus.vrmmobile.Models

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by smorales on 13/06/2019.
 */
@RealmClass
open class MeasureType: RealmModel, RealmModelSync {

    @PrimaryKey
    override var Id: String = UUID.randomUUID().toString()
    var Name: String = ""
    var Description: String? = ""


    override fun toString(): String {
        return Name
    }
}
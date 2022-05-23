package com.horus.vrmmobile.Models

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*



/**
 * Created by mparraga on 28/8/2018.
 */
@RealmClass
open class Position: RealmModel, RealmModelSync {

    @PrimaryKey
    override var Id: String = UUID.randomUUID().toString()
    var Name: String = ""
    var Description: String? = ""
    var Abbreviation: String? = ""

    override fun toString(): String {
        return Name
    }
}
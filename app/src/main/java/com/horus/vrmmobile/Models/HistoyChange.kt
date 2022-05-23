package com.horus.vrmmobile.Models

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by mparraga on 26/9/2018.
 */
@RealmClass
open class HistoyChange: RealmModel {

    @PrimaryKey
    var Id: String = ""
    var ModelName: String = ""
    var ModelId: String = ""
    var Action: String = ""
    var RegisterDate: Date = Date()
}
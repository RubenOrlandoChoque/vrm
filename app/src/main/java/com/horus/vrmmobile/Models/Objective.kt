package com.horus.vrmmobile.Models

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by smorales on 12/06/2019.
 */
@RealmClass
open class Objective: RealmModel, RealmModelSync {

    @PrimaryKey
    override var Id: String = UUID.randomUUID().toString()
    var Quantity: Int = -1
    var Description: String = ""
    var MeasureTypeId: String? = null
    var StartDateTime: String = ""
    var FinishDateTime: String = ""
    var EventId: String = ""

}
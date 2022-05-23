package com.horus.vrmmobile.Models

import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by smorales on 03/06/2019.
 */
@RealmClass
open class Action: RealmModel, RealmModelAudit {

    @PrimaryKey
    override var Id: String = UUID.randomUUID().toString()
    var ActiontName: String? = ""
    var Description: String? = ""
    var Address: String? = ""
    var ActionStateId: String? = null
    var ActionGeometry: String? = "POINT (0.0 0.0)"
    var GeometryType: String? = null
    var AttendeesQuantity: Int = 0
    var StartDateTime: String = ""
    var FinishDateTime: String? = ""
    var EventZonesId: String? = ""

    @Ignore
    var Partakers = RealmList<Partaker>()
    @Ignore
    var Notes = RealmList<Note>()
    @Ignore
    var StartDate: Date? = null


    override var DeletedBy: String? = ""
    override var DeletedDate: String?  = ""
    override var IsDeleted: Boolean = false
    override var RegisterBy: String? = ""
    override var RegisterDate: String = ""
    override var UpdatedBy: String? = ""
    override var UpdatedDate: String? = ""
}
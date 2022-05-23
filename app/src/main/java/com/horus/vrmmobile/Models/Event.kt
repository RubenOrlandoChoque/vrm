package com.horus.vrmmobile.Models

import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by mparraga on 28/8/2018.
 */
@RealmClass
open class Event: RealmModel, RealmModelAudit {

    @PrimaryKey
    override var Id: String = UUID.randomUUID().toString()
    var EventTypeId: String = ""
    var EventStateId: String = ""
    var Address: String? = ""
    var EventName: String = ""
    var Description: String? = ""
    var EventGeometry: String? = "POINT (0.0 0.0)"
    var AttendeesQuantity: Int = 0
    var StartDateTime: String = ""
    var FinishDateTime: String? = ""
    var ZonePoliticalFrontId: String? = null
    var Organizers = RealmList<Partaker>()
    var Speakers = RealmList<Partaker>()
    var Partakers = RealmList<Partaker>()
    var Notes = RealmList<Note>()
    var Instances = RealmList<Event>()
    var ParentId: String? = null
    var IsTracked: Boolean = false
    var ProjectId: String = ""
    var IsObjectiveAutomatic: Boolean = false

    override var DeletedBy: String? = ""
    override var DeletedDate: String?  = ""
    override var IsDeleted: Boolean = false
    override var RegisterBy: String? = ""
    override var RegisterDate: String = ""
    override var UpdatedBy: String? = ""
    override var UpdatedDate: String? = ""
}
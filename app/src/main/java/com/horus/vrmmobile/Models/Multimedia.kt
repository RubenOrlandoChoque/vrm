package com.horus.vrmmobile.Models

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by mparraga on 28/8/2018.
 */
@RealmClass
open class Multimedia: RealmModel, RealmModelAudit {

    @PrimaryKey
    override var Id: String = UUID.randomUUID().toString()
    var MultimediaTypeId: String = ""
    var MultimediaCategoryId: String = ""
    var Duration: Int = 0
    var Path: String = ""
    var PathLocal: String = ""
    var Gps_X: Double = 0.0
    var Gps_Y: Double = 0.0

    override var DeletedBy: String? = ""
    override var DeletedDate: String?  = ""
    override var IsDeleted: Boolean = false
    override var RegisterBy: String? = ""
    override var RegisterDate: String = ""
    override var UpdatedBy: String? = ""
    override var UpdatedDate: String? = ""
}
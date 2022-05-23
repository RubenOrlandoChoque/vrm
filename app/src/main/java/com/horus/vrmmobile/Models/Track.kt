package com.horus.vrmmobile.Models

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by mparraga on 28/8/2018.
 */
@RealmClass
open class Track: RealmModel, RealmModelAudit {
    @PrimaryKey
    override var Id: String = UUID.randomUUID().toString()
    var LngX: Double = 0.0
    var LatY: Double = 0.0
    var Ordering: Int = 0
    var Precision: Int = 0
    var Distance: Float = 0f
    var ActionId: String = ""

    override var DeletedBy: String? = ""
    override var DeletedDate: String?  = ""
    override var IsDeleted: Boolean = false
    override var RegisterBy: String? = ""
    override var RegisterDate: String = ""
    override var UpdatedBy: String? = ""
    override var UpdatedDate: String? = ""
}
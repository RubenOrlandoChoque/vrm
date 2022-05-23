package com.horus.vrmmobile.Models

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by mparraga on 28/8/2018.
 */
@RealmClass
open class PersonHierarchicalStructure: RealmModel, RealmModelAudit {

    @PrimaryKey
    override var Id: String = UUID.randomUUID().toString()
    var PersonId: String = ""
    var PositionId: String? = ""
    var ZonePoliticalFrontId: String? = ""
    var ParentId: String? = null
    var FullId: String = ""
    var Person: Person? = null
    var ZonePoliticalFront: ZonePoliticalFront? = null

    override var DeletedBy: String? = ""
    override var DeletedDate: String?  = ""
    override var IsDeleted: Boolean = false
    override var RegisterBy: String? = ""
    override var RegisterDate: String = ""
    override var UpdatedBy: String? = ""
    override var UpdatedDate: String? = ""
}
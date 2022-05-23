package com.horus.vrmmobile.Models

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

@RealmClass
open class GeneralHierarchicalStructure: RealmModel, RealmModelAudit {

    @PrimaryKey
    override var Id: String = UUID.randomUUID().toString()
    var PoliticalFrontId: String = ""
    var PositionId: String = ""
    var ParentId: String? = null
    var FullId: String = ""

    override var DeletedBy: String? = ""
    override var DeletedDate: String?  = ""
    override var IsDeleted: Boolean = false
    override var RegisterBy: String? = ""
    override var RegisterDate: String = ""
    override var UpdatedBy: String? = ""
    override var UpdatedDate: String? = ""
}
package com.horus.vrmmobile.Models

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by fsoria on 03/06/2019.
 */
@RealmClass
open class Config: RealmModel, RealmModelAudit {

    @PrimaryKey
    override var Id: String = UUID.randomUUID().toString()
    var Synchronized: Boolean = false

    override var DeletedBy: String? = ""
    override var DeletedDate: String?  = ""
    override var IsDeleted: Boolean = false
    override var RegisterBy: String? = ""
    override var RegisterDate: String = ""
    override var UpdatedBy: String? = ""
    override var UpdatedDate: String? = ""
}
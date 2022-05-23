package com.horus.vrmmobile.Models

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by smorales on 05/06/2019.
 */
@RealmClass
open class ActionMultimedia: RealmModel, RealmModelAudit {

    @PrimaryKey
    override var Id: String = UUID.randomUUID().toString()
    var MultimediaId: String = ""
    var ActionId: String = ""
    var Multimedia: Multimedia? = null

    override var DeletedBy: String? = ""
    override var DeletedDate: String?  = ""
    override var IsDeleted: Boolean = false
    override var RegisterBy: String? = ""
    override var RegisterDate: String = ""
    override var UpdatedBy: String? = ""
    override var UpdatedDate: String? = ""
}
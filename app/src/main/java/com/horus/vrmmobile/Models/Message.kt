package com.horus.vrmmobile.Models

import io.realm.RealmModel
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Modified by mgarzon on 24/06/2019.
 */

@RealmClass
open class Message: RealmModel, RealmModelAudit {

    @PrimaryKey
    override var Id: String = UUID.randomUUID().toString()
    var Title: String = ""
    var Text: String = ""
    var Sender: String = ""
    var SenderId: String = ""
    var Destination: String = ""
    var SendDate: String = ""
    var IsRead: Boolean = false
    var NotificationType: String = ""
    var CompleteDownload: Boolean = false
    var Color: Int = -1
    var HasAttachments = false
    var FromMobile = false
    @Ignore
    var SendDateTime: Date? = null


    override var DeletedBy: String? = ""
    override var DeletedDate: String?  = ""
    override var IsDeleted: Boolean = false
    override var RegisterBy: String? = ""
    override var RegisterDate: String = ""
    override var UpdatedBy: String? = ""
    override var UpdatedDate: String? = ""

}

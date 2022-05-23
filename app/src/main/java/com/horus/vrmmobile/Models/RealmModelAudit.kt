package com.horus.vrmmobile.Models

interface RealmModelAudit : RealmModelSync {
    var DeletedBy: String?
    var DeletedDate: String?
    var IsDeleted: Boolean
    var RegisterBy: String?
    var RegisterDate: String
    var UpdatedBy: String?
    var UpdatedDate: String?
}


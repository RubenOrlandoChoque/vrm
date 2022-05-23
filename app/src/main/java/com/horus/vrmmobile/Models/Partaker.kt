package com.horus.vrmmobile.Models

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by mparraga on 28/8/2018.
 */
@RealmClass
open class Partaker: RealmModel, RealmModelAudit {

    @PrimaryKey
    override var Id: String = UUID.randomUUID().toString()
    var ActionId: String = ""
    var Position: String? = ""
    var Profession: String? = ""
    var PersonId: String? = null
    //var Person: Person? = null
    //var PersonHierarchicalStructureId: String? = null
    //var FullName: String = ""
    //var PersonHierarchicalStructure: PersonHierarchicalStructure? = null
    var PartakerTypeId: String = ""
    var FirstSurname: String = ""
    var SecondSurname: String? = ""
    var FirstName: String = ""
    var SecondName: String? = ""
    var PhoneNumber: String? = ""
    var DocumentationNumber: String = ""
    var BirthDate: String = ""
    var IdentificationTypeId: String? = null
    var SexId: String? = null
    var CountryCode: String? = ""
    var AreaCode: String? = ""
    var LocalNumberPhone: String? = ""
    var Email: String? = ""
    var Address: String? = ""

    override var DeletedBy: String? = ""
    override var DeletedDate: String?  = ""
    override var IsDeleted: Boolean = false
    override var RegisterBy: String? = ""
    override var RegisterDate: String = ""
    override var UpdatedBy: String? = ""
    override var UpdatedDate: String? = ""

    fun separateNames(){
        val names = FirstName.trim().split(" ", limit = 2)
        if(names.size > 1){
            FirstName = names[0].trim()
            SecondName = names[1].trim()
        }

        val surnames = FirstSurname.trim().split(" ", limit = 2)
        if(surnames.size > 1){
            FirstSurname = surnames[0].trim()
            SecondSurname = surnames[1].trim()
        }
    }
}
package com.horus.vrmmobile.Models.Dtos

import com.horus.vrmmobile.Models.Partaker
import java.util.*

/**
 * Created by mparraga on 28/8/2018.
 */
open class PartakerDtoUpload(r: Partaker) {

    var Id: String = ""
    var ActionId: String = ""
    var Position: String? = ""
    var Profession: String? = ""
    //var PersonId: String? = null
    //var PersonHierarchicalStructureId: String? = null
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
    var CountryCode: String? = null
    var AreaCode: String? = null
    var LocalNumberPhone: String? = null
    var Email: String?=""
    var Address: String?=""

    init {
        Id = r.Id
        ActionId = r.ActionId
        Position = r.Position
        Profession = r.Profession
        //PersonId = r.PersonId
        //PersonHierarchicalStructureId = r.PersonHierarchicalStructureId
        PartakerTypeId = r.PartakerTypeId
        FirstSurname = r.FirstSurname
        SecondSurname = r.SecondSurname
        FirstName = r.FirstName
        SecondName = r.SecondName
        PhoneNumber = r.PhoneNumber
        DocumentationNumber = r.DocumentationNumber
        BirthDate = r.BirthDate
        IdentificationTypeId = r.IdentificationTypeId
        SexId = r.SexId
        CountryCode = r.CountryCode
        AreaCode = r.AreaCode
        LocalNumberPhone = r.LocalNumberPhone
        Email = r.Email
        Address = r.Address
    }
}
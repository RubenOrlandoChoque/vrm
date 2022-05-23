package com.horus.vrmmobile.Models.Dtos
import com.horus.vrmmobile.Models.Person

/**
 * Created by mparraga on 28/8/2018.
 */
class PersonDto(person: Person, position: String) {

    var Id: String = ""
    var FirstSurname: String = ""
    var FirstName: String = ""
    var PhoneNumber: String? = ""
    var DocumentationNumber: String = ""
    var BirthDate: String = ""
    var PersonTypeId: String? = ""
    var SexId: String? = null
    var Position: String = ""

    init {
        this.Id = person.Id
        this.FirstSurname = person.FirstSurname
        this.FirstName = person.FirstName
        this.PhoneNumber = person.PhoneNumber
        this.DocumentationNumber = person.DocumentationNumber
        this.BirthDate = person.BirthDate
        this.PersonTypeId = person.PersonTypeId
        this.SexId = person.SexId
        this.Position = position
    }

    override fun toString(): String {
        return "$FirstName $FirstSurname"
    }
}
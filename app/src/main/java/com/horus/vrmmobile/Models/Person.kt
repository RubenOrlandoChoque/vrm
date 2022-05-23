package com.horus.vrmmobile.Models

import android.util.Log
import com.horus.vrmmobile.Utils.DateUtils
import com.horus.vrmmobile.Utils.UniversalCode
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.lang.Exception
import java.util.*

/**
 * Created by mparraga on 28/8/2018.
 */
@RealmClass
open class Person(params: String = ""): RealmModel, RealmModelAudit {

    @PrimaryKey
    override var Id: String = UUID.randomUUID().toString()
    var FirstSurname: String = ""
    var SecondSurname: String? = ""
    var FirstName: String = ""
    var SecondName: String? = ""
    var PhoneNumber: String? = ""
    var DocumentationNumber: String = ""
    var BirthDate: String = ""
    var IdentificationTypeId: String? = null
    var PersonTypeId: String? = null
    var MaritialStatusId: String? = null
    var ReligionId: String? = null
    var SexId: String? = null
    var GenderId: String? = null
    var NationalityId: String? = null
    var CountryCode: String? = ""
    var AreaCode: String? = ""
    var LocalNumberPhone: String? = ""

    override var DeletedBy: String? = ""
    override var DeletedDate: String?  = ""
    override var IsDeleted: Boolean = false
    override var RegisterBy: String? = ""
    override var RegisterDate: String = ""
    override var UpdatedBy: String? = ""
    override var UpdatedDate: String? = ""

    init{
        if(!params.isEmpty()){
            try {
                if(params.split("@").size > 12) {
                    val values = params.split("@")
                    FirstName = values[5].trim()
                    FirstSurname = values[4].trim()
                    BirthDate = DateUtils.convertToShortFormatBase(values[7]).trim()
                    SexId = if (values[8] == "M") "SEX_MASCULINO" else "SEX_FEMENINO"
                    DocumentationNumber = values[1].trim()
                    separateNames()
                    generateId()
                }else if (params.split("@").size > 7){
                    val values = params.split("@")
                    FirstName = values[2].trim()
                    FirstSurname = values[1].trim()
                    BirthDate = DateUtils.convertToShortFormatBase(values[6]).trim()
                    SexId = if (values[3] == "M") "SEX_MASCULINO" else "SEX_FEMENINO"
                    DocumentationNumber = values[4].trim()
                    separateNames()
                    generateId()
                }
                IdentificationTypeId = "IT_DNI"
            } catch (e: Exception) {
                Log.e("Person init", e.message)
            }
        }
    }

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

    fun generateId(){
        separateNames()
//        Id = UniversalCode.calcularCodPersona(
//                FirstName,
//                if(SecondName.isNullOrEmpty()) "" else SecondName!!,
//                FirstSurname,
//                if(SecondSurname.isNullOrEmpty()) "" else SecondSurname!!,
//                BirthDate)
    }

    override fun toString(): String {
        val secName = if(SecondName.isNullOrEmpty()) "" else " ${SecondName}"
        val secSurname = if(SecondSurname.isNullOrEmpty()) "" else " ${SecondSurname}"
        return "$FirstName$secName $FirstSurname$secSurname"
    }
}
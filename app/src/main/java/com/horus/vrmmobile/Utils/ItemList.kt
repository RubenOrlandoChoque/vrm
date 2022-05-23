package com.horus.vrmmobile.Utils

import java.util.*

open class ItemList {

    var Id: String = ""
    var Name: String = ""
    var Description: String? = ""
    var IdAux: String = ""
    var PoliticalFrontId: String = ""
    var StartDate: String = ""
    var FinishDate: String = ""
    var StartDateTime: Date? = null

    override fun toString(): String {
        return Name
    }
}
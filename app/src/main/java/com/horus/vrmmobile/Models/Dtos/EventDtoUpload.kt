package com.horus.vrmmobile.Models.Dtos

import com.horus.vrmmobile.Models.Event

/**
 * Created by mparraga on 28/8/2018.
 */
open class EventDtoUpload(r: Event) {
    var Id: String = ""
    var EventTypeId: String = ""
    var EventStateId: String = ""
    var Address: String? = ""
    var EventName: String = ""
    var Description: String? = ""
    var EventGeometry: String? = "POINT (0.0 0.0)"
    var AttendeesQuantity: Int = 0
    var StartDateTime: String = ""
    var FinishDateTime: String? = ""
    var ZonePoliticalFrontId: String? = null
    var ParentId: String? = null

    init {
        Id = r.Id
        EventTypeId = r.EventTypeId
        EventStateId = r.EventStateId
        Address = r.Address
        EventName = r.EventName
        Description = r.Description
        EventGeometry = r.EventGeometry
        AttendeesQuantity = r.AttendeesQuantity
        StartDateTime = r.StartDateTime
        FinishDateTime = r.FinishDateTime
        ZonePoliticalFrontId = r.ZonePoliticalFrontId
        ParentId = r.ParentId
    }
}
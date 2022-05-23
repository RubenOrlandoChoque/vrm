package com.horus.vrmmobile.Models.Dtos

import com.horus.vrmmobile.Models.Action
import com.horus.vrmmobile.Models.Event

/**
 * Created by mparraga on 28/8/2018.
 */
open class EventDto(event: Event? = null, eventDate: String = "", eventDateFormat: String = ""){
    var Id: String = ""
    var EventDate: String = ""
    var EventDateFormat: String = ""
    var Description: String = ""
    var EventName: String = ""
    var Address: String? = ""
    var EventGeometry: String? = ""
    var AttendeesQuantity: Int = 0
    var StartDateTime: String = ""
    var FinishDateTime: String = ""
    var EventTypeId: String = ""
    var EventStateId: String = ""
    var ProjectId: String = ""
    var Actions: List<Action> = ArrayList()
    var IsTracked: Boolean = false
    var Instances = ArrayList<EventDto>()

    init {
        if (event != null){
            this.Id = event.Id
            this.EventTypeId = event.EventTypeId
            this.EventStateId = event.EventStateId
            this.Address = event.Address
            this.EventGeometry = event.EventGeometry
            this.EventName = event.EventName
            this.Description = if(event.Description == null) "" else event.Description!!
            this.StartDateTime = event.StartDateTime
            this.FinishDateTime = if(event.FinishDateTime == null) "" else event.FinishDateTime!!
            this.EventDate = eventDate
            this.EventDateFormat = eventDateFormat
            this.ProjectId = event.ProjectId
            this.IsTracked = event.IsTracked

            event.Instances.forEach { t ->
                val intance = EventDto()
                intance.Id = t.Id
                intance.EventTypeId = t.EventTypeId
                intance.EventStateId = t.EventStateId
                intance.Address = t.Address
                intance.EventGeometry = t.EventGeometry
                intance.EventName = t.EventName
                intance.Description = if(t.Description == null) "" else t.Description!!
                intance.StartDateTime = t.StartDateTime
                intance.FinishDateTime = if(t.FinishDateTime == null) "" else t.FinishDateTime!!
                intance.EventDate = eventDate
                intance.EventDateFormat = eventDateFormat

                this.Instances.add(intance)
            }
        }
    }
}
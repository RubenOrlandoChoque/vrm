package com.horus.vrmmobile.Models.Dtos

import com.horus.vrmmobile.Models.Message

open class MessageDtoUpload(message: Message? = null) {
    var Id = ""
    var PersonId = ""
    var FromMobile = true
    var Title = ""
    var MessageText = ""
    var PriorityTypeId = ""
    init {
        if(message != null) {
            Id = message.Id
            PersonId = message.SenderId
            Title = message.Title
            MessageText = message.Text
            PriorityTypeId = "HIGH"
        }
    }
}
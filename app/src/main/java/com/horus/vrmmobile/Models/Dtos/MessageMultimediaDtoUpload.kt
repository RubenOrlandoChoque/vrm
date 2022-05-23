package com.horus.vrmmobile.Models.Dtos

import com.horus.vrmmobile.Models.MessageMultimedias

open class MessageMultimediaDtoUpload(messageMultimedia: MessageMultimedias? = null) {
    var Id = ""
    var MultimediaId = ""
    var MessageId = ""

    init {
        if (messageMultimedia != null) {
            Id = messageMultimedia.Id
            MultimediaId = messageMultimedia.MultimediaId
            MessageId = messageMultimedia.MessageId
        }
    }
}
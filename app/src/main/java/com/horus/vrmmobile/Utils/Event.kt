package com.horus.vrmmobile.Utils

/**
 * Created by USUARIO 004 on 27/8/2018.
 */
class Event (eventName: String, data: String? = null){
    var eventName: String = ""
    var data: String? = null

    init{
        this.eventName = eventName
        this.data = data
    }
}
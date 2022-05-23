package com.horus.vrmmobile.Models

import java.util.UUID

/**
 * Created by USUARIO 004 on 23/3/2018.
 */

class NotificationData {
    var id = UUID.randomUUID().toString()
    var title: String = ""
    var body: String = ""
    var extra: String = ""
    var origin: String = ""
}

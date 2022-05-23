package com.horus.vrmmobile.Config

/**
 * Created by USUARIO 004 on 27/8/2018.3.72:808
 */
object Constant {
//    var urlVRMServer: String = "http://192.168.3.68:8082/" //local
    var urlVRMServer: String = "https://apivrm-staging.azurewebsites.net/" // staging
//    var urlVRMServer: String = "https://apivrm.azurewebsidtes.net/"// production
    var urlMultimediaServer: String = "http://apisurvey-apisurveyquality.azurewebsites.net/"
    var secretKey: String = "8C7F8B3E2D94C386E31A511E619A34C8F9149649F21AE73B99BE73FEBC"
    var urlSurveyApi : String = "http://apisurvey-apisurveyquality.azurewebsites.net/"

//    sincornizacion de datacollector
    var urlSyncDataCollector : String = "http://apisurvey-apisurveyquality.azurewebsites.net/" // calidad
//    var urlSyncDataCollector : String = "http://apisurvey.azurewebsites.net/" // produccion
    val DataCollectorCodUser = "VVVR430609KD0"
}
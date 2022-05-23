package com.horus.vrmmobile.events

import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.horus.vrmmobile.Activities.PhoneActivity
import com.horus.vrmmobile.Config.Constant
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Utils.Utils
import org.json.JSONObject


/**
 * Created by dmendez on 28/3/2019.
 */
open class EventFcmToken private constructor() {

    private object Holder { val INSTANCE = EventFcmToken() }
    companion object {
        val instance: EventFcmToken by lazy { Holder.INSTANCE }
        const val TAG: String = "EventFcmT"
    }
    //Tengo un token para enviar al servidor entonces intento mandarlo
    //Fix me  el server Todavia no devuelve el formato correcto.
    fun sendRegistrationFCMToken(fcmToken:String) {
        //Reviso si se puede mandar ahora el fcmtoken
        if  (SharedConfig.getUserId().isBlank()
                || SharedConfig.getToken().isBlank()){
            return
        }
        val apiMethodUpload = "api/messages/users/&userid&/fcmTokens/"
        val   body =  JSONObject()
        body.put("FcmToken",fcmToken)

        val url = Constant.urlVRMServer + apiMethodUpload
                .replace("&userid&", SharedConfig.getUserId())
        AndroidNetworking.post(url)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Authorization","Bearer " + SharedConfig.getToken())
                .addJSONObjectBody(body)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        try {
                            Log.i(TAG,"RESPONDE EL SERVER")
                        } catch (e: Exception) {
                            Log.e(TAG, e.message)
                        }
                    }

                    override fun onError(anError: ANError) {
                        var messageError = Utils.getMessageError(anError)
                        messageError = if(messageError.isEmpty()) "Error al conectar con el servidor $url" else messageError

                        Log.e(TAG, messageError)
                    }
                })

    }


}
package com.horus.vrmmobile.services

/**
 * Created by USUARIO 004 on 16/3/2018.
 */

import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.horus.vrmmobile.Config.Constant
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Utils.Utils
import com.horus.vrmmobile.events.EventFcmToken
import org.json.JSONObject


class FirebaseInstanceIDService : FirebaseInstanceIdService() {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    override fun onTokenRefresh() {
        // Get updated InstanceID token.
        val refreshedToken = FirebaseInstanceId.getInstance().token
        Log.e(TAG, "Refreshed token: " + refreshedToken!!)
        SharedConfig.setFcmToken(refreshedToken)
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        sendRegistrationToServer(refreshedToken)
    }
    // [END refresh_token]


    //[START sendFcmToken]
    /**
     * Persist fcmtoken en el servidor
     * de esta manera le van a poder enviar mensajes desde la web.
     *
     * @param token The new fcm token.
     */
      fun  sendRegistrationToServer(token: String) {
        EventFcmToken.instance.sendRegistrationFCMToken(token)

    }//[END SendFcmToken]

    companion object {
        private const val TAG = "FirebaseInstService"

    }
    }




package com.horus.vrmmobile

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
//import android.util.Log
//import cafe.adriel.androidaudioconverter.AndroidAudioConverter
//import cafe.adriel.androidaudioconverter.callback.ILoadCallback
import com.androidnetworking.AndroidNetworking
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.horus.vrmmobile.Helpers.CommonHelper
import com.horus.vrmmobile.Models.migrations.Migration
import com.horus.vrmmobile.services.NetworkChangeReceiver
import com.horus.vrmmobile.services.SyncServiceTimer
import com.uphyca.stetho_realm.RealmInspectorModulesProvider
import io.realm.Realm
import io.realm.RealmConfiguration
import okhttp3.OkHttpClient
import mpi.dc.clases.DataCollector
import org.acra.ACRA
import org.acra.annotation.ReportsCrashes
import org.acra.sender.HttpSender

/**
 * Created by USUARIO 004 on 27/8/2018.
 */
@ReportsCrashes(formKey = "", httpMethod = HttpSender.Method.PUT, reportType = HttpSender.Type.JSON)
class VRMApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        VRMApplication.context = this
        ACRA.init(this)
        ACRA.getErrorReporter().checkReportsOnApplicationStart()
        Realm.init(this)
        val config = RealmConfiguration.Builder()
                .schemaVersion(1)
                .migration(Migration())
                .deleteRealmIfMigrationNeeded()
                .build()
        Realm.setDefaultConfiguration(config)
        Realm.getInstance(config)
        DataCollector.init(context)
        val realmInspector = RealmInspectorModulesProvider.builder(this)
                .withDeleteIfMigrationNeeded(true)
                .withLimit(10000)
                .build()
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(realmInspector)
                        .build())

        AndroidNetworking.initialize(applicationContext)

        //interceptar trafico de red usando OkHttpClient. Desactivar en produccion, da error con la subida de imagenes. En produccion usar AndroidNetworking.initialize(applicationContext)}
        /*val client = OkHttpClient().newBuilder()
            .addNetworkInterceptor(StethoInterceptor())
            .build()
        AndroidNetworking.initialize(applicationContext, client)*/

        val intentFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(NetworkChangeReceiver(), intentFilter)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getSystemService(NotificationManager::class.java)
            } else {
                null
            }
            notificationManager!!.createNotificationChannel(NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW))
        }

        initializeFirebase()
        FirebaseMessaging.getInstance()

//        AndroidAudioConverter.load(this, object: ILoadCallback {
//            override fun onSuccess() { Log.e("success", "succes") }
//            override fun onFailure(error: Exception ) { Log.e("error", "error") }
//        })
    }

    private fun initializeFirebase() {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context, FirebaseOptions.fromResource(context))
        }
        val config = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        config.setConfigSettings(configSettings)
    }

    companion object {
        var context: Context? = null
        fun getAppContext(): Context? {
            return context
        }

        fun init(context: Context) {
            this.context = context
        }
    }
}
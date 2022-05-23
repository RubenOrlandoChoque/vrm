package com.horus.vrmmobile.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.GpsStatus
import android.location.GpsStatus.GPS_EVENT_STARTED
import android.location.GpsStatus.GPS_EVENT_STOPPED
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.Track
import com.horus.vrmmobile.Utils.Task
import com.horus.vrmmobile.Utils.Utils
import java.text.ParseException
import java.util.*
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.github.bkhezry.mapdrawingtools.model.DrawingOption
import com.google.android.gms.maps.model.LatLng
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.ActionRepository
import com.horus.vrmmobile.Repositories.TrackRepository
import com.horus.vrmmobile.Utils.GeometryUtils


class TrackingService : Service() {

    var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    var mLocationRequest: LocationRequest? = null
    var mLocationCallback: LocationCallback? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        try {
            actionId = intent.extras!!.get("actionId") as String
        } catch (ex: Exception) {
            Log.e(TAG, ex.message)
        }

        lastPointValid = null
        SharedConfig.setLastTrack(lastPointValid)
        initializeTracking(this)
        if(SharedConfig.isTracking()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                @SuppressLint("WrongConstant") val notificationChannel = NotificationChannel(ANDROID_CHANNEL_ID,
                        ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
                notificationChannel.enableLights(false)
                notificationChannel.enableVibration(false)
                notificationChannel.lightColor = Color.GREEN
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

                val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(notificationChannel)

                val builder = Notification.Builder(this, ANDROID_CHANNEL_ID)
                        .setContentTitle("VRM")
                        .setContentText("Tracking...")
                        .setAutoCancel(true)
                val notification = builder.build()
                startForeground(NOTIFICATION_ID, notification)
            } else {
                val builder = NotificationCompat.Builder(this)
                        .setContentTitle("VRM")
                        .setContentText("Tracking...")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                val notification = builder.build()
                startForeground(NOTIFICATION_ID, notification)
            }
        }
        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        SharedConfig.setLastLocation(SharedConfig.getLastLocationTmp())
        SharedConfig.setLastLocationTmp(HashSet())

        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.removeGpsStatusListener(gpsStatusListener)

        if (mLocationCallback != null) {
            mFusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback)
        }
        mFusedLocationProviderClient = null
        mLocationRequest = null
        mLocationCallback = null
//        stopForeground(true)

        super.onDestroy()
    }

    private val gpsStatusListener = GpsStatus.Listener { event ->
        when (event) {
            GPS_EVENT_STARTED -> {
            }
            GPS_EVENT_STOPPED -> {
                lastPointValid = null
                SharedConfig.setLastTrack(lastPointValid)
//                    val i = Intent(this@TrackingService, DialogActivity::class.java)
//                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    startActivity(i)
            }
        }
    }

    private fun createNewLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation

                if (location != null && location.latitude != 0.0 && location.longitude != 0.0) {
//                    if(SharedConfig.isTracking()) {
                        if (SharedConfig.getLastLocation().contains(location.latitude.toString()) &&
                                SharedConfig.getLastLocation().contains(location.longitude.toString())) {
                            return
                        } else {
                            SharedConfig.setLastLocation(HashSet())
                        }
                        val lastLocations = HashSet<String>()
                        lastLocations.add(location.latitude.toString())
                        lastLocations.add(location.longitude.toString())
                        SharedConfig.setLastLocationTmp(lastLocations)

                        val track = Track()
                        track.LngX = location.longitude
                        track.LatY = location.latitude
                        track.Precision = location.accuracy.toInt()
                        track.ActionId = actionId
                        if (location.hasSpeed()) {
//                        track.Speed = location.speed
                        }

                        addTraking(track)
//                    } else {
//                        val track = Track()
//                        track.LngX = location.longitude
//                        track.LatY = location.latitude
//                        track.Precision = location.accuracy.toInt()
//                        track.ActionId = actionId
//                        lastPointValid  = track
//                    }
                }
            }
        }
    }

    private fun addTraking(track: Track) {
        object : Task<Boolean>() {
            override fun task(): Boolean {
                var rdo = false
                try {
                    synchronized(this@TrackingService) {
                        if (lastPointValid == null) lastPointValid = SharedConfig.getLastTrack()
                        val validPoint = getLastPointValid(lastPointValid, track)
                        if (validPoint != null && (lastPointValid == null || !validPoint.Id.equals(lastPointValid!!.Id))) {
//                            track.Show = true
                            track.Distance = validPoint.Distance
                            track.Ordering = if (lastPointValid == null) 0 else (lastPointValid!!.Ordering + 1)
                        }
                        if(SharedConfig.isTracking()) {
                            TrackRepository.instance.addOrUpdate(track)
                            if (validPoint?.Ordering == 0) {
                                val a = ActionRepository.instance.getById(actionId)
                                if (a != null) {
                                    val pos = LatLng(validPoint.LatY, validPoint.LngX)
                                    a.ActionGeometry = GeometryUtils.convertListToWKT(arrayListOf(pos), DrawingOption.DrawingType.POINT)
                                    ActionRepository.instance.addOrUpdate(a)
                                }
                            }
                        } else {
                            val a = ActionRepository.instance.getById(actionId)
                            if (validPoint != null && a != null && a.ActionGeometry == "POINT (0.0 0.0)") {
                                val pos = LatLng(validPoint.LatY, validPoint.LngX)
                                a.ActionGeometry = GeometryUtils.convertListToWKT(arrayListOf(pos), DrawingOption.DrawingType.POINT)
                                ActionRepository.instance.addOrUpdate(a)
                            }
                        }
                        if (validPoint != null) {
                            lastPointValid = validPoint
                            SharedConfig.setLastTrack(lastPointValid)
                        }
                    }

                    if (Utils.isNetworkAvailable()) {
                        object : Thread() {
                            override fun run() {
                                //SyncService.push()
                            }
                        }.run()
                    }
                    rdo = true
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
                return rdo
            }
        }.run()
    }

    fun initializeTracking(context: Context) {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        mLocationRequest = LocationRequest.create()
        mLocationRequest!!.interval = UPDATE_INTERVAL
        mLocationRequest!!.setFastestInterval(FASTEST_UPDATE_INTERVAL)
        mLocationRequest!!.setMaxWaitTime(MAX_WAIT_TIME)
        mLocationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        if (mLocationCallback != null) {
            mFusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback)
        }
        mLocationCallback = createNewLocationCallback()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return
        }
        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback, null)

        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.addGpsStatusListener(gpsStatusListener)
    }

    private fun getLastPointValid(lastPoint: Track?, newPoint: Track): Track? {
        val meterSecondMax = MAXIMUM_POSIBLE_VELOCITY_IN_METERS_BY_SECOND
        var result = lastPoint
        if (newPoint.Precision < MAXIMUM_ACCURACY_ACCEPTED) {
            if (lastPoint == null) {
                result = newPoint
            } else {
                try {
                    val distance = Utils.distance(lastPoint.LatY, lastPoint.LngX, newPoint.LatY, newPoint.LngX)
                    //if (distance > DISTANCE) {
                    //val ini = DateUtils.convertDateToStringInverse(lastPoint.RegisterDate)!!
                    //val fin = DateUtils.convertDateToStringInverse(newPoint.RegisterDate)!!
                    //val diffSecond = (fin.time - ini.time) / 1000
                    //val meterSecond = if(diffSecond == 0L) 0.0 else distance / diffSecond
                    //if (meterSecond < meterSecondMax) {
                    result = newPoint
                    result.Distance = distance.toFloat()
                    //} else {
                    //    Log.e("getLastPointValid", "$meterSecond m/seg")
                    //}
                    //}
                    //else {
                    //    result!!.RegisterDate = newPoint.RegisterDate
                    //}
                } catch (e: ParseException) {
                    Log.e("isValidPoint", e.message)
                } catch (ex: Exception) {
                    Log.e("isValidPoint", ex.message)
                }
            }
        }
        return result
    }

    companion object {

        private val TAG = TrackingService::class.java.simpleName

        /**
         * The desired interval for location updates. Inexact. Updates may be more or less frequent.
         */
        private val UPDATE_INTERVAL = (6 * 1000).toLong()

        /**
         * The fastest rate for active location updates. Updates will never be more frequent
         * than this value, but they may be less frequent.
         */
        private val FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2

        /**
         * The max time before batched results are delivered by location services. Results may be
         * delivered sooner than this interval.
         */
        private val MAX_WAIT_TIME = UPDATE_INTERVAL * 2
        private val NOTIFICATION_ID = 1
        private val ANDROID_CHANNEL_ID = "ANDROID_CHANNEL_ID_01"
        private val ANDROID_CHANNEL_NAME = "ANDROID_CHANNEL_NAME_01"
        private var actionId = ""

        var lastPointValid: Track? = null
        var DISTANCE = 30 // 30 metros
        var MAXIMUM_POSIBLE_VELOCITY_IN_METERS_BY_SECOND = 25 // 25 m/seg = 90km/h
        var MAXIMUM_ACCURACY_ACCEPTED = 100

        fun stopSericeGPS(context: Context, actionId: String) {
            val trackService = Intent(context, TrackingService::class.java)
            trackService.putExtra("actionId", actionId)
            context.stopService(trackService)
            SharedConfig.setTracking(false)
            SharedConfig.setActionIdTtracking("")
            lastPointValid = null
            SharedConfig.setLastTrack(lastPointValid)
        }

        fun finishTrackingModal(onPositiveButton: ((it: MaterialDialog) -> Unit)? = null, onNegativeButton: ((it: MaterialDialog) -> Unit)? = null, context: Context) {
            MaterialDialog(context).show {
                title(text = context.getString(R.string.title_dialog_info))
                message(text = context.getString(R.string.finish_action))
                negativeButton(R.string.cancel)
                positiveButton(R.string.accept)
                cancelOnTouchOutside(false)
                noAutoDismiss()
                positiveButton {
                    it.dismiss()
                    onPositiveButton?.invoke(it)
                }
                negativeButton {
                    it.dismiss()
                    onNegativeButton?.invoke(it)
                }
            }
        }
    }
}
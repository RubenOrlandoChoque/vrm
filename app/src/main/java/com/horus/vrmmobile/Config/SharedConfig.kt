package com.horus.vrmmobile.Config

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.horus.vrmmobile.Models.Track
import com.horus.vrmmobile.Utils.Utils
import com.horus.vrmmobile.VRMApplication
import java.lang.Exception
import java.util.HashSet

/**
 * Created by USUARIO 004 on 27/8/2018.
 */
object SharedConfig {
    private val PREFS_FILENAME = "CONFIG_PREFS"
    private val TOKEN_CODE = "TOKEN_CODE"
    private val USER_NAME = "USER_NAME"
    private val USER_CODE = "USER_CODE"
    private val USER_SURNAME = "USER_SURNAME"
    private val USER_PHOTO = "USER_PHOTO"
    private val ORG_ID = "ORG_ID"
    private val ORG_COLOR = "ORG_COLOR"
    private val VERSION_DATA = "VERSION_DATA"
    private val SURVEY_ID = "SURVEY_ID"
    private val VERSION_ID = "VERSION_ID"
    private val FORM_ID = "FORM_ID"
    private val ORGBRANCH_ID = "ORGBRANCH_ID"
    private val LAST_LOCATION = "LAST_LOCATION"
    private val LAST_LOCATION_TMP = "LAST_LOCATION_TMP"
    private val START_CIRCUIT = "START_CIRCUIT"
    private val TRACKING = "TRACKING"
    private val VERIFY_NUMBER = "VERIFY_NUMBER"
    private val DEVICE_ID = "DEVICE_ID"
    private const val FCM_TOKEN = "FCM_TOKEN"
    private val LAST_TRACK = "LAST_TRACK"
    private val ZONE_POLITICAL_FRONT = "ZONE_POLITICAL_FRONT"
    private val POLITICAL_FRONT = "POLITICAL_FRONT"
    private val PERSON_HIERARCHICAL_STRUCTURE = "PERSON_HIERARCHICAL_STRUCTURE"
    private val PHONE_LOGIN = "PHONE_LOGIN"
    private val PHONE_VERIFICATION_ID = "PHONE_VERIFICATION_ID"
    private val PHONE_VERIFIED = "PHONE_VERIFIED"
    private val ACTION_ID_TRACKING = "ACTION_ID_TRACKING"
    private val FIRST_DOWNLOAD_FINISH = "FIRST_DOWNLOAD_FINISH"
    private val SYNCHRONIZED = "SYNCHRONIZED"
    private val PERMISSIONS = "PERMISSIONS"

    private val COUNTNOTIFY = "COUNTNOTIFY"
    private val COUNTEVENT = "COUNTEVENT"
    private val PROJECTIDS = "PROJECTIDS"


    private val PROJECTNAME = "PROJECTNAME"
    private val ZONENAME = "ZONENAME"
    private val EVENTNAME = "EVENTNAME"

    private val UPLOADPHOTO = "UPLOADPHOTO"

    var appStarted = false

    fun getProjectName(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(PROJECTNAME, "")
    }

    fun setProjectName(projectName: String?) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(PROJECTNAME, projectName) //3

        editor.commit() //4
    }

    fun getZoneName(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(ZONENAME, "")
    }

    fun setZoneName(zoneName: String) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(ZONENAME, zoneName) //3

        editor.commit() //4
    }

    fun getEventName(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(EVENTNAME, "")
    }

    fun setEventName(eventName: String?) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(EVENTNAME, eventName) //3

        editor.commit() //4
    }


    fun getFcmToken(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(FCM_TOKEN, "")
    }

    fun setFcmToken(fcmToken: String) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(FCM_TOKEN, fcmToken) //3

        editor.commit() //4
    }

    fun isTracking(): Boolean {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getBoolean(TRACKING, false)
    }

    fun setTracking(lastLocations: Boolean) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putBoolean(TRACKING, lastLocations) //3

        editor.commit() //4
    }

    fun getLastLocationTmp(): Set<String> {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getStringSet(LAST_LOCATION_TMP, HashSet<String>())
    }

    fun setLastLocationTmp(lastLocations: Set<String>) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putStringSet(LAST_LOCATION_TMP, lastLocations) //3

        editor.commit() //4
    }

    fun getLastLocation(): Set<String> {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getStringSet(LAST_LOCATION, HashSet<String>())
    }

    fun setLastLocation(lastLocations: Set<String>) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putStringSet(LAST_LOCATION, lastLocations) //3

        editor.commit() //4
    }

    fun getOrganizationBranchId(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(ORGBRANCH_ID, "")
    }

    fun setOrganizationBranchId(organizationBranchId: String) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(ORGBRANCH_ID, organizationBranchId) //3

        editor.commit() //4
    }

    fun setFormnId(formId: String) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(FORM_ID, formId) //3

        editor.commit() //4
    }

    fun getFormnId(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(FORM_ID, "")
    }

    fun setVersionId(versionId: String) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(VERSION_ID, versionId) //3

        editor.commit() //4
    }

    fun getVersionId(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(VERSION_ID, "")
    }

    fun setSurveyId(surveyId: String) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(SURVEY_ID, surveyId) //3

        editor.commit() //4
    }

    fun getSurveyId(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(SURVEY_ID, "")
    }


    fun setVersionData(version: String) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(VERSION_DATA, version) //3

        editor.commit() //4
    }

    fun getVersionData(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(VERSION_DATA, "0")
    }

    fun setToken(token: String) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(TOKEN_CODE, token) //3

        editor.commit() //4
    }

    fun getToken(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(TOKEN_CODE, "")
    }

    fun setUserName(token: String) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(USER_NAME, token) //3

        editor.commit() //4
    }

    fun getUserName(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(USER_NAME, "")
    }

    fun setUserId(userId: String) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(USER_CODE, userId) //3

        editor.commit() //4
    }

    fun getUserId(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(USER_CODE, "")
    }

    fun setZonePoliticalFrontId(zonePoliticalFronId: String) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(ZONE_POLITICAL_FRONT, zonePoliticalFronId) //3

        editor.commit() //4
    }

    fun getZonePoliticalFrontId(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(ZONE_POLITICAL_FRONT, "")
    }

    fun setPersonHierarchicalStructureId(personHierarchicalStructureId: String) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(PERSON_HIERARCHICAL_STRUCTURE, personHierarchicalStructureId) //3

        editor.commit() //4
    }

    fun getPersonHierarchicalStructureId(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(PERSON_HIERARCHICAL_STRUCTURE, "")
    }

    fun setUserSurname(token: String) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(USER_SURNAME, token) //3

        editor.commit() //4
    }

    fun getUserSurname(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(USER_SURNAME, "")
    }

    fun setOrganizationColor(organizationColor: String) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(ORG_COLOR, organizationColor) //3

        editor.commit() //4
    }

    fun getOrganizationColor(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(ORG_COLOR, "")
    }

    fun setOrganizationId(organizationId: String) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(ORG_ID, organizationId) //3

        editor.commit() //4
    }

    fun getOrganizationId(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(ORG_ID, "")
    }

    fun setUserPhoto(token: String) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(USER_PHOTO, token) //3

        editor.commit() //4
    }

    fun getUserPhoto(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(USER_PHOTO, "")
    }

    fun clear() {
        SharedConfig.setToken("")
        SharedConfig.setUserId("")
        SharedConfig.setUserName("")
        SharedConfig.setUserPhoto("")
        SharedConfig.setUserSurname("")
        SharedConfig.setOrganizationId("")
        SharedConfig.setOrganizationColor("")
        SharedConfig.setZoneName("")
        SharedConfig.setProjectName("")
        SharedConfig.setEventName("")
    }

    fun setVerifyNumber(tiene: Boolean) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor
        editor = settings.edit()
        editor.putBoolean(VERIFY_NUMBER, tiene)
        editor.commit()
    }

    fun setLastTrack(lastTrack: Track?) {
        var lastTrackString = ""
        if (lastTrack != null) {
            try {
                lastTrackString = Utils.gson().toJson(lastTrack)
            } catch (e: Exception) {
                Log.e("setLastTrack", e.message)
            }
        }

        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(LAST_TRACK, lastTrackString) //3

        editor.commit() //4
    }

    fun getLastTrack(): Track? {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        val lastTrackString = settings.getString(LAST_TRACK, "")
        var lastTrack: Track? = null
        if (!lastTrackString.isEmpty()) {
            try {
                lastTrack = Utils.gson().fromJson<Track>(lastTrackString, Track::class.java)
            } catch (e: Exception) {
                Log.e("getLastTrack", e.message)
            }
        }
        return lastTrack
    }

    fun isPhoneVerified(): Boolean {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getBoolean(PHONE_VERIFIED, false)
    }

    fun setPhoneVerified(lastLocations: Boolean) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putBoolean(PHONE_VERIFIED, lastLocations) //3

        editor.commit() //4
    }

    fun setPhoneLogin(organizationId: String) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(PHONE_LOGIN, organizationId) //3

        editor.commit() //4
    }

    fun getPhoneLogin(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(PHONE_LOGIN, "")
    }

    fun setPhoneVerificationId(organizationId: String) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(PHONE_VERIFICATION_ID, organizationId) //3

        editor.commit() //4
    }

    fun getPhoneVerificationId(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(PHONE_VERIFICATION_ID, "")
    }

    fun setPoliticalFrontId(politicalFronId: String) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(POLITICAL_FRONT, politicalFronId) //3

        editor.commit() //4
    }

    fun getPoliticalFrontId(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(POLITICAL_FRONT, "")
    }

    fun setActionIdTtracking(versionId: String) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putString(ACTION_ID_TRACKING, versionId) //3

        editor.commit() //4
    }

    fun getActionIdTtracking(): String {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getString(ACTION_ID_TRACKING, "")
    }

    fun setFirstDownloadFinish(finish: Boolean) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putBoolean(FIRST_DOWNLOAD_FINISH, finish) //3

        editor.commit() //4
    }

    fun isFirstDownloadFinish(): Boolean {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getBoolean(FIRST_DOWNLOAD_FINISH, false)
    }

    fun setSynchronized(synchronized: Boolean) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putBoolean(SYNCHRONIZED, synchronized) //3

        editor.commit() //4
    }

    fun setPermissions(permissions: Set<String>) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putStringSet(PERMISSIONS, permissions) //3

        editor.commit() //4
    }

    fun getPermissions(): Set<String> {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getStringSet(PERMISSIONS, HashSet<String>())
    }

    fun getCountNotify(): Int {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getInt(COUNTNOTIFY, 0)
    }

    fun setCountNotify(countNotify: Int) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2
        editor.putInt(COUNTNOTIFY, countNotify) //3
        editor.commit() //4
    }

    fun getCountEvent(): Int {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getInt(COUNTEVENT, 0)
    }

    fun setCountEvent(countEvent: Int) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2
        editor.putInt(COUNTEVENT, countEvent) //3
        editor.commit() //4
    }


    fun getProjectId(): MutableSet<String> {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getStringSet(PROJECTIDS, HashSet<String>())
    }

    fun setProjectId(projectIds: MutableSet<String>) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2
        editor.putStringSet(PROJECTIDS, projectIds) //3
        editor.commit() //4
    }

    fun getUploadPhoto(): Boolean {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        return settings.getBoolean(UPLOADPHOTO, false)
    }

    fun setUploadPhoto(uploadPhoto: Boolean) {
        val settings: SharedPreferences = VRMApplication.getAppContext()!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE) //1
        val editor: SharedPreferences.Editor = settings.edit() //2

        editor.putBoolean(UPLOADPHOTO, uploadPhoto) //3

        editor.commit() //4
    }
}
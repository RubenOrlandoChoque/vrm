package com.horus.vrmmobile.Utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.afollestad.materialdialogs.MaterialDialog
import com.androidnetworking.error.ANError
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.horus.vrmmobile.VRMApplication
import io.realm.Realm
import io.realm.RealmObject
import mpi.dc.clases.Util
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

object Utils {

    private var gson: Gson? = null

    private fun getGsonParser(): Gson? {
        if (null == gson) {
            val builder = GsonBuilder()
            gson = builder.create()
        }
        return gson
    }

    fun convertObjectToString(item: Any): String{
        var result = ""
        try {
            result = Utils.getGsonParser()!!.toJson(item)
        }catch (e: Exception){
            Log.e("convertObjectToString", e.message)
        }
        return result
    }

    fun <T>convertStringToObject(itemString: String, clazz: Class<T>): T?{
        var result: T? = null
        try {
            result = Utils.getGsonParser()!!.fromJson(itemString, getInterfaceClass(clazz))
        }catch (e: Exception){
            Log.e("convertObjectToString", e.message)
        }
        return result
    }

    fun <T> getInterfaceClass(clazz: Class<T>): Class<T> {
        return clazz
    }


    fun copyDatabaseToSD() {
        val root = File(Environment.getExternalStorageDirectory(), "SCAN")
        if (!root.exists()) {
            root.mkdirs()
        }

        val file = "SCAN/default.realm"
        try {
            val sd = Environment.getExternalStorageDirectory()
            if (sd.canWrite()) {
                val currentDB = File(Realm.getDefaultInstance()!!.configuration.path)
                val backupDB = File(sd, file)
                if (currentDB.exists()) {
                    val src = FileInputStream(currentDB).channel
                    val dst = FileOutputStream(backupDB).channel
                    dst.transferFrom(src, 0, src.size())
                    src.close()
                    dst.close()
                }
            }
        } catch (e: Exception) {
            Log.e("copyDatabaseToSD", e.message)
        }
    }

    fun getMessageError(anError: ANError): String{
        if(!anError.message.isNullOrEmpty()){
            return anError.message.toString()
        }
        if(!anError.errorBody.isNullOrEmpty()){
            return anError.errorBody.toString()
        }
        return ""
    }

    fun getSweetAlert(context: Context, type: Int, title: String, message: String): SweetAlertDialog{
        val disabledBtnDialog = SweetAlertDialog(context, type)
            .setTitleText(title).setContentText(message)
        //disabledBtnDialog!!.setOnShowListener { (disabledBtnDialog.getButton(SweetAlertDialog.BUTTON_CONFIRM).parent as View).visibility = View.GONE }
        disabledBtnDialog.setCancelable(false)
        return disabledBtnDialog
    }

    fun convertToBundle(map: Map<String, String>): Bundle {
        val bundle = Bundle()
        for (key in map.keys) {
            bundle.putString(Utils.getCapialLetter(key), map[key])
        }
        return bundle
    }

    fun getCapialLetter(letters: String): String {
        return letters.substring(0, 1).toUpperCase() + letters.substring(1)
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = VRMApplication.context!!.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }

    fun convertJSONArrayToList(jsonArray: JSONArray): List<JSONObject>{
        val listdata = ArrayList<JSONObject>()
        for (i in 0 until jsonArray.length()) {
            listdata.add(jsonArray.getJSONObject(i))
        }
        return listdata
    }

    fun gson(): Gson {
        return GsonBuilder()
                .setExclusionStrategies(object : ExclusionStrategy {
                    override fun shouldSkipField(f: FieldAttributes): Boolean {
                        return f.declaringClass == RealmObject::class.java
                    }

                    override fun shouldSkipClass(clazz: Class<*>): Boolean {
                        return false
                    }
                })
                .create()
    }

    fun getNumColumns(windowManager: WindowManager, maxWidth: Int): Int{
        val width = getWidthScreen(windowManager)
        val res: Int = width/maxWidth
        return if (res == 0)  1 else res
    }

    private fun getWidthScreen(windowManager: WindowManager): Int{
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        return displayMetrics.widthPixels
    }

    fun getMaxWidth(windowManager: WindowManager, dips: Int): Int{
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return (dips * displayMetrics.density + 0.5f).toInt()
    }

    fun getWidthColumn(windowManager: WindowManager, numColumns: Int, padding: Int = 0): Int {
        val width = getWidthScreen(windowManager) - padding
        return width / numColumns
    }

    fun convertDpToPx(context: Context, dp: Int): Int {
        return Math.round(dp * (context.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))

    }

    fun convertPxToDp(px: Int): Int {
        return Math.round(px / (Resources.getSystem().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val theta = lon1 - lon2
        var dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist = dist * 60.0 * 1.1515 //Millas
        dist = dist * 1.609344 //Kilometros
        dist = dist * 1000 //Metros
        //        if (unit == "K") {
        //            dist = dist * 1.609344;
        //        } else if (unit == "N") {
        //            dist = dist * 0.8684;
        //        }
        return dist
    }

    fun showMaterialDialog(activity: Activity, title: String, content: String, positiveText: String) {
        val materialDialog = MaterialDialog(activity)
                .title(text = title)
                .message(text = content)
                .positiveButton(text = positiveText)
//        val materialDialog = materialDialogBuilder.build()
//        materialDialog.titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 19f)
//        materialDialog.contentView!!.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17f)
        materialDialog.show()
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180 / Math.PI
    }

    fun getDirectoryMultimedia() : String {
        var directoryMultimedia : String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/vrm/"
        var dir = File(directoryMultimedia)
        if(!dir.exists()){
            dir.mkdir()
        }
        return directoryMultimedia
    }

    fun getPixels(unit: Int, size: Float): Float {
        val metrics = Resources.getSystem().displayMetrics
        return TypedValue.applyDimension(unit, size, metrics)
    }

    fun sp2px(sizeInSp: Float): Float {
        return getPixels(TypedValue.COMPLEX_UNIT_SP, sizeInSp)
    }

    fun dp2px(sizeInDp: Int): Float {
        return dp2px(sizeInDp.toFloat())
    }

    fun dp2px(sizeInDp: Float): Float {
        return getPixels(TypedValue.COMPLEX_UNIT_DIP, sizeInDp)
    }

    fun hideSoftKeyboard(view: View?, context: Context) {
        val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (view != null) inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showSoftKeyboard(view: View?) {
        if (view == null) return
        try {
            val inputManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        } catch (e: Exception) {
            Log.e("Utils", "can't show keyboard ", e)
        }

    }

    // convert from bitmap to byte array
    fun getBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
        return stream.toByteArray()
    }

    // convert from byte array to bitmap
    fun getImage(image: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(image, 0, image.size)
    }

    fun isDateSame(c1: Calendar, c2: Calendar): Boolean {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
                c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH)
    }

    fun isDateSame(d1: Date, d2: Date): Boolean {
        val c1 = dateToCalendar(d1)
        val c2 = dateToCalendar(d2)
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
                c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH)
    }

    fun dateToCalendar(date: Date): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar

    }

    /**
     * chooses a random color from array.xml
     */
    fun getRandomMaterialColor(typeColor: String, context: Context): Int {
        var returnColor = Color.GRAY
        val arrayId = context.resources.getIdentifier("mdcolor_$typeColor", "array", context.packageName)

        if (arrayId != 0) {
            val colors = context.resources.obtainTypedArray(arrayId)
            val index = (Math.random() * colors.length()).toInt()
            returnColor = colors.getColor(index, Color.GRAY)
            colors.recycle()
        }
        return returnColor
    }

    fun getDeviceId(): String {
        return Settings.Secure.getString(VRMApplication.getAppContext()?.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun generateTextFileOnSD(folder: String, fileName: String, body: String): String {
        var path = ""
        try {
            val root = File(Environment.getExternalStorageDirectory(), folder)
            if (!root.exists()) {
                root.mkdirs()
            }
            val file = File(root, fileName)
            val writer = FileWriter(file)
            writer.append(body)
            writer.flush()
            writer.close()
            path = file.path
        } catch (e: IOException) {
            Util.log(e)
            e.printStackTrace()
        }
        return path
    }
}
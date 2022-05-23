package com.horus.vrmmobile.Utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

/**
 * Created by USUARIO 004 on 5/9/2018.
 */
object DateUtils {
    @JvmStatic
    fun toSimpleString(date: Date) : String {
        var result = ""
        try {
            val format = SimpleDateFormat("dd/MM/yyy")
            result = format.format(date)
        }catch (e: Exception){
            Log.e("toCompleteString", e.message)
        }
        return result
    }

    @JvmStatic
    fun toCompleteString(date: Date) : String {
        var result = ""
        try {
            val format = SimpleDateFormat("dd/MM/yyy HH:mm")
            format.timeZone = TimeZone.getDefault()
            result = format.format(date)
        }catch (e: Exception){
            Log.e("toCompleteString", e.message)
        }
        return result
    }

    @JvmStatic
    fun toSimpleString(date: String) : String {
        var result = ""
        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
            val dateNew = simpleDateFormat.parse(date)
            val format = SimpleDateFormat("dd/MM/yyy")
            result = format.format(dateNew)
        }catch (e: Exception){
            Log.e("toCompleteString", e.message)
        }
        return result
    }

    @JvmStatic
    fun converSimpleToBaseFormat(date: String) : String {
        var result = ""
        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val format = SimpleDateFormat("dd/MM/yyy")
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
            val dateNew = format.parse(date)
            result = simpleDateFormat.format(dateNew)
        }catch (e: Exception){
            Log.e("toCompleteString", e.message)
        }
        return result
    }

    @JvmStatic
    fun toCompleteString(date: String) : String {
        var result = ""
        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
            val dateNew = simpleDateFormat.parse(date)
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm")
            result = format.format(dateNew)
        }catch (e: Exception){
            Log.e("toCompleteString", e.message)
        }

        if(result.isEmpty()){
            try {
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
                val dateNew = simpleDateFormat.parse(date)
                val format = SimpleDateFormat("dd/MM/yyyy HH:mm")
                result = format.format(dateNew)
            }catch (e: Exception){
                Log.e("toCompleteString", e.message)
            }
        }
        return result
    }

    @JvmStatic
    fun toCompleteString2(date: String) : String {
        var result = ""
        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
            val dateNew = simpleDateFormat.parse(date)
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
            result = format.format(dateNew)
        }catch (e: Exception){
            Log.e("toCompleteString", e.message)
        }
        if(result.isEmpty()){
            try {
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
                val dateNew = simpleDateFormat.parse(date)
                val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                result = format.format(dateNew)
            }catch (e: Exception){
                Log.e("toCompleteString", e.message)
            }
        }
        return result
    }

    @JvmStatic
    fun toCompleteString3(date: String) : String {
        var result = ""
        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
            val dateNew = simpleDateFormat.parse(date)
            val format = SimpleDateFormat("dd/MM/yyyy")
            result = format.format(dateNew)
        }catch (e: Exception){
            Log.e("toCompleteString", e.message)
        }
        return result
    }

    @JvmStatic
    fun toCompleteInverseString(date: String) : String {
        var result = ""
        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
            val dateNew = simpleDateFormat.parse(date)
            val format = SimpleDateFormat("HH:mm dd/MM/yyy")
            result = format.format(dateNew)
        }catch (e: Exception){
            Log.e("toCompleteString", e.message)
        }
        return result
    }

    @JvmStatic
    fun convertDateShortNotUTCToString(date: Date): String {
        var result = ""
        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            result = simpleDateFormat.format(date)
        }catch (e: Exception){
            Log.e("convertDateToString", e.message)
        }
        return result
    }

    @JvmStatic
    fun convertDateShortToString(date: Date): String {
        var result = ""
        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
            result = simpleDateFormat.format(date)
        }catch (e: Exception){
            Log.e("convertDateToString", e.message)
        }
        return result
    }

    @JvmStatic
    fun convertDateShortFormatToString(date: Date): String {
        var result = ""
        try {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
            result = simpleDateFormat.format(date)
        }catch (e: Exception){
            Log.e("convertDateToString", e.message)
        }
        return result
    }

    @JvmStatic
    fun convertDateToString(date: Date): String {
        var result = ""
        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
            result = simpleDateFormat.format(date)
        }catch (e: Exception){
            Log.e("convertDateToString", e.message)
        }
        return result
    }

    @JvmStatic
    fun convertDateToString2(date: Date): String {
        var result = ""
        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
            result = simpleDateFormat.format(date)
        }catch (e: Exception){
            Log.e("convertDateToString", e.message)
        }
        return result
    }

    @JvmStatic
    fun convertStringToDate(date: String): Date? {
        var result: Date? = null
        result = convertDateToStringInverse(date)
        if (result == null) {
            result = convertDateNotMiliSecondToStringInverse(date)
        }
        return result
    }


    @JvmStatic
    fun convertDateShortToStringInverse(date: String) : Date? {
        var result: Date? = null
        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
            result = simpleDateFormat.parse(date)
        }catch (e: Exception){
            Log.e("convertDateToStringInv", e.message)
        }
        return result
    }

    @JvmStatic
    fun convertStringShortToDateInverse(date: String) : Date? {
        var result: Date? = null
        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            result = simpleDateFormat.parse(date)
        }catch (e: Exception){
            Log.e("convertDateToStringInv", e.message)
        }
        return result
    }

    @JvmStatic
    fun convertDateNotMiliSecondToStringInverse(date: String) : Date? {
        var result: Date? = null
        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
            result = simpleDateFormat.parse(date)
        }catch (e: Exception){
            Log.e("convertDateToStringInv", e.message)
        }
        return result
    }

    @JvmStatic
    fun convertDateToDayMonth(date: Date): String {
        var result = ""
        try {
            val simpleDateFormat = SimpleDateFormat("dd MMM")
            simpleDateFormat.timeZone = TimeZone.getDefault()
            result = simpleDateFormat.format(date)
        }catch (e: Exception){
            Log.e("convertDateToString", e.message)
        }
        return result
    }

    @JvmStatic
    fun convertDateToHHmm(date: Date): String {
        var result = ""
        try {
            val simpleDateFormat = SimpleDateFormat("HH:mm")
            simpleDateFormat.timeZone =  TimeZone.getDefault()
            result = simpleDateFormat.format(date)
        }catch (e: Exception){
            Log.e("convertDateToString", e.message)
        }
        return result
    }

    @JvmStatic
    fun convertDateToStringInverse(date: String) : Date? {
        var result: Date? = null
        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
            result = simpleDateFormat.parse(date)
        }catch (e: Exception){
            Log.e("convertDateToStringInv", e.message)
        }
        return result
    }

    @JvmStatic
    fun convertDateToStringInverseSimple(date: String) : String {
        var result = ""
        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
            val newDate = simpleDateFormat.parse(date)
            val format = SimpleDateFormat("yyyy-MM-dd")
            result = format.format(newDate)
        }catch (e: Exception){
            Log.e("convertDateToStringInv", e.message)
        }
        return result
    }

    @JvmStatic
    fun convertStringDateToString(date: String) : String {
        var result = ""
        result = convertDateToStringInverseSimple(date)
        if(result.isEmpty()){
            try {
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
                val newDate = simpleDateFormat.parse(date)
                val format = SimpleDateFormat("yyyy-MM-dd")
                result = format.format(newDate)
            }catch (e: Exception){
                Log.e("convertDateToStringInv", e.message)
            }
        }

        return result
    }

    @JvmStatic
    fun convertCompleteToDb(date: String) : String {
        var result = ""
        try {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
            val newDate = simpleDateFormat.parse(date)
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            format.setTimeZone(TimeZone.getTimeZone("UTC"))
            result = format.format(newDate)
        }catch (e: Exception){
            Log.e("convertDateToStringInv", e.message)
        }
        return result
    }

    @JvmStatic
    fun convertSecondToString(sec: Int): String {
        var result = ""
        try {
            var minutes: Int = sec / 60
            var second = sec - (minutes * 60)
            var minutesString = if(minutes == 0) "" else "$minutes min "
            var secondString = "$second sec"
            return "$minutesString$secondString"
        }catch (e: Exception){
            Log.e("convertDateToString", e.message)
        }
        return result
    }

    @JvmStatic
    fun convertToShortFormatSlash(date: String) : String {
        var result = ""
        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            val dateNew = simpleDateFormat.parse(date)
            val format = SimpleDateFormat("dd/MM/yyyy")
            result = format.format(dateNew)
        }catch (e: Exception){
            Log.e("toCompShortFormatSlash", e.message)
        }
        return result
    }

    @JvmStatic
    fun convertToShortFormatBase(date: String) : String {
        var result = ""
        try {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
            val dateNew = simpleDateFormat.parse(date)
            val format = SimpleDateFormat("yyyy-MM-dd")
            result = format.format(dateNew)
        }catch (e: Exception){
            Log.e("convertToShortFormatB", e.message)
        }
        return result
    }

    @JvmStatic
    fun toShortFormatSlash(date: String) : Date? {
        var result: Date? = null
        try {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
            result = simpleDateFormat.parse(date)
        }catch (e: Exception){
            Log.e("toCompShortFormatSlash", e.message)
        }
        return result
    }

    @JvmStatic
    fun toShortFormatSlashNew(date: String) : Date? {
        var result: Date? = null
        try {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
            result = simpleDateFormat.parse(date)
        }catch (e: Exception){
            Log.e("toCompShortFormatSlash", e.message)
        }
        return result
    }

    @JvmStatic
    fun convertToUniversalCode(date: String) : String {
        var result = ""
        try {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyy")
            val dateNew = simpleDateFormat.parse(date)
            val format = SimpleDateFormat("yyMMdd")
            result = format.format(dateNew)
        }catch (e: Exception){
            Log.e("convertToUniversalCode", e.message)
        }
        return result
    }

    @JvmStatic
    fun convertTraditionalDate(date: String) : Date? {
        var result: Date? = null
        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            result = simpleDateFormat.parse(date)
        }catch (e: Exception){
            Log.e("convertToUniversalCode", e.message)
        }
        return result
    }

    @JvmStatic
    fun convertCustomDateString(date: String, eventDateFormat: String) : String {
        var result = ""
        try {
            if(date.isEmpty()){
                return result
            }
            val startRange = DateUtils.toCompleteString2("${date}Z")
            if(startRange.split(" ")[0] != eventDateFormat){
                if(startRange.split(" ")[0] != eventDateFormat){
                    result = startRange.split(" ")[0]
                }
                if(!startRange.endsWith(":00")){
                    result += (if(result.isEmpty()) "" else " ") + DateUtils.toCompleteString("${date}Z").split(" ")[1]
                }
            }else{
                result += (if(result.isEmpty()) "" else " ") + DateUtils.toCompleteString("${date}Z").split(" ")[1]
            }
        }catch (e: Exception){
            Log.e("convertCustomDateString", e.message)
        }
        return result
    }

    @JvmStatic
    fun convertCustomCompleteDateString(date: String?) : String {
        var result = ""
        try {
            if(date.isNullOrEmpty()){
                return result
            }
            val startRange = DateUtils.toCompleteString2("${date}Z")
            result = startRange.split(" ")[0]
            if(!startRange.endsWith(":00")){
                result += " " + DateUtils.toCompleteString("${date}Z").split(" ")[1]
            }
        }catch (e: Exception){
            Log.e("convertCustomDateString", e.message)
        }
        return result
    }

    /**
     * return dd/MM/yyyy
     */
    fun convertCustomCompleteDateString2(date: String?) : String {
        var result = ""
        try {
            if(date.isNullOrEmpty()){
                return result
            }
            val startRange = toCompleteString2("${date}Z")
            result = startRange.split(" ")[0]
        }catch (e: Exception){
            Log.e("convertCustomDateString", e.message)
        }
        return result
    }

    @JvmStatic
    fun getCalentarCustom(day: Int? = null, month: Int? = null, year: Int? = null, hour: Int? = null, minute: Int? = null) : Calendar {
        val calendar = Calendar.getInstance()
        try {
            calendar.time = Date()
            if(day != null) calendar.set(Calendar.DAY_OF_MONTH, day)
            if(month != null) calendar.set(Calendar.MONTH, month - 1)
            if(year != null) calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.HOUR_OF_DAY, if(hour != null) hour else 0)
            calendar.set(Calendar.MINUTE, if(minute != null) minute else 0)
            calendar.set(Calendar.SECOND, 0)
        }catch (e: Exception){
            Log.e("convertToUniversalCode", e.message)
        }
        return calendar
    }

    @JvmStatic
    fun getDayIdByDayName(dateName: String) : String {
        return when(dateName){
            "MON" -> "COD_LUNES"
            "TUE" -> "COD_MARTES"
            "WED" -> "COD_MIERCOLES"
            "THU" -> "COD_JUEVES"
            "FRI" -> "COD_VIERNES"
            "SAT" -> "COD_SABADO"
            "SUN" -> "COD_DOMINGO"
            else -> ""
        }
    }

    private val SECOND_MILLIS = 1000
    private val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private val DAY_MILLIS = 24 * HOUR_MILLIS

    fun getTimeAgo(time: Long): String? {
        var time = time
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000
        }

        val now = Date().time
        if (time > now || time <= 0) {
            return null
        }

        val diff = now - time
        return if (diff < MINUTE_MILLIS) {
            "justo ahora"
        } else if (diff < 2 * MINUTE_MILLIS) {
            "hace un minuto"
        } else if (diff < 50 * MINUTE_MILLIS) {
            (diff / MINUTE_MILLIS).toString() + " minutos atras"
        } else if (diff < 90 * MINUTE_MILLIS) {
            "hace una hora"
        } else if (diff < 24 * HOUR_MILLIS) {
            (diff / HOUR_MILLIS).toString() + " horas atras"
        } else if (diff < 48 * HOUR_MILLIS) {
            "ayer"
        } else {
            (diff / DAY_MILLIS).toString() + " días atras"
        }
    }
}
package com.horus.vrmmobile.Managers

import android.util.Log
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.Dtos.EventDto
import com.horus.vrmmobile.Models.Event
import com.horus.vrmmobile.Repositories.*
import com.horus.vrmmobile.Utils.CalendarEventOverlapHelper
import com.horus.vrmmobile.Utils.DateInterval
import com.horus.vrmmobile.Utils.DateUtils
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import org.joda.time.DateTime
import org.joda.time.Interval
import android.R.attr.end




object EventManager {

    fun getByZonePoliticalFront(_zonePoliticalFrontId : String) : ArrayList<EventDto>{
        // traer la zona a la que pertence el zonepolitcalfront
        val zpf = ZonePoliticalFrontRepository.instance.getById(_zonePoliticalFrontId)
        val eventZoneList = ArrayList<EventDto>()
        if(zpf != null) {
            val z = ZoneRepository.instance.getById(zpf.ZoneId)
            if(z != null){
                // traer todas las cuyo fullid empieze con el id de la zona encontrada
                val fullId : String = if(z.FullId != null) z.FullId!! else ""
                val zones = ZoneRepository.instance.getBeginWith(fullId)
                val eventss = ArrayList<String>()
                zones?.forEach {
                    val zonePF = ZonePoliticalFrontRepository.instance.getByField(it.Id,"ZoneId" )
                    if(zonePF != null) {
                        val ez = EventZoneRepository.instance.getAllByField("ZonePoliticalFrontId", zonePF.Id)
                        val ezs = ez.map { e -> e.EventId }
                        ezs.forEach {s -> eventss.add(s!!) }
                    }
                }

                val allEventsList = EventRepository.instance.getAll()
                val singerList = ArrayList<Event>()
                singerList.addAll(allEventsList.filter { e -> eventss.contains(e.Id) })

                for (ev in singerList){
                    eventZoneList.add(EventDto(ev,"",""))
                }
            }
        }
        return eventZoneList
    }

    fun getZones(_zonePoliticalFrontId : String) : ArrayList<String>{
        // traer la zona a la que pertence el zonepolitcalfront
        val zpf = ZonePoliticalFrontRepository.instance.getById(_zonePoliticalFrontId)
        val eventZoneList = ArrayList<String>()
        if(zpf != null) {
            val z = ZoneRepository.instance.getById(zpf.ZoneId)
            if(z != null){
                // traer todas las cuyo fullid empieze con el id de la zona encontrada
                val fullId : String = if(z.FullId != null) z.FullId!! else ""
                val zones = ZoneRepository.instance.getBeginWith(fullId)
                val eventss = ArrayList<String>()
                zones?.forEach {
                    val zonePF = ZonePoliticalFrontRepository.instance.getByField(it.Id,"ZoneId" )
                    if(zonePF != null) {
                        eventZoneList.add(zonePF.Id)
                    }
                }
            }
        }
        return eventZoneList
    }

    fun getAll(dateFromString: String, dateToString: String, projectId: String): ArrayList<EventDto>{
        val allEvents = EventRepository.instance.getAll()
        val filterList = ArrayList<Event>()
        val zones = getZones(SharedConfig.getZonePoliticalFrontId())
        val eventZones = EventZoneRepository.instance.getAll().filter { zones.contains(it.ZonePoliticalFrontId) }.distinct().map { it.EventId }
        filterList.addAll(allEvents.filter { it.ParentId.isNullOrEmpty() && it.ProjectId == projectId && eventZones.contains(it.Id)})

        val singerList2 = ArrayList<Event>()
        val dateFrom = DateUtils.convertStringToDate(dateFromString)
        val dateTo = DateUtils.convertStringToDate(dateToString)
        val di1 = DateInterval(dateFrom!!, dateTo!!)
        singerList2.addAll(filterList.filter { s ->
            val startDate = DateUtils.convertStringToDate("${s.StartDateTime}Z")
            if(s.FinishDateTime.isNullOrEmpty()){
                dateFrom.time < startDate!!.time && startDate.time < dateTo.time
            }else{
                val finishDate = DateUtils.convertStringToDate("${s.FinishDateTime}Z")
                Log.e("3rror", startDate.toString())
                Log.e("3rror", finishDate.toString())
                val di2 = DateInterval(startDate!!, finishDate!!)
                val intervals = arrayListOf(di1,di2)
                val ceo = CalendarEventOverlapHelper()
                ceo.findOverlap(intervals)
            }
        })

        val eventList = ArrayList<EventDto>()
        singerList2.forEach { s ->
            try {
                var startDate = DateUtils.convertStringDateToString("${s.StartDateTime}Z")
                val calendarFrom = Calendar.getInstance()
                calendarFrom.time = DateUtils.convertDateShortToStringInverse(startDate)
                var startDateFormat = DateUtils.convertToShortFormatSlash(startDate)
                if(!s.FinishDateTime.isNullOrEmpty()){
                    val finishDate = DateUtils.convertStringDateToString("${s.FinishDateTime}Z")
                    while(startDate != finishDate){
//                        eventList.add(EventDto(s, startDate, startDateFormat))
                        calendarFrom.add(Calendar.DAY_OF_MONTH, 1)
                        startDate = DateUtils.convertDateShortToString(calendarFrom.time)
                        startDateFormat = DateUtils.convertDateShortFormatToString(calendarFrom.time)
                    }
                }
                eventList.add(EventDto(s, startDate, startDateFormat))
            }catch (e: Exception){
                Log.e("", e.message)
            }
        }

        for(oe in eventList){
            oe.Actions = ActionRepository.instance.getByEventId(oe.Id)
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val dateFromShort = dateFromString.split("T")[0]
        val dateI = dateFormat.parse(dateFromShort)
        val dateToShort = dateToString.split("T")[0]
        val dateF = dateFormat.parse(dateToShort)
        val eventList3 = ArrayList<EventDto>()
        eventList3.addAll(eventList.filter { s ->
            val sdt = s.StartDateTime.split("T")[0]
            val fdt = s.FinishDateTime.split("T")[0]
            val startDate = dateFormat.parse(sdt)
            val finishDate = dateFormat.parse(fdt)
            checkDate(startDate,finishDate, dateI, dateF)
        })
        eventList3.sortBy { s -> s.EventDate }
        return eventList3
    }

    fun checkDate(startDate: Date, endDate: Date, checkDateFrom: Date, checkDateTo: Date): Boolean {
        val intervalEvent = Interval(DateTime(startDate),
                DateTime(endDate))
        val intervalFromTo = Interval(DateTime(checkDateFrom),
                DateTime(checkDateTo))
        return !(intervalEvent.end.isBefore(intervalFromTo.start) || intervalEvent.start.isAfter(intervalFromTo.end));
    }

    fun isThereOverlap(t1: Interval, t2: Interval): Boolean {
        return !(t1.end.isBefore(t2.start) || t1.start.isAfter(t2.end))
    }

}
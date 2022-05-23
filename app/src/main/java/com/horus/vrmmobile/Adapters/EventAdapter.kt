package com.horus.vrmmobile.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.Dtos.EventDto
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Utils.DateUtils
import kotlinx.android.synthetic.main.recycler_row.view.*
import java.lang.Exception

abstract class EventAdapter(private val layoutInflater: LayoutInflater,
                   private var eventList: List<EventDto>,
                   @param:LayoutRes private val rowLayout: Int) : RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = layoutInflater.inflate(rowLayout,
                parent,
                false)
        return ViewHolder(v)
    }

    public fun setItems(items: List<EventDto>){
        eventList = items
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = eventList[position]
        var dateRange = ""
        try {
            dateRange += DateUtils.convertCustomDateString(event.StartDateTime, event.EventDateFormat)
            if(!dateRange.isEmpty()){
                val endRange = DateUtils.convertCustomDateString(event.FinishDateTime, event.EventDateFormat)
                if(!endRange.isEmpty()){
                    dateRange += " - $endRange"
                }
            }
        }catch (e: Exception){
            Log.e("EventAdapter", e.message)
        }

        holder.txtEvent.text = event.Description
        holder.txtDate.visibility = if(dateRange.isEmpty()) View.GONE else View.VISIBLE
        holder.txtDate.text = dateRange
        holder.txtCountActions.text = "Cantidad de acciones: " + event.Actions.size
//        if (event.IsTracked) {
//            holder.btnAdd.setImageResource(R.drawable.ic_play_circle_filled)
//        } else {
//            holder.btnAdd.setImageResource(R.drawable.ic_add_circle)
//        }

        holder.infoContent.setOnClickListener {
            setOnClickList(event)
        }
        holder.btnAdd.setOnClickListener {
            setOnClickAdd(event)
        }
        val found = event.Actions.firstOrNull { it.Id == SharedConfig.getActionIdTtracking() }
        if(SharedConfig.isTracking() && event.Actions.isNotEmpty() && found != null) {
            holder.rippleBackground.startRippleAnimation()
        }else{
            holder.rippleBackground.stopRippleAnimation()
        }
    }

    abstract fun setOnClickList(eventDto: EventDto)
    abstract fun setOnClickAdd(eventDto: EventDto)

    override fun getItemCount(): Int = eventList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtEvent = view.txt_event
        val txtDate = view.txt_date
        val txtCountActions = view.txt_ie_count_actions
        val infoContent = view.info_content
        val btnAdd = view.btn_ie_add
        val rippleBackground = view.rippleBackground
    }
}


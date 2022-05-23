package com.horus.vrmmobile.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.Action
import com.horus.vrmmobile.Utils.DateUtils
import com.horus.vrmmobile.services.TrackingService
import kotlinx.android.synthetic.main.fragment_info_event.view.*
import kotlinx.android.synthetic.main.item_instance.view.*


abstract class ActionsAdapter(private val layoutInflater: LayoutInflater,
                              private var itemList: ArrayList<Action>,
                              @param:LayoutRes private val rowLayout: Int,
                              val context: Context) : RecyclerView.Adapter<ActionsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = layoutInflater.inflate(rowLayout,
                parent,
                false)
        return ViewHolder(v)
    }

    fun setItems(items: ArrayList<Action>) {
        itemList = items
        notifyDataSetChanged()
    }

    fun getItems(): ArrayList<Action> {
        return itemList
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val action = itemList[position]

        holder.txtActionName.text = action.ActiontName
        holder.txtActionDate.text = DateUtils.convertCustomCompleteDateString2(action.StartDateTime) + " - " + DateUtils.convertCustomCompleteDateString2(action.FinishDateTime)
        holder.view.setOnClickListener { onSelect(action) }
        holder.btnDeleteAction.setOnClickListener { onDeleteAction(action) }
        holder.btnStopActionTracking.setOnClickListener { onStopActionTracking(action, holder) }

        if(SharedConfig.isTracking() && SharedConfig.getActionIdTtracking() == action.Id){
            holder.btnStopActionTracking.visibility = View.VISIBLE
            holder.btnDeleteAction.visibility = View.GONE
            holder.pulsator.startRippleAnimation()
        }else{
            holder.btnStopActionTracking.visibility = View.GONE
            holder.btnDeleteAction.visibility = View.VISIBLE
            holder.pulsator.stopRippleAnimation()
        }
    }

    private fun onStopActionTracking(action: Action, holder: ViewHolder) {
        TrackingService.finishTrackingModal(onPositiveButton = {
            TrackingService.stopSericeGPS(context, action.Id)
            holder.btnStopActionTracking.visibility = View.GONE
            holder.btnDeleteAction.visibility = View.VISIBLE
            holder.pulsator.stopRippleAnimation()
        }, context = context)
    }

    abstract fun onDeleteAction(action: Action)

    abstract fun onSelect(action: Action)

    override fun getItemCount(): Int = itemList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtActionName = view.txt_action_name_list
        val txtActionDate = view.txt_action_date
        val btnDeleteAction = view.btn_delete_action
        val btnStopActionTracking = view.btn_stop_action_tracking
        val pulsator = view.pulsator
        val view = view
    }
}


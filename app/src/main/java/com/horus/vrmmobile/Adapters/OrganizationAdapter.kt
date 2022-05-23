package com.horus.vrmmobile.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Models.Dtos.EventDto
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Utils.Utils
import kotlinx.android.synthetic.main.item_member.view.*
import org.jetbrains.anko.textColor

abstract class OrganizationAdapter(private val layoutInflater: LayoutInflater,
                                   private var singerList: List<EventDto>,
                                   private val context: Context,
                                   private val parent: Boolean,
                                   private var selectItem: Int = -1,
                                   @param:LayoutRes private val rowLayout: Int) : RecyclerView.Adapter<OrganizationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = layoutInflater.inflate(rowLayout,
                parent,
                false)
        return ViewHolder(v)
    }

    public fun setItems(items: List<EventDto>){
        singerList = items
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val event = singerList[position]

        holder.lineItem.visibility = if(parent) View.INVISIBLE else View.VISIBLE
        holder.imgMember.borderWidth = if(selectItem != position) 0 else Utils.convertDpToPx(context, 2)
        holder.imgMember.borderColor = if (selectItem != position) ContextCompat.getColor(context, R.color.transparent) else ContextCompat.getColor(context, R.color.violet_light)
        holder.txtOccupation.textColor = if (selectItem != position) ContextCompat.getColor(context, R.color.gray_dark) else ContextCompat.getColor(context, R.color.violet_light)
        holder.lineChild.visibility = if(selectItem != position) View.INVISIBLE else View.VISIBLE


        holder.view.setOnClickListener {
            selectItem = position
            notifyDataSetChanged()
//            holder.imgMember.borderWidth = Utils.convertDpToPx(context, 2)
//            holder.imgMember.borderColor = context.getColor(R.color.violet_light)
//            holder.txtOccupation.textColor = context.getColor(R.color.violet_light)
//            holder.lineChild.visibility = View.VISIBLE
            val locationLineChild = IntArray(2)
            holder.lineChild.getLocationOnScreen(locationLineChild)
            setOnClick(locationLineChild[1], selectItem)
        }
    }

    abstract fun setOnClick(top: Int, itemSelect: Int)

    override fun getItemCount(): Int = singerList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgMember = view.img_member
        val lineItem = view.line_item
        val txtMember = view.txt_member
        val txtOccupation = view.txt_occupation
        val lineChild = view.line_child
        val view = view
    }
}


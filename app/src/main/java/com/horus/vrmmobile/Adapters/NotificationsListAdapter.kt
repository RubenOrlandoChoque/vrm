package com.horus.vrmmobile.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Models.Message
import com.horus.vrmmobile.Utils.DateUtils
import kotlinx.android.synthetic.main.item_notification_row.view.*

abstract class NotificationsListAdapter(private val layoutInflater: LayoutInflater,
                                        private var singerList: List<Message>,
                                        @param:LayoutRes private val rowLayout: Int,
                                        @param:DrawableRes private val icon: Int,
                                        @param:DrawableRes private val color: Int) : RecyclerView.Adapter<NotificationsListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = layoutInflater.inflate(rowLayout,
                parent,
                false)
        return ViewHolder(v)
    }

    public fun setItems(items: List<Message>){
        singerList = items
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = singerList[position]
        holder.txtTitle.text = item.Title
        holder.txtSubTitleRead.text = if (item.IsRead) "Leido" else "No leido"
        var d = DateUtils.convertDateToStringInverse(item.SendDate)
        holder.txtSubTitle.text =  DateUtils.getTimeAgo(d!!.time)
        holder.img.setImageResource(icon)
        holder.view.setOnClickListener{
            setOnClick(item)
        }
        holder.viewColor.background = ContextCompat.getDrawable(layoutInflater.context,color)
    }

    abstract fun setOnClick(objItem: Message)

    override fun getItemCount(): Int = singerList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var img = view.img_in_item
        val txtTitle = view.txt_in_title
        val txtSubTitle = view.txt_in_sub_title
        val txtSubTitleRead = view.txt_in_sub_title_read
        val view = view.content_in_row
        val viewColor = view.content_in_color
    }
}
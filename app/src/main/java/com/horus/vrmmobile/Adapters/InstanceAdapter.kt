package com.horus.vrmmobile.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Models.Event
import kotlinx.android.synthetic.main.item_participant.view.*

    abstract class InstanceAdapter(private val layoutInflater: LayoutInflater,
                               private var itemList: ArrayList<Event>,
                               @param:LayoutRes private val rowLayout: Int) : RecyclerView.Adapter<InstanceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = layoutInflater.inflate(rowLayout,
                parent,
                false)
        return ViewHolder(v)
    }

    fun setItems(items: ArrayList<Event>) {
        itemList = items
        notifyDataSetChanged()
    }

    fun getItems(): ArrayList<Event> {
        return itemList
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = itemList[position]

        holder.txtNamePerson.text = event.Description
        holder.txtDocument.text = ""
//        if (person.SexId == "SEX_FEMENINO") {
//            holder.imgSex.setImageResource(R.drawable.ic_woman_default)
//        } else {
//            holder.imgSex.setImageResource(R.drawable.user_default)
//        }
        holder.view.setOnClickListener { onSelect(event) }
    }

    abstract fun onSelect(event: Event)

    override fun getItemCount(): Int = itemList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNamePerson = view.txt_name_person
        val txtDocument = view.txt_document
        val view = view
        val imgSex = view.img_sex
    }
}


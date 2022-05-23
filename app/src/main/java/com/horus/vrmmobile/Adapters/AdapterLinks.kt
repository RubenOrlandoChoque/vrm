package com.horus.vrmmobile.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.R
import kotlinx.android.synthetic.main.item_attachment.view.*
import java.util.*

abstract class AdapterLinks(var myContext: Context, links: List<String>) : RecyclerView.Adapter<AdapterLinks.ViewHolder>() {

    var listLinks: ArrayList<String> = ArrayList()

    init {
        listLinks.addAll(links)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_attachment, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvName.text = listLinks[position]
        holder.view.setOnClickListener { onItemClick(listLinks[position]) }
    }

    override fun getItemCount(): Int {
        return listLinks.size
    }

    fun updateData(listUpdate: ArrayList<String>) {
        listLinks.clear()
        listLinks.addAll(listUpdate)
        notifyDataSetChanged()
    }

    inner class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var tvName = view.tvName
    }

    abstract fun onItemClick(path: String)
}
package com.horus.vrmmobile.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Models.Multimedia
import com.horus.vrmmobile.R
import kotlinx.android.synthetic.main.item_compose_attachment.view.*
import java.util.*

class AdapterComposeAttachment(links: ArrayList<Multimedia>, var onDeleteItem: (Multimedia) -> Unit) : RecyclerView.Adapter<AdapterComposeAttachment.ViewHolder>() {

    var listLinks: ArrayList<Multimedia> = links

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_compose_attachment, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val path = if(listLinks[position].Path.isNotEmpty())  listLinks[position].Path else listLinks[position].PathLocal
        val parts = path.split("/")
        var name = ""
        if(parts.isNotEmpty()){
            name = parts[parts.lastIndex]
        }
        holder.tvName.text = name
        holder.ivDelete.setOnClickListener { onDeleteItem(listLinks[position]) }
    }

    override fun getItemCount(): Int {
        return listLinks.size
    }

    fun updateData(listUpdate: ArrayList<Multimedia>) {
        listLinks.clear()
        listLinks.addAll(listUpdate)
        notifyDataSetChanged()
    }

    inner class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var tvName = view.tvName
        var ivDelete = view.ivDelete
    }
}
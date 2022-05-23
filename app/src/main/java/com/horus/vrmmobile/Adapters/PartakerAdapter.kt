package com.horus.vrmmobile.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Models.Partaker
import com.horus.vrmmobile.Models.Person
import com.horus.vrmmobile.R
import kotlinx.android.synthetic.main.item_participant.view.*

abstract class PartakerAdapter(private val layoutInflater: LayoutInflater,
                               private var itemList: ArrayList<Partaker>,
                               @param:LayoutRes private val rowLayout: Int) : RecyclerView.Adapter<PartakerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = layoutInflater.inflate(rowLayout,
                parent,
                false)
        return ViewHolder(v)
    }

    fun setItems(items: ArrayList<Partaker>){
        itemList = items
        notifyDataSetChanged()
    }

    fun getItems(): ArrayList<Partaker>{
        return itemList
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val person = itemList[position]

        holder.txtNamePerson.text = "${person.FirstName} ${person.FirstSurname}"
        holder.txtDocument.text = "D.N.I.: ${person.DocumentationNumber}"

        when (person.PartakerTypeId) {
            "PTT_ATTENDEES" -> {
                holder.txtPartakerType.text = "Participantes"
            }
            "PTT_ORGANIZER" -> {
                holder.txtPartakerType.text = "Organizadores"
            }
            "PTT_SPEAKER" -> {
                holder.txtPartakerType.text = "Disertantes"
            }
            else -> {
                holder.txtPartakerType.text = "Disertantes"
            }
        }
        if(person.SexId == "SEX_FEMENINO"){
            holder.imgSex.setImageResource(R.drawable.ic_woman_default)
        }else{
            holder.imgSex.setImageResource(R.drawable.user_default)
        }

        holder.view.setOnClickListener { onSelect(person) }
    }

    abstract fun onSelect(person: Partaker)

    override fun getItemCount(): Int = itemList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNamePerson = view.txt_name_person
        val txtPartakerType = view.txt_partaker_type
        val txtDocument = view.txt_document
        val view = view
        val imgSex = view.img_sex
    }
}


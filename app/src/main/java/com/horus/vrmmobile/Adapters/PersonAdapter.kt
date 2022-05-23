package com.horus.vrmmobile.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Models.Person
import com.horus.vrmmobile.R
import kotlinx.android.synthetic.main.item_participant.view.*

abstract class PersonAdapter(private val layoutInflater: LayoutInflater,
                    private var itemList: ArrayList<Person>,
                    @param:LayoutRes private val rowLayout: Int) : RecyclerView.Adapter<PersonAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = layoutInflater.inflate(rowLayout,
                parent,
                false)
        return ViewHolder(v)
    }

    fun setItems(items: ArrayList<Person>){
        itemList = items
        notifyDataSetChanged()
    }

    fun getItems(): ArrayList<Person>{
        return itemList
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val person = itemList[position]

        holder.txtNamePerson.text = "${person.FirstName} ${person.FirstSurname}"
        holder.txtDocument.text = "D.N.I.: ${person.DocumentationNumber}"
        if(person.SexId == "SEX_FEMENINO"){
            holder.imgSex.setImageResource(R.drawable.ic_woman_default)
        }else{
            holder.imgSex.setImageResource(R.drawable.user_default)
        }

        holder.view.setOnClickListener { onSelect(person) }
    }

    abstract fun onSelect(person: Person)

    override fun getItemCount(): Int = itemList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNamePerson = view.txt_name_person
        val txtDocument = view.txt_document
        val view = view
        val imgSex = view.img_sex
    }
}


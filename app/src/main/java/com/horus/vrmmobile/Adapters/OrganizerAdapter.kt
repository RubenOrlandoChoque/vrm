package com.horus.vrmmobile.Adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Models.PersonHierarchicalStructure
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.PositionRepository
import kotlinx.android.synthetic.main.item_member_3.view.*

class OrganizerAdapter(private val layoutInflater: LayoutInflater,
                                private var itemList: List<PersonHierarchicalStructure>,
                                @param:LayoutRes private val rowLayout: Int) : RecyclerView.Adapter<OrganizerAdapter.ViewHolder>() {

    private var positions: Map<String, String> = PositionRepository.instance.getAll().map { it.Id to it.Name }.toMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = layoutInflater.inflate(rowLayout,
                parent,
                false)
        return ViewHolder(v)
    }

    fun setItems(items: List<PersonHierarchicalStructure>){
        itemList = items
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val person = itemList[position]

        holder.txtNamePerson.text = person.Person.toString()
        holder.txtPosition.text = positions[person.PositionId]
        if(person.Person!!.SexId == "SEX_FEMENINO"){
            holder.imgSex.setImageResource(R.drawable.ic_woman_default)
        }else{
            holder.imgSex.setImageResource(R.drawable.user_default)
        }
    }

    override fun getItemCount(): Int = itemList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNamePerson = view.txt_name_person
        val txtPosition = view.txt_position
        val imgSex = view.img_sex
    }
}


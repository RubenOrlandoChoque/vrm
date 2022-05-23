package com.horus.vrmmobile.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.Partaker
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.PositionRepository
import kotlinx.android.synthetic.main.item_member_with_delete.view.*

abstract class AddOrganizerAdapter(private val layoutInflater: LayoutInflater,
                          private var itemList: List<Partaker>,
                          @param:LayoutRes private val rowLayout: Int) : RecyclerView.Adapter<AddOrganizerAdapter.ViewHolder>() {

    private var positions: Map<String, String> = PositionRepository.instance.getAll().map { it.Id to it.Name }.toMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = layoutInflater.inflate(rowLayout,
                parent,
                false)
        return ViewHolder(v)
    }

    fun setItems(items: List<Partaker>){
        itemList = items
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val organizer = itemList[position]

        /*holder.txtNamePerson.text = organizer.PersonHierarchicalStructure?.Person?.toString()
        holder.txtPosition.text = positions[organizer.PersonHierarchicalStructure?.PositionId]
        if(organizer.PersonHierarchicalStructure?.Person?.SexId == "SEX_FEMENINO"){
            holder.imgSex.setImageResource(R.drawable.ic_woman_default)
        }else{
            holder.imgSex.setImageResource(R.drawable.user_default)
        }

        holder.btnDelete.visibility = if(organizer.PersonHierarchicalStructure?.PersonId == SharedConfig.getUserId()) View.GONE else View.VISIBLE
        holder.btnDelete.setOnClickListener { onDelete(organizer) }
        holder.view.setOnClickListener { onClick(organizer) }*/
    }

    override fun getItemCount(): Int = itemList.size

    abstract fun onDelete(organizer: Partaker)
    abstract fun onClick(organizer: Partaker)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNamePerson = view.txt_name_person
        val txtPosition = view.txt_position
        val btnDelete = view.btn_delete
        val imgSex = view.img_sex
        val view = view
    }
}


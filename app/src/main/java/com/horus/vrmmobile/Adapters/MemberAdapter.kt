package com.horus.vrmmobile.Adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Models.Dtos.PersonDto
import com.horus.vrmmobile.R
import kotlinx.android.synthetic.main.item_member_2.view.*
import com.github.zawadz88.materialpopupmenu.popupMenu
import com.horus.vrmmobile.Config.SharedConfig

abstract class MemberAdapter(private val context: Activity, private val layoutInflater: LayoutInflater,
                    private var itemList: List<PersonDto>,
                    @param:LayoutRes private val rowLayout: Int) : RecyclerView.Adapter<MemberAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = layoutInflater.inflate(rowLayout,
                parent,
                false)
        return ViewHolder(v)
    }

    fun setItems(items: List<PersonDto>){
        itemList = items
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val person = itemList[position]

        holder.txtNamePerson.text = "${person.FirstName} ${person.FirstSurname}"
        holder.txtDocument.text = person.Position
        if(person.SexId == "SEX_FEMENINO"){
            holder.imgSex.setImageResource(R.drawable.ic_woman_default)
        }else{
            holder.imgSex.setImageResource(R.drawable.user_default)
        }
        holder.btnMore.visibility = if(SharedConfig.getUserId() == person.Id) View.GONE else View.VISIBLE
        holder.btnMore.setOnClickListener { showMenuPopup(it, person) }
    }

    fun showMenuPopup(view: View, person: PersonDto) {
        val popupMenu = popupMenu {
            section {
                item {
                    label = "Editar"
                    icon = R.drawable.ic_edit
                    callback = {
                        onEdit(person)
                    }
                }
                item {
                    label = "Quitar"
                    icon = R.drawable.ic_delete
                    callback = {
                        onDelete(person)
                    }
                }
            }
        }
        popupMenu.show(context, view)
    }

    override fun getItemCount(): Int = itemList.size

    abstract fun onEdit(person: PersonDto)
    abstract fun onDelete(person: PersonDto)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNamePerson = view.txt_name_person
        val txtDocument = view.txt_document
        val btnMore = view.btn_more
        val imgSex = view.img_sex
    }
}


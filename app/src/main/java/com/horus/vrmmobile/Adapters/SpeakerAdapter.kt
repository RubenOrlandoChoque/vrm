package com.horus.vrmmobile.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Models.Partaker
import com.horus.vrmmobile.R
import kotlinx.android.synthetic.main.item_member_4.view.*

class SpeakerAdapter(private val layoutInflater: LayoutInflater,
                     private var itemList: List<Partaker>,
                     @param:LayoutRes private val rowLayout: Int) : RecyclerView.Adapter<SpeakerAdapter.ViewHolder>() {

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
        val person = itemList[position]

        //holder.txtNamePerson.text = person.FullName

        if(!person.Position.isNullOrEmpty()){
            holder.txtPosition.text = person.Position
            holder.txtPosition.visibility = View.VISIBLE
        }else{
            holder.txtPosition.visibility = View.GONE
        }

        if(!person.Profession.isNullOrEmpty()){
            holder.txtProfession.text = person.Profession
            holder.txtProfession.visibility = View.VISIBLE
        }else{
            holder.txtProfession.visibility = View.GONE
        }

        /*if(person.Person != null && person.Person!!.SexId == "SEX_FEMENINO"){
            holder.imgSex.setImageResource(R.drawable.ic_woman_default)
        }else{
            holder.imgSex.setImageResource(R.drawable.user_default)
        }*/
    }

    override fun getItemCount(): Int = itemList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNamePerson = view.txt_name_person
        val txtPosition = view.txt_position
        val txtProfession = view.txt_profession
        val imgSex = view.img_sex
    }
}


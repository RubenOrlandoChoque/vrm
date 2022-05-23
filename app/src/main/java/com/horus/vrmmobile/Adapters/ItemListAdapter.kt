package com.horus.vrmmobile.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Utils.ItemList
import kotlinx.android.synthetic.main.item_list_row.view.*


abstract class ItemListAdapter(private val layoutInflater: LayoutInflater,
                               private var singerList: List<ItemList>,
                               @param:LayoutRes private val rowLayout: Int,
                               @param:DrawableRes private val icon: Int,
                               @param:DrawableRes private val color: Int) : RecyclerView.Adapter<ItemListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = layoutInflater.inflate(rowLayout,
                parent,
                false)
        return ViewHolder(v)
    }

    public fun setItems(items: List<ItemList>){
        singerList = items
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = singerList[position]
        holder.itemList = item
        val projectIds = SharedConfig.getProjectId()
        holder.count.visibility = View.GONE
        if(projectIds.size > 0){
            projectIds.forEach { s ->
                if(holder.itemList?.Id == s){
                    holder.count.visibility = View.VISIBLE
                }
            }
        }
        holder.txtTitle.text = item.Name

        if(!item.Description.equals(""))
            holder.txtFromTo.text = item.Description
        else
            holder.txtFromTo.visibility = View.GONE
        holder.img.setImageResource(icon)
        holder.view.setOnClickListener{
            setOnClick(item)
//            holder.img.setImageResource(R.drawable.group30)
            if(projectIds.contains(item.Id)){
                projectIds.remove(item.Id)
                SharedConfig.setProjectId(projectIds)
                holder.count.visibility = View.GONE
            }
        }
        holder.viewColor.background = ContextCompat.getDrawable(layoutInflater.context,color)
    }

    abstract fun setOnClick(objItem: ItemList)

    override fun getItemCount(): Int = singerList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var img = view.img_il_item
        val txtTitle = view.txt_il_title
        val txtFromTo = view.txt_il_from_to
        val view = view.content_il_row
        val viewColor = view.content_il_color
        val count = view.text_count
        var itemList: ItemList? = null
    }
}
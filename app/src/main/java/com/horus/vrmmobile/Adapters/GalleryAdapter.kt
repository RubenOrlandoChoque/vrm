package com.horus.vrmmobile.Adapters

import android.content.Context
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView


import com.bumptech.glide.Glide
import com.horus.vrmmobile.R

import java.util.ArrayList

import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Models.Dtos.MultimediaData
import kotlinx.android.synthetic.main.card_item_gallery.view.*

/**
 * Created by Administrador on 26/3/2018.
 */

abstract class GalleryAdapter(var myContext: Context, _listObjeto: List<MultimediaData>, var height: Int) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    var listProjects: ArrayList<MultimediaData> = ArrayList()
//    val addObject = MultimediaData("+", "+")

    init {
//        listProjects.add(addObject)
        listProjects.addAll(_listObjeto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.card_item_gallery, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val newHeight = height// - Utils.convertDpToPx(myContext, 4)
        holder.contentImg.layoutParams = ViewGroup.LayoutParams(
                newHeight,
                newHeight)
        LoadImagen(listProjects[position].Path, holder.img, myContext).execute()
        if(listProjects[position].Path == "+"){
            holder.ivDeletePhoto.visibility = View.GONE
        }else{
            holder.ivDeletePhoto.visibility = View.VISIBLE
        }
        holder.view.setOnClickListener {
            if(listProjects[position].Path == "+"){
                onAddClick()
            }else{
                onItemClick(listProjects[position])
            }
        }
        holder.ivDeletePhoto.setOnClickListener { onDelete(listProjects[position]) }
    }

    override fun getItemCount(): Int {
        return listProjects.size
    }

    fun updateData(listUpdate: ArrayList<MultimediaData>) {
        listProjects.clear()
//        listProjects.add(addObject)
        listProjects.addAll(listUpdate)
        notifyDataSetChanged()
    }

    inner class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var img: ImageView = view.item_gallery
        var contentImg = view.content_img
        var ivDeletePhoto = view.iv_delete_photo
//        init {
//            ivDeletePhoto.visibility = View.VISIBLE
//        }
    }

    abstract fun onItemClick(multimediaData: MultimediaData)
    abstract fun onAddClick()
    abstract fun onDelete(multimediaData: MultimediaData)

    private inner class LoadImagen(var path: String?, private val imgView: ImageView, val context: Context) : AsyncTask<String, Int, Boolean>() {

        override fun doInBackground(vararg params: String): Boolean? {
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            if(path == "+"){
                imgView.setImageResource(R.drawable.ic_add)
                return
            }
            if (path != null && !path!!.isEmpty()) {
                path += if (path!!.startsWith("http")) "?height=150" else ""
                Glide.with(myContext)
                        .load(path)
                        .centerCrop()
                        .error(R.drawable.default_error)
                        .placeholder(R.drawable.ic_hourglass)
                        .into(imgView)
                System.gc()
            }
        }

        override fun onPreExecute() {

        }
    }
}
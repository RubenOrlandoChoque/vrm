package com.horus.vrmmobile.Adapters

import android.content.Context
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.horus.vrmmobile.R
import kotlinx.android.synthetic.main.card_item_gallery.view.*
import java.util.ArrayList

abstract class AdapterImage(var myContext: Context, _listPhotos: List<String>, var height: Int) : RecyclerView.Adapter<AdapterImage.ViewHolder>() {

    var listProjects: ArrayList<String> = ArrayList()

    init {
        listProjects.addAll(_listPhotos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.card_item_gallery, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val newHeight = height
        holder.contentImg.layoutParams = ViewGroup.LayoutParams(
                newHeight,
                newHeight)
        LoadImagen(listProjects[position], holder.img, myContext).execute()
        holder.view.setOnClickListener {
            onItemClick(listProjects[position])
        }
    }

    override fun getItemCount(): Int {
        return listProjects.size
    }

    fun updateData(listUpdate: ArrayList<String>) {
        listProjects.clear()
        listProjects.addAll(listUpdate)
        notifyDataSetChanged()
    }

    inner class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var img: ImageView = view.item_gallery
        var contentImg = view.content_img
    }

    abstract fun onItemClick(path: String)

    private inner class LoadImagen(var path: String?, private val imgView: ImageView, val context: Context) : AsyncTask<String, Int, Boolean>() {

        override fun doInBackground(vararg params: String): Boolean? {
            return true
        }

        override fun onPostExecute(result: Boolean?) {
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
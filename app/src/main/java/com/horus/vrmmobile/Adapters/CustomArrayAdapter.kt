package com.horus.vrmmobile.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes

abstract class CustomArrayAdapter<T>(mContext: Context, @param:LayoutRes private val mResource: Int,
                                     private val items: ArrayList<T>) :
        ArrayAdapter<T>(mContext, 0, items),
        AdapterView.OnItemClickListener {

    private val mInflater: LayoutInflater = LayoutInflater.from(mContext)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if(view == null) view = mInflater.inflate(mResource, parent, false)

        val text = view!!.findViewById<View>(android.R.id.text1) as TextView
        text.text = items[position].toString()

        return view
    }

    abstract fun onSelectItem(item: T)

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        onSelectItem(items[position])
    }
}
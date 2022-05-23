package com.horus.vrmmobile.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.horus.vrmmobile.R
import java.util.ArrayList
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Models.Dtos.ItemEncuestaSyncByFormulario

class EncuestaSyncByFormularioAdapter(private val myContext: Context, private val itemList: ArrayList<ItemEncuestaSyncByFormulario>) : RecyclerView.Adapter<EncuestaSyncByFormularioAdapter.ViewHolders>() {
    private var onItemClickListener: OnItemClickListener? = null

    override fun getItemCount(): Int = itemList.size

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolders {

        val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.item_form_sincronizacion, null)
        return ViewHolders(layoutView)
    }

    override fun onBindViewHolder(holder: ViewHolders, position: Int) {
        holder.chk_ifs_selected.isChecked = itemList[position].isSeleccionado

        if (holder.pnlItem.visibility == View.VISIBLE) {
            holder.txt_name_formulario.text = "▼ Formulario: " + itemList[position].nombreFormulario
        } else {
            holder.txt_name_formulario.text = "▲ Formulario: " + itemList[position].nombreFormulario
        }
        if (itemList[position].estado == 0) {
            holder.txt_count_encuestas.text = itemList[position].encuestas.toString()
            holder.txt_count_novedades.text = itemList[position].novedades.toString()
            holder.txt_count_punto_interes.text = itemList[position].puntosInteres.toString()
            holder.txt_count_archivos.text = itemList[position].archivos.toString()
        } else {
            holder.txt_count_encuestas.text = "${itemList[position].encuestas_sync}/${itemList[position].encuestas}"
            holder.txt_count_novedades.text = "${itemList[position].novedades_sync}/${itemList[position].novedades}"
            holder.txt_count_punto_interes.text = "${itemList[position].puntosInteres_sync}/${itemList[position].puntosInteres}"
            holder.txt_count_archivos.text = "${itemList[position].archivos_sync}/${itemList[position].archivos}"
            if (itemList[position].isEncuesta_error)
                holder.txt_count_encuestas.setTextColor(ContextCompat.getColor(myContext, R.color.row_item_error_sinc))
            if (itemList[position].isNovedades_error)
                holder.txt_count_novedades.setTextColor(ContextCompat.getColor(myContext, R.color.row_item_error_sinc))
            if (itemList[position].isPuntoInteres_error)
                holder.txt_count_punto_interes.setTextColor(ContextCompat.getColor(myContext, R.color.row_item_error_sinc))
            if (itemList[position].isArchivos_error)
                holder.txt_count_archivos.setTextColor(ContextCompat.getColor(myContext, R.color.row_item_error_sinc))
        }

        holder.pb_encuestas.visibility = View.GONE
        holder.pb_novedades.visibility = View.GONE
        holder.pb_punto_interes.visibility = View.GONE
        holder.pb_archivos.visibility = View.GONE
        if (itemList[position].isSyncEncuestas) {
            holder.pb_encuestas.visibility = View.VISIBLE
            holder.pb_novedades.visibility = View.VISIBLE
            holder.pb_punto_interes.visibility = View.VISIBLE
        }
        if (itemList[position].isSyncArchivos) {
            holder.pb_archivos.visibility = View.VISIBLE
        }

        if (itemList[position].isSyncronizando) {
            holder.chk_ifs_selected.isEnabled = false
            holder.pnlItem.isEnabled = false
        } else {
            holder.chk_ifs_selected.isEnabled = true
            holder.pnlItem.isEnabled = true
        }
    }

    inner class ViewHolders(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var txt_count_encuestas: TextView
        var txt_count_novedades: TextView
        var txt_count_punto_interes: TextView
        var txt_count_archivos: TextView
        var txt_name_formulario: TextView
        var pb_encuestas: ProgressBar
        var pb_novedades: ProgressBar
        var pb_punto_interes: ProgressBar
        var pb_archivos: ProgressBar
        var chk_ifs_selected: CheckBox
        public val pnlItem: LinearLayout

        init {
            txt_name_formulario = itemView.findViewById<View>(R.id.txt_ifs_formulario) as TextView
            txt_count_encuestas = itemView.findViewById<View>(R.id.txt_ifs_encuestas) as TextView
            txt_count_novedades = itemView.findViewById<View>(R.id.txt_ifs_novedades) as TextView
            txt_count_punto_interes = itemView.findViewById<View>(R.id.txt_ifs_punto_interes) as TextView
            txt_count_archivos = itemView.findViewById<View>(R.id.txt_ifs_archivos) as TextView

            pb_encuestas = itemView.findViewById<View>(R.id.pb_ifs_encuesta) as ProgressBar
            pb_novedades = itemView.findViewById<View>(R.id.pb_ifs_novedades) as ProgressBar
            pb_punto_interes = itemView.findViewById<View>(R.id.pb_ifs_punto_interes) as ProgressBar
            pb_archivos = itemView.findViewById<View>(R.id.pb_ifs_archivos) as ProgressBar

            chk_ifs_selected = itemView.findViewById<View>(R.id.chk_ifs_selected) as CheckBox
            chk_ifs_selected.setOnClickListener { v -> onItemClickListener!!.onItemClick(v, getAdapterPosition()) }

            pnlItem = itemView.findViewById<View>(R.id.pnl_ifs_item) as LinearLayout
            pnlItem.setOnClickListener { v -> onItemClickListener!!.onItemClick(v, getAdapterPosition()) }

            txt_name_formulario.setOnClickListener {
                if (pnlItem.visibility == View.VISIBLE) {
                    pnlItem.visibility = View.GONE
                } else {
                    pnlItem.visibility = View.VISIBLE
                }
                notifyDataSetChanged()
            }
        }
    }
}
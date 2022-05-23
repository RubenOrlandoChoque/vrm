package com.horus.vrmmobile.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Models.Survey.Form
import kotlinx.android.synthetic.main.form_row.view.*

abstract class FormListAdapter(private val layoutInflater: LayoutInflater,
                               private var singerList: List<Form>,
                               @param:LayoutRes private val rowLayout: Int) : RecyclerView.Adapter<FormListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = layoutInflater.inflate(rowLayout,
                parent,
                false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val form = singerList[position]
        holder.txtTitle.text = form.Formulario
        holder.view.setOnClickListener {
            onItemClick(form)
        }
        holder.surveysDone.text = form.SurveysDone.toString()
        holder.surveysSync.text = form.SurveysSync.toString()

        holder.downloadForm.setOnClickListener { onDownloadIconClick(form) }
        holder.updateForm.setOnClickListener { onUpdateFormIconClick(form) }
        if (form.Download) {
            holder.downloadForm.visibility = View.GONE
            holder.updateForm.visibility = View.VISIBLE
        } else {
            holder.downloadForm.visibility = View.VISIBLE
            holder.updateForm.visibility = View.GONE
        }
    }

    abstract fun onItemClick(objForm: Form)
    abstract fun onDownloadIconClick(objForm: Form)
    abstract fun onUpdateFormIconClick(objForm: Form)

    override fun getItemCount(): Int = singerList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTitle = view.txt_lf_title
        val txtDateCreate = view.txt_lf_date_create
        val view = view.content_lf_row
        val surveysDone = view.surveys_done
        val surveysSync = view.surveys_sync
        val downloadForm = view.download_form
        val updateForm = view.update_form
    }
}
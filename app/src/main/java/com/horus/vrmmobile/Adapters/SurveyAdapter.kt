package com.horus.vrmmobile.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Models.Dtos.SurveyDto
import kotlinx.android.synthetic.main.survey_item_row.view.*

abstract class SurveyAdapter(private val layoutInflater: LayoutInflater,
                             public var surveyList: ArrayList<SurveyDto>,
                             @param:LayoutRes private val rowLayout: Int) : RecyclerView.Adapter<SurveyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = layoutInflater.inflate(rowLayout,
                parent,
                false)
        return ViewHolder(v)
    }

    public fun setItems(items: ArrayList<SurveyDto>){
        surveyList = items
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val survey = surveyList[position]
        holder.txtSurvey.text = survey.OpcionSearch
        holder.txtSurveyState.text = survey.NombreEstadoEncuesta
        holder.txtJefeHogar.text = survey.JefeDeHogar

        holder.infoContent.setOnClickListener {
            setOnClickList(survey)
        }
        holder.btnEdit.setOnClickListener {
            setOnClickEdit(survey)
        }
    }

    abstract fun setOnClickList(surveyDto: SurveyDto)
    abstract fun setOnClickEdit(surveyDto: SurveyDto)

    override fun getItemCount(): Int = surveyList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtSurvey = view.txt_ss_survey
        val txtSurveyState = view.txt_ss_survey_state
        val txtJefeHogar = view.txt_ss_jefe_hogar
        val infoContent = view.info_content
        val btnEdit = view.btn_edit_survey
    }
}


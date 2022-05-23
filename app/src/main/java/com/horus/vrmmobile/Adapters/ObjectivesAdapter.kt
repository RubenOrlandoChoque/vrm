package com.horus.vrmmobile.Adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Models.ContributionToTheObjective
import com.horus.vrmmobile.Repositories.ContributionToTheObjectiveRepository
import com.horus.vrmmobile.Repositories.MeasureTypeRepository
import com.horus.vrmmobile.Repositories.ObjectiveRepository
import kotlinx.android.synthetic.main.item_objective.view.*


abstract class ObjectivesAdapter(private val layoutInflater: LayoutInflater,
                                 private var items: List<ContributionToTheObjective>,

                                 @param:LayoutRes private val rowLayout: Int) : RecyclerView.Adapter<ObjectivesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = layoutInflater.inflate(rowLayout,
                parent,
                false)
        return ViewHolder(v)
    }

    fun setItems(items: List<ContributionToTheObjective>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun disabledContributions(enabled: Boolean) {
        this.items.forEach { it.Enabled = enabled}
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contribution = items[position]

        val ob = ObjectiveRepository.instance.getById(contribution.ObjectiveId)
        holder.title.text = ob?.Description
        holder.contribution = contribution
        holder.amount.setText(contribution.AmountMade.toString())
        val measureType = MeasureTypeRepository.instance.getById(contribution.MeasureTypeId)
        holder.measureType.text = measureType?.Name
//        holder.amount.setOnFocusChangeListener { v, hasFocus -> onFocusChange(v, hasFocus, holder.contribution?) }
        holder.amount.isEnabled = contribution.Enabled
        holder.amount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val text = s.toString()
                when {
                    text.isEmpty() -> holder.contribution?.AmountMade = 0
                    else -> {
                        holder.contribution?.AmountMade = s.toString().toInt()
                    }
                }
            }
        })
    }

    private fun onFocusChange(v: View?, hasFocus: Boolean, contribution: ContributionToTheObjective) {
        if (!hasFocus) {
            ContributionToTheObjectiveRepository.instance.addOrUpdate(contribution)
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.objective_title
        val amount = view.objective_amount
        val measureType = view.measure_type
        val view = view
        var contribution: ContributionToTheObjective? = null
    }
}


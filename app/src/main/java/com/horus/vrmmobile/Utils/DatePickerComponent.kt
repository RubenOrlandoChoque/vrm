package com.horus.vrmmobile.Utils

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import java.util.*
import android.view.ViewGroup
import android.widget.*
import com.horus.vrmmobile.R

/**
 * Created by USUARIO 004 on 10/9/2018.
 */
open class DatePickerComponent(
        context: Context
//        var value: String,
//        var maxValue: String,
//        var minValue: String
): RelativeLayout(context), ComponentInterface{

    private var changeValue: ComponentInterface.OnChangeValues? = null

    private var selected = ""
    private var textLabel = TextView(context)
    private var _year = 0
    private var _month = 0
    private var _day = 0

    init {
        this.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

        val view = LayoutInflater.from(context).inflate(R.layout.datepicker_view, this as ViewGroup, false) as RelativeLayout
        this.addView(view)
        val c = Calendar.getInstance()
        textLabel = view.findViewById(R.id.txt_label)

//        if (!value.isEmpty()){
//            c.time = DateUtils.convertDateShortToStringInverse(value)
//            selected = DateUtils.convertToShortFormatSlash(value)
//            textLabel.text = (if(selected.isNullOrEmpty()) "--/--/----" else selected)
//        }

        _year = c.get(Calendar.YEAR)
        _month = c.get(Calendar.MONTH)
        _day = c.get(Calendar.DAY_OF_MONTH)

        view.setOnClickListener {
            val dpd = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                _year = year
                _month = monthOfYear
                _day = dayOfMonth
                selected = dayOfMonth.toString() + "/" + (monthOfYear  + 1) + "/" + year
                this.changeValue!!.onChangeValues(selected)
            }, _year, _month, _day)
            dpd.show()
        }
    }

    override fun setOnChangeValues(l: ComponentInterface.OnChangeValues) {
        this.changeValue = l
    }


}
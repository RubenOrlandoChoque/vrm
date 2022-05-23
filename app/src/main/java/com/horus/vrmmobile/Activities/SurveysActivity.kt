package com.horus.vrmmobile.Activities

import android.app.ProgressDialog
import android.os.*
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.horus.vrmmobile.Adapters.SurveyAdapter
import com.horus.vrmmobile.Config.Constant
import com.horus.vrmmobile.Managers.SearchSurveyManager
import com.horus.vrmmobile.Models.Dtos.SurveyDto
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Utils.DateUtils
import com.savvi.rangedatepicker.CalendarPickerView
import kotlinx.android.synthetic.main.activity_surveys.*
import kotlinx.android.synthetic.main.dialog_filter.*
import mpi.dc.clases.DataCollector
import xyz.sangcomz.stickytimelineview.RecyclerSectionItemDecoration
import xyz.sangcomz.stickytimelineview.model.SectionInfo
import java.text.SimpleDateFormat
import java.util.*

class SurveysActivity : AppCompatActivity() {
    var loading: ProgressDialog? = null
    private var adapter: SurveyAdapter? = null
    val calendarFrom = Calendar.getInstance()
    val calendarTo = Calendar.getInstance()
    val calendarMin = Calendar.getInstance()
    val calendarMax = Calendar.getInstance()
    var surveyList = ArrayList<SurveyDto>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surveys)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.green)
        }
        loading = ProgressDialog(this)
        loading?.setMessage("Cargando")
        txt_form_name.text = DataCollector.getFormulario().nombre_version_formulario
        setMinMaxCalendar()
        setButtons()
        setListEvents()

    }

    private fun monthFilter() {
        calendarFrom.time = Date()
        calendarFrom.set(Calendar.DAY_OF_MONTH, 1)
        calendarTo.time = Date()
        calendarTo.add(Calendar.MONTH, 1)
        calendarTo.set(Calendar.DAY_OF_MONTH, 1)
        calendarTo.add(Calendar.DAY_OF_MONTH, -1)
        filter()
    }


    private fun setMinMaxCalendar() {
        calendarMin.time = Date()
        calendarMin.add(Calendar.YEAR, -1)
        calendarMax.time = Date()
        calendarMax.add(Calendar.YEAR, 1)
    }

    private fun setButtons() {
        btn_back.setOnClickListener {
            this.onBackPressed()
        }
        btn_today.setOnClickListener {
            changeButtonSelect(btn_today)
            todayFilter()
        }
        btn_filters.setOnClickListener {
            changeButtonSelect(btn_filters)
            showFilters()
        }
        btn_month.setOnClickListener {
            changeButtonSelect(btn_month)
            monthFilter()
        }
    }

    private fun showFilters() {
        val dialogScan = MaterialDialog(this).show {
            customView(viewRes = R.layout.dialog_filter)
            negativeButton(R.string.cancel)
            positiveButton(R.string.accept)
            cancelOnTouchOutside(false)
            noAutoDismiss()
            positiveButton {
                val selecteds = it.calendar_view.selectedDates
                if (selecteds.size > 0) {
                    this@SurveysActivity.calendarFrom.time = selecteds[0]
                    this@SurveysActivity.calendarTo.time = selecteds[selecteds.size - 1]
                }
                filter()
                setTexts()
                it.dismiss()
            }
            negativeButton {
                it.dismiss()
            }
        }

        dialogScan.calendar_view.init(
                calendarMin.time,
                calendarMax.time,
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()))
                .inMode(CalendarPickerView.SelectionMode.RANGE)
                .withSelectedDate(Date())
    }

    private fun setTexts() {
        txt_datefrom.text = ""
        if (calendarFrom != calendarTo) {
            txt_datefrom.text = DateUtils.convertDateShortFormatToString(calendarFrom.time)
        }
        txt_dateto.text = DateUtils.convertDateShortFormatToString(calendarTo.time)
    }

    private fun changeButtonSelect(button: Button) {
        btn_today.setBackgroundColor(if (button == btn_today) ContextCompat.getColor(this, R.color.yellow_transparent) else ContextCompat.getColor(this, R.color.transparent))
        btn_month.setBackgroundColor(if (button == btn_month) ContextCompat.getColor(this, R.color.yellow_transparent) else ContextCompat.getColor(this, R.color.transparent))
        btn_filters.setBackgroundColor(if (button == btn_filters) ContextCompat.getColor(this, R.color.yellow_transparent) else ContextCompat.getColor(this, R.color.transparent))
    }

    private fun setListEvents() {
        recycler_view.layoutManager = LinearLayoutManager(this,
                RecyclerView.VERTICAL,
                false)
        recycler_view.addItemDecoration(getSectionCallback(ArrayList()))
        adapter = object : SurveyAdapter(layoutInflater,
                surveyList,
                R.layout.survey_item_row) {
            override fun setOnClickList(surveyDto: SurveyDto) {

            }

            override fun setOnClickEdit(surveyDto: SurveyDto) {
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    try {
                        DataCollector.editarEncuesta(surveyDto.CodEncuesta, this@SurveysActivity, Constant.DataCollectorCodUser)
                        if (loading?.isShowing == true) {
                            loading?.dismiss()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        if (loading?.isShowing == true) {
                            loading?.dismiss()
                        }
                    }
                }, 100)
            }
        }

        recycler_view.adapter = adapter
        todayFilter()
    }

    override fun onResume() {
        super.onResume()
//        nextClick = true
        if (loading?.isShowing == true) {
            loading?.dismiss()
        }
//        searchSurvey()
    }

    private fun todayFilter() {
        calendarFrom.time = Date()
        calendarTo.time = calendarFrom.time
        filter()
    }

    private fun filter() {
        txt_datefrom.text = ""
        txt_dateto.text = ""
        val objSearch = SearchSurveyManager(this@SurveysActivity, "", calendarFrom.time, calendarTo.time, "", "COD_TODAS", true)
//        event_empty.visibility = if(singerList.size == 0) View.VISIBLE else View.GONE
//        recycler_view.visibility = if(singerList.size == 0) View.GONE else View.VISIBLE
        recycler_view.removeItemDecoration(recycler_view.getItemDecorationAt(0))
        recycler_view.addItemDecoration(getSectionCallback(surveyList))
        objSearch.adapter = adapter
        objSearch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private fun getSectionCallback(surveyList: List<SurveyDto>): RecyclerSectionItemDecoration.SectionCallback {
        return object : RecyclerSectionItemDecoration.SectionCallback {
            //In your data, implement a method to determine if this is a section.
            override fun isSection(position: Int): Boolean =
                    surveyList[position].FechaEncuesta.split(" ")[0] != surveyList[position - 1].FechaEncuesta.split(" ")[0]

            //Implement a method that returns a SectionHeader.
            override fun getSectionHeader(position: Int): SectionInfo? =
                    SectionInfo(surveyList[position].FechaEncuesta.split(" ")[0], null)
        }
    }
}

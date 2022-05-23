package com.horus.vrmmobile.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.horus.vrmmobile.Config.Configuration
import com.horus.vrmmobile.Config.Constant
import com.horus.vrmmobile.R
import kotlinx.android.synthetic.main.activity_form_active.*
import mpi.dc.Modulos.FormularioActual.Querys
import mpi.dc.clases.Config
import mpi.dc.clases.DataCollector
import mpi.dc.clases.Global

class ActiveFormActivity : AppCompatActivity() {
    var startNewSurvey: ProgressDialog? = null
    var firstTime = true
    var startSurvey = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.horus.vrmmobile.R.layout.activity_form_active)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.green)
        }
        setButtons()
        startNewSurvey = ProgressDialog(this)
        startNewSurvey?.setMessage("Cargando")
        form_name.text = DataCollector.getFormulario().nombre_version_formulario
    }

    override fun onResume() {
        super.onResume()
        if (firstTime) {
            firstTime = false
            Global.getFormulario().codFormulario = DataCollector.getFormulario().codFormulario
            val config = Config()
            config.ShowRelationship = Configuration.showParentesco
            config.ShowHeadOfHousehold = Configuration.ShowHeadOfHousehold
            config.RecordAudio = Configuration.RECORD_AUDIO
            config.OnlineSyncMode = Configuration.OnlineSyncMode
            config.OfflineSyncMode = Configuration.OfflineSyncMode
            config.ShowSurveyListScreen = Configuration.ShowSurveyListScreen
            config.ShowSurveyCounter = Configuration.ShowSurveyCounter
            config.ShowNewnessCounter = Configuration.ShowNewnessCounter
            config.ShowPointsOfInterestCounter = Configuration.ShowPointsOfInterestCounter
            DataCollector.initializeFormTask(this@ActiveFormActivity, DataCollector.getFormulario().codFormulario, config)
        }
        getDataSurveys()
    }

    private fun setButtons() {
        btn_lp_back.setOnClickListener {
            this.onBackPressed()
        }
        btn_fa_new_survey.setOnClickListener {
            startSurvey = true
            startNewSurvey?.show()
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                try {
                    val r = DataCollector.startNewSurvey(this@ActiveFormActivity, Constant.DataCollectorCodUser)
                            .subscribe { result ->
                                if (startNewSurvey?.isShowing == true) {
                                    startNewSurvey?.dismiss()
                                }
                            }
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (startNewSurvey?.isShowing == true) {
                        startNewSurvey?.dismiss()
                    }
                }
            }, 100)
        }
        btn_fa_edit_survey.setOnClickListener {
            val i = Intent(this@ActiveFormActivity, SurveysActivity::class.java)
            startActivity(i)
        }
        btn_fa_syncronizar.setOnClickListener {
            val i = Intent(this@ActiveFormActivity, SurveySyncActivity::class.java)
            startActivity(i)
        }
    }

    private fun getDataSurveys() {
        btn_fa_encuestas.text = DataCollector.getRecolectorRepository().getStringSelect(Querys.getTotalEncuestasByVersion(DataCollector.getFormulario().codVersion, "", ""))
        btn_fa_encuestas_sincronizadas.text = DataCollector.getRecolectorRepository().getStringSelect(Querys.getTotalEncuestasByVersionSincronizado(DataCollector.getFormulario().codVersion, "", ""))
    }
}

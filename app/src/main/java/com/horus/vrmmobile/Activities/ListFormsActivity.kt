package com.horus.vrmmobile.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.horus.vrmmobile.Adapters.FormListAdapter
import com.horus.vrmmobile.BuildConfig
import com.horus.vrmmobile.Config.Constant
import com.horus.vrmmobile.Models.Survey.Form
import com.horus.vrmmobile.R
import kotlinx.android.synthetic.main.activity_list_forms.*
import mpi.dc.Modulos.FormularioActual.Querys
import mpi.dc.Modulos.syncform.FormularioSync
import mpi.dc.Modulos.syncform.SyncForm
import mpi.dc.Modulos.syncform.SyncFormAdapater
import mpi.dc.clases.DataCollector
import mpi.dc.clases.Formulario
import mpi.dc.clases.Version
import org.json.JSONException
import org.json.JSONObject

class ListFormsActivity : AppCompatActivity() {

    private var formsList = ArrayList<Form>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_forms)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.green)
        }
        setButtons()
        DataCollector.getFormulario().isRestartFormActivity = false
        setListForms()
        listForms()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setButtons() {
        btn_lp_back.setOnClickListener {
            this.onBackPressed()
        }
    }

    private fun setListForms() {
        rv_lf_list_forms.layoutManager = LinearLayoutManager(this,
                RecyclerView.VERTICAL,
                false)
        rv_lf_list_forms.adapter = object : FormListAdapter(layoutInflater,
                formsList,
                R.layout.form_row) {
            override fun onItemClick(objForm: Form) {
                if (objForm.Download) {
                    val version: Version = DataCollector.getVersionByCodFormulario(objForm.CodFormulario)

                    val form: mpi.dc.clases.Form = DataCollector.getFormulario()
                    form.codFormulario = objForm.CodFormulario
                    form.codVersion = version.codVersion
                    form.codSistema = version.codSistema
                    form.codTipoRelevamiento = version.codTipoRelevamiento
                    form.nombre_version_formulario = version.version

                    val i = Intent(this@ListFormsActivity, ActiveFormActivity::class.java)
                    i.putExtra("codFormulario", objForm.CodFormulario)
                    startActivity(i)
                } else {
                    questionDownloadForm(objForm, true)
                }
            }

            override fun onDownloadIconClick(objForm: Form) {
                questionDownloadForm(objForm, false)
            }

            override fun onUpdateFormIconClick(objForm: Form) {
                MaterialDialog(this@ListFormsActivity).show {
                    title(R.string.notification)
                    message(text = "¿Desea actualizar el formulario ${objForm.Formulario}?.")
                    positiveButton(R.string.accept)
                    negativeButton(R.string.cancel)
                    cancelOnTouchOutside(false)
                    positiveButton {
                        downloadForm(objForm, false)
                    }
                }
            }
        }
    }

    private fun questionDownloadForm(objForm: Form, goToActiveForm: Boolean) {
        MaterialDialog(this@ListFormsActivity).show {
            title(R.string.notification)
            message(text = "El formulario no se encuentra descargado \n¿Desea descargar el formulario ${objForm.Formulario}?.")
            positiveButton(R.string.accept)
            negativeButton(R.string.cancel)
            cancelOnTouchOutside(false)
            positiveButton {
                downloadForm(objForm, goToActiveForm)
            }
        }
    }

    private fun downloadForm(objForm: Form, goToActiveForm: Boolean) {
        val listaFormDescarga = ArrayList<FormularioSync>()
        val objFormSync = FormularioSync()
        objFormSync.formularioCod = objForm.CodFormulario
        objFormSync.versionCod = objForm.CodVersion
        listaFormDescarga.add(objFormSync)

        val dialog = ProgressDialog(this@ListFormsActivity)
        SyncForm(this@ListFormsActivity, listaFormDescarga, Constant.urlSurveyApi, Constant.DataCollectorCodUser, BuildConfig.APPLICATION_ID, BuildConfig.VERSION_CODE, object : SyncFormAdapater() {
            override fun onFinishSync(errores: Int, listaerrores: String?, actualizados_form: Int) {
                val version: Version = DataCollector.getVersionByCodFormulario(objForm.CodFormulario)
                val form: mpi.dc.clases.Form = DataCollector.getFormulario()
                form.codFormulario = objForm.CodFormulario
                form.codVersion = version.codVersion
                form.codSistema = version.codSistema
                form.codTipoRelevamiento = version.codTipoRelevamiento
                form.nombre_version_formulario = version.version

                val f = formsList.firstOrNull { it.CodFormulario == form.codFormulario }
                f?.Download = true
                runOnUiThread { (rv_lf_list_forms.adapter as FormListAdapter).notifyDataSetChanged() }
                
                if (goToActiveForm) {
                    val i = Intent(this@ListFormsActivity, ActiveFormActivity::class.java)
                    i.putExtra("codFormulario", objForm.CodFormulario)
                    startActivity(i)
                }
            }
        }).sync(dialog)
    }

    private fun listForms() {
        //Si tiene conexion a Internet
        //Tiene conexion a Internet
        val jsonObject = JSONObject()
        try {
            jsonObject.put("codUsuario", Constant.DataCollectorCodUser)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val formsInDB = Formulario.getFormularios(Constant.DataCollectorCodUser)
        formsInDB.forEach {
            val version = DataCollector.getVersionByCodFormulario(it.codFormulario)
            val form = Form()
            form.CodVersion = version.codVersion
            form.CodFormulario = it.codFormulario
            form.Formulario = it.formulario
            form.Download = true
            try {
                form.SurveysDone = DataCollector.getRecolectorRepository().getStringSelect(Querys.getTotalEncuestasByVersion(form.CodVersion, "", "")).toInt()
            } catch (e: Exception) {
            }
            try {
                form.SurveysSync = DataCollector.getRecolectorRepository().getStringSelect(Querys.getTotalEncuestasByVersionSincronizado(form.CodVersion, "", "")).toInt()
            } catch (e: Exception) {
            }
            formsList.add(form)
        }
        (rv_lf_list_forms.adapter as FormListAdapter).notifyDataSetChanged()

        AndroidNetworking.post(Constant.urlSurveyApi + "api/forms/ListForm")
                .addJSONObjectBody(jsonObject)
                .setTag("ListForm")
                .setPriority(Priority.HIGH)
                .build()
                .getAsObjectList(Form::class.java, object : ParsedRequestListener<List<Form>> {
                    override fun onResponse(forms: List<Form>) {
                        val codesVersions: String = DataCollector.getRecolectorRepository().getStringSelect("SELECT ifnull(group_concat(v.CodVersion,','),'') AS Versiones FROM versiones AS v")
                        for (objForm in forms) {
                            if (codesVersions.contains(objForm.CodVersion)) {
                                objForm.Download = true
                            }
                            if (formsList.find { it.CodFormulario == objForm.CodFormulario } == null) {
                                formsList.add(objForm)
                            }
                        }
                        pb_lp_loading_forms.visibility = View.GONE
                    }

                    override fun onError(anError: ANError) {
                        val msjError: String
                        pb_lp_loading_forms.visibility = View.GONE
                        if (anError.errorCode != 0) {
                            msjError = anError.errorBody
                        } else {
                            msjError = when (anError.errorDetail) {
                                "connectionError" -> "Se produjo un error en la conexión - Timeout"
                                else -> anError.errorDetail
                            }
                        }
                        MaterialDialog(this@ListFormsActivity).show {
                            title(R.string.error)
                            message(text = msjError)
                            positiveButton(R.string.accept)
                            cancelOnTouchOutside(false)
                        }
                    }
                })
    }

}
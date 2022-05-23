package com.horus.vrmmobile.Managers

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.os.AsyncTask
import android.util.Log
import androidx.collection.ArrayMap
import br.com.zbra.androidlinq.Linq.stream
import com.horus.vrmmobile.Adapters.SurveyAdapter
import com.horus.vrmmobile.Models.Dtos.SurveyDto
import com.horus.vrmmobile.querys.Querys
import mpi.dc.clases.DataCollector
import java.text.SimpleDateFormat
import java.util.*


class SearchSurveyManager(private val myContext: Context, txtBuscador: String, txtFechaDesde: Date, txtFechaHasta: Date, buscarEn: String, detalleBusqueda: String, searchBdApp: Boolean) : AsyncTask<String, Int, Boolean>() {
    private var cantidad_familias = 1
    private var txtBuscador = ""
    private var txtFechaDesde : Date
    private var txtFechaHasta : Date
    private var buscarEn = ""
    private var loading1: ProgressDialog? = null
    private var detalleBusqueda = ""
    private var searchBdApp = false
    internal var indiceFamilia = 1
    var adapter: SurveyAdapter? = null
    init {
        this.txtBuscador = txtBuscador
        this.txtFechaDesde = txtFechaDesde
        this.txtFechaHasta = txtFechaHasta
        this.buscarEn = buscarEn
        this.detalleBusqueda = detalleBusqueda
        this.searchBdApp = searchBdApp
    }

    override fun doInBackground(vararg params: String): Boolean? {
        try {
            var c: Cursor? = null
            var cSec: Cursor? = null
            adapter?.surveyList?.clear()
            if (buscarEn != "temp") {
                val queryClaves = Querys.getClavesByBuscadorCenso(DataCollector.getFormulario().codVersion, txtBuscador)
                c = DataCollector.getRecolectorRepository().executeSelect(queryClaves, c)
                var condition = ""
                while (c!!.moveToNext()) {
                    condition += "OR (" + c.getString(3) + ") "
                }
                c = null
                val opcionesTerreno = DataCollector.getRecolectorRepository().getStringSelect(Querys.getOpcionesTerreno(DataCollector.getFormulario().codVersion))
                var queryFiltros = Querys.getEncuestasByBuscadorCenso(DataCollector.getFormulario().codVersion, condition, txtFechaDesde, txtFechaHasta, txtBuscador, opcionesTerreno)
                Log.i("QUERY EDIT->>>", queryFiltros)
                var condicionDetalle = ""
                when (detalleBusqueda) {
                    "COD_ENCUESTAS" -> condicionDetalle = " AND e.Estado IN ('COD_COMPLETA','COD_FINALIZADA_INCOMPLETA','COD_EN_PROCESO','COD_DE_PRUEBA') "
                    "COD_NOVEDADES" -> condicionDetalle = " AND e.Estado IN ('COD_IMPOSIBILITADA') "
                    "COD_PUNTO_INTERES" -> condicionDetalle = " AND e.Estado IN ('COD_PUNTO_INTERES') "
                    "COD_REVISITAS" -> condicionDetalle = Querys.getEncuestasRevisitas(DataCollector.getFormulario().codVersion)
                    "COD_TODAS" -> condicionDetalle = ""
                    "COD_VACIAS" -> condicionDetalle = " AND e.Estado = '' "
                }
                queryFiltros += condicionDetalle
                Log.i("QUERY EDIT->>>", queryFiltros)
                c = DataCollector.getRecolectorRepository().executeSelect(queryFiltros, c)
                loadAdapter(c, "bd_app")
            } else {
                val condicion = txtBuscador.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                var condicionPalabras = ""
                for (k in condicion.indices) {
                    condicionPalabras += " cadena like '%" + condicion[k] + "%' AND"
                }
                condicionPalabras = condicionPalabras.substring(0, condicionPalabras.length - 3)
                c = null
                val queryClavesNuevaBusqueda = Querys.getClavesByBuscadorCensoNuevaBusqueda(DataCollector.getFormulario().codVersion, txtBuscador)
                c = DataCollector.getRecolectorRepository().executeSelect(queryClavesNuevaBusqueda, c)
                var condicionBusquedaNueva = ""
                while (c!!.moveToNext()) {
                    condicionBusquedaNueva += "'" + c.getString(3) + "',"
                }
                condicionBusquedaNueva = condicionBusquedaNueva.substring(0, condicionBusquedaNueva.length - 1)


                var queryNueva: String
                c = null
                cSec = null
                if (!searchBdApp) {
                    queryNueva = Querys.getEncuestasReferenciaCloneNew(DataCollector.getFormulario().codVersion, condicionBusquedaNueva, condicionPalabras)
                    Log.i("QUERY>>>", queryNueva)
                    c = DataCollector.getTempRepository().executeSelect(queryNueva, c)
                    loadAdapter(c, "bd_temp")
                }
                queryNueva = Querys.getEncuestasReferencia(DataCollector.getFormulario().codVersion, condicionBusquedaNueva, condicionPalabras)
                cSec = DataCollector.getRecolectorRepository().executeSelect(queryNueva, cSec)
                loadAdapter(cSec, "bd_app")
            }
            return true
        } catch (ex: Exception) {
            Log.i("Error", ex.message)
            return false
        }

    }

    private fun loadAdapter(c: Cursor?, _origen: String) {
        if (c == null) return
        val surveys = ArrayMap<String, SurveyDto>()
        while (c.moveToNext()) {
            val survey = SurveyDto()
            survey.CodEncuesta = c.getString(c.getColumnIndex("CodEncuesta"))
            survey.NombreEncuesta = c.getString(c.getColumnIndex("NombreEncuesta"))
            survey.JefeDeHogar = c.getString(c.getColumnIndex("resultado"))
            survey.OpcionSearch = "Encuesta " + Integer.toString(indiceFamilia)
            survey.NombreEstadoEncuesta = c.getString(c.getColumnIndex("EstadoEncuesta"))
            survey.FechaEncuesta = c.getString(c.getColumnIndex("FechaEncuesta"))
            Log.e("Survey date", survey.FechaEncuesta)
            survey.Origen = _origen
            survey.CodVivienda = c.getString(c.getColumnIndex("CodVivienda"))
            survey.FechaFinal = c.getString(c.getColumnIndex("FechaFinal"))
            survey.Nim = c.getString(c.getColumnIndex("NIM"))
            if (buscarEn != "temp") {
                survey.CodUsuarioRevisita = c.getString(c.getColumnIndex("CodUsuarioRevisita"))
            }
            if (surveys.containsKey(survey.CodEncuesta)) {
                val e = surveys.get(survey.CodEncuesta)
                e?.JefeDeHogar = e?.JefeDeHogar + "\n" + survey.JefeDeHogar
            } else {
                surveys.put(survey.CodEncuesta, survey)
                if (buscarEn != "temp") {
                    adapter?.surveyList?.add(survey)
                } else {
                    val ec = stream(adapter!!.surveyList!!).where { e -> e.CodVivienda == c.getString(c.getColumnIndex("CodVivienda")) }.firstOrNull()
                    if (ec == null) {
                        adapter?.surveyList?.add(survey)
                    } else {
                        val fechaFinalE = ec.FechaFinal
                        val fechaFinalE2 = c.getString(c.getColumnIndex("FechaFinal"))
                        try {
                            val format = SimpleDateFormat("yyyy-MM-dd HH:mm")
                            val d1 = format.parse(fechaFinalE)
                            val d2 = format.parse(fechaFinalE2)
                            if (d2.time > d1.time) {
                                adapter?.surveyList?.remove(ec)
                                adapter?.surveyList?.add(survey)
                            }
                        } catch (e: Exception) {
                            Log.i("Mario", e.message)
                        }

                    }
                }
                indiceFamilia++
            }
            cantidad_familias++
            var indice = 1
            for (e in adapter?.surveyList!!) {
                e.OpcionSearch = "Encuesta " + (indice++)
            }
        }
    }

    override fun onPostExecute(result: Boolean?) {
        super.onPostExecute(result)
        if (!this.isCancelled) {
            try {
                if (loading1!!.isShowing) {
                    loading1!!.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            adapter!!.notifyDataSetChanged()
            val intent = Intent("BUSQUEDA_FINISH")
            intent.putExtra("cantidad_familias", cantidad_familias)
            myContext.sendBroadcast(intent)
        }
    }

    override fun onPreExecute() {
        if (loading1 == null) {
            loading1 = ProgressDialog(myContext)
            loading1?.setTitle("Buscando coincidencias")
            loading1?.setMessage("Esto puede demorar algunos minutos.\nAguarde por favor...")
            loading1?.setCancelable(true)
            loading1?.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar") { dialog, which ->
                this@SearchSurveyManager.cancel(true)
                dialog.dismiss()
            }
            loading1?.show()
        }
    }

    override fun onCancelled() {
        try {
            if (loading1?.isShowing == true) {
                loading1?.dismiss()
            }
//            Message.info(myContext, "La busqueda fue cancelada", "Notificación")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
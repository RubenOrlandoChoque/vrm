package com.horus.vrmmobile.Activities

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.horus.vrmmobile.Adapters.EncuestaSyncByFormularioAdapter
import com.horus.vrmmobile.BuildConfig
import com.horus.vrmmobile.Config.Constant
import com.horus.vrmmobile.Helpers.Data
import com.horus.vrmmobile.Models.Dtos.ItemEncuestaSyncByFormulario
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Utils.Utils
import com.horus.vrmmobile.querys.Querys
import kotlinx.android.synthetic.main.activity_survey_sync.*
import mpi.dc.activity.LogActivity
import mpi.dc.clases.*
import mpi.dc.model.SyncAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SurveySyncActivity : AppCompatActivity(){
    private val itemForms = ArrayList<ItemEncuestaSyncByFormulario>()
    private var listaEncuestas = java.util.ArrayList<EncuestaSync>()
    private var rcAdapter: EncuestaSyncByFormularioAdapter? = null
    private val listaVersiones = ArrayList<String>()
    private var idEncabezado = ""
    private var eventBack = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey_sync)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.green)
        }

        rv_sincronizacion.layoutManager = LinearLayoutManager(this@SurveySyncActivity)
        rcAdapter = EncuestaSyncByFormularioAdapter(this@SurveySyncActivity, itemForms)
        rcAdapter?.setOnItemClickListener(object : EncuestaSyncByFormularioAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                itemForms[position].isSeleccionado = !itemForms[position].isSeleccionado
                rcAdapter?.notifyDataSetChanged()
            }
        })
        rv_sincronizacion.adapter = rcAdapter
        setButtons()
        loadDetalleSincronizacion()
        bottom_action_buttons.menu.findItem(R.id.action_refresh_sync).isVisible = false
        bottom_action_buttons.menu.findItem(R.id.action_back).isVisible = false
        bottom_action_buttons.menu.findItem(R.id.action_accept).isVisible = false
        if (itemForms.isEmpty()) {
            this@SurveySyncActivity.bottom_action_buttons.menu.findItem(R.id.action_sync_survey).isVisible = false
            this@SurveySyncActivity.bottom_action_buttons.menu.findItem(R.id.action_refresh_sync).isVisible = false
            this@SurveySyncActivity.bottom_action_buttons.menu.findItem(R.id.action_back).isVisible = true
            this@SurveySyncActivity.bottom_action_buttons.menu.findItem(R.id.action_accept).isVisible = false
            MaterialDialog(this).show {
                title(R.string.title)
                message(text = getString(R.string.not_survey_to_sync))
                positiveButton(R.string.accept)
                cancelOnTouchOutside(false)
            }
        }
    }

    private fun loadDetalleSincronizacion() {
        var c: Cursor? = null
        c = DataCollector.getRecolectorRepository().executeSelect(Querys.getItemSincronizacion(), c)
        itemForms.clear()
        while (c!!.moveToNext()) {
            val objSyncForm = ItemEncuestaSyncByFormulario()
            objSyncForm.encuestas = c.getInt(c.getColumnIndex("Encuestas"))
            objSyncForm.novedades = c.getInt(c.getColumnIndex("Imposibilitadas"))
            objSyncForm.puntosInteres = c.getInt(c.getColumnIndex("PuntoInteres"))
            objSyncForm.archivos = c.getInt(c.getColumnIndex("TotalMultimedia"))
            objSyncForm.nombreFormulario = c.getString(c.getColumnIndex("Version"))
            objSyncForm.codFormulario = c.getString(c.getColumnIndex("CodFormulario"))
            objSyncForm.codVersion = c.getString(c.getColumnIndex("CodVersion"))
            objSyncForm.isSeleccionado = true
            itemForms.add(objSyncForm)
        }
        c.close()
        pb_loading_survey_to_sync.visibility = View.GONE
    }

    private fun setButtons() {
        btn_lp_back.setOnClickListener { this.onBackPressed() }
        bottom_action_buttons.setOnNavigationItemSelectedListener { bottomActions(it) }
        bottom_action_buttons.setOnNavigationItemReselectedListener { bottomActions(it) }
    }

    private fun bottomActions(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sync_survey -> sincronizar()
            R.id.action_refresh_sync -> {
                val i = Intent(this@SurveySyncActivity, SurveySyncActivity::class.java)
                startActivity(i)
                finish()
            }
            R.id.action_accept -> finish()
            R.id.action_back -> finish()
        }
        return true
    }

    private fun seleccionFormulario(): Boolean {
        var rdo = false
        for (item in itemForms) {
            if (item.isSeleccionado) {
                rdo = true
                break
            }
        }
        return rdo
    }

    private fun sincronizar() {
        if (seleccionFormulario()) {
            initConfirm()
        } else {
            MaterialDialog(this).show {
                title(R.string.title)
                message(text = getString(R.string.message_no_form_selected_to_sync))
                positiveButton(R.string.accept)
                cancelOnTouchOutside(false)
            }
        }
    }

    private fun initConfirm() {
        MaterialDialog(this).show {
            title(R.string.confirmation)
            message(text = getString(R.string.confirm_sync_survey_and_multimedia))
            positiveButton(R.string.accept)
            negativeButton(R.string.cancel)
            positiveButton {
                Data.cleanContadores()
                if (Utils.isNetworkAvailable()) {
                    when (DataCollector.getFormulario().codTipoRelevamiento) {
                        Encuesta.CENSO, Encuesta.SEGUIMIENTO, Encuesta.ENCUESTA -> {
                            eventBack = false
                            this@SurveySyncActivity.bottom_action_buttons.menu.findItem(R.id.action_sync_survey).isVisible = false
                            pivotSincronizacionFormulario()
                        }
                        else -> {
                            MaterialDialog(this@SurveySyncActivity).show {
                                title(R.string.title)
                                message(text = getString(R.string.message_no_type_survey))
                                positiveButton(R.string.accept)
                                cancelOnTouchOutside(false)
                            }
                        }
                    }
                } else {
                    MaterialDialog(this@SurveySyncActivity).show {
                        title(R.string.title)
                        message(text = getString(R.string.message_no_internet_to_sync))
                        positiveButton(R.string.accept)
                        cancelOnTouchOutside(false)
                    }
                }
            }
            cancelOnTouchOutside(false)
        }
    }

    private fun pivotSincronizacionFormulario() {
        var pendienteSincronizar = false
        var codFormulario = ""
        var codVersion = ""
        for (z in itemForms.indices) {
            if (itemForms[z].isSeleccionado) {
                if (itemForms[z].estado == 0) {
                    pendienteSincronizar = true
                    codFormulario = itemForms[z].codFormulario
                    codVersion = itemForms[z].codVersion
                    itemForms[z].estado = 1
                    rcAdapter?.notifyDataSetChanged()
                    listaVersiones.add(codVersion)
                    break
                }
            }
        }

        if (!pendienteSincronizar) {
            sincronizando(false)
            onFinishSync()
            Data.cleanContadores()
            return
        }

        sincronizando(true)
        initSincronizacion(codFormulario, codVersion)
    }

    private fun initSincronizacion(codFormulario: String, codVersion: String) {
        listaEncuestas = ArrayList()

        idEncabezado = UUID.randomUUID().toString()
        DataCollector.getRecolectorRepository().executeSQLQuery(Querys.insertSyncLog(idEncabezado, codFormulario, "COD_WIFI"))
        val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date())
        var cursor: Cursor? = null
        cursor = DataCollector.getRecolectorRepository().executeSelect(Querys.selectDetalleEncuestasSincronizar(codVersion), cursor)
        while (cursor!!.moveToNext()) {
            val values = ContentValues()
            values.put("Id", UUID.randomUUID().toString())
            values.put("SyncLogId", idEncabezado)
            values.put("Dispositivo", Utils.getDeviceId())
            values.put("Zip", "")
            values.put("CodFormulario", codFormulario)
            values.put("SincronizarEncuestas", cursor.getInt(cursor.getColumnIndex("TotalEncuestas")))
            values.put("Completa", cursor.getInt(cursor.getColumnIndex("Completa")))
            values.put("FinalizadaIncompleta", cursor.getInt(cursor.getColumnIndex("FinalizadaIncompleta")))
            values.put("EnProceso", cursor.getInt(cursor.getColumnIndex("EnProceso")))
            values.put("Revisita", cursor.getInt(cursor.getColumnIndex("Revisita")))
            values.put("Imposibilitada", cursor.getInt(cursor.getColumnIndex("Imposibilitada")))
            values.put("PuntoInteres", cursor.getInt(cursor.getColumnIndex("PuntoInteres")))
            values.put("DePrueba", cursor.getInt(cursor.getColumnIndex("DePrueba")))
            values.put("Vacia", cursor.getInt(cursor.getColumnIndex("Vacia")))
            values.put("EncuestasSincronizadas", 0)
            values.put("NovedadesSincronizadas", 0)
            values.put("PuntoInteresSincronizadas", 0)
            values.put("SincronizarMultimedia", cursor.getInt(cursor.getColumnIndex("TotalMultimedia")))
            values.put("Imagenes", cursor.getInt(cursor.getColumnIndex("Imagen")))
            values.put("Audios", cursor.getInt(cursor.getColumnIndex("Audio")))
            values.put("Videos", cursor.getInt(cursor.getColumnIndex("Video")))
            values.put("MultimediaSincronizadas", 0)
            values.put("FechaEncuesta", cursor.getString(cursor.getColumnIndex("Fecha")))
            values.put("Fecha", fecha)

            DataCollector.getRecolectorRepository().insert("SyncDetailLog", values)
        }
        cursor.close()
        cursor = null

        listaEncuestas.clear()
        cursor = DataCollector.getRecolectorRepository().executeSelect(mpi.dc.Modulos.dataSync.Querys.getAllEncuestas(codVersion), cursor)
        while (cursor!!.moveToNext()) {
            val objEncuesta = EncuestaSync()
            objEncuesta.encuestaCod = cursor.getString(cursor.getColumnIndex("CodEncuesta"))
            objEncuesta.codUsuarioRevisita = cursor.getString(cursor.getColumnIndex("CodUsuarioRevisita"))
            objEncuesta.fechaEncuesta = cursor.getString(cursor.getColumnIndex("Fecha"))
            objEncuesta.codEstado = cursor.getString(cursor.getColumnIndex("Estado"))
            listaEncuestas.add(objEncuesta)
        }
        cursor.close()

        Data.total_registros = Data.total_registros + listaEncuestas.size


        Syncronizar(
                this,
                listaVersiones,
                object : SyncAdapter() {
                    override fun onErrorSyncLogSurvery(msg: String?) {
                        Data.listaerrores += msg
                        cambiarEstadoSincronizacion(codFormulario)
                    }

                    override fun onPivotSincronizar() {
                        val position = getPositionList(codFormulario)
                        if (listaEncuestas.size > 0) {
                            itemForms[position].isSyncEncuestas = true
                            rcAdapter?.notifyDataSetChanged()
                        } else {
                            itemForms[position].isSyncArchivos = true
                            rcAdapter?.notifyDataSetChanged()
                        }
                    }

                    override fun onSincronizarMultimediaDeletePreview() {
                        val position = getPositionList(codFormulario)
                        itemForms[position].isSyncEncuestas = false
                        itemForms[position].isSyncArchivos = true
                        rcAdapter?.notifyDataSetChanged()
                    }

                    override fun onSyncCurrentSurveyFinish(indiceEncuesta: Int) {
                        notificarUi(codFormulario, listaEncuestas[indiceEncuesta].codEstado, true)
                    }

                    override fun onErrorSyncCurrentSurvey(indiceEncuesta: Int, msg: String?) {
                        notificarUi(codFormulario, listaEncuestas[indiceEncuesta].codEstado, false)
                    }

                    override fun onSyncMultimediaError(multimediaSync: MultimediaSync?, error: String?) {
                        notificarUi(codFormulario, "COD_ARCHIVO", false)
                    }

                    override fun onSyncMultimediaFinish(multimediaSync: MultimediaSync?) {
                        notificarUi(codFormulario, "COD_ARCHIVO", true)
                    }

                    override fun onFinishSync(errores: Int, sincronizado: Int, listaErrores: String?, archivo_total: Int, archivo_sincronizado: Int, archivo_error: Int) {
                        Data.errores += errores
                        Data.sincronizado += sincronizado
                        Data.listaerrores += listaErrores
                        Data.archivo_total += archivo_total
                        Data.archivo_sincronizado += archivo_sincronizado
                        Data.archivo_error += archivo_error

                        val position = getPositionList(codFormulario)
                        itemForms[position].isSyncEncuestas = false
                        itemForms[position].isSyncArchivos = false
                        rcAdapter?.notifyDataSetChanged()
                        pivotSincronizacionFormulario()
                    }
                },
                Constant.urlSyncDataCollector,
                BuildConfig.VERSION_CODE
        ).sync()
    }

    private fun sincronizando(estado: Boolean) {
        for (itemForm in itemForms) {
            itemForm.isSyncronizando = estado
        }
        rcAdapter?.notifyDataSetChanged()
    }

    private fun cambiarEstadoSincronizacion(codFormulario: String) {
        for (item in itemForms) {
            if (item.codFormulario == codFormulario) {
                item.estado = 2
            }
        }
        val position = getPositionList(codFormulario)
        itemForms[position].isSyncEncuestas = false
        itemForms[position].isSyncArchivos = false
        rcAdapter?.notifyDataSetChanged()
        pivotSincronizacionFormulario()
    }

    private fun getPositionList(codFormulario: String): Int {
        var position = -1
        for (z in itemForms.indices) {
            if (itemForms[z].codFormulario == codFormulario) {
                position = z
                break
            }
        }
        return position
    }

    private fun notificarUi(codFormulario: String, codEstado: String, sincCorrecta: Boolean) {
        val position = getPositionList(codFormulario)
        when (codEstado) {
            "COD_COMPLETA", "COD_FINALIZADA_INCOMPLETA", "COD_EN_PROCESO", "COD_REVISITA", "COD_DE_PRUEBA" -> {
                if (!sincCorrecta)
                    itemForms[position].isEncuesta_error = true
                else
                    itemForms[position].encuestas_sync = itemForms[position].encuestas_sync + 1
                rcAdapter?.notifyDataSetChanged()
            }
            "COD_IMPOSIBILITADA" -> {
                if (!sincCorrecta)
                    itemForms[position].isNovedades_error = true
                else
                    itemForms[position].novedades_sync = itemForms[position].novedades_sync + 1
                rcAdapter?.notifyDataSetChanged()
            }
            "COD_PUNTO_INTERES" -> {
                if (!sincCorrecta)
                    itemForms[position].isPuntoInteres_error = true
                else
                    itemForms[position].puntosInteres_sync = itemForms[position].puntosInteres_sync + 1
                rcAdapter?.notifyDataSetChanged()
            }
            "COD_ARCHIVO" -> {
                if (!sincCorrecta)
                    itemForms[position].isArchivos_error = true
                else
                    itemForms[position].archivos_sync = itemForms[position].archivos_sync + 1
                rcAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun onFinishSync() {
        eventBack = true
        val total = Data.total_registros
        val sincronizados = Data.sincronizado
        val errores = Data.errores
        val archivosTotal = Data.archivo_total
        val archivosSincronizados = Data.archivo_sincronizado
        val archivosErrores = Data.archivo_error
        val messageError = Data.listaerrores

        val message: String
        bottom_action_buttons.menu.findItem(R.id.action_accept).isVisible = true
        if (messageError.isEmpty()) {
            message = "- Sincronización completa \n" +
                    "Total de registros: " + total + "\n" +
                    "Sincronizadas: " + sincronizados + "\n" +
                    "Errores en sincronización: " + errores + "\n" +
                    "- Multimedia \n" +
                    "Total archivos: " + archivosTotal + "\n" +
                    "Archivos sincronizados: " + archivosSincronizados + "\n" +
                    "Archivos no sincronizados: " + archivosErrores
            MaterialDialog(this@SurveySyncActivity).show {
                title(R.string.notification)
                message(text = message)
                positiveButton(R.string.accept)
                positiveButton { onBackPressed() }
                cancelOnTouchOutside(false)
            }
        } else {
            bottom_action_buttons.menu.findItem(R.id.action_refresh_sync).isVisible = true
            message = "- Sincronización completa con errores \n" +
                    "Total de registros: " + total + "\n" +
                    "Sincronizadas: " + sincronizados + "\n" +
                    "Errores en sincronización: " + errores + "\n" +
                    "- Multimedia \n" +
                    "Total archivos: " + archivosTotal + "\n" +
                    "Archivos sincronizados: " + archivosSincronizados + "\n" +
                    "Archivos no sincronizados: " + archivosErrores
            MaterialDialog(this@SurveySyncActivity).show {
                title(R.string.title)
                message(text = message)
                positiveButton(R.string.accept)
                negativeButton(R.string.log)
                negativeButton {
                    val nameFile = "log_sync_form_" + SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(Date()) + ".txt"
                    val path = Utils.generateTextFileOnSD("vrm/log", nameFile, if (messageError.isEmpty()) "Error no especificado" else messageError)
                    val intent = Intent(this@SurveySyncActivity, LogActivity::class.java)
                    intent.putExtra("path", path)
                    startActivity(intent)
                }
                cancelOnTouchOutside(false)
            }
        }


    }

    override fun onBackPressed() {
        if (eventBack)
            super.onBackPressed()
        return
    }
}

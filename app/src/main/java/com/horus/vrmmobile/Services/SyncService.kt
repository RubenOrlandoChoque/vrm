package com.horus.vrmmobile.services

import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.horus.vrmmobile.Config.Constant
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.HistoyChange
import com.horus.vrmmobile.Models.PersonHierarchicalStructure
import com.horus.vrmmobile.Repositories.*
import com.horus.vrmmobile.Utils.Event
import com.horus.vrmmobile.Utils.Task
import com.horus.vrmmobile.Utils.TaskIterator
import com.horus.vrmmobile.Utils.Utils
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

object SyncService {
    private val TAG = "SyncService"

    private var uploading = AtomicBoolean(false)
    private var downloading = AtomicBoolean(false)
    var synchronizing = AtomicBoolean(false)
    var myUploads = ArrayList<Long>()

    fun syncronizate(){
        if (synchronizing.get()) {
            Log.e("SyncService", "Bloqueo Sync")
            return
        }
        Log.e("SyncService", "Inicio Sync")
        synchronizing.set(true)
        val task = object: Task<String>(){
            override fun task(): String{
                try {
                    pull()
                }catch (e: Exception){
                    synchronizing.set(false)
                    Log.e("SyncService", "Fin Sync error")
                    Log.e(TAG, e.message)
                }
                return ""
            }
        }
        push(task)
    }

    fun push(taskAfter: Task<*>? = null){
        if (uploading.get()) {
            Log.e("SyncService", "Bloqueo Subida")
            return
        }
        Log.e("SyncService", "Inicio Subida")
        uploading.set(true)
        val historyChanges = HistoryChangeRepository.getPending()
        val contadorService = object : TaskIterator<HistoyChange>(historyChanges) {
            override fun actionEnd() {
                uploading.set(false)
                Log.e("SyncService", "Fin Subida")
                if (taskAfter != null) {
                    taskAfter.run()
                }else{
                    synchronizing.set(false)
                    Log.e("SyncService", "Fin Sync")
//                    FileSyncManager.uploadFiles()
                }
            }

            override fun actionItem(item: HistoyChange) {
                try {
                    val service = getServiceByModelName(item.ModelName)
                    if (service != null) {
                        val itemModel = service.getByIdAsJSONObject(item.ModelId)
                        if(itemModel != null){
                            service.uploadById(itemModel, item.ModelId, item.Action)
                        }else{
                            EventBus.getDefault().post(Event("upload_finish"))
                        }
                    }else{
                        EventBus.getDefault().post(Event("upload_finish"))
                    }
                }catch (e: Exception){
                    EventBus.getDefault().post(Event("upload_finish"))
                    Log.e(TAG, e.message)
                }
            }
        }
        contadorService.simultaneousWork = 1
        contadorService.eventNext = "upload_finish"
        contadorService.eventEnd = "upload_finish_total"
        contadorService.start()
    }

    fun pull(taskAfter: Task<*>? = null){
        if (downloading.get()) {
            Log.e("SyncService", "Bloqueo Descarga")
            return
        }
        Log.e("SyncService", "Inicio Descarga")
        downloading.set(true)
        val url = Constant.urlVRMServer + "api/history-changes/GetHistoryChange/${SharedConfig.getVersionData()}"
        AndroidNetworking.get(url)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Authorization","Bearer " + SharedConfig.getToken())
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        try {
                            if (response.getInt("ResultCode") == 1) {
                                val result = response.getJSONObject("Result")
                                val items = result.getJSONArray("Changes")
                                val contadorService = object : TaskIterator<JSONObject>(Utils.convertJSONArrayToList(items)) {
                                    override fun actionEnd() {
                                        SharedConfig.setVersionData(result.getString("LastVersion"))
                                        updateVersionsUpload(result.getLong("LastVersion"))
                                        EventBus.getDefault().post(Event("pull_finish"))
                                        finalizePull(taskAfter)
                                    }

                                    override fun actionItem(item: JSONObject){
                                        try {
                                            val service = getServiceByModelName(item.getString("ModelName"))
                                            if(service != null) {
                                                val action = item.getString("Action")
                                                if (action.equals("DELETE") ){
                                                    service.removeById(item.getString("ModelId"))
                                                }else {
                                                    service.downloadById(item.getString("ModelId"))
                                                }
                                            } else {
                                                EventBus.getDefault().post(Event("download_item_finish"))
                                            }
                                        } catch (e: Exception) {
                                            EventBus.getDefault().post(Event("download_item_finish"))
                                            Log.e(TAG, e.message)
                                        }
                                    }
                                }
                                contadorService.simultaneousWork = 1
                                contadorService.eventNext = "download_item_finish"
                                contadorService.start()
                            } else {
                                EventBus.getDefault().post(Event("pull_finish", response.getString("ResultMsg")))
                                finalizePull(taskAfter)
                                Log.e(TAG, response.getString("ResultMsg"))
                            }
                        } catch (e: Exception) {
                            EventBus.getDefault().post(Event("pull_finish", e.message))
                            finalizePull(taskAfter)
                            Log.e(TAG, e.message)
                        }
                    }

                    override fun onError(anError: ANError) {
                        EventBus.getDefault().post(Event("pull_finish", "Hubo un problema al conectarse con el servidor."))
                        finalizePull(taskAfter)
                        var messageError = Utils.getMessageError(anError)
                        messageError = if(messageError.isEmpty()) "Error al conectar con el servidor $url" else messageError
                        Log.e(TAG, messageError)
                    }
                })
    }

    private fun finalizePull(taskAfter: Task<*>? = null){
        downloading.set(false)
        Log.e("SyncService", "Fin Descarga")
        if (taskAfter != null) {
            taskAfter.run()
        }else{
            synchronizing.set(false)
            Log.e("SyncService", "Fin Sync")
//      FileSyncManager.uploadFiles()
        }
    }

    private fun updateVersionsUpload(lastVersion: Long){
        val versionsList  =  myUploads.filter{ s -> s > lastVersion }
        myUploads.clear()
        myUploads.addAll(versionsList)
    }

    private fun getServiceByModelName(modelName: String): RepositoryInterface<*>?{
        return when(modelName){
            "Event" -> EventRepository.instance
            "Multimedia" -> MultimediaRepository.instance
            "Person" -> PersonRepository.instance
            "Note" -> NoteRepository.instance
            "PersonHierarchicalStructure" -> PersonHierarchicalStructureRepository.instance
            "Partaker" -> PartakerRepository.instance
            "Action" -> ActionRepository.instance
            "ActionMultimedia" -> ActionMultimediaRepository.instance
            "Track" -> TrackRepository.instance
            "ContributionToTheObjective" -> ContributionToTheObjectiveRepository.instance
            "Message" -> MessageRepository.instance
            "MessageMultimedias" -> MessageMultimediaRepository.instance
            else -> null
        }
    }
}
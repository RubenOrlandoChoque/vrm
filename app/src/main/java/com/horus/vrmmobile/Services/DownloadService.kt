package com.horus.vrmmobile.services

import android.content.Context
import android.content.Intent
import com.horus.vrmmobile.Activities.InfoEventActivity
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.Config
import com.horus.vrmmobile.Repositories.*
import com.horus.vrmmobile.Utils.ModelSyncBuilder
import com.horus.vrmmobile.Utils.TaskIterator
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.runOnUiThread

object DownloadService {

    private fun getListTablesToDownload(): ArrayList<RepositoryInterface<*>> {
        val listDown = ArrayList<RepositoryInterface<*>>()
        listDown.add(EventTypeRepository.instance)
        listDown.add(EventStateRepository.instance)
        listDown.add(PositionRepository.instance)
        listDown.add(SexRepository.instance)
        listDown.add(PersonHierarchicalStructureRepository.instance)
        listDown.add(MultimediaTypeRepository.instance)
        listDown.add(MultimediaCategoryRepository.instance)
        listDown.add(EventRepository.instance)
        listDown.add(PersonRepository.instance)
        listDown.add(ActionMultimediaRepository.instance)
        listDown.add(MultimediaRepository.instance)
        listDown.add(NoteRepository.instance)
        listDown.add(ZonePoliticalFrontRepository.instance)
        listDown.add(ZoneRepository.instance)
        listDown.add(GeneralHierarchicalStructureRepository.instance)
        listDown.add(PartakerRepository.instance)
        listDown.add(PartakerTypeRepository.instance)
        listDown.add(ActionRepository.instance)
        listDown.add(ProjectRepository.instance)
        listDown.add(EventZoneRepository.instance)
        listDown.add(ObjectiveRepository.instance)
        listDown.add(MeasureTypeRepository.instance)
        listDown.add(ContributionToTheObjectiveRepository.instance)
        return listDown
    }

     fun getListTablesEventDownload(): ArrayList<RepositoryInterface<*>> {
        val listDown = ArrayList<RepositoryInterface<*>>()
        listDown.add(EventRepository.instance)
        listDown.add(EventZoneRepository.instance)
        listDown.add(ObjectiveRepository.instance)
        listDown.add(ProjectRepository.instance)
        return listDown
    }

    fun downloadCatalogs(context: Context, action: () -> Unit) {
        context.runOnUiThread {
            val dialog = context.indeterminateProgressDialog(message = "Descargando datos, por favor espere un momento...")
            dialog.setCancelable(false)
            dialog.show()

            val contadorService = object : TaskIterator<RepositoryInterface<*>>(getListTablesToDownload()) {
                override fun actionEnd() {
                    dialog.dismiss()
                    SharedConfig.setFirstDownloadFinish(true)
                    var sync = ModelSyncBuilder.create(Config())
                    sync.Synchronized = true
                    ConfigRepository.instance.addOrUpdate(sync, false)
                    action()
                }

                override fun actionItem(item: RepositoryInterface<*>) {
                    item.download()
                }
            }
            contadorService.simultaneousWork = 3
            contadorService.eventNext = "download_finish"
            contadorService.start()
        }
    }
}
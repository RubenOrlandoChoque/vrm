package com.horus.vrmmobile.Activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Adapters.PartakerAdapter
import com.horus.vrmmobile.Models.Action
import com.horus.vrmmobile.Models.Partaker
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.PartakerRepository
import com.horus.vrmmobile.Utils.DialogAddPartaker
import com.horus.vrmmobile.Utils.Utils
import kotlinx.android.synthetic.main.app_bar_info_event.view.*
import kotlinx.android.synthetic.main.content_action_attendees.*
import kotlinx.android.synthetic.main.content_action_attendees.view.*
import kotlinx.android.synthetic.main.content_action_organizers.*
import kotlinx.android.synthetic.main.content_action_organizers.view.*
import kotlinx.android.synthetic.main.content_action_other_partakers.*
import kotlinx.android.synthetic.main.content_action_other_partakers.view.*
import kotlinx.android.synthetic.main.content_action_speakers.*
import kotlinx.android.synthetic.main.content_action_speakers.view.*
import kotlinx.android.synthetic.main.fragment_action_partaker_list.*
import kotlinx.android.synthetic.main.fragment_action_partaker_list.view.*
import kotlinx.android.synthetic.main.view_zero_partaker.*
import kotlinx.android.synthetic.main.view_zero_partaker.view.*


class PartakerListFragment : Fragment() {

    private var action: Action? = null
    val attendeesList = ArrayList<Partaker>()
    val organizerList = ArrayList<Partaker>()
    val speakerList = ArrayList<Partaker>()
    val otherPartakersList = ArrayList<Partaker>()

    var btnAddPartaker: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val actionString = arguments?.getString("EVENT_PARAM")!!
        action = Utils.convertStringToObject(actionString, Action::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_action_partaker_list, container, false)

        action!!.Partakers.clear()
        attendeesList.clear()
        organizerList.clear()
        speakerList.clear()
        otherPartakersList.clear()

        btnAddPartaker = activity!!.findViewById<View>(R.id.icon_add) as Button


        action!!.Partakers.addAll(PartakerRepository.instance.getByActionId(action!!.Id))

        attendeesList.addAll(PartakerRepository.instance.getByIds(action!!.Partakers.filter { it.PartakerTypeId == "PTT_ATTENDEES" }.map { it.Id!! }))
        organizerList.addAll(PartakerRepository.instance.getByIds(action!!.Partakers.filter { it.PartakerTypeId == "PTT_ORGANIZER" }.map { it.Id!! }))
        speakerList.addAll(PartakerRepository.instance.getByIds(action!!.Partakers.filter { it.PartakerTypeId == "PTT_SPEAKER" }.map { it.Id!! }))
        otherPartakersList.addAll(PartakerRepository.instance.getByIds(action!!.Partakers.filter { it.PartakerTypeId != "PTT_ATTENDEES" && it.PartakerTypeId != "PTT_ORGANIZER" && it.PartakerTypeId != "PTT_SPEAKER" }.map { it.Id!! }))

        view.action_attendees_list.layoutManager = LinearLayoutManager(activity,
                RecyclerView.VERTICAL,
                false)
        view.action_attendees_list.adapter = object : PartakerAdapter(layoutInflater,
                attendeesList,
                R.layout.item_participant) {
            override fun onSelect(person: Partaker) {
                showScanDialog(person)
            }
        }

        view.action_organizer_list.layoutManager = LinearLayoutManager(activity,
                RecyclerView.VERTICAL,
                false)

        view.action_organizer_list.adapter = object : PartakerAdapter(layoutInflater,
                organizerList,
                R.layout.item_participant) {
            override fun onSelect(person: Partaker) {
                showScanDialog(person)
            }
        }

        view.action_speaker_list.layoutManager = LinearLayoutManager(activity,
                RecyclerView.VERTICAL,
                false)
        view.action_speaker_list.adapter = object : PartakerAdapter(layoutInflater,
                speakerList,
                R.layout.item_participant) {
            override fun onSelect(person: Partaker) {
                showScanDialog(person)
            }
        }

        view.action_other_partaker_list.layoutManager = LinearLayoutManager(activity,
                RecyclerView.VERTICAL,
                false)
        view.action_other_partaker_list.adapter = object : PartakerAdapter(layoutInflater,
                otherPartakersList,
                R.layout.item_participant) {
            override fun onSelect(person: Partaker) {
                showScanDialog(person)
            }
        }

        if (attendeesList.size > 0) {
            view.content_attendees.visibility = View.VISIBLE
        } else {
            view.content_attendees.visibility = View.GONE
        }

        if (organizerList.size > 0) {
            view.content_organizers.visibility = View.VISIBLE
        } else {
            view.content_organizers.visibility = View.GONE
        }

        if (speakerList.size > 0) {
            view.content_speakers.visibility = View.VISIBLE
        } else {
            view.content_speakers.visibility = View.GONE
        }

        if (otherPartakersList.size > 0) {
            view.content_other_partaker.visibility = View.VISIBLE
        } else {
            view.content_other_partaker.visibility = View.GONE
        }

        if( attendeesList.size == 0 && organizerList.size == 0 && speakerList.size == 0 && otherPartakersList.size == 0){
            view.txt_count_participants.visibility = View.GONE
            view.zero_partaker_view.visibility = View.VISIBLE
        }else {
            view.txt_count_participants.visibility = View.VISIBLE
            view.zero_partaker_view.visibility = View.GONE
        }

        view.txt_count_participants.text = "${attendeesList.size + organizerList.size + speakerList.size + otherPartakersList.size} personas"

        view.txt_list_count_attendees.text = "Asistentes (${attendeesList.size})"
        view.txt_list_count_organizer.text = "Organizadores (${organizerList.size})"
        view.txt_list_count_speakers.text = "Disertantes (${speakerList.size})"
        view.txt_list_count_other_partakers.text = "Otros perfiles (${speakerList.size})"

//        view.toolbar1.icon_add.setOnClickListener {
//            showScanDialog()
//        }

        btnAddPartaker!!.setOnClickListener { showScanDialog() }
        return view
    }

    private fun showScanDialog(partaker: Partaker? = null) {

        object : DialogAddPartaker(activity!!, action!!, partaker) {
            override fun onResult(partaker: Partaker?) {
                if (partaker != null) {
                    saveData(partaker)
                    remove(partaker.Id)
                    when (partaker.PartakerTypeId) {
                        "PTT_ATTENDEES" -> {
                            if (attendeesList.firstOrNull { it.Id == partaker.Id } == null) {
                                attendeesList.add(partaker)
                            }
//                            val tmp = attendeesList.sortedWith(compareBy({ it.FirstName }, { it.FirstSurname }))
//                            attendeesList.clear()
//                            attendeesList.addAll(tmp)
                            attendeesList.sortBy { it.FirstName }
                            (action_attendees_list.adapter as PartakerAdapter).setItems(attendeesList)
                            txt_list_count_attendees.text = "Participantes (${attendeesList.size})"
                            content_attendees.visibility = View.VISIBLE
                        }
                        "PTT_ORGANIZER" -> {
                            if (organizerList.firstOrNull { it.Id == partaker.Id } == null) {
                                organizerList.add(partaker)
                            }
                            organizerList.sortBy { it.FirstName }
                            (action_organizer_list.adapter as PartakerAdapter).setItems(organizerList)
                            txt_list_count_organizer.text = "Organizadores (${organizerList.size})"
                            content_organizers.visibility = View.VISIBLE
                        }
                        "PTT_SPEAKER" -> {
                            if (speakerList.firstOrNull { it.Id == partaker.Id } == null) {
                                speakerList.add(partaker)
                            }
                            speakerList.sortBy { it.FirstName }
                            (action_speaker_list.adapter as PartakerAdapter).setItems(speakerList)
                            txt_list_count_speakers.text = "Disertantes (${speakerList.size})"
                            content_speakers.visibility = View.VISIBLE
                        }
                        else -> {
                            if (otherPartakersList.firstOrNull { it.Id == partaker.Id } == null) {
                                otherPartakersList.add(partaker)
                            }
                            otherPartakersList.sortBy { it.FirstName }
                            (action_other_partaker_list.adapter as PartakerAdapter).setItems(otherPartakersList)
                            txt_list_count_other_partakers.text = "Disertantes (${otherPartakersList.size})"
                            content_other_partaker.visibility = View.VISIBLE
                        }
                    }
                    txt_count_participants.text = "${attendeesList.size + organizerList.size + speakerList.size + otherPartakersList.size} personas"
                    if( attendeesList.size == 0 && organizerList.size == 0 && speakerList.size == 0 && otherPartakersList.size == 0){
                        txt_count_participants.visibility = View.GONE
                        zero_partaker_view.visibility  =View.VISIBLE
                    }else {
                        txt_count_participants.visibility = View.VISIBLE
                        zero_partaker_view.visibility  =View.GONE
                    }
                }
            }
        }.show()
    }

    private fun saveData(partaker: Partaker) {
        PartakerRepository.instance.addOrUpdate(partaker)
    }

    private fun remove(id: String){
        val at = attendeesList.firstOrNull { it.Id == id }
        if(at != null) {
            attendeesList.remove(at)
        }
        val o = organizerList.firstOrNull { it.Id == id }
        if(o!=null) {
            organizerList.remove(o)
        }
        val s = speakerList.firstOrNull { it.Id == id }
        if(s!=null) {
            speakerList.remove(speakerList.firstOrNull { it.Id == id })
        }
        val ot = otherPartakersList.firstOrNull { it.Id == id }
        if(ot!=null) {
            otherPartakersList.remove(otherPartakersList.firstOrNull { it.Id == id })
        }

        (action_attendees_list.adapter as PartakerAdapter).notifyDataSetChanged()
        (action_organizer_list.adapter as PartakerAdapter).notifyDataSetChanged()
        (action_speaker_list.adapter as PartakerAdapter).notifyDataSetChanged()
        (action_other_partaker_list.adapter as PartakerAdapter).notifyDataSetChanged()

        content_attendees.visibility = if(attendeesList.size > 0) View.VISIBLE else View.GONE
        content_organizers.visibility = if(organizerList.size > 0) View.VISIBLE else View.GONE
        content_speakers.visibility = if(speakerList.size > 0) View.VISIBLE else View.GONE
        content_other_partaker.visibility = if(otherPartakersList.size > 0) View.VISIBLE else View.GONE

    }
}
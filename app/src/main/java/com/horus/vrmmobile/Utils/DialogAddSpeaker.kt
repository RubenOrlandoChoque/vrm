package com.horus.vrmmobile.Utils

import android.app.Activity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.horus.vrmmobile.Adapters.PersonAdapter
import com.horus.vrmmobile.Models.Partaker
import com.horus.vrmmobile.Models.Person
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.PersonHierarchicalStructureRepository
import com.horus.vrmmobile.Repositories.PersonRepository
import com.horus.vrmmobile.Repositories.PositionRepository
import org.jetbrains.anko.toast


abstract class DialogAddSpeaker(val context: Activity) {

    private var dialogAddSpeaker: MaterialDialog? = null
    private var speaker = Partaker()
    private var searchResultLayout: LinearLayout? = null
    private var person_list: RecyclerView? = null
    private var txtSearch: EditText? = null

    fun show() {
        showDialogSpeaker()
    }

    private fun showDialogSpeaker() {
        dialogAddSpeaker = MaterialDialog(context).show {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
            customView(viewRes = R.layout.dialog_add_speakers, scrollable = false)
            positiveButton(R.string.accept)
            negativeButton(R.string.cancel)
            neutralButton(R.string.clear)
            cancelOnTouchOutside(false)
            noAutoDismiss()
            neutralButton { clear() }
            positiveButton {
                saveValuesTextViews()
                if (isValid()) {
                    onResult(speaker)
                    it.dismiss()
                }
            }
            negativeButton {
                it.dismiss()
            }
        }

        val btnScan = dialogAddSpeaker!!.findViewById<Button>(R.id.btn_scan)
        btnScan.setOnClickListener { showScanDialog() }

        txtSearch = dialogAddSpeaker!!.findViewById(R.id.txt_search)
        txtSearch!!.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchString = txtSearch!!.text.toString()
                hideKeyboard(txtSearch!!)
                search(searchString)
                true
            } else {
                false
            }
        }

        searchResultLayout = dialogAddSpeaker!!.findViewById(R.id.expansionLayout_dialog)
        person_list = dialogAddSpeaker!!.findViewById(R.id.person_list)
        person_list!!.layoutManager = LinearLayoutManager(context,
                RecyclerView.VERTICAL,
                false)
        person_list!!.adapter = object : PersonAdapter(context.layoutInflater,
                ArrayList(),
                R.layout.item_participant) {
            override fun onSelect(person: Person) {
                txtSearch!!.setText("")
                setPerson(person)
                searchResultLayout?.visibility = View.GONE
            }
        }

        setValues()
    }

    private fun search(searchString: String) {
        val persons = PersonRepository.instance.getBySearchString(searchString)
        (person_list!!.adapter as PersonAdapter).setItems(persons)
        searchResultLayout?.visibility = View.VISIBLE
        (person_list!!.adapter as PersonAdapter).notifyDataSetChanged()

    }

    private fun isValid(): Boolean {
        /*if (speaker.FullName.isEmpty()) {
            context.toast("No ha ingresado el nombre y apellido.")
            return false
        }*/
        return true
    }

    private fun saveValuesTextViews() {
        //speaker.FullName = dialogAddSpeaker!!.findViewById<TextView>(R.id.txt_fullname).text.toString()
        speaker.Profession = dialogAddSpeaker!!.findViewById<TextView>(R.id.txt_profession).text.toString()
        speaker.Position = dialogAddSpeaker!!.findViewById<TextView>(R.id.txt_position).text.toString()
        speaker.PartakerTypeId = "PTT_SPEAKER"
    }

    private fun setValues() {
        //dialogAddSpeaker!!.findViewById<TextView>(R.id.txt_fullname).text = speaker.FullName
        //dialogAddSpeaker!!.findViewById<TextView>(R.id.txt_fullname).isEnabled = speaker.PersonId.isNullOrEmpty()
        dialogAddSpeaker!!.findViewById<TextView>(R.id.txt_profession).text = speaker.Profession
        dialogAddSpeaker!!.findViewById<TextView>(R.id.txt_position).text = speaker.Position
        dialogAddSpeaker!!.findViewById<TextView>(R.id.txt_position).visibility = if (speaker.Position.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    private fun clear() {
        speaker = Partaker()
        txtSearch!!.setText("")
        searchResultLayout?.visibility = View.GONE
        setValues()
    }

    abstract fun onResult(speaker: Partaker)

    private fun showScanDialog() {
        object : ScanDocument(context) {
            override fun onResult(person: Person) {
                txtSearch!!.setText("")
                setPerson(person)
                searchResultLayout?.visibility = View.GONE
            }
        }.show()
    }

    private fun hideKeyboard(view: View) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setPerson(person: Person) {
        //speaker.Person = person
        //speaker.PersonId = person.Id
        //speaker.FullName = person.toString()
        speaker.Position = ""
        speaker.Profession = ""
        val phs = PersonHierarchicalStructureRepository.instance.getByPersonId(person.Id)
        if (phs != null) {
            val position = PositionRepository.instance.getById(phs.PositionId!!)
            if (position != null) speaker.Position = position.Name
        }
        setValues()
    }
}
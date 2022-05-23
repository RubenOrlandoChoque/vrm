package com.horus.vrmmobile.Utils

import android.app.Activity
import android.app.DatePickerDialog
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.horus.vrmmobile.Adapters.CustomArrayAdapter
import com.horus.vrmmobile.Adapters.PersonAdapter
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.*
import com.horus.vrmmobile.Models.Event
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.*
import com.rengwuxian.materialedittext.MaterialEditText
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner
import org.jetbrains.anko.sdk27.coroutines.onFocusChange
import org.jetbrains.anko.toast
import java.util.*


abstract class DialogAddOrganizer(val context: Activity, val event: Event, var organizer: Partaker? = null) {

    private val c = Calendar.getInstance()
    private var _year = c.get(Calendar.YEAR)
    private var _month = c.get(Calendar.MONTH)
    private var _day = c.get(Calendar.DAY_OF_MONTH)

    private var dialogAddSpeaker: MaterialDialog? = null
    private var phs: PersonHierarchicalStructure? = null
    private var person: Person? = null
    private var searchResultLayout: LinearLayout? = null
    private var person_list: RecyclerView? = null
    private var txtSearch: EditText? = null

    private var cmb_sexs: MaterialBetterSpinner? = null
    private var cmb_positions: MaterialBetterSpinner? = null
    private var cmd_person_parent: MaterialBetterSpinner? = null
    private var txt_date: MaterialEditText? = null
    private val personHierarchicalStructures = PersonHierarchicalStructureRepository.instance.getAll()

    private val sexes = SexRepository.instance.getAll()
    private val positions = java.util.ArrayList<Position>()

    fun show() {
        if (organizer == null) {
            organizer = Partaker()
        }
        organizer?.ActionId = event.Id

        /*if (organizer?.PersonHierarchicalStructure == null) {
            phs = PersonHierarchicalStructureRepository.instance.getById(organizer!!.PersonHierarchicalStructureId!!)
            if (phs == null) phs = PersonHierarchicalStructure()
            organizer?.PersonHierarchicalStructure = phs
            organizer?.PersonHierarchicalStructureId = phs!!.Id
            organizer?.PersonHierarchicalStructure?.ZonePoliticalFrontId = SharedConfig.getZonePoliticalFrontId()
        }

        if (organizer?.PersonHierarchicalStructure?.Person == null) {
            person = PersonRepository.instance.getById(phs!!.PersonId)
            if (person == null) person = Person()
            organizer?.PersonHierarchicalStructure?.Person = person
        }*/
        showDialogSpeaker()
    }

    private fun showDialogSpeaker() {
        dialogAddSpeaker = MaterialDialog(context).show {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
            customView(viewRes = R.layout.dialog_add_organizer, scrollable = false)
            positiveButton(R.string.accept)
            negativeButton(R.string.cancel)
            neutralButton(R.string.clear)
            cancelOnTouchOutside(false)
            noAutoDismiss()
            neutralButton { clear() }
            positiveButton {
                saveValues()
                if (isValid()) {
                    onResult(organizer)
                    it.dismiss()
                }
            }
            negativeButton {
                it.dismiss()
            }
            neutralButton {
                clear()
            }
        }

        val btnScan = dialogAddSpeaker!!.findViewById<Button>(R.id.btn_scan)
        btnScan.setOnClickListener { showScanDialog() }

        txtSearch = dialogAddSpeaker!!.findViewById(R.id.txt_search)
        txtSearch!!.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchString = txtSearch!!.text.toString()
                hideKeyboard(txtSearch!!)
                Log.e("buscador", searchString)
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
                searchResultLayout!!.visibility = View.GONE
            }
        }

        cmb_sexs = dialogAddSpeaker!!.findViewById(R.id.cmb_sexs)
        cmb_positions = dialogAddSpeaker!!.findViewById(R.id.cmb_positions)
        cmd_person_parent = dialogAddSpeaker!!.findViewById(R.id.cmd_person_parent)
        txt_date = dialogAddSpeaker!!.findViewById(R.id.txt_date)

        loadCatalogs()
        setValues()
        setEvents()
    }

    private fun loadCatalogs() {
        // combo de sexos
        val sexAdapter = object : CustomArrayAdapter<Sex>(context, android.R.layout.simple_dropdown_item_1line, sexes) {
            override fun onSelectItem(item: Sex) {
                //organizer?.PersonHierarchicalStructure?.Person?.SexId = item.Id
            }
        }
        cmb_sexs?.setAdapter(sexAdapter)
        cmb_sexs?.onItemClickListener = sexAdapter

        // combor de cargos
        val positionsByPoliticalFront = GeneralHierarchicalStructureRepository.instance.getAllByField("PoliticalFrontId", SharedConfig.getPoliticalFrontId()).map { it.PositionId }.toList()
        positions.addAll(PositionRepository.instance.getByIds(positionsByPoliticalFront).sortedBy { it.Name })
        val positionAdapter = object : CustomArrayAdapter<Position>(context, android.R.layout.simple_dropdown_item_1line, positions) {
            override fun onSelectItem(item: Position) {
                //organizer?.PersonHierarchicalStructure?.PositionId = item.Id
            }
        }
        cmb_positions?.setAdapter(positionAdapter)
        cmb_positions?.onItemClickListener = positionAdapter

        // combo de superior en la jerarquia
        val persons = java.util.ArrayList<Person>()
        persons.addAll(personHierarchicalStructures.mapNotNull { it.Person }.sortedBy { it.toString() })
        val personAdapter = object : CustomArrayAdapter<Person>(context, android.R.layout.simple_dropdown_item_1line, persons) {
            override fun onSelectItem(item: Person) {
                val parent = personHierarchicalStructures.find { it.PersonId == item.Id }
                //organizer?.PersonHierarchicalStructure?.ParentId = parent!!.Id
                //organizer?.PersonHierarchicalStructure?.ZonePoliticalFrontId = parent.ZonePoliticalFrontId
            }
        }
        cmd_person_parent?.setAdapter(personAdapter)
        cmd_person_parent?.onItemClickListener = personAdapter
        cmd_person_parent?.visibility = if (persons.size > 0) View.VISIBLE else View.GONE
    }

    private fun setEvents() {
        txt_date?.onFocusChange { v, hasFocus ->
            if (hasFocus) showDatePickerDialog(v as EditText)
        }
    }

    private fun showDatePickerDialog(e: EditText) {
        hideKeyboard(e)
        e.clearFocus()
        /*val dpd = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            val text = "$dayOfMonth/${month + 1}/$year"
            organizer?.PersonHierarchicalStructure?.Person?.BirthDate = "$year-${month + 1}-$dayOfMonth"
            _year = year
            _month = month
            _day = dayOfMonth
            e.setText(text)
        }, _year, _month, _day)*/
        //dpd.show()
    }

    private fun search(searchString: String) {
        val persons = PersonRepository.instance.getBySearchString(searchString)
        (person_list!!.adapter as PersonAdapter).setItems(persons)
        searchResultLayout!!.visibility = View.VISIBLE
        (person_list!!.adapter as PersonAdapter).notifyDataSetChanged()
    }

    private fun isValid(): Boolean {
        /*if (organizer?.PersonHierarchicalStructure?.Person?.FirstName?.isEmpty()!!) {
            context.toast("No ha ingresado un nombre.")
            return false
        }
        if (organizer?.PersonHierarchicalStructure?.Person?.FirstSurname?.isEmpty()!!) {
            context.toast("No ha ingresado un apellido.")
            return false
        }
        if (organizer?.PersonHierarchicalStructure?.Person?.DocumentationNumber?.isEmpty()!!) {
            context.toast("No ha ingresado el número de documento.")
            return false
        }
        if (organizer?.PersonHierarchicalStructure?.Person?.BirthDate?.isEmpty()!!) {
            context.toast("No ha ingresado la fecha de nacimiento.")
            return false
        }
        if (organizer?.PersonHierarchicalStructure?.Person?.SexId?.isEmpty()!!) {
            context.toast("No ha seleccionado el sexo.")
            return false
        }
        if (organizer?.PersonHierarchicalStructure?.PositionId?.isNullOrEmpty()!!) {
            context.toast("No ha seleccionado el cargo.")
            return false
        }*/
//        if(cmd_person_parent.visibility == View.VISIBLE){
//            if(personHierarchicalStructure.ParentId.isNullOrEmpty()){
//                toast("No ha seleccionado el superior.")
//                return false
//            }
//        }
        return true
    }

    private fun saveValues() {
        /*organizer?.PersonHierarchicalStructure?.Person?.FirstName = dialogAddSpeaker!!.findViewById<MaterialEditText>(R.id.txt_name).text.toString().trim()
        organizer?.PersonHierarchicalStructure?.Person?.FirstSurname = dialogAddSpeaker!!.findViewById<MaterialEditText>(R.id.txt_last_name).text.toString().trim()
        organizer?.PersonHierarchicalStructure?.Person?.DocumentationNumber = dialogAddSpeaker!!.findViewById<MaterialEditText>(R.id.txt_document_number).text.toString().trim()


        var parentFullId: String? = ""
        if (organizer?.PersonHierarchicalStructure?.ParentId != null) {
            val e = PersonHierarchicalStructureRepository.instance.getById(organizer?.PersonHierarchicalStructure?.ParentId!!)
            parentFullId = e?.FullId
        }

        organizer?.PersonHierarchicalStructure?.FullId = if (!parentFullId.isNullOrEmpty()) parentFullId + '.' + organizer?.PersonHierarchicalStructure?.Id else organizer?.PersonHierarchicalStructure?.Id + ""
        organizer?.PersonHierarchicalStructure?.Person?.separateNames()

        organizer?.PersonHierarchicalStructure?.Person?.generateId()
        organizer?.PersonHierarchicalStructure?.PersonId = organizer?.PersonHierarchicalStructure?.Person?.Id!!*/
    }

    private fun setValues() {
        /*dialogAddSpeaker!!.findViewById<MaterialEditText>(R.id.txt_name).text = Editable.Factory.getInstance().newEditable(organizer?.PersonHierarchicalStructure?.Person?.FirstName + " " + organizer?.PersonHierarchicalStructure?.Person?.SecondName)
        dialogAddSpeaker!!.findViewById<MaterialEditText>(R.id.txt_last_name).text = Editable.Factory.getInstance().newEditable(organizer?.PersonHierarchicalStructure?.Person?.FirstSurname + " " + organizer?.PersonHierarchicalStructure?.Person?.SecondSurname)
        dialogAddSpeaker!!.findViewById<MaterialEditText>(R.id.txt_document_number).text = Editable.Factory.getInstance().newEditable(organizer?.PersonHierarchicalStructure?.Person?.DocumentationNumber)

        Log.e("document number", organizer?.PersonHierarchicalStructure?.Person?.DocumentationNumber)

        if (!organizer?.PersonHierarchicalStructure?.Person?.BirthDate!!.isEmpty()) {
            txt_date?.setText(DateUtils.convertToShortFormatSlash(organizer?.PersonHierarchicalStructure?.Person?.BirthDate!!))
            try {
                val c = Calendar.getInstance()
                c.time = DateUtils.convertStringShortToDateInverse(organizer?.PersonHierarchicalStructure?.Person?.BirthDate!!)
                _year = c.get(Calendar.YEAR)
                _month = c.get(Calendar.MONTH)
                _day = c.get(Calendar.DAY_OF_MONTH)
            } catch (e: Exception) {
                Log.e("setValues", e.message)
            }
        }

        val sex = sexes.find { it.Id == organizer?.PersonHierarchicalStructure?.Person?.SexId }
        if (sex != null) cmb_sexs?.setText(sex.toString())

        val position = positions.find { it.Id == organizer?.PersonHierarchicalStructure?.PositionId }
        if (position != null) cmb_positions?.setText(position.toString())

        val phs = personHierarchicalStructures.find { it.Id == organizer?.PersonHierarchicalStructure?.ParentId }
        if (phs != null) cmd_person_parent?.setText(phs.Person.toString())*/
    }

    private fun clear() {
//        speaker = Speaker()
        txtSearch!!.setText("")
        searchResultLayout!!.visibility = View.GONE
        setValues()
    }

    abstract fun onResult(organizer: Partaker?)

    private fun showScanDialog() {
        object : ScanDocument(context) {
            override fun onResult(person: Person) {
                txtSearch!!.setText("")
                setPerson(person)
                searchResultLayout!!.visibility = View.GONE
            }
        }.show()
    }

    private fun hideKeyboard(view: View) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setPerson(person: Person) {
        //organizer?.PersonHierarchicalStructure?.Person = person
        setValues()
    }
}
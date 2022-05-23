package com.horus.vrmmobile.Utils

import android.app.Activity
import android.app.DatePickerDialog
import android.text.Editable
import android.text.InputType
import android.text.method.DigitsKeyListener
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
import com.horus.vrmmobile.Models.*
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.PartakerTypeRepository
import com.horus.vrmmobile.Repositories.PersonRepository
import com.horus.vrmmobile.Repositories.SexRepository
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.rengwuxian.materialedittext.MaterialEditText
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner
import org.jetbrains.anko.sdk27.coroutines.onFocusChange
import org.jetbrains.anko.toast
import java.util.*


abstract class DialogAddPartaker(val context: Activity, val action: Action, var partaker: Partaker? = null) {

    private val c = Calendar.getInstance()
    private var _year = c.get(Calendar.YEAR)
    private var _month = c.get(Calendar.MONTH)
    private var _day = c.get(Calendar.DAY_OF_MONTH)

    private var dialogAddPartaker: MaterialDialog? = null
    private var searchResultLayout: LinearLayout? = null
    private var person_list: RecyclerView? = null
    private var txtSearch: EditText? = null

    private var cmb_sexs: MaterialBetterSpinner? = null
    private var cmb_positions: MaterialBetterSpinner? = null
    private var cmb_partaker_type: MaterialBetterSpinner? = null
    private var cmd_person_parent: MaterialBetterSpinner? = null
    private var txt_date: MaterialEditText? = null

    private val sexes = SexRepository.instance.getAll()
    private val partakerTypes = PartakerTypeRepository.instance.getAll()

    fun show() {
        if (partaker == null) {
            partaker = Partaker()
            partaker?.ActionId = action.Id
        }
        showDialogPartaker()
    }

    private fun showDialogPartaker() {
        dialogAddPartaker = MaterialDialog(context).show {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
            customView(viewRes = R.layout.dialog_add_partaker, scrollable = false)
            positiveButton(R.string.accept)
            negativeButton(R.string.cancel)
            neutralButton(R.string.clear)
            cancelOnTouchOutside(false)
            noAutoDismiss()
            neutralButton { clear() }
            positiveButton {
                saveValues()
                if (isValid()) {
                    onResult(partaker)
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

        val btnScan = dialogAddPartaker!!.findViewById<Button>(R.id.btn_scan)
        btnScan.setOnClickListener { showScanDialog() }

        txtSearch = dialogAddPartaker!!.findViewById(R.id.txt_search)
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

        searchResultLayout = dialogAddPartaker!!.findViewById(R.id.expansionLayout_dialog)
        person_list = dialogAddPartaker!!.findViewById(R.id.person_list)
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

        cmb_sexs = dialogAddPartaker!!.findViewById(R.id.cmb_sexs)
        cmb_positions = dialogAddPartaker!!.findViewById(R.id.cmb_positions)
        cmb_partaker_type = dialogAddPartaker!!.findViewById(R.id.cmb_partaker_type)
        cmd_person_parent = dialogAddPartaker!!.findViewById(R.id.cmd_person_parent)
        txt_date = dialogAddPartaker!!.findViewById(R.id.txt_date)

        cmb_positions?.visibility = View.GONE
        cmb_partaker_type?.visibility = View.VISIBLE
        cmd_person_parent?.visibility = View.GONE

        loadCatalogs()
        setValues()
        setEvents()
    }

    private fun loadCatalogs() {
        // combo de sexos
        val sexAdapter = object : CustomArrayAdapter<Sex>(context, android.R.layout.simple_dropdown_item_1line, sexes) {
            override fun onSelectItem(item: Sex) {
                partaker?.SexId = item.Id
            }
        }
        cmb_sexs?.setAdapter(sexAdapter)
        cmb_sexs?.onItemClickListener = sexAdapter

        val partakerTypeAdapter = object : CustomArrayAdapter<PartakerType>(context, android.R.layout.simple_dropdown_item_1line, partakerTypes) {
            override fun onSelectItem(item: PartakerType) {
                partaker?.PartakerTypeId = item.Id
            }
        }

        cmb_partaker_type?.setAdapter(partakerTypeAdapter)
        cmb_partaker_type?.onItemClickListener = partakerTypeAdapter
    }

    private fun setEvents() {
        txt_date?.onFocusChange { v, hasFocus ->
            if (hasFocus) showDatePickerDialog(v as EditText)
        }
        dialogAddPartaker!!.findViewById<EditText>(R.id.phone_code1).inputType = InputType.TYPE_CLASS_NUMBER
        dialogAddPartaker!!.findViewById<EditText>(R.id.phone_code1).keyListener = DigitsKeyListener.getInstance("1234567890")
        val listener = MaskedTextChangedListener.installOn(
                dialogAddPartaker!!.findViewById<EditText>(R.id.phone_code1),
                "[9900]",
                object : MaskedTextChangedListener.ValueListener {
                    override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                    }
                }
        )
        dialogAddPartaker!!.findViewById<EditText>(R.id.phone_code1).addTextChangedListener(listener)

        dialogAddPartaker!!.findViewById<EditText>(R.id.phone_number1).inputType = InputType.TYPE_CLASS_NUMBER
        dialogAddPartaker!!.findViewById<EditText>(R.id.phone_number1).keyListener = DigitsKeyListener.getInstance("1234567890")
        val listener2 = MaskedTextChangedListener.installOn(
                dialogAddPartaker!!.findViewById<EditText>(R.id.phone_number1),
                "15[99999990]",
                object : MaskedTextChangedListener.ValueListener {
                    override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                    }
                }
        )
        dialogAddPartaker!!.findViewById<EditText>(R.id.phone_number1).addTextChangedListener(listener2)

    }


    private fun showDatePickerDialog(e: EditText) {
        hideKeyboard(e)
        e.clearFocus()
        val dpd = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            val text = "$dayOfMonth/${month + 1}/$year"
            partaker?.BirthDate = "$year-${month + 1}-$dayOfMonth"
            _year = year
            _month = month
            _day = dayOfMonth
            e.setText(text)
        }, _year, _month, _day)
        dpd.show()
    }

    private fun search(searchString: String) {
        val persons = PersonRepository.instance.getBySearchString(searchString)
        (person_list!!.adapter as PersonAdapter).setItems(persons)
        searchResultLayout!!.visibility = View.VISIBLE
        (person_list!!.adapter as PersonAdapter).notifyDataSetChanged()
    }

    private fun isValid(): Boolean {
        if (partaker?.FirstName?.isEmpty()!!) {
            context.toast("No ha ingresado un nombre.")
            return false
        }
        if (partaker?.FirstSurname?.isEmpty()!!) {
            context.toast("No ha ingresado un apellido.")
            return false
        }
        if (partaker?.BirthDate?.isEmpty()!!) {
            context.toast("No ha ingresado la fecha de nacimiento.")
            return false
        }

        if (partaker?.SexId == null) {
            context.toast("No ha seleccionado el sexo.")
            return false
        }
        if (partaker?.PartakerTypeId?.isNullOrEmpty()!!) {
            context.toast("No ha seleccionado el perfil")
            return false
        }
        return true
    }

    private fun saveValues() {
        partaker?.FirstName = dialogAddPartaker!!.findViewById<MaterialEditText>(R.id.txt_name).text.toString().trim()
        partaker?.FirstSurname = dialogAddPartaker!!.findViewById<MaterialEditText>(R.id.txt_last_name).text.toString().trim()
        partaker?.DocumentationNumber = dialogAddPartaker!!.findViewById<MaterialEditText>(R.id.txt_document_number).text.toString().trim()
        partaker?.Email = dialogAddPartaker!!.findViewById<MaterialEditText>(R.id.txt_email).text.toString().trim()
        partaker?.Address = dialogAddPartaker!!.findViewById<MaterialEditText>(R.id.txt_address).text.toString().trim()
        partaker?.CountryCode = "+54"
        partaker?.AreaCode = dialogAddPartaker!!.findViewById<EditText>(R.id.phone_code1).text.toString().replaceFirst("^0+(?!$)".toRegex(), "")
        partaker?.LocalNumberPhone = (dialogAddPartaker!!.findViewById<EditText>(R.id.phone_number1).text.toString().replaceFirst("^15{1}(?!$)".toRegex(), ""))

        partaker?.separateNames()

    }

    private fun setValues() {
        dialogAddPartaker!!.findViewById<MaterialEditText>(R.id.txt_name).text = Editable.Factory.getInstance().newEditable(partaker?.FirstName + " " + partaker?.SecondName)
        dialogAddPartaker!!.findViewById<MaterialEditText>(R.id.txt_last_name).text = Editable.Factory.getInstance().newEditable(partaker?.FirstSurname + " " + partaker?.SecondSurname)
        dialogAddPartaker!!.findViewById<MaterialEditText>(R.id.txt_document_number).text = Editable.Factory.getInstance().newEditable(partaker?.DocumentationNumber)
        dialogAddPartaker!!.findViewById<MaterialEditText>(R.id.txt_email).text = Editable.Factory.getInstance().newEditable(partaker?.Email)
        dialogAddPartaker!!.findViewById<MaterialEditText>(R.id.txt_address).text = Editable.Factory.getInstance().newEditable(partaker?.Address)
        dialogAddPartaker!!.findViewById<EditText>(R.id.phone_code1).text = Editable.Factory.getInstance().newEditable(partaker?.AreaCode)
        dialogAddPartaker!!.findViewById<EditText>(R.id.phone_number1).text = Editable.Factory.getInstance().newEditable(partaker?.LocalNumberPhone)

        if (!partaker?.BirthDate!!.isEmpty()) {
            txt_date?.setText(DateUtils.convertToShortFormatSlash(partaker?.BirthDate!!))
            try {
                val c = Calendar.getInstance()
                c.time = DateUtils.convertStringShortToDateInverse(partaker?.BirthDate!!)
                _year = c.get(Calendar.YEAR)
                _month = c.get(Calendar.MONTH)
                _day = c.get(Calendar.DAY_OF_MONTH)
            } catch (e: Exception) {
                Log.e("setValues", e.message)
            }
        }

        val sex = sexes.find { it.Id == partaker?.SexId }
        if (sex != null) cmb_sexs?.setText(sex.toString())

        val partakerType = partakerTypes.find { it.Id == partaker?.PartakerTypeId }
        if (partakerType != null) cmb_partaker_type?.setText(partakerType.toString())
    }

    private fun clear() {
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
        partaker?.FirstName = person.FirstName
        partaker?.SecondName = person.SecondName
        partaker?.FirstSurname = person.FirstSurname
        partaker?.SecondSurname = person.SecondSurname
        partaker?.DocumentationNumber = person.DocumentationNumber
        partaker?.BirthDate = person.BirthDate
        partaker?.SexId = person.SexId
        setValues()
    }
}
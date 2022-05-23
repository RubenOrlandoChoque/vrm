package com.horus.vrmmobile.Activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.horus.vrmmobile.Adapters.CustomArrayAdapter
import com.horus.vrmmobile.Config.Constant
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.*
import com.horus.vrmmobile.Models.Dtos.AddPhsDto
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.*
import com.horus.vrmmobile.Utils.DateUtils
import com.horus.vrmmobile.Utils.ModelSyncBuilder
import com.horus.vrmmobile.Utils.ScanDocument
import com.horus.vrmmobile.Utils.Utils
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.android.synthetic.main.activity_add_member.*
import kotlinx.android.synthetic.main.app_bar_add_member.*
import kotlinx.android.synthetic.main.persons_data.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.sdk27.coroutines.onFocusChange
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class AddMemberActivity : AppCompatActivity() {

    private val c = Calendar.getInstance()
    private var _year = c.get(Calendar.YEAR)
    private var _month = c.get(Calendar.MONTH)
    private var _day = c.get(Calendar.DAY_OF_MONTH)
    private var savedInstanceState: Bundle? = null
    private var scanDocument: ScanDocument? = null
    private val sexes = SexRepository.instance.getAll()
    private val partakerTypes = PartakerTypeRepository.instance.getAll()
    private val personHierarchicalStructures = PersonHierarchicalStructureRepository.instance.getAll()
    private var personHierarchicalStructure = PersonHierarchicalStructure()
    private val positions = ArrayList<Position>()
    private var isEdition = false
    var btnSaveMember: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.savedInstanceState = savedInstanceState
        setContentView(R.layout.activity_add_member1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.menuColor)
        }

        loadPerson()
        loadCatalogs()
        setValues()
        setEvents()
        txt_address.visibility = View.GONE
        txt_email.visibility = View.GONE

        toolbarAddMember.title = "Nuevo Miembro"
        toolbarAddMember.setTitleTextColor(getResources().getColor(R.color.black))
        toolbarAddMember.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_black)
        toolbarAddMember.setNavigationOnClickListener{
            onBackPressed()
        }
    }

    private fun loadPerson() {
        if (intent.hasExtra("partakerId") && !intent.getStringExtra("partakerId").isNullOrEmpty()) {
            val phs: PersonHierarchicalStructure? = PersonHierarchicalStructureRepository.instance.getById(intent.getStringExtra("partakerId"))!!
            if (phs == null) {
                personHierarchicalStructure = ModelSyncBuilder.create(PersonHierarchicalStructure())
                personHierarchicalStructure.Person = ModelSyncBuilder.create(Person())
            } else {
                personHierarchicalStructure = phs
                personHierarchicalStructure.Person = PersonRepository.instance.getById(phs.PersonId)
            }
            isEdition = true
            txt_title.text = "Editar Miembro"
        } else {
            personHierarchicalStructure = ModelSyncBuilder.create(PersonHierarchicalStructure())
//            partaker.PersonHierarchicalStructure = ModelSyncBuilder.create(PersonHierarchicalStructure())
            personHierarchicalStructure.Person = ModelSyncBuilder.create(Person())
        }
    }

    private fun setValues() {
        val secondName = if (personHierarchicalStructure.Person!!.SecondName.isNullOrEmpty()) "" else " ${personHierarchicalStructure.Person!!.SecondName}"
        txt_name.setText("${personHierarchicalStructure.Person!!.FirstName}$secondName")
        val secondSurname = if (personHierarchicalStructure.Person!!.SecondSurname.isNullOrEmpty()) "" else " ${personHierarchicalStructure.Person!!.SecondSurname}"
        txt_last_name.setText("${personHierarchicalStructure.Person!!.FirstSurname}$secondSurname")
        txt_document_number.setText(personHierarchicalStructure.Person!!.DocumentationNumber)
        if (!personHierarchicalStructure.Person!!.BirthDate.isEmpty()) {
            txt_date.setText(DateUtils.convertToShortFormatSlash(personHierarchicalStructure.Person!!.BirthDate))
            try {
                val c = Calendar.getInstance()
                c.time = DateUtils.convertStringShortToDateInverse(personHierarchicalStructure.Person!!.BirthDate)
                _year = c.get(Calendar.YEAR)
                _month = c.get(Calendar.MONTH)
                _day = c.get(Calendar.DAY_OF_MONTH)
            } catch (e: Exception) {
                Log.e("setValues", e.message)
            }
        }

        if (!personHierarchicalStructure.Person!!.SexId.isNullOrEmpty()) {
            val sex = sexes.find { it.Id == personHierarchicalStructure.Person!!.SexId }
            if (sex != null) cmb_sexs.setText(sex.toString())
        }

        if (!personHierarchicalStructure.PositionId.isNullOrEmpty()) {
            val position = positions.find { it.Id == personHierarchicalStructure.PositionId }
            if (position != null) cmb_positions.setText(position.toString())
        }

        if (!personHierarchicalStructure.ParentId.isNullOrEmpty()) {
            val phs = personHierarchicalStructures.find { it.Id == personHierarchicalStructure.ParentId }
            if (phs != null) cmd_person_parent.setText(phs.Person.toString())
        }

//        if(!personHierarchicalStructure.isNullOrEmpty()){
//            var pt = partakerTypes.find { it.Id == personHierarchicalStructure.PartakerTypeId }
//            if(pt != null) cmb_partaker_type.setText(pt.toString())
//        }

        // fecha de naciminiento
        phone_code1.setText(if (personHierarchicalStructure.Person!!.AreaCode.isNullOrEmpty()) "" else " ${personHierarchicalStructure.Person!!.AreaCode}")
        phone_number1.setText(if (personHierarchicalStructure.Person!!.LocalNumberPhone.isNullOrEmpty()) "" else " ${personHierarchicalStructure.Person!!.LocalNumberPhone}")

        personHierarchicalStructure.Person!!.CountryCode = if (ccp.selectedCountryCode == "54") ccp.selectedCountryCode + "9" else ccp.selectedCountryCode
        personHierarchicalStructure.Person!!.AreaCode = phone_code1.text.toString().replaceFirst("^0+(?!$)".toRegex(), "")
        personHierarchicalStructure.Person!!.LocalNumberPhone = phone_number1.text.toString().replaceFirst("^15{1}(?!$)".toRegex(), "")
    }

    private fun isValid(): Boolean {
        if (personHierarchicalStructure.Person!!.FirstName.isEmpty()) {
            toast("No ha ingresado un nombre.")
            return false
        }
        if (personHierarchicalStructure.Person!!.FirstSurname.isEmpty()) {
            toast("No ha ingresado un apellido.")
            return false
        }
//        if (personHierarchicalStructure.Person!!.DocumentationNumber.isEmpty()) {
//            toast("No ha ingresado el número de documento.")
//            return false
//        }
        if (personHierarchicalStructure.Person!!.BirthDate.isEmpty()) {
            toast("No ha ingresado la fecha de nacimiento.")
            return false
        }
//        if (personHierarchicalStructure.Person!!.SexId.isNullOrEmpty()) {
////            toast("No ha seleccionado el sexo.")
////            return false
////        }

        val ountryCode = (if (ccp.selectedCountryCode == "54") ccp.selectedCountryCode + "9" else ccp.selectedCountryCode).trim()
        val areaCode = phone_code1.text.toString().replaceFirst("^0+(?!$)".toRegex(), "").trim()
        val localNumberPhone = phone_number1.text.toString().replaceFirst("^15{1}(?!$)".toRegex(), "").trim()

        if (ountryCode.isEmpty() || areaCode.isEmpty() || localNumberPhone.isEmpty()) {
            toast("El número de teléfono no tiene el formato correcto")
            return false
        }

        if (personHierarchicalStructure.PositionId.isNullOrEmpty()) {
            toast("No ha seleccionado el cargo.")
            return false
        }
        if (cmd_person_parent.visibility == View.VISIBLE) {
            if (personHierarchicalStructure.ParentId.isNullOrEmpty()) {
                toast("No ha seleccionado el superior.")
                return false
            }
        }
        return true
    }

    private fun loadCatalogs() {
        val sexAdapter = object : CustomArrayAdapter<Sex>(this@AddMemberActivity, android.R.layout.simple_dropdown_item_1line, sexes) {
            override fun onSelectItem(item: Sex) {
                personHierarchicalStructure.Person!!.SexId = item.Id
            }
        }
        cmb_sexs.setAdapter(sexAdapter)
        cmb_sexs.onItemClickListener = sexAdapter

        // traer positions segun political front id
        val myPhs = PersonHierarchicalStructureRepository.instance.getById(SharedConfig.getPersonHierarchicalStructureId())
        //var ghs1 = listStructure.filter { s -> s.PoliticalFrontId == SharedConfig.getPoliticalFrontId() && s.PositionId  == phs!!.PositionId }
        var myGhs = GeneralHierarchicalStructureRepository.instance.getAll().filter { s -> s.PoliticalFrontId == SharedConfig.getPoliticalFrontId() && s.PositionId == myPhs!!.PositionId }.first()
        var myGhsSup = GeneralHierarchicalStructureRepository.instance.getAll().filter { s -> s.Id == myGhs.ParentId }.first()
        val positionsByPoliticalFront = GeneralHierarchicalStructureRepository.instance.getAll().filter { s -> s.FullId.contains(myGhs.Id) }.map { it.PositionId }.toList()
        //val positionsByPoliticalFront1 = GeneralHierarchicalStructureRepository.instance.getAllByField("PoliticalFrontId", SharedConfig.getPoliticalFrontId()).map { it.PositionId }.toList()

        positions.addAll(PositionRepository.instance.getByIds(positionsByPoliticalFront).sortedBy { it.Name })
        val positionAdapter = object : CustomArrayAdapter<Position>(this@AddMemberActivity, android.R.layout.simple_dropdown_item_1line, positions) {
            override fun onSelectItem(item: Position) {
                personHierarchicalStructure.PositionId = item.Id
            }
        }
        cmb_positions.setAdapter(positionAdapter)
        cmb_positions.onItemClickListener = positionAdapter

        val partakerTypeAdapter = object : CustomArrayAdapter<PartakerType>(this@AddMemberActivity, android.R.layout.simple_dropdown_item_1line, partakerTypes) {
            override fun onSelectItem(item: PartakerType) {
//                personHierarchicalStructure?.PartakerTypeId = item.Id
            }
        }
        cmb_partaker_type.setAdapter(partakerTypeAdapter)
        cmb_partaker_type.onItemClickListener = partakerTypeAdapter
        cmb_partaker_type.visibility = View.GONE

        var persons = ArrayList<Person>()
        persons.addAll(personHierarchicalStructures.filter { s -> s.PositionId == myGhsSup.PositionId }.mapNotNull { it.Person }.sortedBy { it.toString() })
        if (persons == null)
            persons = ArrayList()
        val personAdapter = object : CustomArrayAdapter<Person>(this@AddMemberActivity, android.R.layout.simple_dropdown_item_1line, persons) {
            override fun onSelectItem(item: Person) {
                val parent = personHierarchicalStructures.find { it.PersonId == item.Id }
                personHierarchicalStructure.ParentId = parent!!.Id
                personHierarchicalStructure.ZonePoliticalFrontId = parent.ZonePoliticalFrontId
            }
        }
        cmd_person_parent.setAdapter(personAdapter)
        cmd_person_parent.onItemClickListener = personAdapter
        if (persons.size == 0)
            cmd_person_parent.visibility = View.GONE
    }

    private fun setEvents() {
        txt_date.onFocusChange { v, hasFocus ->
            if (hasFocus) showDatePickerDialog(v as EditText)
        }

        btnSaveMember = findViewById<View>(R.id.icon_add_Member) as Button
        btnSaveMember!!.setOnClickListener {
            saveValuesTextViews()
            save()
        }

        btn_scan.setOnClickListener { showScanDialog() }

        phone_code1.inputType = InputType.TYPE_CLASS_NUMBER
        phone_code1.keyListener = DigitsKeyListener.getInstance("1234567890")
        val listener = MaskedTextChangedListener.installOn(
                phone_code1,
                "[9900]",
                object : MaskedTextChangedListener.ValueListener {
                    override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                    }
                }
        )
        phone_code1.addTextChangedListener(listener)

        phone_number1.inputType = InputType.TYPE_CLASS_NUMBER
        phone_number1.keyListener = DigitsKeyListener.getInstance("1234567890")
        val listener2 = MaskedTextChangedListener.installOn(
                phone_number1,
                "15[99999990]",
                object : MaskedTextChangedListener.ValueListener {
                    override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                    }
                }
        )
        phone_number1.addTextChangedListener(listener2)
    }

    private fun saveValuesTextViews() {
        personHierarchicalStructure.Person!!.FirstName = txt_name.text.toString()
        personHierarchicalStructure.Person!!.FirstSurname = txt_last_name.text.toString()
        personHierarchicalStructure.Person!!.DocumentationNumber = txt_document_number.text.toString()
        personHierarchicalStructure.Person!!.CountryCode = if (ccp.selectedCountryCode == "54") ccp.selectedCountryCode + "9" else ccp.selectedCountryCode
        personHierarchicalStructure.Person!!.AreaCode = phone_code1.text.toString().replaceFirst("^0+(?!$)".toRegex(), "")
        personHierarchicalStructure.Person!!.LocalNumberPhone = phone_number1.text.toString().replaceFirst("^15{1}(?!$)".toRegex(), "")
    }

    private fun save() {
        runOnUiThread {
            val dialog = indeterminateProgressDialog(message = "Descargando datos, por favor espere un momento...")
            dialog.setCancelable(false)
            dialog.show()

            if (isValid()) {
                personHierarchicalStructure.Person!!.separateNames()
                personHierarchicalStructure.ZonePoliticalFrontId = SharedConfig.getZonePoliticalFrontId()
                if (!isEdition) {
                    personHierarchicalStructure.Person!!.generateId()
                    personHierarchicalStructure.PersonId = personHierarchicalStructure.Person!!.Id
//                personHierarchicalStructure.PersonHierarchicalStructureId = personHierarchicalStructure.PersonHierarchicalStructure!!.Id
                    var phsUser = PersonHierarchicalStructureRepository.instance.getById(SharedConfig.getPersonHierarchicalStructureId())
                    if (phsUser != null) {
                        if (!phsUser.FullId.isNullOrEmpty()) {
                            personHierarchicalStructure.FullId = phsUser.FullId + "." + personHierarchicalStructure.Id
                        } else {
                            personHierarchicalStructure.FullId = personHierarchicalStructure.Id
                        }
                    } else
                        personHierarchicalStructure.FullId = personHierarchicalStructure.Id
                }

                // prepare data
                val myPhs = PersonHierarchicalStructureRepository.instance.getById(SharedConfig.getPersonHierarchicalStructureId())
                val myGhs = GeneralHierarchicalStructureRepository.instance.getAll().filter { s -> s.PoliticalFrontId == SharedConfig.getPoliticalFrontId() && s.PositionId == myPhs!!.PositionId }.first()
                val zpf = ZonePoliticalFrontRepository.instance.getById(SharedConfig.getZonePoliticalFrontId())

                val addPhsDto = AddPhsDto()
                addPhsDto.GhsId = myGhs.Id
                addPhsDto.ZoneId = zpf?.ZoneId ?: ""

                addPhsDto.PhsDto.Id = personHierarchicalStructure.Id
                addPhsDto.PhsDto.FullId = personHierarchicalStructure.FullId
                addPhsDto.PhsDto.ParentId = personHierarchicalStructure.ParentId
                val p = personHierarchicalStructure.Person
                if (p != null) {
                    addPhsDto.PhsDto.Person.Id = p.Id
                    addPhsDto.PhsDto.Person.FirstSurname = p.FirstSurname
                    addPhsDto.PhsDto.Person.SecondSurname = if (p.SecondSurname != null) p.SecondSurname!! else ""
                    addPhsDto.PhsDto.Person.FirstName = p.FirstName
                    addPhsDto.PhsDto.Person.SecondName = if (p.SecondName != null) p.SecondName!! else ""
                    addPhsDto.PhsDto.Person.DocumentationNumber = p.DocumentationNumber
                    addPhsDto.PhsDto.Person.BirthDate = p.BirthDate
                    addPhsDto.PhsDto.Person.IdentificationTypeId = p.IdentificationTypeId
                    addPhsDto.PhsDto.Person.PersonTypeId = p.PersonTypeId
                    addPhsDto.PhsDto.Person.MaritialStatusId = p.MaritialStatusId
                    addPhsDto.PhsDto.Person.ReligionId = p.ReligionId
                    addPhsDto.PhsDto.Person.SexId = p.SexId
                    addPhsDto.PhsDto.Person.GenderId = p.GenderId
                    addPhsDto.PhsDto.Person.NationalityId = p.NationalityId
                    addPhsDto.PhsDto.Person.CountryCode = p.CountryCode
                    addPhsDto.PhsDto.Person.AreaCode = p.AreaCode
                    addPhsDto.PhsDto.Person.LocalNumberPhone = p.LocalNumberPhone
                    addPhsDto.PhsDto.Position.Id = if (personHierarchicalStructure.PositionId != null) personHierarchicalStructure.PositionId!! else ""
                    val position = positions.find { it.Id == personHierarchicalStructure.PositionId }
                    addPhsDto.PhsDto.Position.Name = if (position?.Name != null) position.Name else ""

                    // usuario mobile por defecto
                    addPhsDto.PhsDto.Person.IsUser = true
                    addPhsDto.PhsDto.Person.UserName = addPhsDto.PhsDto.Person.CountryCode + addPhsDto.PhsDto.Person.AreaCode + addPhsDto.PhsDto.Person.LocalNumberPhone
                    addPhsDto.PhsDto.Person.RoleId = "mobile"
                    addPhsDto.PhsDto.Person.Password = addPhsDto.PhsDto.Person.CountryCode + addPhsDto.PhsDto.Person.AreaCode + addPhsDto.PhsDto.Person.LocalNumberPhone
                    addPhsDto.PhsDto.Person.ConfirmPassword = addPhsDto.PhsDto.Person.CountryCode + addPhsDto.PhsDto.Person.AreaCode + addPhsDto.PhsDto.Person.LocalNumberPhone
                    addPhsDto.PhsDto.Person.PoliticalFrontId = SharedConfig.getPoliticalFrontId()
                }

                val url = Constant.urlVRMServer + if(isEdition) "api/phs/editmobile" else "api/phs/add"

                val jo = JSONObject(Utils.convertObjectToString(addPhsDto))
                val method =if(isEdition)  AndroidNetworking.put(url) else AndroidNetworking.post(url)
                method
                        .addHeaders("Content-Type", "application/json")
                        .addHeaders("Authorization", "Bearer " + SharedConfig.getToken())
                        .addJSONObjectBody(jo)
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject) {
                                dialog.dismiss()
                                Log.e("resultl", "success")
//                                val phs = PersonHierarchicalStructureRepository.instance.getByPersonId(personHierarchicalStructure.PersonId)
//                                if (phs != null) {
//                                    object : SweetAlert(this, SweetAlertDialog.ERROR_TYPE, "Aviso", "Ya existe este miembro.") {
//                                        override fun onClose() {
//                                        }
//                                    }.show()
//                                    return
//                                }
                                if (PersonHierarchicalStructureRepository.instance.addOrUpdate(personHierarchicalStructure, false)) {
                                    PersonRepository.instance.addOrUpdate(personHierarchicalStructure.Person!!, false)
                                    MaterialDialog(this@AddMemberActivity).show {
                                        title(R.string.success)
                                        message(text = getString(R.string.message_data_saved_successfully))
                                        positiveButton(R.string.accept)
                                        positiveButton {
                                            val i = intent
                                            this@AddMemberActivity.setResult(Activity.RESULT_OK, i)
                                            this@AddMemberActivity.finish()
                                        }
                                        cancelOnTouchOutside(false)
                                    }
                                }
                            }

                            override fun onError(anError: ANError) {
                                // try get error
                                dialog.dismiss()
                                var error = getString(R.string.message_problem_sync_phs)
                                if (!Utils.isNetworkAvailable()) {
                                    error = getString(R.string.title_no_internet_verify)
                                }
                                try {
                                    val obj = JSONObject(anError.errorBody)
                                    error = obj.getString("Message")
                                } catch (e: Exception) {
                                }
                                Log.e("url", anError.errorDetail)
                                MaterialDialog(this@AddMemberActivity).show {
                                    title(R.string.title)
                                    message(text = error)
                                    positiveButton(R.string.accept)
                                    cancelOnTouchOutside(false)
                                }
                            }
                        })
            } else {
                dialog.dismiss()
            }
        }
    }

    private fun showDatePickerDialog(e: EditText) {
        hideKeyboard()
        e.clearFocus()
        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            val text = "$dayOfMonth/${month + 1}/$year"
            personHierarchicalStructure.Person!!.BirthDate = "$year-${month + 1}-$dayOfMonth"
            _year = year
            _month = month
            _day = dayOfMonth
            e.setText(text)
        }, _year, _month, _day)
        dpd.show()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = currentFocus
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showScanDialog() {
        scanDocument = object : ScanDocument(this) {
            override fun onResult(person: Person) {
                personHierarchicalStructure.Person = person
                setValues()
            }
        }
        scanDocument!!.show()
    }

    override fun onPause() {
        super.onPause()
        if (scanDocument != null) scanDocument!!.close()
    }
}
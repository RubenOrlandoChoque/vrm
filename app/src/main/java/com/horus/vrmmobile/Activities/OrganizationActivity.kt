package com.horus.vrmmobile.Activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Adapters.MemberAdapter
import com.horus.vrmmobile.R
import kotlinx.android.synthetic.main.activity_organization.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.Dtos.PersonDto
import com.horus.vrmmobile.Models.Partaker
import com.horus.vrmmobile.Models.PersonHierarchicalStructure
import com.horus.vrmmobile.Repositories.PartakerRepository
import com.horus.vrmmobile.Repositories.PersonHierarchicalStructureRepository
import com.horus.vrmmobile.Repositories.PersonRepository
import com.horus.vrmmobile.Repositories.PositionRepository
import com.horus.vrmmobile.Utils.BlockUI
import com.horus.vrmmobile.Utils.ModelSyncBuilder
import com.horus.vrmmobile.Utils.Task
import kotlinx.android.synthetic.main.app_bar_organization.*

class OrganizationActivity : AppCompatActivity() {

    private var items = ArrayList<PersonDto>()
    private var itemsFilters = ArrayList<PersonDto>()
    private var itemsFiltersText = ArrayList<PersonDto>()
    private var positions = ArrayList<String>()
    private var personHierarchicalStructure = ArrayList<PersonHierarchicalStructure>()
    private var positionMap = HashMap<String, String>()
    private val ADD_MEMBER_REQUEST = 451
    private lateinit var blockUI: BlockUI
    var btnAddMember: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.menuColor)
        }

        blockUI = BlockUI(this)
        blockUI.start("Cargando...")
        object : Task<String>() {
            override fun task(): String {
                loadPartakers()
                positionMap.putAll(PositionRepository.instance.getAll().map { s -> s.Id to s.Name }.toMap())
                this@OrganizationActivity.runOnUiThread {
                    loadMembers()
                    loadPositions()
                    blockUI.stop()
                }
                return ""
            }
        }.run()
        setButtons()
        setToolbar()
    }

    private fun setToolbar(){
        toolbarOrganization.title = "RRHH"
        toolbarOrganization.setTitleTextColor(getResources().getColor(R.color.black))
        toolbarOrganization.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_black)
        toolbarOrganization.setNavigationOnClickListener{
            onBackPressed()
        }
    }

    private fun loadPartakers() {
        personHierarchicalStructure.clear()
        val phs = PersonHierarchicalStructureRepository.instance.getAllByField("ZonePoliticalFrontId", SharedConfig.getZonePoliticalFrontId())
//        partakers = PartakerRepository.instance.getAll().distinctBy { Pair(it.PersonId, it.PersonId) } as ArrayList<Partaker>
        for (p in phs) {
//            p.PersonHierarchicalStructure = PersonHierarchicalStructureRepository.instance.getById(p.PersonHierarchicalStructureId)
            p.Person = PersonRepository.instance.getById(p.PersonId)
        }
        personHierarchicalStructure.addAll(phs)
    }

    private fun loadPositions() {
        val dataAdapter = ArrayAdapter(this,
                R.layout.spinner_position_item, positions)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roles.adapter = dataAdapter

        roles.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                filter(positions[position])
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
            }
        }
    }

    private fun loadMembers() {
        items.clear()
        items.addAll(personHierarchicalStructure.map { s -> PersonDto(s.Person!!, positionMap[s.PositionId]!!) }.sortedBy { s -> "${s.FirstName} ${s.FirstSurname}" })
        positions.add("Todos los cargos")
        positions.addAll(items.map { s -> s.Position }.distinct().sorted())

        member_list.layoutManager = LinearLayoutManager(this,
                RecyclerView.VERTICAL,
                false)
        member_list.adapter = object : MemberAdapter(this, layoutInflater,
                items,
                R.layout.item_member_2) {
            override fun onEdit(person: PersonDto) {
                onEditMember(person)
            }

            override fun onDelete(person: PersonDto) {
                showDeleteDialog(person)
            }
        }
    }

    private fun onEditMember(person: PersonDto) {
        val i = Intent(this@OrganizationActivity, AddMemberActivity::class.java)
        val phs = personHierarchicalStructure.find { s -> s.PersonId == person.Id }
        i.putExtra("partakerId", phs!!.Id)
        startActivityForResult(i, ADD_MEMBER_REQUEST)
    }

    private fun showDeleteDialog(person: PersonDto) {
        MaterialDialog(this@OrganizationActivity).show {
            title(R.string.title)
            message(text = getString(R.string.message_delete, person.toString()))
            positiveButton(R.string.accept)
            negativeButton(R.string.cancel)
            positiveButton {
                var phs = personHierarchicalStructure.find { s -> s.Person!!.Id == person.Id }
                if (phs != null) {
                    phs = ModelSyncBuilder.delete(phs)
                    PersonHierarchicalStructureRepository.instance.addOrUpdate(phs)
                    refresh()
                }
            }
        }
    }

    private fun refresh() {
        loadPartakers()
        items.clear()
        items.addAll(personHierarchicalStructure.map { s -> PersonDto(s.Person!!, positionMap[s.PositionId]!!) }.sortedBy { s -> "${s.FirstName} ${s.FirstSurname}" })
        txt_search.setText("")
        filter()
    }

    private fun filter(position: String = "Todos los cargos") {
        itemsFilters.clear()
        if (position == "Todos los cargos") {
            itemsFilters.addAll(items)
        } else {
            itemsFilters.addAll(items.filter { s -> s.Position == position })
        }
        filterText()
    }

    private fun filterText() {
        itemsFiltersText.clear()
        if (txt_search.text.isNullOrEmpty()) {
            itemsFiltersText.addAll(itemsFilters)
        } else {
            val textSearch = txt_search.text.toString().toUpperCase()
            itemsFiltersText.addAll(itemsFilters.filter { s -> s.FirstName.toUpperCase().contains(textSearch) || s.FirstSurname.toUpperCase().contains(textSearch) })
        }
        (member_list.adapter as MemberAdapter).setItems(itemsFiltersText)
    }

    private fun setButtons() {
        btnAddMember = findViewById<View>(R.id.icon_add_Member) as Button
        btnAddMember!!.setOnClickListener {
            val i = Intent(this, AddMemberActivity::class.java)
            startActivityForResult(i, ADD_MEMBER_REQUEST)
        }
//        btn_back.setOnClickListener {
//            this.onBackPressed()
//        }
//
//        btn_add.setOnClickListener {
//            val i = Intent(this, AddMemberActivity::class.java)
//            startActivityForResult(i, ADD_MEMBER_REQUEST)
//        }
//
//        icon_org.setOnClickListener {
//            val i = Intent(this, OrganizationTreeActivity::class.java)
//            startActivity(i)
//        }

        txt_search.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                filterText()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ADD_MEMBER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                refresh()
            }
        }
    }
}
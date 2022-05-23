package com.horus.vrmmobile.Activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.bkhezry.extramaputils.builder.ViewOptionBuilder
import com.github.bkhezry.extramaputils.model.ViewOption
import com.github.bkhezry.extramaputils.utils.MapUtils
import com.github.bkhezry.mapdrawingtools.model.DrawingOption
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.horus.vrmmobile.Adapters.ObjectivesAdapter
import com.horus.vrmmobile.Models.Action
import com.horus.vrmmobile.Models.ContributionToTheObjective
import com.horus.vrmmobile.Models.Event
import com.horus.vrmmobile.Models.EventZone
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.*
import com.horus.vrmmobile.Utils.DateUtils
import com.horus.vrmmobile.Utils.GeometryUtils
import com.horus.vrmmobile.Utils.ModelSyncBuilder
import com.horus.vrmmobile.Utils.Utils
import kotlinx.android.synthetic.main.app_bar_info_event.*
import kotlinx.android.synthetic.main.content_action_objectives.view.*
import kotlinx.android.synthetic.main.content_action_speakers.view.*
import kotlinx.android.synthetic.main.content_event_fields.view.*
import kotlinx.android.synthetic.main.content_event_map.view.*
import kotlinx.android.synthetic.main.fragment_info_event.view.*
import org.jetbrains.anko.support.v4.toast

class InfoEventFragment(eventParam: (actionName: String) -> Unit) : Fragment(), OnMapReadyCallback {

    private var action: Action? = null
    private var mMap: GoogleMap? = null
    private val positionDefault = LatLng(-24.789105, -65.412024)
    private lateinit var mapView: MapView
    private var isFirst: Boolean = true
    var btnAddAction: Button? = null
    var contributions: ArrayList<ContributionToTheObjective> = ArrayList()
    var event: ((actionName: String) -> Unit)? = null
    init {
        event = eventParam
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val actionString = arguments?.getString("EVENT_PARAM")!!
        action = Utils.convertStringToObject(actionString, Action::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
//        view?.play_action?.visibility = View.GONE

        isFirst = true
        val view = inflater.inflate(R.layout.fragment_info_event, container, false)

        val actionZone: EventZone? = EventZoneRepository.instance.getById(action!!.EventZonesId!!)
        val event: Event? = EventRepository.instance.getById(actionZone!!.EventId)
        val eventType = EventTypeRepository.instance.getById(event!!.EventTypeId)
        if (eventType != null) view.txt_event_type.text = eventType.Name

        view.txt_event_type.text = "Evento: " + event.EventName

        view.txt_description.setText(action!!.Description)

        view.txt_description.setOnFocusChangeListener { v, hasFocus ->
            //setData(hasFocus)
        }

        view.txt_description.setText(action!!.Description)
        view.txt_date_from.text = DateUtils.convertCustomCompleteDateString(action!!.StartDateTime)

        view.content_dateto.visibility = if (action!!.FinishDateTime.isNullOrEmpty()) View.GONE else View.VISIBLE
        if (!action!!.FinishDateTime.isNullOrEmpty()) view.txt_date_to.text = DateUtils.convertCustomCompleteDateString(action!!.FinishDateTime)

        view.content_address.visibility = if (action!!.Address.isNullOrEmpty()) View.GONE else View.VISIBLE
        if (!action!!.Address.isNullOrEmpty()) view.txt_address.text = action!!.Address

        view.btn_edit_action_data.setOnClickListener {
            InfoEventActivity.editAction = true
            enabledActionEdition()
        }
        view.btn_edit_objective_data.setOnClickListener {
            InfoEventActivity.editContributions = true
            enabledContributionsEdition()
        }
        view.btn_save_action_data.setOnClickListener { saveActionData() }
        view.btn_save_objective_data.setOnClickListener { saveObjectivesData() }

        view.btn_cancel_action_edition.setOnClickListener {
            InfoEventActivity.editAction = false
            enabledActionEdition()
        }
        view.btn_cancel_objective_edition.setOnClickListener {
            InfoEventActivity.editContributions = false
            enabledContributionsEdition()
        }
        btnAddAction = activity!!.findViewById<View>(R.id.icon_add) as Button
        btnAddAction!!.setOnClickListener { addNewInstance() }


        view.action_objectives.layoutManager = LinearLayoutManager(activity,
                RecyclerView.VERTICAL,
                false)

        // objetivos
        contributions = ContributionToTheObjectiveRepository.instance.getAllByField("ActionId", action!!.Id)
        if(contributions.isEmpty()) { // copiar los objetivos
            val ob = ObjectiveRepository.instance.getAllByField("EventId", event.Id).toList()
            ob.forEach {
                val contribution = ModelSyncBuilder.create(ContributionToTheObjective())
                contribution.ActionId = action!!.Id
                contribution.ObjectiveId = it.Id
                contribution.AmountMade = if (event.IsObjectiveAutomatic) 1 else 0
                contribution.MeasureTypeId = it.MeasureTypeId
                contributions.add(contribution)
                ContributionToTheObjectiveRepository.instance.addOrUpdate(contribution,false)
            }
        }

        contributions.forEach{it.Enabled = (InfoEventActivity.newAction||InfoEventActivity.editContributions)}

        view?.action_objectives?.adapter = object : ObjectivesAdapter(activity!!.layoutInflater,
                contributions,
                R.layout.item_objective) {}

        if(event.IsObjectiveAutomatic){
            view.content_objectives.visibility = View.GONE
        }

        view.content_speakers.visibility = View.GONE

        mapView = view.findViewById(R.id.mapViewPreview)
        mapView.onCreate(savedInstanceState)
        MapsInitializer.initialize(activity!!)
        mapView.getMapAsync(this)

        if (InfoEventActivity.newAction) {
            view.btn_edit_action_data.visibility = View.GONE
            view.btn_edit_objective_data.visibility = View.GONE
            view.txt_description.isEnabled = true
            view.txt_description.setSelection(0,view.txt_description.text.toString().length)
        } else {
            view.txt_description.isEnabled = InfoEventActivity.editAction
            view.btn_edit_action_data.visibility = View.VISIBLE
            view.btn_edit_objective_data.visibility = View.VISIBLE
        }

        return view
    }

    private fun addNewInstance() {
//        val i = Intent(this, InfoEventActivity::class.java)
//        i.putExtra("newAction", true)
//        i.putExtra("eventId", this.event.Id)
//        i.putExtra("projectId", projectId)
//        startActivityForResult(i, ADD_INSTANCE_REQUEST)

        val i = Intent(activity, InfoEventActivity::class.java)
        i.putExtra("newAction", true)
//        i.putExtra("eventId", this.event.Id)
//        i.putExtra("projectId", projectId)
    }

    private fun saveObjectivesData() {
        contributions.forEach{ContributionToTheObjectiveRepository.instance.addOrUpdate(it)}
        (view!!.action_objectives.adapter as ObjectivesAdapter).notifyDataSetChanged()
        InfoEventActivity.editContributions = false
        enabledContributionsEdition()
    }

    private fun saveActionData() {
        if (!view?.txt_description?.text!!.isEmpty()) {
            val a = ActionRepository.instance.getById(action?.Id)
            if (a != null) {
                action!!.ActionGeometry = a.ActionGeometry
            }
            action!!.Description = view?.txt_description?.text.toString()
            action!!.ActiontName = action!!.Description!!

            ActionRepository.instance.addOrUpdate(action!!)
            InfoEventActivity.editAction = false
            enabledActionEdition()
            if(action?.ActiontName != null) {
                event?.invoke(action!!.ActiontName!!)
            }
        } else {
            toast("No ha ingresado una descripción")
        }


    }

    private fun enabledActionEdition() {
        if (InfoEventActivity.editAction) {
            view!!.txt_description.isEnabled = true
            view!!.btn_edit_action_data.visibility = View.GONE
            view!!.btn_save_action_data.visibility = View.VISIBLE
            view!!.btn_cancel_action_edition.visibility = View.VISIBLE
        } else {
            view!!.txt_description.isEnabled = false
            view!!.btn_edit_action_data.visibility = View.VISIBLE
            view!!.btn_save_action_data.visibility = View.GONE
            view!!.btn_cancel_action_edition.visibility = View.GONE
        }
    }

    private fun enabledContributionsEdition() {
        contributions.forEach{it.Enabled = InfoEventActivity.editContributions}
        (view!!.action_objectives.adapter as ObjectivesAdapter).notifyDataSetChanged()
        if (InfoEventActivity.editContributions) {

            view!!.btn_edit_objective_data.visibility = View.GONE
            view!!.btn_save_objective_data.visibility = View.VISIBLE
            view!!.btn_cancel_objective_edition.visibility = View.VISIBLE
        } else {
            view!!.btn_edit_objective_data.visibility = View.VISIBLE
            view!!.btn_save_objective_data.visibility = View.GONE
            view!!.btn_cancel_objective_edition.visibility = View.GONE
        }
    }

    fun disabledComponents() {
        view!!.txt_description.isEnabled = InfoEventActivity.editAction
        view!!.btn_edit_action_data.visibility = View.VISIBLE
        view!!.btn_edit_objective_data.visibility = View.VISIBLE
        contributions.forEach{it.Enabled = false}
        (view!!.action_objectives.adapter as ObjectivesAdapter).notifyDataSetChanged()
    }

    override fun onResume() {
//        view?.play_action?.visibility = View.GONE
        mapView.onResume()
        super.onResume()
//        if (isFirst && !SharedConfig.isTracking()) {
//            view?.play_action?.toggle()
//        }
//        isFirst = false
//        Handler().postDelayed({
//            view?.play_action?.visibility = View.VISIBLE
//        }, 100)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    fun getDescription(): String {
        return view?.txt_description?.text.toString()
    }

    private fun setGeometry() {
        val geometry = GeometryUtils.convertWKTToList(action!!.ActionGeometry!!) ?: return

        val viewOptionBuilder = ViewOptionBuilder()
                .withStyleName(ViewOption.StyleDef.DEFAULT)
                .withCenterCoordinates(positionDefault)
                .withForceCenterMap(false)

        var showMap = true
        when (geometry.first) {
            DrawingOption.DrawingType.POINT -> {
                if (geometry.second[0].latitude != 0.0 && geometry.second[0].longitude != 0.0) {
                    viewOptionBuilder
                            .withCenterCoordinates(geometry.second[0])
                            .withMapsZoom(17f)
                            .withForceCenterMap(true)
                            .withMarkers(GeometryUtils.getPoint(geometry.second[0]))
                } else {
                    showMap = false
                }
            }
            DrawingOption.DrawingType.POLYLINE -> {
                viewOptionBuilder.withPolylines(GeometryUtils.getPolyline(geometry.second.toTypedArray(), activity!!))
            }
            DrawingOption.DrawingType.POLYGON -> {
                viewOptionBuilder.withPolygons(GeometryUtils.getPolygon(geometry.second.toTypedArray(), activity!!))
            }
        }
        val viewOption = viewOptionBuilder.build()
        MapUtils.showElements(viewOption, mMap, activity!!)
        if (showMap) view!!.card_map.visibility = View.VISIBLE
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.uiSettings.isMapToolbarEnabled = false
        mMap!!.uiSettings.isScrollGesturesEnabled = false
        mMap!!.uiSettings.isZoomGesturesEnabled = false
        mMap!!.setOnMapClickListener {
            //            showMapDialog()
        }

//        mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(applicationContext, R.raw.style_json))
        if (!action!!.ActionGeometry.isNullOrEmpty()) setGeometry()
    }
}
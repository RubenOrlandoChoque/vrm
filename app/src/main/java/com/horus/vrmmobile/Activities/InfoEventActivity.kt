package com.horus.vrmmobile.Activities

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.ashokvarma.bottomnavigation.BottomNavigationBar
import com.ashokvarma.bottomnavigation.BottomNavigationItem
import com.github.bkhezry.mapdrawingtools.model.DrawingOption
import com.google.android.gms.maps.model.LatLng
import com.horus.vrmmobile.Adapters.NoteListFragment
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Helpers.CommonHelper
import com.horus.vrmmobile.Models.Action
import com.horus.vrmmobile.Models.Event
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.*
import com.horus.vrmmobile.Utils.DateUtils
import com.horus.vrmmobile.Utils.GeometryUtils
import com.horus.vrmmobile.Utils.Utils
import com.horus.vrmmobile.services.DownloadService
import com.horus.vrmmobile.services.TrackingService
import com.horus.vrmmobile.views.animatedvector.FloatingActionButtonTrack
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.holder.BadgeStyle
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_info_event.*
import kotlinx.android.synthetic.main.app_bar_action.*
import kotlinx.android.synthetic.main.app_bar_info_event.*
import kotlinx.android.synthetic.main.app_bar_info_event.btn_add1
import kotlinx.android.synthetic.main.content_info_event.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.toast
import java.util.*


class InfoEventActivity : AppCompatActivity(), BottomNavigationBar.OnTabSelectedListener {

    private lateinit var bottomNavigationBar: BottomNavigationBar
    private lateinit var infoEventFragment: InfoEventFragment
    private lateinit var participantsFragment: PartakerListFragment
    private lateinit var galleryFragment: GalleryFragment
    private lateinit var noteListFragment: NoteListFragment
    private var lastSelectedPosition = 0
    private lateinit var action: Action
    private var fromSplash: Boolean = false
    private var isTracked: Boolean = false
    private var countNotify = 0
    private var countEvent = 0
    private var projectId: String = ""

    // Items Menu
    private lateinit var result: Drawer
    var itemProject : PrimaryDrawerItem? = null
    var itemEvent : SecondaryDrawerItem? = null
    var itemAction : SecondaryDrawerItem? = null
    var itemRRHH : SecondaryDrawerItem? = null
    var itemUpdate : SecondaryDrawerItem? = null
    var itemZoneSelect : SecondaryDrawerItem? = null
    var itemLogOut : SecondaryDrawerItem? = null
    var itemReceived: SecondaryDrawerItem? = null
    var itemSend: SecondaryDrawerItem? = null

    companion object {
        var newAction = false
        var editAction = false
        var editContributions = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_events1)
        play_stop_action1?.visibility = View.GONE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.menuColor)
        }
        projectId = intent.getStringExtra("projectId")
        bottomNavigationBar = bottom_navigation_bar1
        fromSplash = intent.getBooleanExtra("fromSplash", false)
        newAction = intent.getBooleanExtra("newAction", false)

        val eventId = intent.getStringExtra("eventId")
        val event = EventRepository.instance.getCompleteById(eventId)!!

        if (!newAction) {
            if (!intent.hasExtra("actionId") || intent.getStringExtra("actionId").isNullOrEmpty()) {
                return
            }
            val actionId = intent.getStringExtra("actionId")
            action = ActionRepository.instance.getCompleteById(actionId)!!
        } else {
            action = createIntanceEvent(event)
            action.ActiontName = "Cargar acción"
            action.Description = event.Description
        }

        val ev = EventRepository.instance.getById(eventId)
        isTracked = ev?.IsTracked == true

        if (newAction && !isTracked) {
            startGPS()
        }

        val params = Bundle()
        params.putString("EVENT_PARAM", Utils.convertObjectToString(action))
        infoEventFragment = InfoEventFragment {
            txt_event_name.text = it
        }
        infoEventFragment.arguments = params
        participantsFragment = PartakerListFragment()
        participantsFragment.arguments = params
        galleryFragment = GalleryFragment()
        galleryFragment.arguments = params
        noteListFragment = NoteListFragment()
        noteListFragment.arguments = params


        bottomNavigationBar.setTabSelectedListener(this)

        btn_add1.setOnClickListener { saveData() }

        if (!isTracked) {
            play_stop_action1.visibility = View.GONE
        } else {
            if (SharedConfig.isTracking() && SharedConfig.getActionIdTtracking() == action.Id) {
                play_stop_action.changeMode(FloatingActionButtonTrack.Mode.STOPPLAY)
                play_stop_action.visibility = View.VISIBLE
            } else {
                if (TrackRepository.instance.getAllByField("ActionId", action.Id).isEmpty()) {
                    play_stop_action.changeMode(FloatingActionButtonTrack.Mode.PLAYSTOP)
                    if (!newAction) {
                        play_stop_action.visibility = View.VISIBLE
                    }
                }
            }
        }

        play_stop_action1?.setOnActionFabClickListener(object : FloatingActionButtonTrack.OnActionFabClickListener {
            override fun onClick(view: View): Boolean {
                return playAction()
            }
        })

        refresh()
        disabledComponents()

        setImageProfile()
        setMenu()
        setToolbar()
//        setColorIconToolbar()
    }

    fun setColorIconToolbar(){
        toolbar1.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_black))
    }

    fun setImageProfile(){
        //initialize and create the image loader logic
        DrawerImageLoader.init(object : AbstractDrawerImageLoader() {
            override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable) {
                Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            override fun cancel(imageView: ImageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView)
            }
        })
    }

    fun setMenu(){
        // Create the AccountHeader
        val headerResult = AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.menuColor)
                .addProfiles(
                        ProfileDrawerItem().withName(SharedConfig.getUserName() + " " + SharedConfig.getUserSurname())
                                .withIcon(getResources().getDrawable(R.drawable.user_default))
                )
                .withOnAccountHeaderListener(object : AccountHeader.OnAccountHeaderListener {
                    override fun onProfileChanged(view: View?, profile: IProfile<*>, current: Boolean): Boolean {
                        return false
                    }
                })
                .build()

        itemProject = PrimaryDrawerItem()
                .withIdentifier(1)
                .withName("Proyectos")
                .withTextColor(getResources().getColor(R.color.black))
                .withIcon(R.drawable.group15)
                .withDescription(SharedConfig.getProjectName())
                .withDescriptionTextColor(getResources().getColor(R.color.gray))
                .withEnabled(false)
        itemEvent = SecondaryDrawerItem()
                .withIdentifier(2)
                .withTextColor(getResources().getColor(R.color.black))
                .withName("Eventos")
                .withIcon(R.drawable.evento)
                .withEnabled(false)
                .withDescription(SharedConfig.getEventName())
                .withDescriptionTextColor(getResources().getColor(R.color.gray))
        itemAction = SecondaryDrawerItem()
                .withTextColor(getResources().getColor(R.color.black))
                .withIdentifier(3)
                .withName("Acciones")
                .withIcon(R.drawable.acciones2)
                .withEnabled(false)
        itemRRHH = SecondaryDrawerItem()
                .withIdentifier(4)
                .withTextColor(getResources().getColor(R.color.black))
                .withName("Recursos Humanos")
                .withIcon(R.drawable.ic_people_black)
        itemUpdate = SecondaryDrawerItem()
                .withIdentifier(5)
                .withTextColor(getResources().getColor(R.color.black))
                .withName("Actualizar")
                .withIcon(R.drawable.ic_cached_24px)
        itemZoneSelect = SecondaryDrawerItem()
                .withIdentifier(6)
                .withTextColor(getResources().getColor(R.color.black))
                .withName("Elegir Zona")
                .withIcon(R.drawable.ic_map_24px)
                .withEnabled(false)
                .withDescription(SharedConfig.getZoneName())
                .withDescriptionTextColor(getResources().getColor(R.color.gray))
        itemLogOut = SecondaryDrawerItem()
                .withTextColor(getResources().getColor(R.color.black))
                .withIdentifier(7)
                .withName("Cerrar Sesion")
                .withIcon(R.drawable.ic_exit_to_app_24px)

        itemReceived = SecondaryDrawerItem()
                .withIdentifier(9)
                .withName("Recibidos")
                .withIcon(R.drawable.ic_inbox_black_24dp)
                .withTextColor(getResources().getColor(R.color.black))

        itemSend = SecondaryDrawerItem()
                .withIdentifier(10)
                .withName("Enviados")
                .withIcon(R.drawable.ic_send_black_24dp)
                .withTextColor(getResources().getColor(R.color.black))


        result = DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar1)
                .withAccountHeader(headerResult)
                .addDrawerItems (
                        itemProject,
                        DividerDrawerItem(),
                        itemEvent,
                        DividerDrawerItem(),
                        itemAction,
                        DividerDrawerItem(),
                        ExpandableDrawerItem()
                                .withName("Mensajeria").withIcon(R.drawable.mensajeria)
                                .withIdentifier(8)
                                .withSubItems(itemReceived, itemSend),
                        DividerDrawerItem(),
                        itemRRHH,
                        DividerDrawerItem(),
                        itemUpdate,
                        DividerDrawerItem(),
                        itemZoneSelect,
                        DividerDrawerItem(),
                        itemLogOut,
                        DividerDrawerItem()
                )
                .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                    override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*,*>): Boolean {
                        val identifierItem = drawerItem.identifier.toInt()
                        itemSelect(identifierItem)
                        return false
                    }
                })
                .build()
    }

    fun setToolbar(){
        toolbarInfoEvent.title = "Cargar acciones"
        toolbarInfoEvent.setTitleTextColor(getResources().getColor(R.color.black))
    }

    fun itemSelect(identifier: Int){
        when (identifier) {
            1 -> {
                //Proyectos
                goListProject()
                true
            }
            2 -> {
                //Eventos
                goListEvent()
                true
            }
            3 -> {
                //Acciones
                true
            }
            4 -> {
                //Recursos Humanos
                goRRHH()
            }
            5 -> {
                //Actualizar
                DownloadService.downloadCatalogs(this@InfoEventActivity) {}
            }
            6 -> {
                //Elegir Zona
                goListZones()
                true
            }
            7 -> {
                //Cerrar Sesion
                closeSession()
                true
            }
            9 -> {
                //Mensajes Recibidos
                onMessageInbox()
                true
            }
            10 -> {
                //Mensajes Enviados
                onMessageSend()
                true
            }
        }
    }


    private fun onMessageInbox() {
        SharedConfig.setCountNotify(0)
        val i = Intent(this, NotificationsListActivity::class.java)
        i.putExtra("origen", "main")
        i.putExtra("inboxMsg", true)
        startActivity(i)
    }

    private fun onMessageSend() {
        val i = Intent(this, NotificationsListActivity::class.java)
        i.putExtra("origen", "main")
        i.putExtra("sendMsg", true)
        startActivity(i)
    }

    private fun isValid(): Boolean {
        var actionName = getActionDescriptionFromFragment()
        if (actionName.isEmpty()) {
            toast("No ha ingresado una descripción")
            return false
        }
        return true
    }

    private fun saveData() {
        setActionDescription()
        if (isValid()) {
            ActionRepository.instance.addOrUpdate(action)

            if (newAction) {
                var contributions = ContributionToTheObjectiveRepository.instance.getAllByField("ActionId", action.Id)
                contributions.forEach {
                    it.Enabled = false
                    if (it.AmountMade == 0) {

                    }
                    ContributionToTheObjectiveRepository.instance.addOrUpdate(it)
                }
                if (isTracked) {
                    play_stop_action.visibility = View.VISIBLE
                }

            }
            newAction = false
            disabledComponents()
        }
    }

    private fun disabledComponents() {
        if (newAction) {
            btn_add1.visibility = View.VISIBLE
            bottom_navigation_bar1.visibility = View.GONE
        } else {
            btn_add1.visibility = View.GONE
            bottom_navigation_bar1.visibility = View.VISIBLE
            val fragments = supportFragmentManager.fragments
            if (fragments.size > 0 && fragments[0] is InfoEventFragment) {
                (fragments[0] as InfoEventFragment).disabledComponents()
            }
        }
    }

    private fun playAction(): Boolean {
        var res = false
        if (SharedConfig.isTracking() && SharedConfig.getActionIdTtracking() != action.Id) {
            MaterialDialog(this).show {
                title(text = getString(R.string.title_dialog_info))
                message(text = getString(R.string.other_action_is_tracking))
                positiveButton(R.string.accept)
                cancelOnTouchOutside(false)
                noAutoDismiss()
                positiveButton {
                    it.dismiss()
                }
            }
            return res
        }
        if (SharedConfig.isTracking()) {
            TrackingService.finishTrackingModal(
                    onPositiveButton = {
                        TrackingService.stopSericeGPS(this, action.Id)
                        play_stop_action.visibility = View.GONE
                        res = true
                    },
                    onNegativeButton = { play_stop_action.changeMode(FloatingActionButtonTrack.Mode.STOPPLAY) },
                    context = this
            )
        } else {
            SharedConfig.setActionIdTtracking(action.Id)
            res = true
            setServiceGPSWithTracking()
        }
        return res
    }

    private fun setServiceGPSWithTracking() {
        SharedConfig.setTracking(true)
        startGPS()
    }

    private fun startGPS() {
        val isTrackingServiceRunning = CommonHelper.isServiceRunning(this, TrackingService::class.java)
        if (!isTrackingServiceRunning) {
            val trackingService = Intent(this, TrackingService::class.java)
            trackingService.putExtra("actionId", action.Id)
            if (!isTracked) {
                startService(trackingService)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(trackingService)
                } else {
                    startService(trackingService)
                }
            }
        }
    }

    private fun createIntanceEvent(event: Event): Action {
        val eventZone = EventZoneRepository.instance.getAll().filter { it.EventId.equals(event.Id) && it.ZonePoliticalFrontId.equals(SharedConfig.getZonePoliticalFrontId()) }.distinct().firstOrNull()
        val action = Action()
        action.Address = event.Address
        action.ActiontName = "Nueva acción"
        action.StartDateTime = DateUtils.convertDateToString2(Date())
        action.FinishDateTime = DateUtils.convertDateToString2(Date())
        action.EventZonesId = eventZone?.Id
        action.ActionStateId = event.EventStateId
        action.AttendeesQuantity = 0
        return action
    }

    private fun refresh() {
        bottomNavigationBar.clearAll()

        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED) //"MODE_DEFAULT", "MODE_FIXED", "MODE_SHIFTING", "MODE_FIXED_NO_TITLE", "MODE_SHIFTING_NO_TITLE"
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_RIPPLE) //"BACKGROUND_STYLE_DEFAULT", "BACKGROUND_STYLE_STATIC", "BACKGROUND_STYLE_RIPPLE"

        bottomNavigationBar.addItem(BottomNavigationItem(R.drawable.ic_info_24dp, "Info").setActiveColorResource(R.color.menuColor))//.setBadgeItem(numberBadgeItem))
        bottomNavigationBar.addItem(BottomNavigationItem(R.drawable.ic_people_black, "Personas").setActiveColorResource(R.color.menuColor))
        bottomNavigationBar.addItem(BottomNavigationItem(R.drawable.ic_add_a_photo_black, "Fotos").setActiveColorResource(R.color.menuColor))
        bottomNavigationBar.addItem(BottomNavigationItem(R.drawable.ic_note_black, "Notas").setActiveColorResource(R.color.menuColor))

        bottomNavigationBar.initialise()
        bottomNavigationBar.selectTab(if (lastSelectedPosition > 3) 3 else lastSelectedPosition, true)
    }

    override fun onTabSelected(position: Int) {
        if (newAction || editAction || editContributions) {
            bottomNavigationBar.initialise()
            replaceFragments(0)
            if (position > 0) {
                MaterialDialog(this).show {
                    title(text = getString(R.string.title_dialog_info))
                    message(text = getString(R.string.save_action))
                    positiveButton(R.string.accept)
                    cancelOnTouchOutside(false)
                }
            }
        } else {
            lastSelectedPosition = position
            replaceFragments(position)
        }
    }

    override fun onTabUnselected(position: Int) {}

    override fun onTabReselected(position: Int) {
    }

    fun changeToolbarTitle(position: Int) {
        if(position == 0){
            toolbarInfoEvent.title = "Cargar acción"
//            icon_add.visibility = View.GONE
            toolbarInfoEvent.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_black)
            toolbarInfoEvent.setNavigationOnClickListener{
                onBackPressed()
            }
        }else if(position == 1){
            toolbarInfoEvent.title = "Personas"
            icon_add.visibility = View.VISIBLE
            toolbarInfoEvent.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_black)
            toolbarInfoEvent.setNavigationOnClickListener{
                onBackPressed()
            }
        }else if(position == 2){
            toolbarInfoEvent.title = "Galeria de Fotos"
            icon_add.visibility = View.VISIBLE
            toolbarInfoEvent.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_black)
            toolbarInfoEvent.setNavigationOnClickListener{
                onBackPressed()
            }
        }else{
            toolbarInfoEvent.title = "Notas"
            icon_add.visibility = View.VISIBLE
            toolbarInfoEvent.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_black)
            toolbarInfoEvent.setNavigationOnClickListener{
                onBackPressed()
            }
        }
    }

    private fun replaceFragments(position: Int) {
        setActionDescription()
        changeToolbarTitle(position)
        supportFragmentManager.beginTransaction().apply {
            when (position) {
                0 -> replace(R.id.content_fragment, infoEventFragment)
                1 -> replace(R.id.content_fragment, participantsFragment)
                2 -> replace(R.id.content_fragment, galleryFragment)
                3 -> replace(R.id.content_fragment, noteListFragment)
                else -> replace(R.id.content_fragment, infoEventFragment)
            }
        }.commitAllowingStateLoss()
    }

    private fun setActionDescription() {
        val fragments = supportFragmentManager.fragments
        if (fragments.size > 0 && fragments[0] is InfoEventFragment) {
            action.Description = (fragments[0] as InfoEventFragment).getDescription()
            action.ActiontName = action.Description!!
        }
    }

    private fun getActionDescriptionFromFragment(): String {
        val fragments = supportFragmentManager.fragments
        var actionDescription = ""
        if (fragments.size > 0 && fragments[0] is InfoEventFragment) {
            actionDescription = (fragments[0] as InfoEventFragment).getDescription()
        }
        return actionDescription
    }

    private fun verifyData() {
        if (newAction || editAction || editContributions) {
            MaterialDialog(this).show {
                title(text = getString(R.string.title_dialog_info))
                message(text = getString(R.string.action_not_saved))
                negativeButton(R.string.cancel)
                positiveButton(R.string.accept)
                cancelOnTouchOutside(false)
                noAutoDismiss()
                positiveButton {
                    editAction = false
                    editContributions = false
                    newAction = false
                    finishResult(Activity.RESULT_CANCELED)
                }
                negativeButton {
                    it.dismiss()
                }
            }
        } else {
            finishResult(Activity.RESULT_OK)
        }
    }

    private fun finishResult(result: Int) {
        // Capturar GPS Cuando sea del tipo track
//        Log.e("lastPointValid", (TrackingService.lastPointValid != null).toString())
        if (!isTracked && TrackingService.lastPointValid != null) {
            val track = TrackingService.lastPointValid
//            Log.e("track != null", (track != null).toString())
//            Log.e("track", "${track?.LatY}  -   ${track?.LngX}")
            if (track != null) {
                val pos = LatLng(track.LatY, track.LngX)
                val a = ActionRepository.instance.getById(action.Id)
                if (a != null && a.ActionGeometry == "POINT (0.0 0.0)") {
                    a.ActionGeometry = GeometryUtils.convertListToWKT(arrayListOf(pos), DrawingOption.DrawingType.POINT)
                    ActionRepository.instance.addOrUpdate(a)
                }
            }
            if (!SharedConfig.isTracking()) { // si alguien no esta trackeando, detenemos el servicio
                TrackingService.stopSericeGPS(this, action.Id)
            }
        }

        if (fromSplash) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        } else {
            val data = Intent()
            setResult(result, data)
            finish()
        }
    }

    private fun goListEvent() {
        val i = Intent(this, EventsActivity::class.java)
        i.putExtra("origen", "main")
        i.putExtra("projectId", projectId)
        startActivity(i)
    }

    private fun goListProject() {
        val i = Intent(this, ProjectListActivity::class.java)
        startActivity(i)
    }

    private fun goRRHH() {
        val i = Intent(this, OrganizationActivity::class.java)
        startActivity(i)
    }

    private fun goListZones() {
        val i = Intent(this, MainActivity::class.java)
        i.putExtra("origen", "events")
        startActivity(i)
    }

    private fun closeSession() {
        SharedConfig.clear()
        val i = Intent(this, PhoneActivity::class.java)
        startActivity(i)
        finish()
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
        if (SharedConfig.getCountNotify() > 0) {
            countNotify = SharedConfig.getCountNotify()
            itemReceived!!.withBadge(countNotify.toString())
            itemReceived!!.withBadgeStyle(BadgeStyle().withTextColor(getResources().getColor(R.color.white)).withColorRes(R.color.green))
        }else {
            result.updateBadge(10, null)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        EventBus.getDefault().unregister(this)
        verifyData()
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onEventListener(event: com.horus.vrmmobile.Utils.Event) {
        if (event.eventName.equals("countNotify")) {
            if(SharedConfig.getCountNotify() > 0){
                this@InfoEventActivity.runOnUiThread(java.lang.Runnable {
                    countNotify = SharedConfig.getCountNotify()
                    itemReceived!!.withBadge(countNotify.toString())
                    itemReceived!!.withBadgeStyle(BadgeStyle().withTextColor(getResources().getColor(R.color.white)).withColorRes(R.color.green))
                })
            }
        }
    }
}


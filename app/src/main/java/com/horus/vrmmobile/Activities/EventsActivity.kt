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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.tabs.TabLayout
import com.horus.vrmmobile.Adapters.EventAdapter
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Managers.EventManager
import com.horus.vrmmobile.Models.Action
import com.horus.vrmmobile.Models.Dtos.EventDto
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.ProjectRepository
import com.horus.vrmmobile.Utils.DateUtils
import com.horus.vrmmobile.Utils.Event
import com.horus.vrmmobile.Utils.Utils
import com.horus.vrmmobile.services.DownloadService
import com.horus.vrmmobile.services.SyncService
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
import com.savvi.rangedatepicker.CalendarPickerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.app_bar_principal.*
import kotlinx.android.synthetic.main.content_event.*
import kotlinx.android.synthetic.main.dialog_filter.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import xyz.sangcomz.stickytimelineview.RecyclerSectionItemDecoration
import xyz.sangcomz.stickytimelineview.model.SectionInfo
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EventsActivity : AppCompatActivity(){

    val calendarFrom = Calendar.getInstance()
    val calendarTo = Calendar.getInstance()
    val calendarMin = Calendar.getInstance()
    val calendarMax = Calendar.getInstance()
    private val ADD_EVENT_REQUEST = 451
    private val EDIT_REQUEST = 452
    private val ADD_INSTANCE_REQUEST = 444
    private var projectId: String = ""
    private var eventId: String? = null
    private var fromNotification = false

    private var countNotify = 0
    private var countEvent = 0

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.menuColor)
        }

        fromNotification = intent.getBooleanExtra("fromNotification", false)
        eventId = intent.getStringExtra("eventId")
        projectId = intent.getStringExtra("projectId")
        if (projectId.isNullOrEmpty())
            projectId = ""

        setTabs()
        setImageProfile()
        setMenu()
        setToolbar()

        setMinMaxCalendar()
        setListEvents()
        setViews()
        if (fromNotification) {
//            changeButtonSelect(btn_filters)
        }
        setColorIconToolbar()
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

    fun setToolbar(){
        toolbar1.title = "Eventos"
        toolbar1.setTitleTextColor(getResources().getColor(R.color.black))
        toolbar1.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_menu_black)
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

    fun setTabs(){
        val tabs = findViewById<TabLayout>(R.id.tabs)
        tabs.addTab(tabs.newTab().setText("Hoy"))
        tabs.addTab(tabs.newTab().setText("Mes"))
        tabs.addTab(tabs.newTab().setText("Filtro"))
        tabs.setSelectedTabIndicatorColor(getResources().getColor(R.color.white))

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab!!.position){
                    0 -> {
                        //Hoy
                        todayFilter()
                    }
                    1 -> {
                        //Mes
                        monthFilter()
                    }
                    2 -> {
                        //Filtros
                        showFilters()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
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
                DownloadService.downloadCatalogs(this@EventsActivity) {}
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

    private fun setViews() {
        val project = ProjectRepository.instance.getById(projectId)
        itemProject!!.withDescription(project?.Name).withDescriptionTextColor(getResources().getColor(R.color.gray))
        SharedConfig.setProjectName(project?.Name)
        itemZoneSelect!!.withDescription(SharedConfig.getZoneName()).withDescriptionTextColor(getResources().getColor(R.color.gray))
    }

    private fun setListEvents() {
        recycler_view1.layoutManager = LinearLayoutManager(this,
                RecyclerView.VERTICAL,
                false)
        recycler_view1.addItemDecoration(getSectionCallback(ArrayList()))
        recycler_view1.adapter = object : EventAdapter(layoutInflater,
                ArrayList(),
                R.layout.recycler_row) {
            override fun setOnClickList(eventDto: EventDto) {
                val i = Intent(this@EventsActivity, ActionsListActivity::class.java)
                i.putExtra("eventId", eventDto.Id)
                i.putExtra("projectId", projectId)
                startActivityForResult(i, EDIT_REQUEST)
            }

            override fun setOnClickAdd(eventDto: EventDto) {
                val ii = Intent(this@EventsActivity, InfoEventActivity::class.java)
                ii.putExtra("newAction", true)
                ii.putExtra("eventId", eventDto.Id)
                ii.putExtra("projectId", projectId)
                startActivityForResult(ii, ADD_INSTANCE_REQUEST)
            }
        }
        if (fromNotification) {
            customEventFilter()
        } else {
            todayFilter()
        }
    }

    private fun createIntanceEvent(eventDto: EventDto): Action {
        val action = Action()
        action.Address = eventDto.Address
        action.ActiontName = eventDto.EventName
        action.StartDateTime = DateUtils.convertDateToString2(Date())
        action.FinishDateTime = DateUtils.convertDateToString2(Date())
        action.EventZonesId = eventDto?.Id
        action.ActionStateId = eventDto.EventStateId
        action.AttendeesQuantity = 0
        return action
    }

    private fun monthFilter() {
        calendarFrom.time = Date()
        calendarFrom.set(Calendar.DAY_OF_MONTH, 1)
        calendarTo.time = Date()
        calendarTo.add(Calendar.MONTH, 1)
        calendarTo.set(Calendar.DAY_OF_MONTH, 1)
        calendarTo.add(Calendar.DAY_OF_MONTH, -1)
        filter()
    }

    private fun todayFilter() {
        calendarFrom.time = Date()
        calendarTo.time = calendarFrom.time
        filter()
    }

    private fun customEventFilter() {
        calendarFrom.time = calendarMin.time
        calendarTo.time = calendarMax.time
        filter()
    }


    private fun getSectionCallback(singerList: List<EventDto>): RecyclerSectionItemDecoration.SectionCallback {
        return object : RecyclerSectionItemDecoration.SectionCallback {
            //In your data, implement a method to determine if this is a section.
            override fun isSection(position: Int): Boolean =
                    singerList[position].EventDate != singerList[position - 1].EventDate

            //Implement a method that returns a SectionHeader.
            override fun getSectionHeader(position: Int): SectionInfo? =
                    SectionInfo(singerList[position].EventDateFormat, null)
        }
    }

    private fun setMinMaxCalendar() {
        calendarMin.time = Date()
        calendarMin.add(Calendar.YEAR, -1)
        calendarMax.time = Date()
        calendarMax.add(Calendar.YEAR, 1)
    }

    private fun showFilters() {
        val dialogScan = MaterialDialog(this).show {
            customView(viewRes = R.layout.dialog_filter)
            negativeButton(R.string.cancel)
            positiveButton(R.string.accept)
            cancelOnTouchOutside(false)
            noAutoDismiss()
            positiveButton {
                val selecteds = it.calendar_view.selectedDates
                if (selecteds.size > 0) {
                    this@EventsActivity.calendarFrom.time = selecteds[0]
                    this@EventsActivity.calendarTo.time = selecteds[selecteds.size - 1]
                }
                filter()
//                setTexts()
                it.dismiss()
            }
            negativeButton {
                it.dismiss()
            }
        }

        dialogScan.calendar_view.init(
                calendarMin.time,
                calendarMax.time,
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()))
                .inMode(CalendarPickerView.SelectionMode.RANGE)
//                .withSelectedDates(arrayListOf(calendarFrom.time, calendarTo.time))
                .withSelectedDate(Date())
    }

    private fun filter() {
        val dateFromString = DateUtils.convertDateShortNotUTCToString(calendarFrom.time) + "T00:00:00Z"
        val dateToString = DateUtils.convertDateShortNotUTCToString(calendarTo.time) + "T59:59:59Z"
        var singerList = EventManager.getAll(dateFromString, dateToString, projectId!!)

        if (fromNotification && eventId != null) {
            singerList = ArrayList(singerList.filter { e -> e.Id == eventId }.toList())
        }

        event_empty1.visibility = if (singerList.size == 0) View.VISIBLE else View.GONE
        recycler_view1.visibility = if (singerList.size == 0) View.GONE else View.VISIBLE
        recycler_view1.removeItemDecoration(recycler_view1.getItemDecorationAt(0))
        recycler_view1.addItemDecoration(getSectionCallback(singerList))
        (recycler_view1.adapter as EventAdapter).setItems(singerList)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ADD_EVENT_REQUEST -> {
                    sync()
                    todayFilter()
                }
                EDIT_REQUEST -> {
                    sync()
                }
                ADD_INSTANCE_REQUEST -> {
                    sync()
                }
            }
        }
    }

    private fun sync() {
        if (Utils.isNetworkAvailable()) {
            object : Thread() {
                override fun run() {
                    SyncService.push()
                }
            }.run()
        }
    }

    private fun onFinish() {
        if (!SharedConfig.appStarted) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        }
        finish()
    }

    private fun goListProject() {
        val i = Intent(this, ProjectListActivity::class.java)
        startActivity(i)
        finish()
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
        filter()
        eventId = null
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
        onFinish()
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onEventListener(event: Event) {
        if (event.eventName.equals("countNotify")) {
            if(SharedConfig.getCountNotify() > 0){
                this@EventsActivity.runOnUiThread(java.lang.Runnable {
                    countNotify = SharedConfig.getCountNotify()
                    itemReceived!!.withBadge(countNotify.toString())
                    itemReceived!!.withBadgeStyle(BadgeStyle().withTextColor(getResources().getColor(R.color.white)).withColorRes(R.color.green))
                })
            }
        }
    }
}


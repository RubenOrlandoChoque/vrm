package com.horus.vrmmobile.Activities

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.horus.vrmmobile.Adapters.ActionsAdapter
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.Action
import com.horus.vrmmobile.Models.Event
import com.horus.vrmmobile.Models.EventZone
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.*
import com.horus.vrmmobile.Utils.DateUtils
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
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_principal.*
import kotlinx.android.synthetic.main.app_bar_action.*
import kotlinx.android.synthetic.main.app_bar_action.toolbar1
import kotlinx.android.synthetic.main.app_bar_principal.*
import kotlinx.android.synthetic.main.content_action.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlinx.android.synthetic.main.view_zero_actions.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ActionsListActivity : AppCompatActivity() {
    private val ADD_INSTANCE_REQUEST = 444
    private lateinit var event: Event
    private var eventZone: EventZone? = null
    private var projectId: String = ""
    private var actionsList: ArrayList<Action> = ArrayList()
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
        setContentView(R.layout.activity_action1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.menuColor)
        }
        if(!intent.hasExtra("eventId") || intent.getStringExtra("eventId").isNullOrEmpty()){
            return
        }
        projectId = intent.getStringExtra("projectId")

        val eventId = intent.getStringExtra("eventId")!!
        setEvents()
        event = EventRepository.instance.getCompleteById(eventId)!!
        actionsList = ArrayList()
        eventZone = EventZoneRepository.instance.getAll().filter { it.EventId.equals(eventId) && it.ZonePoliticalFrontId.equals(SharedConfig.getZonePoliticalFrontId()) }.distinct().firstOrNull()
        instances_list1.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        instances_list1.adapter = object : ActionsAdapter(layoutInflater,
                ArrayList(),
                R.layout.item_instance,
                this) {
            override fun onSelect(action: Action) {
                val i = Intent(this@ActionsListActivity, InfoEventActivity::class.java)
                i.putExtra("actionId", action.Id)
                i.putExtra("eventId", event.Id)
                i.putExtra("projectId", projectId)
                startActivityForResult(i, ADD_INSTANCE_REQUEST)
            }

            override fun onDeleteAction(action: Action) {
                MaterialDialog(this@ActionsListActivity).show {instances_list1
                    title(text = getString(R.string.title_dialog_info))
                    message(text = getString(R.string.delete_action, action.Description))
                    negativeButton(R.string.cancel)
                    positiveButton(R.string.accept)
                    cancelOnTouchOutside(false)
                    noAutoDismiss()
                    positiveButton {dialog ->
                        dialog.dismiss()
                        // borrar la accion y los objetos relacionados a la accion
                        ActionRepository.instance.softDelete(action.Id)
                        // notas
                        val notes = NoteRepository.instance.getByActionId(action.Id)
                        notes.forEach { NoteRepository.instance.softDelete(it.Id) }
                        // multimedia
                        val multimedia = ActionMultimediaRepository.instance.getByActionId(action.Id)
                        multimedia.forEach { ActionMultimediaRepository.instance.softDelete(it.Id) }
                        // partakers
                        val partakers = PartakerRepository.instance.getByActionId(action.Id)
                        partakers.forEach { PartakerRepository.instance.softDelete(it.Id) }
                        // contributions
                        val contributions = ContributionToTheObjectiveRepository.instance.getAllByField("ActionId", action.Id)
                        contributions.forEach { ContributionToTheObjectiveRepository.instance.softDelete(it.Id) }
                        // tracks
                        val tracks = TrackRepository.instance.getAllByField("ActionId", action.Id)
                        tracks.forEach { TrackRepository.instance.softDelete(it.Id) }

                        loadActions()
                    }
                    negativeButton {
                        it.dismiss()
                    }
                }
            }
        }
        setImageProfile()
        setToolbar()
        setMenu()

        itemEvent!!.withDescription(event.Description).withDescriptionTextColor(getResources().getColor(R.color.gray))
        SharedConfig.setEventName(event.Description)
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
                .withDescription(SharedConfig.getEventName())
                .withDescriptionTextColor(getResources().getColor(R.color.gray))
        itemAction = SecondaryDrawerItem()
                .withTextColor(getResources().getColor(R.color.black))
                .withIdentifier(3)
                .withName("Acciones")
                .withIcon(R.drawable.acciones2)
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


    fun setToolbar(){
        toolbar1.title = "Acciones"
        toolbar1.setTitleTextColor(getResources().getColor(R.color.black))
        toolbar1.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_menu_black)
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
                DownloadService.downloadCatalogs(this@ActionsListActivity) {}
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

    private fun loadActions(){
        actionsList.clear()
        if(eventZone != null) {
            actionsList.addAll(ActionRepository.instance.getByEventZonesId(eventZone!!.Id))
        }
        setCountInstances(actionsList.size)
        actionsList.forEach {
            try{
                val d = if (it.StartDateTime.toLowerCase().endsWith("z")) DateUtils.convertDateToStringInverse(it.StartDateTime) else DateUtils.convertDateToStringInverse(it.StartDateTime + "Z")
                it.StartDate = d
            }catch (e: Exception) { }
        }
        actionsList.sortByDescending {it.StartDate}
        (instances_list1.adapter as ActionsAdapter).setItems(actionsList)
        setVisibility()
    }


    fun setVisibility() {
        if(actionsList.size == 0){
            zero_actions_view.visibility = View.VISIBLE
            txt_count_instances1.visibility = View.GONE
        }else{
            zero_actions_view.visibility = View.GONE
            txt_count_instances1.visibility = View.VISIBLE
        }
    }

    private fun setEvents() {
        btn_add1.setOnClickListener {
            addNewInstance()
        }
    }

    private fun addNewInstance() {
        val i = Intent(this, InfoEventActivity::class.java)
        i.putExtra("newAction", true)
        i.putExtra("eventId", this.event.Id)
        i.putExtra("projectId", projectId)
        startActivityForResult(i, ADD_INSTANCE_REQUEST)
    }

    private fun setCountInstances(count: Number){
        txt_count_instances1.text = "$count Acciones"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
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

    private fun goListEvent() {
        val i = Intent(this, EventsActivity::class.java)
        i.putExtra("origen", "main")
        i.putExtra("projectId",projectId)
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
        loadActions()
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
        SharedConfig.appStarted = false
        finish()
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onEventListener(event: com.horus.vrmmobile.Utils.Event) {
        if (event.eventName.equals("countNotify")) {
            if(SharedConfig.getCountNotify() > 0){
                this@ActionsListActivity.runOnUiThread(java.lang.Runnable {
                    countNotify = SharedConfig.getCountNotify()
                    itemReceived!!.withBadge(countNotify.toString())
                    itemReceived!!.withBadgeStyle(BadgeStyle().withTextColor(getResources().getColor(R.color.white)).withColorRes(R.color.green))
                })
            }
        }
    }
}
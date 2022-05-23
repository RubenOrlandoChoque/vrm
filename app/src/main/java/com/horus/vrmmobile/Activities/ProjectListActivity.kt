package com.horus.vrmmobile.Activities

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Adapters.ItemListAdapter
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Managers.EventManager
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.ProjectRepository
import com.horus.vrmmobile.Repositories.ZonePoliticalFrontRepository
import com.horus.vrmmobile.Utils.DateUtils
import com.horus.vrmmobile.Utils.Event
import com.horus.vrmmobile.Utils.ItemList
import com.horus.vrmmobile.services.DownloadService
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
import kotlinx.android.synthetic.main.activity_project_list.*
import kotlinx.android.synthetic.main.app_bar_principal.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*


private var countNotify = 0
private var countEvent = 0

// Items Menu
private lateinit var result: Drawer
var itemProject1 : PrimaryDrawerItem? = null
var itemEvent1 : SecondaryDrawerItem? = null
var itemAction1: SecondaryDrawerItem? = null
var itemRRHH1 : SecondaryDrawerItem? = null
var itemUpdate1 : SecondaryDrawerItem? = null
var itemZoneSelect1 : SecondaryDrawerItem? = null
var itemLogOut1 : SecondaryDrawerItem? = null
var itemReceived1: SecondaryDrawerItem? = null
var itemSend1: SecondaryDrawerItem? = null

class ProjectListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.menuColor)
        }

        setImageProfile()
        setToolbar()
        setMenu()
        setZoneName()
        setViews()
        loadProjects()
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

    fun setZoneName(){
        val objZonePoliticalFront = ZonePoliticalFrontRepository.instance.getCompleteById(SharedConfig.getZonePoliticalFrontId())
        if(objZonePoliticalFront != null){
            itemZoneSelect1!!.withDescription(objZonePoliticalFront.Zone!!.Name).withDescriptionTextColor(getResources().getColor(R.color.gray))
            SharedConfig.setZoneName(objZonePoliticalFront.Zone!!.Name)
        }
    }

    fun setToolbar(){
        toolbar1.title = "Proyectos"
        toolbar1.setTitleTextColor(getResources().getColor(R.color.black))
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

        itemProject1 = PrimaryDrawerItem()
                .withIdentifier(1)
                .withName("Proyectos")
                .withTextColor(getResources().getColor(R.color.black))
                .withIcon(R.drawable.group15)
                .withDescription(SharedConfig.getProjectName())
                .withDescriptionTextColor(getResources().getColor(R.color.gray))
                .withEnabled(false)
        itemEvent1 = SecondaryDrawerItem()
                .withIdentifier(2)
                .withTextColor(getResources().getColor(R.color.black))
                .withName("Eventos")
                .withIcon(R.drawable.evento)
                .withEnabled(false)
                .withDescription(SharedConfig.getEventName())
                .withDescriptionTextColor(getResources().getColor(R.color.gray))
        itemAction1 = SecondaryDrawerItem()
                .withTextColor(getResources().getColor(R.color.black))
                .withIdentifier(3)
                .withName("Acciones")
                .withIcon(R.drawable.acciones2)
                .withEnabled(false)
        itemRRHH1 = SecondaryDrawerItem()
                .withIdentifier(4)
                .withTextColor(getResources().getColor(R.color.black))
                .withName("Recursos Humanos")
                .withIcon(R.drawable.ic_people_black)
        itemUpdate1 = SecondaryDrawerItem()
                .withIdentifier(5)
                .withTextColor(getResources().getColor(R.color.black))
                .withName("Actualizar")
                .withIcon(R.drawable.ic_cached_24px)
        itemZoneSelect1 = SecondaryDrawerItem()
                .withIdentifier(6)
                .withTextColor(getResources().getColor(R.color.black))
                .withName("Elegir Zona")
                .withIcon(R.drawable.ic_map_24px)
                .withDescription(SharedConfig.getZoneName())
                .withDescriptionTextColor(getResources().getColor(R.color.gray))
        itemLogOut1 = SecondaryDrawerItem()
                .withTextColor(getResources().getColor(R.color.black))
                .withIdentifier(7)
                .withName("Cerrar Sesion")
                .withIcon(R.drawable.ic_exit_to_app_24px)

        itemReceived1 = SecondaryDrawerItem()
                .withIdentifier(9)
                .withName("Recibidos")
                .withIcon(R.drawable.ic_inbox_black_24dp)
                .withTextColor(getResources().getColor(R.color.black))

        itemSend1 = SecondaryDrawerItem()
                .withIdentifier(10)
                .withName("Enviados")
                .withIcon(R.drawable.ic_send_black_24dp)
                .withTextColor(getResources().getColor(R.color.black))


        result = DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar1)
                .withAccountHeader(headerResult)
                .addDrawerItems (
                        itemProject1,
                        DividerDrawerItem(),
                        itemEvent1,
                        DividerDrawerItem(),
                        itemAction1,
                        DividerDrawerItem(),
                        ExpandableDrawerItem()
                                .withName("Mensajeria").withIcon(R.drawable.mensajeria)
                                .withIdentifier(8)
                                .withSubItems(itemReceived1, itemSend1),
                        DividerDrawerItem(),
                        itemRRHH1,
                        DividerDrawerItem(),
                        itemUpdate1,
                        DividerDrawerItem(),
                        itemZoneSelect1,
                        DividerDrawerItem(),
                        itemLogOut1,
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

    fun itemSelect(identifier: Int){
        when (identifier) {
            1 -> {
                //Proyectos
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
                DownloadService.downloadCatalogs(this@ProjectListActivity) {}
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

    private fun setViews(){
        rv_ap_list_projects.layoutManager = LinearLayoutManager(this,
                RecyclerView.VERTICAL,
                false)
        rv_ap_list_projects.adapter = object: ItemListAdapter(layoutInflater,
                ArrayList(),
                R.layout.item_list_row,
                R.drawable.group15,
                R.drawable.bg_gradient_orange){
            override fun setOnClick(objItem: ItemList) {
                val i = Intent(this@ProjectListActivity, EventsActivity::class.java)
                i.putExtra("projectId",objItem.Id)
                startActivity(i)
            }
        }
    }

    private fun loadProjects(){
        val projects = ArrayList<ItemList>()
        var listEventos = EventManager.getByZonePoliticalFront(SharedConfig.getZonePoliticalFrontId())
        var projectList = ArrayList<String>()
        for(e in listEventos){
            projectList.add(e.ProjectId)
        }
        var projectListClean = HashSet<String>(projectList)
        for(p in projectListClean){
            var objProject = ProjectRepository.instance.getById(p)
            if(objProject != null){
                var item = ItemList()
                item.Id = objProject.Id
                item.Name = objProject.Name
                var startDate = DateUtils.convertStringDateToString("${objProject.StartDateTime}Z")
                val calendarFrom = Calendar.getInstance()
                calendarFrom.time = DateUtils.convertDateShortToStringInverse(startDate)
                var startDateFormat = DateUtils.convertDateShortFormatToString(calendarFrom.time)
                var finishDate = DateUtils.convertStringDateToString("${objProject.FinishDateTime}Z")
                val calendarFinish = Calendar.getInstance()
                calendarFinish.time = DateUtils.convertDateShortToStringInverse(finishDate)
                var finishDateFormat = DateUtils.convertDateShortFormatToString(calendarFinish.time)
                item.Description = startDateFormat + " al " + finishDateFormat
                item.StartDate = startDateFormat
                item.StartDateTime = calendarFrom.time
                projects.add(item)
            }
        }
        projects.sortByDescending {it.StartDateTime}
        (rv_ap_list_projects.adapter as ItemListAdapter).setItems(projects)
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

    override fun onBackPressed() {
        onFinish()
        EventBus.getDefault().unregister(this)
    }

    private fun onFinish() {
        if (SharedConfig.appStarted) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        }
        finish()
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
        if (SharedConfig.getCountNotify() > 0) {
            countNotify = SharedConfig.getCountNotify()
            itemReceived1!!.withBadge(countNotify.toString())
            itemReceived1!!.withBadgeStyle(BadgeStyle().withTextColor(getResources().getColor(R.color.white)).withColorRes(R.color.green))
        }else {
            result.updateBadge(10, null)
        }
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onEventListener(event: Event) {
        if (event.eventName.equals("countNotify")) {
            if(SharedConfig.getCountNotify() > 0){
                this@ProjectListActivity.runOnUiThread(java.lang.Runnable {
                    countNotify = SharedConfig.getCountNotify()
                    itemReceived1!!.withBadge(countNotify.toString())
                    itemReceived1!!.withBadgeStyle(BadgeStyle().withTextColor(getResources().getColor(R.color.white)).withColorRes(R.color.green))
                })
            }
        }
    }
}
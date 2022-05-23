package com.horus.vrmmobile.Activities

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.horus.vrmmobile.Adapters.ItemListAdapter
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.PersonHierarchicalStructureRepository
import com.horus.vrmmobile.Utils.Event
import com.horus.vrmmobile.Utils.ItemList
import com.horus.vrmmobile.services.DownloadService
import com.mikepenz.iconics.IconicsDrawable
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
import com.mikepenz.materialdrawer.util.DrawerUIUtils
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_zone_list.*
import kotlinx.android.synthetic.main.app_bar_principal.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

private var countNotify1 = 0
private var countEvent1 = 0

// Items Menu
private lateinit var result: Drawer
private lateinit var headerResult: AccountHeader
var itemProject : PrimaryDrawerItem? = null
var itemEvent : SecondaryDrawerItem? = null
var itemAction : SecondaryDrawerItem? = null
var itemRRHH : SecondaryDrawerItem? = null
var itemUpdate : SecondaryDrawerItem? = null
var itemZoneSelect : SecondaryDrawerItem? = null
var itemLogOut : SecondaryDrawerItem? = null
var itemReceived: SecondaryDrawerItem? = null
var itemSend: SecondaryDrawerItem? = null

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal1)
        SharedConfig.appStarted = true

        setImageProfile()
        setToolbar()
        setHeader()
        setMenu()
        setViews()
        loadZones()
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
        toolbar1.title = "Elegir Zona"
        toolbar1.setTitleTextColor(getResources().getColor(R.color.black))
    }

    fun setHeader(){
        // Create the AccountHeader
        val profile = ProfileDrawerItem().withName(SharedConfig.getUserName() + " " + SharedConfig.getUserSurname())
                .withIcon(SharedConfig.getUserPhoto())
        headerResult = AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.menuColor)
                .addProfiles(
                        profile
                )
                .withOnAccountHeaderListener(object : AccountHeader.OnAccountHeaderListener {
                    override fun onProfileChanged(view: View?, profile: IProfile<*>, current: Boolean): Boolean {
                        return false
                    }
                })
                .build()
    }

    fun setMenu(){
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(false)

        setEnableItem()
    }

    private fun setEnableItem(){
        if(SharedConfig.getPoliticalFrontId().isEmpty()){
            itemRRHH!!.withEnabled(false)
        }else {
            itemRRHH!!.withEnabled(true)
        }
    }

    override fun onSaveInstanceState(_outState: Bundle) {
        super.onSaveInstanceState(result?.saveInstanceState(_outState) ?: _outState)
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
                DownloadService.downloadCatalogs(this@MainActivity) {}
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
        rv_az_list_zones.layoutManager = LinearLayoutManager(this,
                RecyclerView.VERTICAL,
                false)
        rv_az_list_zones.addItemDecoration(DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL))
        rv_az_list_zones.adapter = object: ItemListAdapter(layoutInflater,
                ArrayList(),
                R.layout.item_list_row,
                R.drawable.group8,
                R.drawable.bg_white_border){
            override fun setOnClick(objItem: ItemList) {
                SharedConfig.setZonePoliticalFrontId(objItem.Id)
                SharedConfig.setPersonHierarchicalStructureId((objItem.IdAux))
                SharedConfig.setPoliticalFrontId(objItem.PoliticalFrontId)
                val i = Intent(this@MainActivity, ProjectListActivity::class.java)
                startActivity(i)
                finish()
            }
        }
    }

    private fun loadZones(){
        val zones = ArrayList<ItemList>()
        var listZona = PersonHierarchicalStructureRepository.instance.getListByPersonId(SharedConfig.getUserId())
        for(z in listZona){
            if(z.ZonePoliticalFront != null)
                if(z.ZonePoliticalFront!!.Zone != null){
                    var item = ItemList()
                    item.Id = z.ZonePoliticalFrontId!!
                    item.Name = z.ZonePoliticalFront!!.Zone!!.Name
                    item.IdAux = z.Id
                    item.PoliticalFrontId = z.ZonePoliticalFront!!.PoliticalFrontId!!
                    zones.add(item)
                }
        }
        (rv_az_list_zones.adapter as ItemListAdapter).setItems(zones)
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

    private fun goRRHH() {
        val i = Intent(this, OrganizationActivity::class.java)
        startActivity(i)
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
        if (SharedConfig.getCountNotify() > 0) {
            countNotify1 = SharedConfig.getCountNotify()
            itemReceived!!.withBadge(countNotify1.toString())
            itemReceived!!.withBadgeStyle(BadgeStyle().withTextColor(getResources().getColor(R.color.white)).withColorRes(R.color.green))
        }else {
            result.updateBadge(10, null)
        }
        if (SharedConfig.getCountEvent() > 0) {
            countEvent1 = SharedConfig.getCountEvent()
            itemEvent!!.withBadge(countEvent1.toString())
            itemEvent!!.withBadgeStyle(BadgeStyle().withTextColor(getResources().getColor(R.color.white)).withColorRes(R.color.green))
        }else {
            result.updateBadge(2, null)
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
    fun onEventListener(event: Event) {
        if (event.eventName.equals("countNotify")) {
            if(SharedConfig.getCountNotify() > 0){
                this@MainActivity.runOnUiThread(java.lang.Runnable {
                    countNotify1 = SharedConfig.getCountNotify()
                    itemReceived!!.withBadge(countNotify1.toString())
                    itemReceived!!.withBadgeStyle(BadgeStyle().withTextColor(getResources().getColor(R.color.white)).withColorRes(R.color.green))
                })
            }
        }else if(event.eventName.equals("countEvent")){
            if(SharedConfig.getCountEvent() > 0){
                this@MainActivity.runOnUiThread(java.lang.Runnable {
                    countEvent1 = SharedConfig.getCountEvent()
                    itemEvent!!.withBadge(countEvent1.toString())
                    itemEvent!!.withBadgeStyle(BadgeStyle().withTextColor(getResources().getColor(R.color.white)).withColorRes(R.color.green))
                })
            }
        }
    }
}
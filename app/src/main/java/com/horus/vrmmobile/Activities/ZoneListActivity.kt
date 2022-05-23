package com.horus.vrmmobile.Activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.horus.vrmmobile.Adapters.ItemListAdapter
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.*
import com.horus.vrmmobile.Utils.ItemList
import kotlinx.android.synthetic.main.activity_zone_list.*

class ZoneListActivity : AppCompatActivity() {

    private var origen : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zone_list)

        txt_username.text = "${SharedConfig.getUserName()} ${SharedConfig.getUserSurname()}"

        origen = intent.getStringExtra("origen")
        if(origen.isNullOrEmpty())
            origen = ""

        Glide.with(this)
                .load(SharedConfig.getUserPhoto())
                .asBitmap()
                .error(R.drawable.ic_no_user)
                .placeholder(R.drawable.ic_no_user)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(profile_image)
        setViews()
        loadZones()
    }

    private fun setViews(){
        rv_az_list_zones.layoutManager = LinearLayoutManager(this,
                RecyclerView.VERTICAL,
                false)
        rv_az_list_zones.adapter = object: ItemListAdapter(layoutInflater,
                ArrayList(),
                R.layout.item_list_row,
                R.drawable.ic_map_v2,
                R.drawable.bg_gradient){
            override fun setOnClick(objItem: ItemList) {
                SharedConfig.setZonePoliticalFrontId(objItem.Id)
                SharedConfig.setPersonHierarchicalStructureId((objItem.IdAux))
                SharedConfig.setPoliticalFrontId(objItem.PoliticalFrontId)
                if(origen.equals("login")){
                    val i = Intent(this@ZoneListActivity, MainActivity::class.java)
                    startActivity(i)
                }
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
}
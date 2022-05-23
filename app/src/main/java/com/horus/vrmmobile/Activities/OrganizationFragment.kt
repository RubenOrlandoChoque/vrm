package com.horus.vrmmobile.Activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.horus.vrmmobile.Adapters.OrganizationAdapter
import com.horus.vrmmobile.Listeners.OrganizationListener
import com.horus.vrmmobile.Models.Dtos.EventDto
import com.horus.vrmmobile.R
import kotlinx.android.synthetic.main.fragment_organizations.view.*

internal const val PARENT_POSITION = "PARENT_POSITION"
internal const val ITEM_SELECT = "ITEM_SELECT"

class OrganizationFragment : Fragment() {

    private var parentPosition: Int = 0
    private var itemSelect: Int = -1
    private var organizationListener: OrganizationListener = object : OrganizationListener(){
        override fun onClick(top: Int) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentPosition = arguments!!.getInt(PARENT_POSITION, 0)
        itemSelect = arguments!!.getInt(ITEM_SELECT, -1)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_organizations, container, false)

        view.line_items.visibility = if(parentPosition == 0) View.INVISIBLE else View.VISIBLE
        view.members_list.layoutManager = LinearLayoutManager(context,
                RecyclerView.VERTICAL,
                false)

        val items = ArrayList<EventDto>()
        IntArray((1..10).random()).forEach {
            items.add(EventDto())
        }


        view.members_list.adapter = object: OrganizationAdapter(layoutInflater,
                items,
                context!!,
                parentPosition == 0,
                itemSelect,
                R.layout.item_member){
            override fun setOnClick(top: Int, itemSelect: Int) {
                arguments!!.putInt(ITEM_SELECT, itemSelect)
                organizationListener.onClick(top)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val locationContentMembers = IntArray(2)
        view.content_members.getLocationOnScreen(locationContentMembers)

        if(parentPosition == 0){
            view.line_parent.visibility = View.INVISIBLE
        }else{
            view.line_parent.visibility = View.VISIBLE
            val layoutParams = view.line_parent.layoutParams as RelativeLayout.LayoutParams
            layoutParams.setMargins(0,parentPosition - locationContentMembers[1],0,0)
            view.line_parent.layoutParams = layoutParams
        }
    }

    fun setOrganizationListener(organizationListener: OrganizationListener){
        this.organizationListener = organizationListener
    }
}
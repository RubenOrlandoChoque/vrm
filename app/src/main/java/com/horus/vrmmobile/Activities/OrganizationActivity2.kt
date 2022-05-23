package com.horus.vrmmobile.Activities

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.horus.vrmmobile.Listeners.OrganizationListener
import com.horus.vrmmobile.R
import java.util.*

class OrganizationActivity2 : AppCompatActivity() {
    val stackFragments = Stack<OrganizationFragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organizations2)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.purple)
        }

        createFragment(0)
    }

    private fun createFragment(top: Int) {
        val organizationFragment = OrganizationFragment()
        val bundle = Bundle()
        bundle.putInt(PARENT_POSITION, top)
        organizationFragment.arguments = bundle
        organizationFragment.setOrganizationListener(object: OrganizationListener(){
            override fun onClick(top: Int) {
                createFragment(top)
            }
        })

        stackFragments.push(organizationFragment)
        if(stackFragments.size == 1){
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.content_fragment, organizationFragment)
            }.commit()
        }else{
            val ft = supportFragmentManager.beginTransaction()
            ft.setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left)
            ft.replace(R.id.content_fragment, organizationFragment)
            ft.commit()
        }
    }

    private fun backFragment() {
        stackFragments.pop()
        val organizationFragment = stackFragments.peek()
        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(R.anim.enter_left_to_right, R.anim.exit_left_to_right)
        ft.replace(R.id.content_fragment, organizationFragment)
        ft.commit()
    }

    override fun onBackPressed() {
        if(stackFragments.size == 1){
            super.onBackPressed()
        }else{
            backFragment()
        }
    }
}


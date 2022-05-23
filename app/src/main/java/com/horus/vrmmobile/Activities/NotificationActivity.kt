package com.horus.vrmmobile.Activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.horus.vrmmobile.Adapters.AdapterImage
import com.horus.vrmmobile.Adapters.AdapterLinks
import com.horus.vrmmobile.Models.Message
import com.horus.vrmmobile.Models.Multimedia
import com.horus.vrmmobile.Models.MultimediaType
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.HistoryChangeRepository
import com.horus.vrmmobile.Repositories.MessageMultimediaRepository
import com.horus.vrmmobile.Repositories.MessageRepository
import com.horus.vrmmobile.Repositories.MultimediaRepository
import com.horus.vrmmobile.Utils.DateUtils
import com.horus.vrmmobile.Utils.Utils
import com.horus.vrmmobile.services.NotificationJobService
import kotlinx.android.synthetic.main.include_message_addresses.*
import kotlinx.android.synthetic.main.include_message_attachments.*
import kotlinx.android.synthetic.main.include_message_body.*
import kotlinx.android.synthetic.main.include_message_images.*
import kotlinx.android.synthetic.main.include_message_navigation.*
import java.util.*
import kotlin.collections.ArrayList


class NotificationActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    var expand: Boolean = false
    var message: Message? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.include_message_body)
        message = MessageRepository.instance.getById(intent.getStringExtra("id"))
        setRead()
        onToggleAddresses()
        setButtons()
        tvNoInternetBody.visibility = View.GONE
        setInfo()
        if(message?.CompleteDownload == true) {
            swipe_refresh_message.isEnabled = false
        }
        swipe_refresh_message.setColorSchemeColors(Color.WHITE, Color.WHITE, Color.WHITE)
        swipe_refresh_message.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this, R.color.toolbarBackgroundColor))
        if(message?.FromMobile == true){
            inNavigation.visibility = View.GONE
        }
    }

    private fun setRead() {
        if (message != null && message?.IsRead == false) {
            message!!.IsRead = true
            HistoryChangeRepository.add(message!!.Id, "MessagesTo", "INSERT")
            MessageRepository.instance.addOrUpdate(message!!, false)
            MessageRepository.instance.syncReadMessages()
        }
    }

    private fun setButtons() {
        ivExpanderAddress.setOnClickListener { onToggleAddresses() }
        btn_back.setOnClickListener { finish() }
//        bnvActions.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED
        bnvActions.setOnNavigationItemSelectedListener(object : BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.action_delete -> {
                        MaterialDialog(this@NotificationActivity).show {
                            title(R.string.title)
                            message(text = getString(R.string.message_delete_message))
                            positiveButton(R.string.accept)
                            negativeButton(R.string.cancel)
                            cancelOnTouchOutside(false)
                            positiveButton {
                                MessageRepository.instance.softDelete(message!!.Id)
                                it.dismiss()
                                val data = Intent()
                                data.putExtra("id", message!!.Id)
                                data.putExtra("deleted", true)
                                setResult(Activity.RESULT_OK, data)
                                finish()
                            }
                        }
                        return true
                    }
                    R.id.action_reply -> {
                        val i = Intent(this@NotificationActivity, MessageComposeActivity::class.java)
                        startActivity(i)
                        return true
                    }
                    else -> return false
                }
            }
        })

        swipe_refresh_message.setOnRefreshListener(this)
    }

    override fun onRefresh() {
        swipe_refresh_message.isRefreshing = true
        NotificationJobService.downloadMessage(message!!.Id,
                onSuccess = {
                    swipe_refresh_message.isRefreshing = false
                    message = MessageRepository.instance.getById(intent.getStringExtra("id"))
                    setInfo()
                    if(message?.CompleteDownload == true) {
                        swipe_refresh_message.isEnabled = false
                    }
                },
                onError = {swipe_refresh_message.isRefreshing = false})
    }

    private fun setInfo() {
        if (message != null) {
            tvBody.text = message!!.Text
            tvFromEx.text = if (!message!!.Sender.isNullOrEmpty()) message!!.Sender else "Sin remitente"
            tvSubjectEx.text = message!!.Title

            try {
                val d = DateUtils.convertDateToStringInverse(message!!.SendDate)
                if (d != null) {
                    if (Utils.isDateSame(d, Date())) {
                        tvTimeEx.text = "Hoy ${DateUtils.convertDateToHHmm(d)}"
                    } else {
                        tvTimeEx.text = DateUtils.toCompleteString(d)
                    }
                }else{
                    tvTimeEx.text = " - "
                }
            }catch (e: Exception){
                tvTimeEx.text = " - "
            }

            // load multimedia
            val messagesMultimedias = MessageMultimediaRepository.instance.getAllByField("MessageId", message!!.Id)
            val multimedias = ArrayList<Multimedia>()
            messagesMultimedias.forEach {
                val multimedia = MultimediaRepository.instance.getById(it.MultimediaId)
                if (multimedia != null) {
                    multimedias.add(multimedia)
                }
            }
            // links
            val links = multimedias.filter { it.MultimediaTypeId != MultimediaType.photo }.map { it.Path } // todos los que no sean fotos
            rvAttachment.layoutManager = LinearLayoutManager(this)
            rvAttachment.itemAnimator = null
            rvAttachment.adapter = object : AdapterLinks(this, links) {
                override fun onItemClick(path: String) {
                    try{
                        var p = path
                        if(!p.startsWith("http")) {
                            p = "http://$p"
                        }
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(p)
                        startActivity(i)
                    }catch (e: java.lang.Exception) {

                    }
                }
            }
            (rvAttachment.adapter as AdapterLinks).notifyDataSetChanged()
            inAttachmentsLinks.visibility = if (links.isEmpty()) View.GONE else View.VISIBLE

            // photos
            val maxWidth = Utils.getMaxWidth(this.windowManager, 120)
            val numColumns = Utils.getNumColumns(this.windowManager, maxWidth)
            rvImage.setHasFixedSize(false)
            rvImage.layoutManager = GridLayoutManager(this, numColumns)
            val photos = multimedias.filter { it.MultimediaTypeId == MultimediaType.photo }.map { it.Path }
            rvImage.adapter = object : AdapterImage(this, photos, Utils.getWidthColumn(this.windowManager, numColumns, Utils.convertDpToPx(this, 52))) {
                override fun onItemClick(path: String) {
                    val i = Intent(this@NotificationActivity, FullScreenImageActivity::class.java)
                    i.putExtra("path", path)
                    startActivity(i)
                }
            }
            inImages.visibility = if (photos.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun onToggleAddresses() {
        expand = !expand
        ivExpanderAddress.setImageLevel(if (expand) 1/* more */ else 0  /* less */)
        container_addresses_header.visibility = if (expand) View.VISIBLE else View.GONE
    }
}
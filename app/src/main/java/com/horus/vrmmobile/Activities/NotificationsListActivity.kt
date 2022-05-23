package com.horus.vrmmobile.Activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.horus.vrmmobile.Adapters.MessagesAdapter
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Helpers.DividerItemDecoration
import com.horus.vrmmobile.Models.Message
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.MessageRepository
import com.horus.vrmmobile.Utils.DateUtils
import com.horus.vrmmobile.Utils.Event
import com.horus.vrmmobile.services.NotificationJobService
import com.miguelcatalan.materialsearchview.MaterialSearchView
import kotlinx.android.synthetic.main.activity_list_notifications.*
import kotlinx.android.synthetic.main.activity_list_notifications.drawer_layout
import kotlinx.android.synthetic.main.activity_principal.*
import kotlinx.android.synthetic.main.app_bar_principal.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.textColor
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class NotificationsListActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener, MessagesAdapter.MessageAdapterListener {
    private var textSearch: String = ""
    private var messages = ArrayList<Message>()
    private var mAdapter: MessagesAdapter? = null
    private val VIEW_MESSAGE = 555
    private var origen: String? = ""
    private var inboxMsg: Boolean? = false
    private var sendMsg: Boolean? = false
    private var inbox = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_notifications)
        origen = intent.getStringExtra("origen")

        inboxMsg = intent.getBooleanExtra("inboxMsg", false)
        sendMsg = intent.getBooleanExtra("sendMsg", false)

        swipe_refresh_layout!!.setOnRefreshListener(this)
        mAdapter = MessagesAdapter(this, messages, this)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        recycler_view!!.layoutManager = mLayoutManager
        recycler_view!!.itemAnimator = DefaultItemAnimator()
        recycler_view!!.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recycler_view!!.adapter = mAdapter
        var timer = Timer()
        val DELAY: Long = 1000 // milliseconds

        search_view.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                textSearch = query
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                textSearch = newText
                timer.cancel()
                timer = Timer()
                timer.schedule(
                        object : TimerTask() {
                            override fun run() {
                                filter()
                            }
                        },
                        DELAY
                )
                return false
            }
        })
        btn_search.setOnClickListener {
            search_view.showSearch(true)
        }

        main_toolbar_container.visibility = View.VISIBLE
        delete_container.visibility = View.GONE

        btn_delete_notifications.setOnClickListener { deleteNotifications() }
        btn_back_selected_to_delete.setOnClickListener { backDeleteContainer() }
//        btn_back.setOnClickListener { onFinish() }

        swipe_refresh_layout!!.post { getMessages() }
        swipe_refresh_layout.setColorSchemeColors(Color.WHITE, Color.WHITE, Color.WHITE)
        swipe_refresh_layout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this, R.color.toolbarBackgroundColor))

        navview.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_inbox -> {
                    onMessageInbox()
                }
                R.id.menu_sent -> {
                    onMessageSend()
                }
            }
            drawer_layout.closeDrawers()
            true
        }
        newMessage.setOnClickListener { newMessage() }

        setEvent()

        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_black)
        toolbar.setNavigationOnClickListener{
            onBackPressed()
        }
    }

    fun setEvent(){
        if (inboxMsg!!) { onMessageInbox()}
        if (sendMsg!!) { onMessageSend()}
    }

    private fun setDrawerMenu() {
        navview.getHeaderView(0).lbluser.text = SharedConfig.getUserName() + " " + SharedConfig.getUserSurname()
        Glide.with(this)
                .load(SharedConfig.getUserPhoto())
                .asBitmap()
                .placeholder(R.drawable.user_default)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(navview.getHeaderView(0).imguser)
    }

    private fun newMessage() {
        val i = Intent(this@NotificationsListActivity, MessageComposeActivity::class.java)
        startActivity(i)
    }

    private fun onMessageInbox() {
        messages.clear()
        inbox = true
        txt_ab_title.text = "Mensajes Recibidos"
        getMessages()
    }

    private fun onMessageSend() {
        messages.clear()
        txt_ab_title.text = "Mensajes Enviados"
        inbox = false
        getMessages()
    }

    private fun onFinish() {
        if(!SharedConfig.appStarted) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        }
        finish()
    }

    override fun onBackPressed() {
        onFinish()
    }

    private fun backDeleteContainer() {
        main_toolbar_container.visibility = View.VISIBLE
        delete_container.visibility = View.GONE

        mAdapter!!.clearSelections()
        swipe_refresh_layout!!.isEnabled = true
        recycler_view!!.post {
            mAdapter!!.resetAnimationIndex()
        }
    }

    private fun deleteNotifications() {
        deleteMessages()
        main_toolbar_container.visibility = View.VISIBLE
        delete_container.visibility = View.GONE
    }

    private fun filter() {
        Log.e("filter", textSearch)
        runOnUiThread {
            if (textSearch.trim().isNullOrEmpty()) {
                messages = ArrayList()
                mAdapter = MessagesAdapter(this, messages, this)
                recycler_view!!.adapter = mAdapter
                getMessages()
            } else {
                messages = ArrayList(messages.filter { it.Title.contains(textSearch, true) || it.Text.contains(textSearch, true) || it.Sender.contains(textSearch, true) })
                mAdapter = MessagesAdapter(this, messages, this)
                recycler_view!!.adapter = mAdapter
                mAdapter!!.notifyDataSetChanged()
            }
        }
    }

    /**
     * Fetches mail messages by making HTTP request
     * url: http://api.androidhive.info/json/inbox.json
     */
    private fun getMessages() {
        swipe_refresh_layout!!.isRefreshing = true
        val messagesList =
                when (inbox) {
                    true ->
                        ArrayList(MessageRepository.instance.getAll().filter { !it.IsDeleted && !it.FromMobile })
                    false ->
                        ArrayList(MessageRepository.instance.getAll().filter { !it.IsDeleted && it.FromMobile })
                }
        messagesList.forEach { message ->
            if (messages.find { it.Id == message.Id } == null) {
                try{
                    val d = DateUtils.convertDateToStringInverse(message.SendDate)
                    message.SendDateTime = d
                }catch (e: Exception) { }
                messages.add(message)
            }
        }
        messages.sortByDescending {it.SendDateTime}
        showHideEmptyMessages()
        mAdapter!!.notifyDataSetChanged()
        swipe_refresh_layout!!.isRefreshing = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onRefresh() {
        // swipe refresh is performed, fetch the messages again
        getMessages()
    }

    override fun onIconClicked(position: Int) {
        main_toolbar_container.visibility = View.GONE
        delete_container.visibility = View.VISIBLE

        toggleSelection(position)
    }

    override fun onIconImportantClicked(position: Int) {
        // Star icon is clicked,
        // mark the message as important
        val message = messages[position]
//        message.isImportant = !message.isImportant
        messages[position] = message
        mAdapter!!.notifyDataSetChanged()
    }

    override fun onMessageRowClicked(position: Int) {
        // verify whether action mode is enabled or not
        // if enabled, change the row state to activated
        if (MessagesAdapter.getSelectedItemCount(mAdapter!!) > 0) {
            enableActionMode(position)
        } else {
            main_toolbar_container.visibility = View.VISIBLE
            delete_container.visibility = View.GONE
            // read the message which removes bold from the row
            val message = messages[position]
            message.IsRead = true
//            MessageRepository.instance.addOrUpdate(message, false)
            messages[position] = message
            mAdapter!!.notifyDataSetChanged()

            val intent = Intent(this@NotificationsListActivity, NotificationActivity::class.java)
            intent.putExtra("id", message.Id)
            startActivityForResult(intent, VIEW_MESSAGE)
        }
    }

    override fun onRowLongClicked(position: Int) {
        // long press is performed, enable action mode
        enableActionMode(position)
    }

    override fun onDownloadClick(position: Int) {
        swipe_refresh_layout!!.isRefreshing = true
        NotificationJobService.downloadMessage(messages[position].Id,
                onSuccess = {
                    onDownloadMessageFinish(messages[position].Id)
                    swipe_refresh_layout!!.isRefreshing = false
                },
                onError = {
                    swipe_refresh_layout!!.isRefreshing = false
                })
    }

    private fun enableActionMode(position: Int) {
        main_toolbar_container.visibility = View.GONE
        delete_container.visibility = View.VISIBLE

        toggleSelection(position)
    }

    private fun toggleSelection(position: Int) {
        mAdapter!!.toggleSelection(position)
        val count = MessagesAdapter.getSelectedItemCount(mAdapter!!)

        if (count == 0) {
            main_toolbar_container.visibility = View.VISIBLE
            delete_container.visibility = View.GONE
        } else {
            txt_count_to_delete.text = count.toString()
        }
    }

    // deleting the messages from recycler view
    private fun deleteMessages() {
        mAdapter!!.resetAnimationIndex()
        val selectedItemPositions = mAdapter!!.getSelectedItems()
        for (i in selectedItemPositions.indices.reversed()) {
            mAdapter!!.removeData(selectedItemPositions[i])
        }
        mAdapter!!.notifyDataSetChanged()
        mAdapter!!.clearSelections()
        swipe_refresh_layout!!.isEnabled = true
        recycler_view!!.post {
            mAdapter!!.resetAnimationIndex()
        }
        showHideEmptyMessages()
    }

    private fun showHideEmptyMessages() {
        if (messages.isEmpty()) {
            recycler_view.visibility = View.GONE
            notification_empty_list.visibility = View.VISIBLE
        } else {
            recycler_view.visibility = View.VISIBLE
            notification_empty_list.visibility = View.GONE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewMessage(event: Event) {
        when (event.eventName) {
            "new_message_incoming" -> onNewMessageIncoming(event.data)
            "download_message_finish" -> onDownloadMessageFinish(event.data)
        }
    }

    private fun onNewMessageIncoming(data: String?) {
        val n = messages.firstOrNull { it.Id == data }
        if (n == null) {
            val found = MessageRepository.instance.getById(data)
            if (found != null) {
                messages.add(0, found)
                try{
                    val d = DateUtils.convertDateToStringInverse(found.SendDate)
                    found.SendDateTime = d
                }catch (e: Exception) { }
                mAdapter!!.notifyDataSetChanged()
                showHideEmptyMessages()
            }
        }
    }

    private fun onDownloadMessageFinish(data: String?) {
        val n = messages.firstOrNull { it.Id == data }
        val found = MessageRepository.instance.getById(data)
        if (n != null && found != null) {
            n.Text = found.Text
            n.Color = found.Color
            n.CompleteDownload = found.CompleteDownload
            n.HasAttachments = found.HasAttachments
            mAdapter!!.notifyDataSetChanged()
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == VIEW_MESSAGE) {
            val deleted = data?.getBooleanExtra("deleted", false)
            if (deleted == true) {
                val id = data.getStringExtra("id")
                val m = MessageRepository.instance.getById(id)
                val mm = messages.firstOrNull { it.Id == m?.Id }
                messages.remove(mm)
                mAdapter!!.notifyDataSetChanged()
                showHideEmptyMessages()
            }
        }
    }
}


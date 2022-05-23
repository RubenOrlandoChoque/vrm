package com.horus.vrmmobile.Activities

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.horus.vrmmobile.Models.Note
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.NoteRepository
import kotlinx.android.synthetic.main.activity_note.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class NoteActivity : AppCompatActivity() {

    internal var noteId: String? = null
    internal var note: Note? = null

    private var shouldFireDeleteEvent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        noteId = intent.getStringExtra("noteId")
        if (noteId == null) {
            note = Note()
//            val now = Date()
//            note!!.setCreatedAt(now)
//            note!!.save()
            noteId = note!!.Id
        }else{
            note = NoteRepository.instance.getById(noteId)
        }
        bind()
    }

    private fun bind() {
        body!!.setText(note?.NoteBody)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 1) {
            //SQLite.delete().from(Note::class.java).where(Note_Table.id.`is`(note!!.getId())).execute()
            shouldFireDeleteEvent = true
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNoteEditedEvent(noteEditedEvent: Note) {
        Log.e(TAG, "onNoteEditedEvent() called with: noteEditedEvent = [$noteEditedEvent]")
        if (note!!.Id === noteEditedEvent.Id) {
            bind()
        }
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onNoteFoldersUpdatedEvent(noteFoldersUpdatedEvent: NoteFoldersUpdatedEvent) {
//        if (note!!.Id === noteFoldersUpdatedEvent.getNoteId()) {
//            bind()
//        }
//    }

    override fun onBackPressed() {
        super.onBackPressed()
        assert(note != null)
        if (shouldFireDeleteEvent) {
            EventBus.getDefault().postSticky(note)
        } else {
            note!!.NoteBody = body!!.text.toString()
//            note!!.save()
            NoteRepository.instance.addOrUpdate(note!!)
            EventBus.getDefault().postSticky(note)
        }
    }

    companion object {
        private val TAG = "NoteActivity"
    }
}
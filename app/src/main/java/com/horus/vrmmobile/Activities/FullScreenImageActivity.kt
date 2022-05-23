package com.horus.vrmmobile.Activities

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.Action
import com.horus.vrmmobile.Models.Note
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.NoteRepository
import com.horus.vrmmobile.Utils.DateUtils
import com.horus.vrmmobile.Utils.ModelSyncBuilder
import com.horus.vrmmobile.Utils.Utils
import com.horus.vrmmobile.VRMApplication.Companion.context
import kotlinx.android.synthetic.main.activity_full_screen_image.*
import kotlinx.android.synthetic.main.activity_note.view.*
import kotlinx.android.synthetic.main.view_note_card.view.*
import kotlinx.android.synthetic.main.view_note_card.view.body
import org.greenrobot.eventbus.EventBus
import java.util.*

class FullScreenImageActivity : Activity() {

    private var actionId: String? = ""
    private var multimediaId:String?=""
    private var activityOrigin:String?=""
    private var objectNote: Note?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_full_screen_image)

        activityOrigin = intent.getStringExtra("activity_origin")
        multimediaId = intent.getStringExtra("MultimediaId")
        actionId = intent.getStringExtra("actionId")
        val path = intent.getStringExtra("path")

        objectNote = NoteRepository.instance.getByMultimediaId(multimediaId)

        if(objectNote != null){
            btnAddNote.text = "EDITAR NOTA"
        }

        val imgDisplay = findViewById<SubsamplingScaleImageView>(R.id.imgDisplay)
        if (path.startsWith("http")) {
            Glide.with(this)
                    .load("$path?height=700")
                    .asBitmap()
                    .error(R.drawable.default_error)
                    .placeholder(R.drawable.ic_hourglass)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, glideAnimation: GlideAnimation<in Bitmap>) {
                            img_loading.visibility = View.GONE
                            imgDisplay.setImage(ImageSource.bitmap(resource))
                        }
                    })
        } else {
            imgDisplay.setImage(ImageSource.uri(path))
        }

        btnClose.setOnClickListener { this@FullScreenImageActivity.finish() }
        btnAddNote.setOnClickListener { showNoteDialog() }

        if(activityOrigin.equals("MessageComposeActivity")) {
            btnAddNote.visibility = View.GONE
        }
    }


    private fun showNoteDialog() {
        if(objectNote != null){
            editNote(objectNote!!)
        }else {
            val dialog = MaterialDialog(this).show {
                customView(viewRes = R.layout.activity_note, scrollable = false, noVerticalPadding = true)
                cancelOnTouchOutside(false)
                noAutoDismiss()
            }
            val view = dialog.getCustomView()
            view.body.text = ""
            view.title_note_header.text = getString(R.string.add_note)
            view.btn_delete_note.visibility = View.GONE
            view.btn_cancel_note.setOnClickListener { dialog.dismiss() }
            view.btn_accept_note.setOnClickListener {
                val note = ModelSyncBuilder.create(Note())
                note.AuthorId = SharedConfig.getPersonHierarchicalStructureId() // todo por ahora se pone el autor en null para poder sincronizar. En realidad deberia ser el usuario logueado, que ademas debe formar parte de la estructura jerarquica
                note.ActionId = actionId
                note.CreationDate = DateUtils.convertDateToString(Date())
                note.NoteBody = view.body.text.toString()
                note.MultimediaId = multimediaId!!
                NoteRepository.instance.addOrUpdate(note)
                EventBus.getDefault().postSticky(note)
                dialog.dismiss()
                this@FullScreenImageActivity.finish()
            }
        }
    }

    fun editNote(note: Note){
        if (note != null) {
            val dialog = MaterialDialog(this).show {
                customView(viewRes = R.layout.activity_note, scrollable = false, noVerticalPadding = true)
                cancelOnTouchOutside(false)
                noAutoDismiss()
            }
            val view = dialog.getCustomView()
            view.body.text = note.NoteBody
            view.title_note_header.text = getString(R.string.edit_note)
            view.btn_cancel_note.setOnClickListener { dialog.dismiss() }
            view.btn_accept_note.setOnClickListener {
                note.NoteBody = view.body.text.toString()
                NoteRepository.instance.addOrUpdate(note)
                dialog.dismiss()
                this@FullScreenImageActivity.finish()
            }
        }
    }

}

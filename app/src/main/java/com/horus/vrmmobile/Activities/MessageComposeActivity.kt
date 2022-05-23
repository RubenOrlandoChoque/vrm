package com.horus.vrmmobile.Activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.horus.vrmmobile.Adapters.AdapterComposeAttachment
import com.horus.vrmmobile.Adapters.AdapterComposeImage
import com.horus.vrmmobile.Adapters.AudioRecordingsAdapter
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.*
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.MessageMultimediaRepository
import com.horus.vrmmobile.Repositories.MessageRepository
import com.horus.vrmmobile.Repositories.MultimediaRepository
import com.horus.vrmmobile.Utils.DateUtils
import com.horus.vrmmobile.Utils.Utils
import com.horus.vrmmobile.VRMApplication.Companion.context
import com.horus.vrmmobile.recorder.AudioFile
import com.horus.vrmmobile.services.SyncService
import com.vansuita.pickimage.bundle.PickSetup
import com.vansuita.pickimage.dialog.PickImageDialog
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_message_compose.*
import me.rosuh.filepicker.config.FilePickerManager
import org.jetbrains.anko.toast
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class MessageComposeActivity : AppCompatActivity() {
    var adapterComposeAttachment: AdapterComposeAttachment? = null
    var adapterComposeImage: AdapterComposeImage? = null
    val attachments = ArrayList<Multimedia>()
    val photos = ArrayList<Multimedia>()
    val audioFileLinks = ArrayList<AudioFile>()
    val AUDIO_RECORDING_RESULT = 12121
    var recordings: AudioRecordingsAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_compose)
        init()
        showGroupAttachments()
        showGroupImages()
        showGroupAudios()
    }

    fun init() {
        // links
        adapterComposeAttachment = AdapterComposeAttachment(attachments) { multimedia ->
            MaterialDialog(this@MessageComposeActivity).show {
                title(R.string.title)
                message(text = getString(R.string.message_delete_multimedia))
                positiveButton(R.string.accept)
                negativeButton(R.string.cancel)
                cancelOnTouchOutside(false)
                positiveButton {
                    attachments.remove(multimedia)
                    adapterComposeAttachment?.notifyDataSetChanged()
                    showGroupAttachments()
                    it.dismiss()
                }
            }
        }
        rvAttachment.layoutManager = LinearLayoutManager(this)
        rvAttachment.itemAnimator = null
        rvAttachment.adapter = adapterComposeAttachment
        adapterComposeAttachment?.notifyDataSetChanged()

        // fotos
        val maxWidth = Utils.getMaxWidth(this.windowManager, 120)
        val numColumns = Utils.getNumColumns(this.windowManager, maxWidth)
        rvImage.setHasFixedSize(false)
        rvImage.layoutManager = GridLayoutManager(this, numColumns)
        adapterComposeImage = AdapterComposeImage(
                this,
                photos,
                Utils.getWidthColumn(this.windowManager, numColumns, Utils.convertDpToPx(this, 52)),
                {
                    val i = Intent(this@MessageComposeActivity, FullScreenImageActivity::class.java)
                    i.putExtra("path", it.PathLocal)
                    i.putExtra("activity_origin", "MessageComposeActivity")
                    startActivity(i)
                },
                { multimedia ->
                    MaterialDialog(this@MessageComposeActivity).show {
                        title(R.string.title)
                        message(text = getString(R.string.message_delete_multimedia))
                        positiveButton(R.string.accept)
                        negativeButton(R.string.cancel)
                        cancelOnTouchOutside(false)
                        positiveButton {
                            photos.remove(multimedia)
                            adapterComposeImage?.notifyDataSetChanged()
                            showGroupImages()
                            it.dismiss()
                        }
                    }
                }
        )
        rvImage.adapter = adapterComposeImage

        // audios
        val layoutManager = LinearLayoutManager(this)
        audio_list.layoutManager = layoutManager
        recordings = AudioRecordingsAdapter(this, layoutManager, audioFileLinks) { showGroupAudios() }
        audio_list.adapter = recordings

        edit_bar.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_take_photo -> {
                    onActionTakePhoto()
                    true
                }
                R.id.menu_attachment -> {
                    onActionAttachment()
                    true
                }
                R.id.menu_link -> {
                    onActionLink()
                    true
                }
                R.id.menu_record_audio -> {
                    onRecordAudio()
                    true
                }
                else -> false
            }
        }

        bottom_navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_delete -> {
                    onDeleteAction()
                    true
                }
                R.id.action_send -> {
                    onSendAction()
                    true
                }
                else -> false
            }
        }

        btn_back.setOnClickListener { onDeleteAction() }
    }

    private fun onRecordAudio() {
        val i = Intent(this@MessageComposeActivity, RecordingActivity::class.java)
        startActivityForResult(i, AUDIO_RECORDING_RESULT)
    }

    private fun onSendAction() {
        val subject = etSubject.text.toString().trim()
        val body = etBody.text.toString().trim()
        val message = Message()
        if (subject.isEmpty() || body.isEmpty()) {
            MaterialDialog(this@MessageComposeActivity).show {
                title(R.string.title)
                message(text = getString(R.string.message_not_complete))
                positiveButton(R.string.accept)
                cancelOnTouchOutside(false)
            }
        } else {
            message.Title = subject
            message.Text = body
            message.Sender = "${SharedConfig.getUserSurname()} ${SharedConfig.getUserName()}"
            message.SendDate = DateUtils.convertDateToString(Date())
            message.SenderId = SharedConfig.getUserId()
            message.FromMobile = true
            message.CompleteDownload = true
            message.Color = -13914325
            message.IsRead = true
            MessageRepository.instance.addOrUpdate(message)

            val multimedia = ArrayList<Multimedia>()
            multimedia.addAll(attachments)
            multimedia.addAll(photos)
            multimedia.addAll(audioFileLinks.map { it.multimedia })
            multimedia.forEach {
                MultimediaRepository.instance.addOrUpdate(it)
                val mm = MessageMultimedias()
                mm.MessageId = message.Id
                mm.MultimediaId = it.Id
                MessageMultimediaRepository.instance.addOrUpdate(mm)
            }
            MultimediaRepository.instance.uploadPending()
            SyncService.push()
            finish()
        }
    }

    private fun onDeleteAction() {
        MaterialDialog(this@MessageComposeActivity).show {
            title(R.string.title)
            message(text = getString(R.string.message_delete_message))
            positiveButton(R.string.accept)
            negativeButton(R.string.cancel)
            cancelOnTouchOutside(false)
            positiveButton {
                finish()
            }
        }
    }

    private fun onActionAttachment() {
        FilePickerManager
                .from(this@MessageComposeActivity)
                .setTheme(R.style.VRMFilePickerTheme)
                .forResult(FilePickerManager.REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            FilePickerManager.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val list = FilePickerManager.obtainData()
                    list.forEach {
                        Log.e("result", it)
                        val multimedia = Multimedia()
                        multimedia.PathLocal = it
                        multimedia.MultimediaTypeId = MultimediaType.file
                        multimedia.MultimediaCategoryId = MultimediaCategory.general
                        attachments.add(multimedia)
                        adapterComposeAttachment?.notifyDataSetChanged()
                        showGroupAttachments()
                    }
                } else {
                    Toast.makeText(this@MessageComposeActivity, "No se seleccionó ningun archivo", Toast.LENGTH_SHORT).show()
                }
            }

            AUDIO_RECORDING_RESULT -> {
                if (resultCode == Activity.RESULT_OK) {
                    val fileName = data?.getStringExtra("filePath")
                    if (!fileName.isNullOrEmpty()) {
                        val f = File(fileName)
                        if (f.isFile) {
                            val mp = MediaPlayer.create(context, Uri.fromFile(f))
                            if (mp != null) {
                                val d = mp.duration
                                mp.release()
                                val multimedia = Multimedia()
                                multimedia.PathLocal = f.path
                                multimedia.MultimediaTypeId = MultimediaType.audio
                                multimedia.MultimediaCategoryId = MultimediaCategory.general
                                val af = AudioFile(f, d, multimedia)
                                audioFileLinks.add(af)
                            } else {
                                Log.e("", f.toString())
                            }
                        }
                        recordings?.notifyDataSetChanged()
                        showGroupAudios()
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showGroupAttachments() {
        grpAttachments.visibility = if (attachments.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showGroupAudios() {
        grpAudios.visibility = if (audioFileLinks.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showGroupImages() {
        grpImages.visibility = if (photos.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun onActionTakePhoto() {
        PickImageDialog.build(
                PickSetup()
                        .setTitle("Seleccione")
                        .setCancelText("Cancelar")
                        .setButtonOrientation(LinearLayoutCompat.HORIZONTAL)
                        .setProgressText("Cargando imagen...")
                        .setIconGravity(Gravity.TOP)
                        .setCameraButtonText("Cámara")
                        .setGalleryButtonText("Galeria")
                        .setMaxSize(500)
        ).setOnPickResult { pickResult ->
            Log.e("pickResult", pickResult?.path)
            if (pickResult.error == null) {
                if (!pickResult.path.isNullOrEmpty()) {
                    val compressedFileName = "_" + File(pickResult.path).name
                    val compressedImageFile = Compressor(this@MessageComposeActivity)
                            .setQuality(70)
                            .setCompressFormat(Bitmap.CompressFormat.JPEG)
                            .setDestinationDirectoryPath(Utils.getDirectoryMultimedia())
                            .compressToFile(File(pickResult.path), compressedFileName)
//                    File(pickResult.path).delete()

                    val multimedia = Multimedia()
                    multimedia.PathLocal = compressedImageFile.path
                    multimedia.MultimediaTypeId = MultimediaType.photo
                    multimedia.MultimediaCategoryId = MultimediaCategory.general
                    photos.add(multimedia)
                    adapterComposeImage?.notifyDataSetChanged()

                    showGroupImages()
                }
            } else {
                context?.toast(pickResult.error.message!!)
            }
        }.show(this@MessageComposeActivity)
    }

    private fun onActionLink() {
        MaterialDialog(this).show {
            input { dialog, text ->
                if (Patterns.WEB_URL.matcher(text).matches()) {
                    val multimedia = Multimedia()
                    multimedia.Path = text.toString()
                    multimedia.PathLocal = text.toString()
                    multimedia.MultimediaTypeId = MultimediaType.link
                    multimedia.MultimediaCategoryId = MultimediaCategory.general
                    adapterComposeAttachment?.notifyDataSetChanged()
                    attachments.add(multimedia)
                    showGroupAttachments()
                    dialog.dismiss()
                } else {
                    MaterialDialog(this@MessageComposeActivity).show {
                        title(R.string.title)
                        message(text = getString(R.string.url_no_matches))
                        positiveButton(R.string.accept)
                        cancelOnTouchOutside(false)
                    }
                }
            }
            noAutoDismiss()
            title(text = "Nuevo Link")
            positiveButton(R.string.accept)
            negativeButton(R.string.cancel)
            cancelOnTouchOutside(false)
            negativeButton { it.dismiss() }
        }
    }

    override fun onBackPressed() {
        onDeleteAction()
    }
}

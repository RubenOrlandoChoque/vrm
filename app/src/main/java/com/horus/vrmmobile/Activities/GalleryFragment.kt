package com.horus.vrmmobile.Activities

import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.horus.vrmmobile.Adapters.GalleryAdapter
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.*
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Utils.Utils
import com.vansuita.pickimage.bundle.PickSetup
import com.vansuita.pickimage.dialog.PickImageDialog
import kotlinx.android.synthetic.main.fragment_gallery_event.view.*
import org.jetbrains.anko.toast
import id.zelory.compressor.Compressor
import java.io.File
import java.util.*
import android.location.LocationManager
import android.content.Context
import android.widget.Button
import com.horus.vrmmobile.Models.Dtos.MultimediaData
import com.horus.vrmmobile.Repositories.*
import com.horus.vrmmobile.Utils.Event
import com.mikepenz.materialdrawer.holder.BadgeStyle
import kotlinx.android.synthetic.main.view_zero_photos.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.support.v4.runOnUiThread


class GalleryFragment : Fragment() {

    private var action: Action? = null
    private val photos = ArrayList<MultimediaData>()
    private var dialogAddPartaker: MaterialDialog? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var latitude: Double? = null
    var longitude: Double? = null
    var btnAddPhoto: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val actionString = arguments?.getString("EVENT_PARAM")!!
        action = Utils.convertStringToObject(actionString, Action::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_gallery_event, container, false)
        val maxWidth = Utils.getMaxWidth(activity!!.windowManager, 120)
        val numColumns = Utils.getNumColumns(activity!!.windowManager, maxWidth)

        photos.clear()
        btnAddPhoto = activity!!.findViewById<View>(R.id.icon_add) as Button

        btnAddPhoto!!.setOnClickListener { addPhoto() }


        var actionMultimedias = ActionMultimediaRepository.instance.getByActionId(action!!.Id)
        for (m in actionMultimedias) {
            var objMultimedia = MultimediaRepository.instance.getById(m.MultimediaId)
            if (objMultimedia != null) {
                photos.add(MultimediaData(objMultimedia.Id, objMultimedia.PathLocal))
//                photos.add(objMultimedia)
            }
        }
        view.txt_count.text = "${photos.size} fotos"

        view.gallery_content.layoutManager = GridLayoutManager(activity, numColumns)
        view.gallery_content.adapter = object : GalleryAdapter(context!!, photos, Utils.getWidthColumn(activity!!.windowManager, numColumns, Utils.convertDpToPx(context!!, 52))) {
            override fun onAddClick() {
                if (!isGPSEnabled(context!!)) {
                    permission()
                    setContadorPhotos(view)
                } else {
                    startGPS()
                    showPickerImage()
                    setContadorPhotos(view)
                }
            }

            override fun onItemClick(multimediaData: MultimediaData) {
                val i = Intent(activity, FullScreenImageActivity::class.java)
                i.putExtra("path", multimediaData.Path)
                i.putExtra("actionId", action!!.Id)
                i.putExtra("MultimediaId", multimediaData.Id)
                startActivity(i)
            }

            override fun onDelete(multimediaData: MultimediaData) {
                MaterialDialog(context!!).show {
                    title(R.string.title)
                    message(text = getString(R.string.message_delete_multimedia_gallery))
                    positiveButton(R.string.accept)
                    negativeButton(R.string.cancel)
                    cancelOnTouchOutside(false)
                    positiveButton {
                        photos.remove(multimediaData)
                        (view!!.gallery_content.adapter as GalleryAdapter).updateData(photos)
                        view!!.txt_count.text = "${photos.size} fotos"
                        (view!!.gallery_content.adapter as GalleryAdapter).notifyDataSetChanged()
                        MultimediaRepository.instance.softDelete(multimediaData.Id)
                        val allByActionIdAndMultimediaId = ActionMultimediaRepository.instance.getAllByActionIdAndMultimediaId(action!!.Id, multimediaData.Id)
                        allByActionIdAndMultimediaId.forEach { am -> ActionMultimediaRepository.instance.softDelete(am.Id) }
                        val objectNote = NoteRepository.instance.getByMultimediaId(multimediaData.Id)
                        if (objectNote != null) {
                            NoteRepository.instance.softDelete(objectNote.Id)
                        }
                        it.dismiss()
                        setContadorPhotos(view)
                    }
                }
            }
        }
        setContadorPhotos(view)
        return view
    }

    fun addPhoto() {
        if (!isGPSEnabled(context!!)) {
            permission()
        } else {
            startGPS()
            showPickerImage()
        }
    }


    fun setContadorPhotos(view: View?) {
        val count = photos.size
        if (count == 0) {
            view?.zero_photos_view?.visibility = View.VISIBLE
            view?.txt_count?.visibility = View.GONE
        } else {
            view?.zero_photos_view?.visibility = View.GONE
            view?.txt_count?.visibility = View.VISIBLE
        }
    }


    private fun permission() {
        dialogAddPartaker = MaterialDialog(context!!).show {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
            customView(viewRes = R.layout.dialog_turnon_gps, scrollable = false)
            positiveButton(R.string.accept)
            negativeButton(R.string.cancel)
            cancelOnTouchOutside(false)
            noAutoDismiss()
            positiveButton {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent)
                it.dismiss()
                startGPS()
                showPickerImage()
            }
            negativeButton {
                showPickerImage()
                it.dismiss()
            }
        }
    }

    fun isGPSEnabled(mContext: Context): Boolean {
        val locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun startGPS() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            latitude = location?.latitude
            longitude = location?.longitude
        }
    }

    private fun showPickerImage() {
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
            if (pickResult.error == null) {
                if (!pickResult.path.isNullOrEmpty()) {
                    val compressedFileName = "_" + File(pickResult.path).name
                    var compressedImageFile = Compressor(context!!)
                            .setQuality(70)
                            .setCompressFormat(Bitmap.CompressFormat.JPEG)
                            .setDestinationDirectoryPath(Utils.getDirectoryMultimedia())
                            .compressToFile(File(pickResult.path), compressedFileName)
//                    File(pickResult.path).delete()
                    val multimediaId = saveImage(compressedImageFile.path)
                    photos.add(MultimediaData(multimediaId, compressedImageFile.path))
                    (view!!.gallery_content.adapter as GalleryAdapter).updateData(photos)
                    view!!.txt_count.text = "${photos.size} fotos"
                    setContadorPhotos(view!!)
                    val objMulti = MultimediaRepository.instance.getByPath(compressedImageFile.path)
                    val i = Intent(activity, FullScreenImageActivity::class.java)
                    i.putExtra("path", compressedImageFile.path)
                    i.putExtra("actionId", action!!.Id)
                    i.putExtra("MultimediaId", objMulti!!.Id)
                    startActivity(i)

                    compressedImageFile = null
                }
            } else {
                context!!.toast(pickResult.error.message!!)
            }
        }.show(activity)
    }


    private fun saveImage(path: String): String {
        val objMultimedia = Multimedia()
        objMultimedia.MultimediaTypeId = MultimediaType.photo
        objMultimedia.MultimediaCategoryId = MultimediaCategory.general
        objMultimedia.PathLocal = path
        objMultimedia.Path = ""
        objMultimedia.Duration = 0
        if (latitude != null) {
            objMultimedia.Gps_X = latitude!!
        } else {
            objMultimedia.Gps_X = 0.0
        }
        if (longitude != null) {
            objMultimedia.Gps_Y = longitude!!
        } else {
            objMultimedia.Gps_Y = 0.0
        }
        MultimediaRepository.instance.addOrUpdate(objMultimedia)

        val objActionMultimedia = ActionMultimedia()
        objActionMultimedia.ActionId = action!!.Id
        objActionMultimedia.MultimediaId = objMultimedia.Id
        ActionMultimediaRepository.instance.addOrUpdate(objActionMultimedia)

        MultimediaRepository.instance.upload(objMultimedia.Id, File(objMultimedia.PathLocal), objMultimedia.Id, SharedConfig.getPoliticalFrontId())
        return objMultimedia.Id
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onEventListener(event: Event) {
        if (event.eventName.equals("did_not_upload")) {
            activity!!.runOnUiThread(java.lang.Runnable {
                MaterialDialog(context!!).show {
                    title(R.string.title)
                    message(text = getString(R.string.not_upload_photo))
                    positiveButton(R.string.accept)
                    negativeButton(R.string.cancel)
                    positiveButton {
                        it.dismiss()
                    }
                }
            })
        }
    }

}
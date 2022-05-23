package com.horus.vrmmobile.Adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener
import com.google.android.material.snackbar.Snackbar
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.Action
import com.horus.vrmmobile.Models.Note
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.HistoryChangeRepository
import com.horus.vrmmobile.Repositories.NoteRepository
import com.horus.vrmmobile.Utils.DateUtils
import com.horus.vrmmobile.Utils.ModelSyncBuilder
import com.horus.vrmmobile.Utils.Utils
import kotlinx.android.synthetic.main.activity_note.view.*
import kotlinx.android.synthetic.main.fragment_note_list.view.*
import kotlinx.android.synthetic.main.view_note_card.view.body
import kotlinx.android.synthetic.main.view_zero_notes.view.*
import org.greenrobot.eventbus.EventBus
import java.util.*
import android.util.Log
import android.widget.Button


/**
 * Created by MohMah on 8/21/2016.
 */
class NoteListFragment : Fragment() {

    internal var adapter: NoteListAdapter? = null
    private var action: Action? = null
    var btnAddNote: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val actionString = arguments?.getString("EVENT_PARAM")!!
        action = Utils.convertStringToObject(actionString, Action::class.java)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_note_list, container, false)

        btnAddNote = activity!!.findViewById<View>(R.id.icon_add) as Button

        btnAddNote!!.setOnClickListener { showNoteDialog() }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val slm = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        slm.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        view!!.fnl_recycler_view.layoutManager = slm
        val notes = NoteRepository.instance.getByActionId(action!!.Id).sortedByDescending { s -> s.RegisterDate }
        adapter = NoteListAdapter(notes,
                onNoteClick = { note ->
                    if (note != null) {
                        val dialog = MaterialDialog(context!!).show {
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
                            if (NoteRepository.instance.addOrUpdate(note)) {
                                adapter!!.notifyDataSetChanged()
                            }
                            dialog.dismiss()
                        }
                        view.btn_delete_note.visibility = View.GONE
                        view.btn_delete_note.setOnClickListener {
                            MaterialDialog(context!!).show {
                                title(R.string.title)
                                message(text = getString(R.string.message_delete_note))
                                positiveButton(R.string.accept)
                                negativeButton(R.string.cancel)
                                cancelOnTouchOutside(false)
                                positiveButton {
//                                    adapter!!.onNoteDeletedEvent(note.Id)
                                    NoteRepository.instance.softDelete(note.Id)
                                    it.dismiss()
                                    dialog.dismiss()
                                    setContadorNotas()
                                }
                            }
                        }
                    }
                }
        )
        view!!.fnl_recycler_view.adapter = adapter
        view!!.fnl_recycler_view.swipeListener = onItemSwipeListener
        view!!.fnl_recycler_view.orientation = DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING
        setContadorNotas()
    }

    fun setContadorNotas() {
        val count = adapter?.itemCount
        view?.txt_count_notes?.text = "${count}  notas"
        if (count == 0) {
            view?.zero_notes_view?.visibility = View.VISIBLE
            view?.fnl_recycler_view?.visibility = View.GONE
            view?.txt_count_notes?.visibility = View.GONE
        } else {
            view?.zero_notes_view?.visibility = View.GONE
            view?.fnl_recycler_view?.visibility = View.VISIBLE
            view?.txt_count_notes?.visibility = View.VISIBLE
        }
    }

    private fun showNoteDialog() {
        val dialog = MaterialDialog(context!!).show {
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
            note.ActionId = action!!.Id
            note.CreationDate = DateUtils.convertDateToString(Date())
            note.NoteBody = view.body.text.toString()
            NoteRepository.instance.addOrUpdate(note)
            EventBus.getDefault().postSticky(note)
            adapter!!.insertItem( 0, note)
            dialog.dismiss()
            setContadorNotas()
        }
    }

    private val onItemSwipeListener = object : OnItemSwipeListener<Note> {
        override fun onItemSwiped(position: Int, direction: OnItemSwipeListener.SwipeDirection, item: Note) {
            var deleted = true
            val itemSwipedSnackBar = Snackbar.make(view!!, getString(R.string.note_delete), Snackbar.LENGTH_SHORT)
            itemSwipedSnackBar.setAction("deshacer") {
                deleted = false
                adapter!!.insertItem( position, item)
                setContadorNotas()
            }
            itemSwipedSnackBar.addCallback(object : Snackbar.Callback() {
                override fun onDismissed(snackbar: Snackbar?, event: Int) {
                    if(deleted) {
                        NoteRepository.instance.softDelete(item.Id)
                        setContadorNotas()
                    }
                }
                override fun onShown(snackbar: Snackbar?) {

                }
            })
            itemSwipedSnackBar.show()
        }
    }
}

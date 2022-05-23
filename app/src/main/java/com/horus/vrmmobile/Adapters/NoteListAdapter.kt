package com.horus.vrmmobile.Adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import com.horus.vrmmobile.Models.Note
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Utils.DateUtils
import com.horus.vrmmobile.Utils.Utils
import com.horus.vrmmobile.VRMApplication

class NoteListAdapter(dataSet: List<Note> = emptyList(), val onNoteClick: (note: Note?) -> Unit) : DragDropSwipeAdapter<Note, NoteListAdapter.ViewHolder>(dataSet) {
    class ViewHolder(iceCreamLayout: View) : DragDropSwipeAdapter.ViewHolder(iceCreamLayout) {
        val body: TextView = itemView.findViewById(R.id.body)
        val txt_note_date: TextView = itemView.findViewById(R.id.txt_note_date)
        var view = itemView
    }

    override fun getViewHolder(itemView: View): ViewHolder {
        return ViewHolder(itemView)
    }


    override fun onBindViewHolder(item: Note, viewHolder: ViewHolder, position: Int) {
        val lp = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val noteGap: Int = (Utils.dp2px(VRMApplication.context!!.resources.getDimensionPixelSize(R.dimen.notes_gap)) / 2).toInt()
        lp.setMargins(noteGap, noteGap, 0, 0)
        viewHolder.view.layoutParams = lp

        viewHolder.body.text = item.NoteBody
        viewHolder.txt_note_date.text = DateUtils.toCompleteString(item.RegisterDate)
        viewHolder.view.setOnClickListener { onNoteClick(item) }
    }

    override fun canBeDragged(item: Note, viewHolder: ViewHolder, position: Int): Boolean {
        return false
    }

    override fun getViewToTouchToStartDraggingItem(item: Note, viewHolder: ViewHolder, position: Int) = null
}


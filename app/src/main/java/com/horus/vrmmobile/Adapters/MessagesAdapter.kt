package com.horus.vrmmobile.Adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils
import android.util.SparseBooleanArray
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.horus.vrmmobile.Helpers.CircleTransform
import com.horus.vrmmobile.Helpers.FlipAnimator
import com.horus.vrmmobile.Models.Message
import com.horus.vrmmobile.Models.MessageOld
import com.horus.vrmmobile.R
import com.horus.vrmmobile.Repositories.MessageRepository
import com.horus.vrmmobile.Utils.DateUtils
import com.horus.vrmmobile.Utils.Utils
import java.lang.Exception
import java.util.*

/**
 * Created by Ravi Tamada on 21/02/17.
 * www.androidhive.info
 */

class MessagesAdapter(private val mContext: Context, private val messageOlds: MutableList<Message>, private val listener: MessageAdapterListener) : RecyclerView.Adapter<MessagesAdapter.MyViewHolder>() {
    private val selectedItems: SparseBooleanArray

    // array used to perform multiple animation at once
    private val animationItemsIndex: SparseBooleanArray
    private var reverseAllAnimations = false

    override fun getItemCount(): Int = messageOlds.size

    val selectedItemCount: Int
        get() = selectedItems.size()

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnLongClickListener {
        var from: TextView
        var subject: TextView
        var message: TextView
        var iconText: TextView
        var timestamp: TextView
        var iconImp: ImageView
        var imgProfile: ImageView
        var messageContainer: LinearLayout
        var iconContainer: RelativeLayout
        var iconBack: RelativeLayout
        var iconFront: RelativeLayout
        var ivHasAttachments: ImageView
        var ivCompleteDownload: ImageView

        init {
            from = view.findViewById<View>(R.id.from) as TextView
            subject = view.findViewById<View>(R.id.txt_primary) as TextView
            message = view.findViewById<View>(R.id.txt_secondary) as TextView
            iconText = view.findViewById<View>(R.id.icon_text) as TextView
            timestamp = view.findViewById<View>(R.id.timestamp) as TextView
            iconBack = view.findViewById<View>(R.id.icon_back) as RelativeLayout
            iconFront = view.findViewById<View>(R.id.icon_front) as RelativeLayout
            iconImp = view.findViewById<View>(R.id.icon_star) as ImageView
            imgProfile = view.findViewById<View>(R.id.icon_profile) as ImageView
            messageContainer = view.findViewById<View>(R.id.message_container) as LinearLayout
            iconContainer = view.findViewById<View>(R.id.icon_container) as RelativeLayout
            ivHasAttachments = view.findViewById<View>(R.id.ivHasAttachments) as ImageView
            ivCompleteDownload = view.findViewById<View>(R.id.ivCompleteDownload) as ImageView
            view.setOnLongClickListener(this)
        }

        override fun onLongClick(view: View): Boolean {
            listener.onRowLongClicked(adapterPosition)
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            return true
        }
    }


    init {
        selectedItems = SparseBooleanArray()
        animationItemsIndex = SparseBooleanArray()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.message_list_row, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val message = messageOlds[position]

        // displaying text view data
        holder.from.text = message.Sender
        holder.subject.text = message.Title
        holder.message.text = message.Text

        try {
            val d = DateUtils.convertDateToStringInverse(message.SendDate)
            if (d != null) {
                if (Utils.isDateSame(d, Date())) {
                    holder.timestamp.text = DateUtils.convertDateToHHmm(d)
                } else {
                    holder.timestamp.text = DateUtils.convertDateToDayMonth(d)
                }
            }else{
                holder.timestamp.text = " - "
            }
        }catch (e: Exception){
            holder.timestamp.text = " - "
        }
        holder.ivHasAttachments.visibility = if(message.HasAttachments) View.VISIBLE else View.GONE

        holder.ivCompleteDownload.visibility = if(message.CompleteDownload) View.GONE else View.VISIBLE

        // displaying the first letter of From in icon text
        holder.iconText.text = if(message.Sender.isNullOrEmpty()) "" else message.Sender.substring(0, 1)?.toUpperCase()

        // change the row state to activated
        holder.itemView.isActivated = selectedItems.get(position, false)

        // change the font style depending on message read status
        applyReadStatus(holder, message)

        // handle message star
        applyImportant(holder, message)

        // handle icon animation
        applyIconAnimation(holder, position)

        // display profile image
        applyProfilePicture(holder, message)

        // apply click events
        applyClickEvents(holder, position)
    }

    private fun applyClickEvents(holder: MyViewHolder, position: Int) {
        holder.iconContainer.setOnClickListener { listener.onIconClicked(position) }

        holder.iconImp.setOnClickListener { listener.onIconImportantClicked(position) }

        holder.messageContainer.setOnClickListener { listener.onMessageRowClicked(position) }

        holder.ivCompleteDownload.setOnClickListener { listener.onDownloadClick(position) }

        holder.messageContainer.setOnLongClickListener { view ->
            listener.onRowLongClicked(position)
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            true
        }
    }

    private fun applyProfilePicture(holder: MyViewHolder, message: Message) {
//        if (!TextUtils.isEmpty(message.picture)) {
//            Glide.with(mContext).load(messageOld.picture)
//                    .thumbnail(0.5f)
//                    .crossFade()
//                    .transform(CircleTransform(mContext))
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .into(holder.imgProfile)
//            holder.imgProfile.colorFilter = null
//            holder.iconText.visibility = View.GONE
//        } else {
        holder.imgProfile.setImageResource(R.drawable.bg_circle)
        holder.imgProfile.setColorFilter(message.Color)
        holder.iconText.visibility = View.VISIBLE
//        }
    }

    private fun applyIconAnimation(holder: MyViewHolder, position: Int) {
        if (selectedItems.get(position, false)) {
            holder.iconFront.visibility = View.GONE
            resetIconYAxis(holder.iconBack)
            holder.iconBack.visibility = View.VISIBLE
            holder.iconBack.alpha = 1f
            if (currentSelectedIndex == position) {
                FlipAnimator.flipView(mContext, holder.iconBack, holder.iconFront, true)
                resetCurrentIndex()
            }
        } else {
            holder.iconBack.visibility = View.GONE
            resetIconYAxis(holder.iconFront)
            holder.iconFront.visibility = View.VISIBLE
            holder.iconFront.alpha = 1f
            if (reverseAllAnimations && animationItemsIndex.get(position, false) || currentSelectedIndex == position) {
                FlipAnimator.flipView(mContext, holder.iconBack, holder.iconFront, false)
                resetCurrentIndex()
            }
        }
    }


    // As the views will be reused, sometimes the icon appears as
    // flipped because older view is reused. Reset the Y-axis to 0
    private fun resetIconYAxis(view: View) {
        if (view.rotationY != 0f) {
            view.rotationY = 0f
        }
    }

    fun resetAnimationIndex() {
        reverseAllAnimations = false
        animationItemsIndex.clear()
    }

//    override fun getItemId(position: Int): Long {
//        super.getItemId(position)
//        return messageOlds[position].id.toLong()
//    }

    private fun applyImportant(holder: MyViewHolder, message: Message) {
        /*if (message.isImportant) {
//            holder.iconImp.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_star_black_24dp))
            holder.iconImp.setColorFilter(ContextCompat.getColor(mContext, R.color.icon_tint_selected))
        } else {
//            holder.iconImp.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_star_border_black_24dp))
            holder.iconImp.setColorFilter(ContextCompat.getColor(mContext, R.color.icon_tint_normal))
        }*/
    }

    private fun applyReadStatus(holder: MyViewHolder, message: Message) {
        if (message.IsRead) {
            holder.from.setTypeface(null, Typeface.NORMAL)
            holder.subject.setTypeface(null, Typeface.NORMAL)
            holder.from.setTextColor(ContextCompat.getColor(mContext, R.color.subject))
            holder.subject.setTextColor(ContextCompat.getColor(mContext, R.color.message))
        } else {
            holder.from.setTypeface(null, Typeface.BOLD)
            holder.subject.setTypeface(null, Typeface.BOLD)
            holder.from.setTextColor(ContextCompat.getColor(mContext, R.color.from))
            holder.subject.setTextColor(ContextCompat.getColor(mContext, R.color.subject))
        }
    }

    fun toggleSelection(pos: Int) {
        currentSelectedIndex = pos
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos)
            animationItemsIndex.delete(pos)
        } else {
            selectedItems.put(pos, true)
            animationItemsIndex.put(pos, true)
        }
        notifyItemChanged(pos)
    }

    fun clearSelections() {
        reverseAllAnimations = true
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<Int> {
        val items = ArrayList<Int>(selectedItems.size())
        for (i in 0 until selectedItems.size()) {
            items.add(selectedItems.keyAt(i))
        }
        return items
    }

    fun removeData(position: Int) {
        val m = messageOlds[position]
        MessageRepository.instance.softDelete(m.Id)
        messageOlds.removeAt(position)
        resetCurrentIndex()
    }

    private fun resetCurrentIndex() {
        currentSelectedIndex = -1
    }

    interface MessageAdapterListener {
        fun onIconClicked(position: Int)

        fun onIconImportantClicked(position: Int)

        fun onMessageRowClicked(position: Int)

        fun onRowLongClicked(position: Int)

        fun onDownloadClick(position: Int)
    }

    companion object {

        // index is used to animate only the selected row
        // dirty fix, find a better solution
        private var currentSelectedIndex = -1

        fun getSelectedItemCount(messagesAdapter: MessagesAdapter): Int {
            return messagesAdapter.selectedItems.size()
        }
    }
}
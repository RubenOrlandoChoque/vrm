package com.horus.vrmmobile.Adapters

import android.app.AlertDialog
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hendraanggrian.recyclerview.widget.ExpandableItem
import com.hendraanggrian.recyclerview.widget.ExpandableRecyclerView
import com.horus.vrmmobile.Models.Multimedia
import com.horus.vrmmobile.R
import com.horus.vrmmobile.recorder.AudioFile
import com.horus.vrmmobile.recorder.RecorderUtils
import java.text.SimpleDateFormat
import java.util.*


class AudioRecordingsAdapter(
        var context: Context,
        var layout: LinearLayoutManager,
        private var audioFileLinks: ArrayList<AudioFile>,
        var onDelete: (m: AudioFile) -> Unit
    ) : ExpandableRecyclerView.Adapter<AudioRecordingsAdapter.ViewHolder>(layout) {
    var player: MediaPlayer? = null
    var updatePlayer: Runnable? = null
    var selected = -1
    val TAG = "AudioRecordingAdapter"
    var handler: Handler = Handler()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
                LayoutInflater.from(context).inflate(
                        R.layout.recording,
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val convertView = holder.view
        val audioFile = audioFileLinks[position]

        val title = convertView.findViewById<TextView>(R.id.recording_title)
        title.text = audioFile.file.name

        val s = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val time = convertView.findViewById(R.id.recording_time) as TextView
        time.text = s.format(Date(audioFile.file.lastModified()))

        val dur = convertView.findViewById(R.id.recording_duration) as TextView
        dur.text = RecorderUtils.formatDuration(context, audioFile.duration.toLong())

        val size = convertView.findViewById(R.id.recording_size) as TextView
        size.text = RecorderUtils.formatSize(context, audioFile.file.length())

        val playerBase = convertView.findViewById<LinearLayout>(R.id.recording_player)
        playerBase.setOnClickListener { }

        val delete = Runnable {
            val builder = AlertDialog.Builder(context, R.style.AlertDialogRecorder)
            builder.setTitle(R.string.delete_recording)
            builder.setMessage("...\\" + audioFile.file.name + "\n\n" + context.getString(R.string.are_you_sure))
            builder.setPositiveButton(R.string.yes) { dialog, which ->
                playerStop()
                dialog.cancel()
                audioFile.file.delete()
                audioFileLinks.remove(audioFile)
                notifyDataSetChanged()
                onDelete(audioFile)
            }
            builder.setNegativeButton(R.string.no) { dialog, which -> dialog.cancel() }
            builder.show()
        }

        updatePlayerText(convertView, audioFile)

        val play = convertView.findViewById<ImageView>(R.id.recording_player_play)
        play.setOnClickListener {
            if (player == null) {
                playerPlay(playerBase, audioFile)
            } else if (player!!.isPlaying) {
                playerPause(playerBase, audioFile)
            } else {
                playerPlay(playerBase, audioFile)
            }
        }

        val trash = convertView.findViewById<ImageView>(R.id.recording_player_trash)
        trash.setOnClickListener { delete.run() }

        convertView.setOnClickListener { select(-1) }

//        convertView!!.setOnLongClickListener(object : View.OnLongClickListener {
//            override fun onLongClick(v: View): Boolean {
//                val popup = PopupMenu(getContext(), v)
//                val inflater = popup.getMenuInflater()
//                inflater.inflate(R.menu.menu_context, popup.getMenu())
//                popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener() {
//                    fun onMenuItemClick(item: MenuItem): Boolean {
//                        if (item.getItemId() == R.id.action_delete) {
//                            delete.run()
//                            return true
//                        }
//                        if (item.getItemId() == R.id.action_rename) {
//                            rename.run()
//                            return true
//                        }
//                        return false
//                    }
//                })
//                popup.show()
//                return true
//            }
//        })
    }

    override fun getItemCount(): Int {
        return audioFileLinks.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val item: ExpandableItem = itemView.findViewById(R.id.row) as ExpandableItem
        val view = itemView
    }

    private fun playerPlay(v: View, audioFile: AudioFile) {
        if (player == null)
            player = MediaPlayer.create(context, Uri.fromFile(audioFile.file))
        if (player == null) {
            Toast.makeText(context, R.string.file_not_found, Toast.LENGTH_SHORT).show()
            return
        }
        player!!.start()

        updatePlayerRun(v, audioFile)
    }

    private fun playerPause(v: View, audioFile: AudioFile) {
        if (player != null) {
            player!!.pause()
        }
        if (updatePlayer != null) {
            handler.removeCallbacks(updatePlayer)
            updatePlayer = null
        }
        updatePlayerText(v, audioFile)
    }

    private fun playerStop() {
        if (updatePlayer != null) {
            handler.removeCallbacks(updatePlayer)
            updatePlayer = null
        }
        if (player != null) {
            player!!.stop()
            player!!.release()
            player = null
        }
    }

    private fun updatePlayerRun(v: View, audioFile: AudioFile) {
        val playing = updatePlayerText(v, audioFile)

        if (updatePlayer != null) {
            handler.removeCallbacks(updatePlayer)
            updatePlayer = null
        }

        if (!playing) {
            return
        }

        updatePlayer = Runnable { updatePlayerRun(v, audioFile) }
        handler.postDelayed(updatePlayer, 200)
    }

    private fun updatePlayerText(v: View, audioFile: AudioFile): Boolean {
        val i = v.findViewById(R.id.recording_player_play) as ImageView

        val playing = player != null && player!!.isPlaying

        i.setImageResource(if (playing) R.drawable.ic_pause_24dp else R.drawable.ic_play_arrow_white_24dp)

        val start = v.findViewById(R.id.recording_player_start) as TextView
        val bar = v.findViewById(R.id.recording_player_seek) as SeekBar
        val end = v.findViewById(R.id.recording_player_end) as TextView

        var c = 0
        var d = audioFile.duration

        if (player != null) {
            c = player!!.currentPosition
            d = player!!.duration
        }

        bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser)
                    return

                if (player == null)
                    playerPlay(v, audioFile)

                if (player != null) {
                    player!!.seekTo(progress)
                    if (!player!!.isPlaying)
                        playerPlay(v, audioFile)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        start.text = RecorderUtils.formatDuration(context, c.toLong())
        bar.max = d
        bar.keyProgressIncrement = 1
        bar.progress = c
        end.text = "-" + RecorderUtils.formatDuration(context, (d - c).toLong())

        return playing
    }

    fun select(pos: Int) {
        selected = pos
        notifyDataSetChanged()
        playerStop()
    }
}



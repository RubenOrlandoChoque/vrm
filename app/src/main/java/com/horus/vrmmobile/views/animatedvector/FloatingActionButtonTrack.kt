package com.horus.vrmmobile.views.animatedvector

import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.horus.vrmmobile.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay

class FloatingActionButtonTrack : FloatingActionButton {
    var onActionFab: OnActionFabClickListener? = null
    private var currentMode: Mode = Mode.PLAYPAUSE
        set(value) {
            field = value
            setImageDrawable(field)
        }

    //<editor-fold desc="Chaining Constructors">
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        val typedArray = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.FloatingActionButtonTrack,
                0, 0)
        try {
            currentMode = getMode(typedArray.getInteger(R.styleable.FloatingActionButtonTrack_FloatingActionButtonTrackMode, Mode.PLAYSTOP.styleableInt))
        } finally {
            typedArray.recycle()
        }
        val eventActor = GlobalScope.actor<View>(Dispatchers.Main) {
            channel.consumeEach {
                if(onActionFab?.onClick(it) == true){
                    this@FloatingActionButtonTrack.drawable.startAsAnimatable()
                    delay(300)
                    val oppositeMode = currentMode.getOppositeMode()
                    currentMode = oppositeMode
                }
            }
        }
        this.setOnClickListener {
            eventActor.offer(it)
        }
    }
    //</editor-fold>

    //<editor-fold desc="Helpers">
    private fun setImageDrawable(mode: Mode) {
        val animatedVector = ContextCompat.getDrawable(context, mode.drawableRes)
        this.setImageDrawable(animatedVector)
    }


    private fun getMode(styleableInt: Int): Mode = listOf(Mode.PLAYPAUSE, Mode.PAUSEPLAY, Mode.PLAYSTOP, Mode.STOPPLAY).first { it.styleableInt == styleableInt }


    private fun Drawable.startAsAnimatable() = (this as Animatable).start()

    sealed class Mode(val styleableInt: Int, @DrawableRes val drawableRes: Int) {
        object PLAYPAUSE : Mode(0, R.drawable.play_to_pause_animation)
        object PLAYSTOP : Mode(1, R.drawable.play_to_stop_animation)
        object PAUSEPLAY : Mode(2, R.drawable.pause_to_play_animation)
        object STOPPLAY : Mode(3, R.drawable.stop_to_play_animation)
    }

    private val opposites = mapOf(
            Mode.PLAYPAUSE to Mode.PAUSEPLAY,
            Mode.PAUSEPLAY to Mode.PLAYPAUSE,
            Mode.PLAYSTOP to Mode.STOPPLAY,
            Mode.STOPPLAY to Mode.PLAYSTOP
    )

    private fun Mode.getOppositeMode() = opposites[this]!!
    fun setOnActionFabClickListener(onActionFabClickListener: OnActionFabClickListener) {
        onActionFab = onActionFabClickListener
    }

    fun changeMode(mode: Mode) {
        currentMode = mode
    }
    interface OnActionFabClickListener {
        fun onClick(view: View): Boolean
    }
}

package com.horus.vrmmobile.Utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet

/**
 * Created by Administrador on 03/06/2015.
 * Clase que maneja las imagenes redondeadas.
 */
class RoundedImageView(ctx: Context, attrs: AttributeSet) : androidx.appcompat.widget.AppCompatImageView(ctx, attrs) {

    override fun onDraw(canvas: Canvas) {

        val drawable = drawable ?: return

        if (width == 0 || height == 0) {
            return
        }
        val b = (drawable as BitmapDrawable).bitmap
        val bitmap = b.copy(Bitmap.Config.ARGB_8888, true)

        val w = width
        val h = height


        val roundBitmap = getRoundedCroppedBitmap(bitmap, w)
        canvas.drawBitmap(roundBitmap, 0f, 0f, null)

    }

    companion object {

        fun getRoundedCroppedBitmap(bitmap: Bitmap, radius: Int): Bitmap {
            val finalBitmap: Bitmap
            if (bitmap.width != radius || bitmap.height != radius)
                finalBitmap = Bitmap.createScaledBitmap(bitmap, radius, radius, false)
            else
                finalBitmap = bitmap
            val output = Bitmap.createBitmap(finalBitmap.width,
                    finalBitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)

            val paint = Paint()
            val rect = Rect(0, 0, finalBitmap.width, finalBitmap.height)

            paint.isAntiAlias = true
            paint.isFilterBitmap = true
            paint.isDither = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = Color.parseColor("#BAB399")
            canvas.drawCircle(finalBitmap.width / 2 + 0.7f, finalBitmap.height / 2 + 0.7f,
                    finalBitmap.width / 2 + 0.1f, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(finalBitmap, rect, rect, paint)


            return output
        }
    }
}
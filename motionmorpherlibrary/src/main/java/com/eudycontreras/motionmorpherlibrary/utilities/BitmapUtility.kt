package com.eudycontreras.motionmorpherlibrary.utilities

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.graphics.drawable.VectorDrawable
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.graphics.Bitmap
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.extensions.dp
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.widget.ImageView
import com.eudycontreras.motionmorpherlibrary.customViews.RoundedImageView
import com.eudycontreras.motionmorpherlibrary.drawables.RoundedBitmapDrawable


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 24 2019
 */

/**
 * Utility class used for extracting bitmaps out of drawable
 * resources.
 */
object BitmapUtility {

    /**
     * Returns the bitmap of the specified drawable resource
     * if any exists.
     */
    fun getBitmapFromDrawable(context: Context, @DrawableRes drawableId: Int): Bitmap {
        val drawable = AppCompatResources.getDrawable(context, drawableId)
        return getBitmapFromDrawable(drawable)
    }

    /**
     * Returns the bitmap of the specified drawable element
     * if any exists.
     * @throws [IllegalArgumentException] when the specified drawable
     * is not a [TransitionDrawable] or a [BitmapDrawable]
     */
    fun getBitmapFromDrawable(drawable: Drawable?): Bitmap {
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else if (drawable is VectorDrawableCompat || drawable is VectorDrawable) {
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            bitmap
        } else if (drawable is TransitionDrawable){
            val bitmap = Bitmap.createBitmap(drawable.getDrawable(0).intrinsicWidth, drawable.getDrawable(0).intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            bitmap
        } else {
            throw IllegalArgumentException("unsupported drawable propertyName")
        }
    }

    fun getRoundedCornerBitmap(bitmap: Bitmap, pixels: Int): Bitmap {
        val output = Bitmap.createBitmap(
            bitmap.width, bitmap
                .height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)

        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)
        val roundPx = pixels.toFloat()

        paint.setAntiAlias(true)
        canvas.drawARGB(0, 0, 0, 0)
        paint.setColor(color)
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    fun drawableToBitmap(drawable: Drawable, defaultWidth: Int = 0, defaultHeight: Int = 0): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        var bitmap: Bitmap

        val width = Math.max(drawable.intrinsicWidth, defaultWidth)
        val height = Math.max(drawable.intrinsicHeight, defaultHeight)

        try {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return null
        }
    }

    fun asRoundedBitmap(view: ImageView, image: BitmapDrawable): RoundedBitmapDrawable {
        return asRoundedBitmap(view, image.bitmap)
    }

    fun asRoundedBitmap(view: ImageView, image: Bitmap): RoundedBitmapDrawable {
        val fullSizeBitmap = image

        val scaledWidth = if (view.measuredWidth <= 0) 2 else view.measuredWidth
        val scaledHeight = if (view.measuredHeight <= 0) 2 else view.measuredHeight

        val scaledBitmap = if (scaledWidth == fullSizeBitmap.width && scaledHeight == fullSizeBitmap.height) {
            fullSizeBitmap
        } else {
            Bitmap.createScaledBitmap(fullSizeBitmap, scaledWidth, scaledHeight, true)
        }

        val cornerRadii = if (view is RoundedImageView) view.corners else CornerRadii(MIN_OFFSET)

        return RoundedBitmapDrawable(view.resources, scaledBitmap, scaledWidth, scaledHeight, cornerRadii)
    }
}
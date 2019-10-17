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
import android.graphics.Bitmap
import com.eudycontreras.motionmorpherlibrary.globals.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii
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
    fun getBitmapFromDrawable(context: Context, @DrawableRes drawableId: Int, defaultWidth: Int = 0, defaultHeight: Int = 0): Bitmap {
        val drawable = AppCompatResources.getDrawable(context, drawableId)
        return getBitmapFromDrawable(drawable, defaultWidth, defaultHeight)
    }

    /**
     * Returns the bitmap of the specified drawable element
     * if any exists.
     * @throws [IllegalArgumentException] when the specified drawable
     * is not a [TransitionDrawable] or a [BitmapDrawable]
     */
    fun getBitmapFromDrawable(drawable: Drawable?, defaultWidth: Int = 0, defaultHeight: Int = 0): Bitmap {
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else if (drawable is VectorDrawableCompat || drawable is VectorDrawable || drawable is RoundedBitmapDrawable) {
            val width = Math.max(drawable.intrinsicWidth, defaultWidth)
            val height = Math.max(drawable.intrinsicHeight, defaultHeight)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            bitmap
        } else if (drawable is TransitionDrawable){
            val width = Math.max(drawable.getDrawable(0).intrinsicWidth, defaultWidth)
            val height = Math.max(drawable.getDrawable(0).intrinsicHeight, defaultHeight)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            bitmap
        } else {
            throw IllegalArgumentException("unsupported drawable propertyName")
        }
    }

    fun asRoundedBitmap(view: ImageView, image: BitmapDrawable): RoundedBitmapDrawable {
        return asRoundedBitmap(view, image.bitmap)
    }

    fun asRoundedBitmap(view: ImageView, image: Bitmap): RoundedBitmapDrawable {
        val scaledWidth = if (view.measuredWidth <= 0) 2 else view.measuredWidth
        val scaledHeight = if (view.measuredHeight <= 0) 2 else view.measuredHeight

        val cornerRadii = if (view is RoundedImageView) view.corners else CornerRadii(MIN_OFFSET)

        return RoundedBitmapDrawable(image, scaledWidth, scaledHeight, cornerRadii)
    }
}
package com.eudycontreras.motionmorpherlibrary.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.graphics.drawable.VectorDrawable
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat


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
}
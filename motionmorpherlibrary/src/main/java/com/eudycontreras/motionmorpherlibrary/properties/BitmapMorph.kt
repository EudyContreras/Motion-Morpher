package com.eudycontreras.motionmorpherlibrary.properties

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since September 01 2019
 */
 
class BitmapMorph(
    var view: ImageView,
    var imageFrom: BitmapDrawable,
    var imageTo: BitmapDrawable
) {
    var onOffset: Float = 0.3f

    var scaleFrom: ImageView.ScaleType = view.scaleType
    var scaleTo: ImageView.ScaleType = view.scaleType

    var ratioXFrom: Int = 0
    var ratioYFrom: Int = 0
}
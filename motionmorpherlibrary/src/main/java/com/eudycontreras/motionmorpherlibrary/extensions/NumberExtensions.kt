package com.eudycontreras.motionmorpherlibrary.extensions

import android.content.res.ColorStateList
import android.content.res.Resources

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

val Int.dp: Float
    get() = this * Resources.getSystem().displayMetrics.density

val Float.dp: Float
    get() = this * Resources.getSystem().displayMetrics.density

fun Int.clamp(min: Int, max: Int): Int {
    return if (this < min) min else if (this > max) max else this
}

fun Float.clamp(min: Float, max: Float): Float {
    return if (this < min) min else if (this > max) max else this
}

fun Int.toStateList(): ColorStateList {
    return ColorStateList.valueOf(this)
}
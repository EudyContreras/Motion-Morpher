package com.eudycontreras.motionmorpherlibrary.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.eudycontreras.motionmorpherlibrary.Morpher
import com.eudycontreras.motionmorpherlibrary.TranslationPositions
import com.eudycontreras.motionmorpherlibrary.drawables.MorphTransitionDrawable
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii
import com.eudycontreras.motionmorpherlibrary.utilities.RevealUtilityCircular


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

fun FloatArray.apply(other: FloatArray): FloatArray {
    if (this.size == other.size) {
        for (index in 0 until other.size) {
            this[index] = other[index]
        }
    }
    return this
}

fun FloatArray.apply(cornerRadii: CornerRadii): FloatArray {
    if (this.size == cornerRadii.size) {
        for (index in 0 until cornerRadii.size) {
            this[index] = cornerRadii[index]
        }
    }
    return this
}

fun <T> List<T>.toArrayList(): ArrayList<T> {
    val arrayList = ArrayList<T>()
    arrayList.addAll(this)
    return arrayList
}

fun <T> Sequence<T>.toArrayList(): ArrayList<T> {
    val arrayList = ArrayList<T>()
    arrayList.addAll(this)
    return arrayList
}

fun Drawable.toBitmap(): Bitmap {
    return RevealUtilityCircular.getBitmapFromDrawable(this)
}

fun MorphLayout.getBackgroundType(): MorphTransitionDrawable.DrawableType {
    return when {
        this.hasVectorDrawable() -> MorphTransitionDrawable.DrawableType.VECTOR
        this.hasBitmapDrawable() -> MorphTransitionDrawable.DrawableType.BITMAP
        else -> MorphTransitionDrawable.DrawableType.OTHER
    }
}

fun Context.getStatusBarHeight(): Int {
    var result = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}

infix fun TranslationPositions.and(other: Morpher.TranslationPosition): TranslationPositions = TranslationPositions.of(other, *this.toTypedArray())

infix fun TranslationPositions.has(other: TranslationPositions) = this.containsAll(other)

infix fun TranslationPositions.has(other: Morpher.TranslationPosition) = this.contains(other)

fun TranslationPositions.get(item: Morpher.TranslationPosition): Morpher.TranslationPosition {
    return this.first { it == item}
}
package com.eudycontreras.motionmorpherlibrary.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.eudycontreras.motionmorpherlibrary.drawables.MorphTransitionDrawable
import com.eudycontreras.motionmorpherlibrary.drawables.RoundedBitmapDrawable
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii
import com.eudycontreras.motionmorpherlibrary.utilities.BitmapUtility

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


inline fun <T, K> Iterable<T>.groupByAnd(keySelector: (T) -> K, predicate: (it:T, other: T) -> Boolean): Map<K, List<T>> {
    var groups: LinkedHashMap<K, MutableList<T>> = LinkedHashMap()

    var lastElement: T? = null

    for(element in this) {
        val key = keySelector(element)
        if (groups.containsKey(key)){
            groups.getValue(key).add(element)
        } else {
            if (lastElement != null) {
                if (predicate(element, lastElement)) {
                    groups.getValue(keySelector(lastElement)).add(element)
                    continue
                }
            }
            val list = groups.getOrPut(key) { ArrayList<T>() }
            list.add(element)
            lastElement = element
        }
    }
    return groups
}

fun Drawable.toBitmap(): Bitmap {
    return BitmapUtility.getBitmapFromDrawable(this)
}

fun MorphLayout.getBackgroundType(): MorphTransitionDrawable.DrawableType {
    return when {
        this.hasVectorDrawable() -> MorphTransitionDrawable.DrawableType.VECTOR
        this.hasBitmapDrawable() -> MorphTransitionDrawable.DrawableType.BITMAP
        else -> MorphTransitionDrawable.DrawableType.OTHER
    }
}

fun BitmapDrawable.asRounded(corners: CornerRadii = CornerRadii()): RoundedBitmapDrawable {
    val scaledWidth = if (this.intrinsicWidth <= 0) 2 else this.intrinsicWidth
    val scaledHeight = if (this.intrinsicHeight <= 0) 2 else this.intrinsicHeight

    return RoundedBitmapDrawable(this.bitmap, scaledWidth, scaledHeight, corners)
}

fun Context.getStatusBarHeight(): Int {
    var result = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}
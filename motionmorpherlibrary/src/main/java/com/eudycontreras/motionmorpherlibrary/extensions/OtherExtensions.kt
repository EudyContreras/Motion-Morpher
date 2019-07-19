package com.eudycontreras.motionmorpherlibrary.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.eudycontreras.motionmorpherlibrary.activities.MorphActivity
import com.eudycontreras.motionmorpherlibrary.activities.MorphDialog
import com.eudycontreras.motionmorpherlibrary.drawables.MorphTransitionDrawable
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii
import com.eudycontreras.motionmorpherlibrary.utilities.RevealUtilityCircular




/**
 * <h1>Class description!</h1>
 *
 *
 *
 * **Note:** Unlicensed private property of the author and creator
 * unauthorized use of this class outside of the Soul Vibe project
 * may result on legal prosecution.
 *
 *
 * Created by <B>Eudy Contreras</B>
 *
 * @author  Eudy Contreras
 * @version 1.0
 * @since   2018-03-31
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


fun MorphActivity.openDialog(fragment: MorphDialog) {
    val prev = supportFragmentManager.findFragmentByTag(fragment::class.java.simpleName)

    val fragmentTransaction = supportFragmentManager.beginTransaction()

    if (supportFragmentManager.fragments.contains(fragment) || prev != null) {
        fragmentTransaction.remove(prev!!)
    }

    fragmentTransaction.addToBackStack(null)
    fragment.show(fragmentTransaction, fragment::class.java.simpleName)
}

fun Context.getStatusBarHeight(): Int {
    var result = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}
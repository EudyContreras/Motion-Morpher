package com.eudycontreras.motionmorpherlibrary.properties

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.eudycontreras.motionmorpherlibrary.*
import com.eudycontreras.motionmorpherlibrary.customViews.RoundedImageView
import com.eudycontreras.motionmorpherlibrary.drawables.RoundedBitmapDrawable
import com.eudycontreras.motionmorpherlibrary.enumerations.FadeType
import com.eudycontreras.motionmorpherlibrary.extensions.dp
import com.eudycontreras.motionmorpherlibrary.utilities.BitmapUtility
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KTypeProjection
import kotlin.reflect.jvm.internal.impl.types.KotlinType
import kotlin.reflect.typeOf


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since September 01 2019
 */

class BitmapMorph(
    val view: ImageView,
    @DrawableRes imageFrom: RoundedBitmapDrawable,
    @DrawableRes imageTo: BitmapDrawable,
    var type: FadeType = FadeType.DISSOLVE
) {
    var onOffset: Float = 0.3f

    @DrawableRes val imageFrom: RoundedBitmapDrawable = imageFrom
    @DrawableRes val imageTo: RoundedBitmapDrawable = BitmapUtility.asRoundedBitmap(view, imageTo)

    constructor(
        view: ImageView,
        @DrawableRes resFrom: Int,
        @DrawableRes resTo: Int
    ): this(
        view,
        BitmapUtility.asRoundedBitmap(view, ContextCompat.getDrawable(view.context, resFrom) as BitmapDrawable),
        ContextCompat.getDrawable(view.context, resTo) as BitmapDrawable)

    constructor(
        view: ImageView,
        @DrawableRes resTo: Int
    ): this(
        view,
        view.drawable as RoundedBitmapDrawable,
        ContextCompat.getDrawable(view.context, resTo) as BitmapDrawable)

    fun build() {
        view.setImageDrawable(imageTo)

        view.overlay.add(imageFrom)
    }

    fun morph(fraction: Float) {
       when (type) {
           FadeType.DISSOLVE -> dissolve(fraction)
           FadeType.CROSSFADE -> crossFade(fraction)
           FadeType.FADETHROUGH -> fadeThrough(fraction)
       }
    }

    private fun fadeThrough(fraction: Float) {

    }

    private fun crossFade(fraction: Float) {

    }

    private fun dissolve(fraction: Float) {
       /* if (imageFrom.bitmap.sameAs(imageTo.bitmap)) {
            return
        }
*/
        imageFrom.alpha = lerp(MAX_COLOR, MIN_COLOR, fraction)

        //view.overlay.remove(imageFrom)
    }
}
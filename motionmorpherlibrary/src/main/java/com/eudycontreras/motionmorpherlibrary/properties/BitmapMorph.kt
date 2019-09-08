package com.eudycontreras.motionmorpherlibrary.properties

import android.animation.TimeInterpolator
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.eudycontreras.motionmorpherlibrary.*
import com.eudycontreras.motionmorpherlibrary.drawables.RoundedBitmapDrawable
import com.eudycontreras.motionmorpherlibrary.enumerations.FadeType
import com.eudycontreras.motionmorpherlibrary.utilities.BitmapUtility


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since September 01 2019
 */

class BitmapMorph(
    val view: ImageView,
    @DrawableRes imageFrom: RoundedBitmapDrawable,
    @DrawableRes imageTo: BitmapDrawable,
    var fadeType: FadeType = FadeType.DISSOLVE
) {
    @DrawableRes val imageFrom: RoundedBitmapDrawable = imageFrom
    @DrawableRes val imageTo: RoundedBitmapDrawable = BitmapUtility.asRoundedBitmap(view, imageTo)

    var onOffset: Float = MID_OFFSET

    var incomingInterpolator: TimeInterpolator = INCOMING

    var outgoingInterpolator: TimeInterpolator = OUTGOING

    constructor(
        view: ImageView,
        @DrawableRes resFrom: Int,
        @DrawableRes resTo: Int,
        fadeType: FadeType = FadeType.DISSOLVE
    ): this(
        view,
        BitmapUtility.asRoundedBitmap(view, ContextCompat.getDrawable(view.context, resFrom) as BitmapDrawable),
        ContextCompat.getDrawable(view.context, resTo) as BitmapDrawable,
        fadeType
    )

    constructor(
        view: ImageView,
        @DrawableRes resTo: Int,
        fadeType: FadeType = FadeType.DISSOLVE
    ): this(
        view,
        view.drawable as RoundedBitmapDrawable,
        ContextCompat.getDrawable(view.context, resTo) as BitmapDrawable,
        fadeType
    )

    fun build() {
        view.setImageDrawable(imageTo)

        view.overlay.add(imageFrom)

        if (fadeType == FadeType.CROSSFADE) {
            imageTo.alpha = MIN_COLOR
        }
    }

    fun morph(fraction: Float) {
        when (fadeType) {
            FadeType.DISSOLVE -> dissolve(fraction)
            FadeType.CROSSFADE -> crossFade(fraction)
            FadeType.FADETHROUGH -> fadeThrough(fraction)
        }
    }

    private fun fadeThrough(fraction: Float) {
        val outgoingFraction = mapRange(fraction, MIN_OFFSET, onOffset, MIN_OFFSET, MAX_OFFSET)
        val incomingFraction = mapRange(fraction, onOffset, MAX_OFFSET, MIN_OFFSET, MAX_OFFSET)

        imageFrom.alpha = lerp(MAX_COLOR, MIN_COLOR, outgoingFraction)

        imageTo.alpha = lerp(MIN_COLOR, MAX_COLOR, incomingFraction)
    }

    private fun crossFade(fraction: Float) {
        imageFrom.alpha = lerp(MAX_COLOR, MIN_COLOR, fraction)

        imageTo.alpha = lerp(MIN_COLOR, MAX_COLOR, fraction)
    }

    private fun dissolve(fraction: Float) {
        imageFrom.alpha = lerp(MAX_COLOR, MIN_COLOR, fraction)
    }

    fun onEnd() {
        view.overlay.remove(imageFrom)
    }
}
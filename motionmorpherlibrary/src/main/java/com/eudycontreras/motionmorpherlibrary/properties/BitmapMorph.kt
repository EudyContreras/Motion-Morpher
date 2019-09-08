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

class BitmapMorph {
    lateinit var view: ImageView

    var onOffset: Float = MID_OFFSET

    var fadeType: FadeType = FadeType.DISSOLVE

    var incomingInterpolator: TimeInterpolator = INCOMING
    var outgoingInterpolator: TimeInterpolator = OUTGOING

    @DrawableRes lateinit var imageFrom: RoundedBitmapDrawable
    @DrawableRes lateinit var imageTo: RoundedBitmapDrawable

    private constructor()

    constructor(
        view: ImageView,
        @DrawableRes resFrom: Int,
        @DrawableRes resTo: Int,
        fadeType: FadeType = FadeType.DISSOLVE
    ) {
        this.view = view
        this.view.setImageResource(resFrom)
        this.imageFrom = view.drawable as RoundedBitmapDrawable
        this.imageTo = BitmapUtility.asRoundedBitmap(view, ContextCompat.getDrawable(view.context, resTo) as BitmapDrawable)
        this.fadeType = fadeType
    }

    constructor(
        view: ImageView,
        @DrawableRes resTo: Int,
        fadeType: FadeType = FadeType.DISSOLVE
    ){
        this.view = view
        this.imageFrom = view.drawable as RoundedBitmapDrawable
        this.imageTo = BitmapUtility.asRoundedBitmap(view, ContextCompat.getDrawable(view.context, resTo) as BitmapDrawable)
        this.fadeType = fadeType
    }

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

        val alphaFrom = lerp(MAX_COLOR, MIN_COLOR, outgoingFraction)
        val alphaTo = lerp(MIN_COLOR, MAX_COLOR, incomingFraction)

        imageFrom.alpha = alphaFrom
        imageTo.alpha = alphaTo
    }

    private fun crossFade(fraction: Float) {
        val alphaFrom = lerp(MAX_COLOR, MIN_COLOR, fraction)
        val alphaTo = lerp(MIN_COLOR, MAX_COLOR, fraction)

        imageFrom.alpha = alphaFrom
        imageTo.alpha = alphaTo
    }

    private fun dissolve(fraction: Float) {
        val alphaTo = lerp(MAX_COLOR, MIN_COLOR, fraction)

        imageFrom.alpha = alphaTo
    }

    fun onEnd() {
        view.overlay.remove(imageFrom)
    }
}
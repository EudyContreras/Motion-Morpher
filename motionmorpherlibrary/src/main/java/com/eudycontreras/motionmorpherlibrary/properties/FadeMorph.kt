package com.eudycontreras.motionmorpherlibrary.properties

import android.animation.TimeInterpolator
import android.graphics.drawable.BitmapDrawable
import android.view.View
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

class FadeMorph(
    val view: View,
    val viewFrom: View,
    val viewTo: View,
    var fadeType: FadeType = FadeType.DISSOLVE
) {
    var onOffset: Float = MID_OFFSET

    var incomingInterpolator: TimeInterpolator = INCOMING
    var outgoingInterpolator: TimeInterpolator = OUTGOING

    fun build() {

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
    }

    private fun crossFade(fraction: Float) {
        val alphaFrom = lerp(MAX_COLOR, MIN_COLOR, fraction)

        val alphaTo = lerp(MIN_COLOR, MAX_COLOR, fraction)
    }

    private fun dissolve(fraction: Float) {
        val alphaTo = lerp(MAX_COLOR, MIN_COLOR, fraction)
    }

    fun onEnd() {

    }
}
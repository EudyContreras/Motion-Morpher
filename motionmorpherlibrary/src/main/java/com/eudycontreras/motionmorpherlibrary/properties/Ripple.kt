package com.eudycontreras.motionmorpherlibrary.properties

import android.animation.TimeInterpolator
import android.graphics.Canvas
import com.eudycontreras.motionmorpherlibrary.MIN_DURATION
import com.google.android.material.circularreveal.CircularRevealHelper


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 28 2019
 */
 
class Ripple {

    var colorStart: Color = MutableColor.WHITE
    var colorEnd: Color = MutableColor.WHITE

    var centerX: Float = Float.MIN_VALUE
    var centerY: Float = Float.MIN_VALUE

    var alphaStart: Float = 0.5f
    var alphaEnd: Float = 0f

    var duration: Long = MIN_DURATION

    var interpolator: TimeInterpolator? = null

    fun animate(fraction: Float) {
         CircularRevealHelper
    }

    fun draw(canvas: Canvas) {

    }
}
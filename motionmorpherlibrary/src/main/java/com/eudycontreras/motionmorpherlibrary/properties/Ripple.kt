package com.eudycontreras.motionmorpherlibrary.properties

import android.animation.TimeInterpolator
import android.graphics.Canvas
import com.eudycontreras.motionmorpherlibrary.MIN_DURATION


/**
 * Class which holds information about a ripple effect.
 * The information held by this class is the starting color. See: [colorStart]
 * the ending color. See: [colorEnd], the center point X of the ripple. See: [centerX],
 * the center point Y of the ripple. See: [centerY], the starting opacity. See: [alphaStart]
 * the end opacity. See: [alphaEnd], the total duration the ripple should last.
 * See: [duration] and the time interpolator to use. See[interpolator]
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

    }

    fun draw(canvas: Canvas) {

    }
}
package com.eudycontreras.motionmorpherlibrary.properties

import android.animation.TimeInterpolator
import android.view.View
import com.eudycontreras.motionmorpherlibrary.Action
import com.eudycontreras.motionmorpherlibrary.MIN_DURATION
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import kotlin.math.hypot



/**
 * Class that holds information about a circular conceal animation.
 * This class specifies the [View] which is to be concealed along with
 * the conceal [duration], the [interpolator], the [radiusStart], the [radiusEnd],
 * the [centerX] and [centerY] of the conceal and the actions to perform upon
 * starting and ending the conceal animation.
 *
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 07 2019
 */

class Conceal(
    val view: View,
    var duration: Long? = MIN_DURATION,
    var interpolator: TimeInterpolator? = null,
    var onEnd: Action = null,
    var onStart: Action = null
){
    var radiusStart: Float = MIN_OFFSET
        private set
    var radiusEnd: Float = MIN_OFFSET
        private set

    var centerX: Float = Float.MIN_VALUE

    var centerY: Float = Float.MIN_VALUE

    constructor(toView: View, fromView: View, interpolator: TimeInterpolator? = null): this(fromView){

        val location = IntArray(2)

        toView.getLocationOnScreen(location)

        centerX = location[0] + toView.width / 2f
        centerY = location[1] + toView.height / 2f

        radiusStart = hypot(fromView.width.toDouble(), fromView.height.toDouble()).toFloat()
        radiusEnd = hypot(toView.width.toDouble(), toView.height.toDouble()).toFloat() / 2f

        this.interpolator = interpolator
    }

    constructor(fromView: View, offsetX: Float, offsetY: Float, radius: Float, interpolator: TimeInterpolator? = null): this(fromView){
        this.centerX = fromView.width * offsetX
        this.centerY = fromView.height * offsetY
        this.radiusEnd = radius
        this.radiusStart = hypot(fromView.width.toDouble(), fromView.height.toDouble()).toFloat()
        this.interpolator = interpolator
    }

    constructor(fromView: View, coordinates: Coordinates, radius: Float, interpolator: TimeInterpolator? = null): this(fromView){
        this.centerX = coordinates.x
        this.centerY = coordinates.y
        this.radiusEnd = radius
        this.radiusStart = hypot(fromView.width.toDouble(), fromView.height.toDouble()).toFloat()
        this.interpolator = interpolator
    }

    constructor(fromView: View, radius: Float, interpolator: TimeInterpolator? = null): this(fromView, Float.MIN_VALUE, Float.MIN_VALUE, radius, interpolator)
}
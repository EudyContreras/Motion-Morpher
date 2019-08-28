package com.eudycontreras.motionmorpherlibrary.properties

import android.animation.TimeInterpolator
import android.view.View
import com.eudycontreras.motionmorpherlibrary.Action
import com.eudycontreras.motionmorpherlibrary.MIN_DURATION
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import kotlin.math.hypot


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 07 2019
 */

class Reveal(
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

    constructor(fromView: View, toView: View, interpolator: TimeInterpolator? = null): this(toView){

        val location = IntArray(2)

        fromView.getLocationOnScreen(location)

        centerX = location[0] + fromView.width / 2f
        centerY = location[1] + fromView.height / 2f

        radiusStart = hypot(fromView.width.toDouble(), fromView.height.toDouble()).toFloat() / 2f
        radiusEnd = hypot(toView.width.toDouble(), toView.height.toDouble()).toFloat()

        this.interpolator = interpolator
    }

    constructor(offsetX: Float, offsetY: Float, radius: Float, toView: View, interpolator: TimeInterpolator? = null): this(toView){
        this.centerX = view.width * offsetX
        this.centerY = view.height * offsetY
        this.radiusStart = radius
        this.radiusEnd = hypot(toView.width.toDouble(), toView.height.toDouble()).toFloat()
        this.interpolator = interpolator
    }

    constructor(coordinates: Coordinates, radius: Float, toView: View, interpolator: TimeInterpolator? = null): this(toView){
        this.centerX = coordinates.x
        this.centerY = coordinates.y
        this.radiusStart = radius
        this.radiusEnd = hypot(toView.width.toDouble(), toView.height.toDouble()).toFloat()
        this.interpolator = interpolator
    }

    constructor(radius: Float, toView: View, interpolator: TimeInterpolator? = null): this(Float.MIN_VALUE, Float.MIN_VALUE, radius, toView, interpolator)
}
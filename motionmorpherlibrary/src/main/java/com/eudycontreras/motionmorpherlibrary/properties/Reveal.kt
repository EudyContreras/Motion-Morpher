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
    var centerX: Float = MIN_OFFSET
        private set
    var centerY: Float = MIN_OFFSET
        private set

    constructor(fromView: View, toView: View): this(toView){

        val location = IntArray(2)

        fromView.getLocationOnScreen(location)

        centerX = location[0] + fromView.width / 2f
        centerY = location[1] + fromView.height / 2f

        radiusStart = hypot(fromView.width.toDouble(), fromView.height.toDouble()).toFloat() / 2f
        radiusEnd = hypot(toView.width.toDouble(), toView.height.toDouble()).toFloat()
    }

    constructor(centerX: Float, centerY: Float, radius: Float, toView: View): this(toView){
        this.centerX = centerX
        this.centerY = centerY
        this.radiusStart = radius
        this.radiusEnd = hypot(toView.width.toDouble(), toView.height.toDouble()).toFloat()
    }
}
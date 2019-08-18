package com.eudycontreras.motionmorpherlibrary.properties

import android.animation.TimeInterpolator


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 16 2019
 */


data class StaggerData(
    var startPoint: FloatPoint,
    var endPoint: FloatPoint,
    var interpolator: TimeInterpolator? = null
) {
    var epicenter: FloatPoint? = null
}
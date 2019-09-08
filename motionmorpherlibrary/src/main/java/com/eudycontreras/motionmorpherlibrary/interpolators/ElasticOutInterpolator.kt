package com.eudycontreras.motionmorpherlibrary.interpolators

import android.animation.TimeInterpolator
import java.lang.StrictMath.pow
import kotlin.math.sin

class ElasticOutInterpolator : TimeInterpolator {

    val factor: Float = 0.3f

    override fun getInterpolation(fraction: Float): Float {
        return (pow(2.0, (-10 * fraction).toDouble()) * sin((fraction - factor / 4) * (2 * Math.PI) / factor) + 1).toFloat()
    }
}
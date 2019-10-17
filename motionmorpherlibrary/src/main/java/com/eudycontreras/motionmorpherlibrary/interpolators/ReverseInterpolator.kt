package com.eudycontreras.motionmorpherlibrary.interpolators

import android.animation.TimeInterpolator
import com.eudycontreras.motionmorpherlibrary.MAX_OFFSET

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 08 2019
 */

class ReverseInterpolator : TimeInterpolator {

    override fun getInterpolation(fraction: Float): Float {
        return MAX_OFFSET - fraction
    }
}
package com.eudycontreras.motionmorpherlibrary.interpolators

import android.animation.TimeInterpolator
import com.eudycontreras.motionmorpherlibrary.MAX_OFFSET

class ReverseInterpolator : TimeInterpolator {

    override fun getInterpolation(fraction: Float): Float {
        return MAX_OFFSET - fraction
    }
}
package com.eudycontreras.motionmorpherlibrary.properties

import android.animation.TimeInterpolator

class IntValues(
        type: String,
        fromValue: Int,
        toValue: Int,
        interpolator: TimeInterpolator? = null
    ): ValueMap<Int>(type, fromValue, toValue, 0, interpolator)

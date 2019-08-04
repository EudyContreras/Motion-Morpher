package com.eudycontreras.motionmorpherlibrary.properties

import android.animation.TimeInterpolator

class FloatValues(
    type: String,
    fromValue: Float = 0f,
    toValue: Float = 1f,
    interpolator: TimeInterpolator? = null
): ValueMap<Float>(type, fromValue, toValue, 0f, interpolator) {

    override val canInterpolate: Boolean
        get() = fromValue != toValue

}
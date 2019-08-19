package com.eudycontreras.motionmorpherlibrary.properties

import android.animation.TimeInterpolator
import kotlin.math.abs

open class FloatValueHolder (
        type: String,
        interpolator: TimeInterpolator? = null,
        vararg values: Float
    ): ValueHolder<Float>(type, interpolator, values.toTypedArray()) {

    override var values: Array<Float> = values.toTypedArray()
        set(value) {
            field = value
            canInterpolate = field.isNotEmpty() && field.distinct().size > 1
            relativeSum = field.sum()
            absoluteSum = field.map { abs(it) }.sum()
        }

    var relativeSum: Float = values.sum()
        protected set

    var absoluteSum: Float = values.sum()
        protected set
}
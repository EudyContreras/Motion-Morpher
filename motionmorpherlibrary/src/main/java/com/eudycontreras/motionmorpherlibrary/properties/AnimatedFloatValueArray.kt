package com.eudycontreras.motionmorpherlibrary.properties

import kotlin.math.abs

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

class AnimatedFloatValueArray (
    type: String,
    vararg values: Float
): AnimatedValueArray<Float>(type, values.toTypedArray()) {

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
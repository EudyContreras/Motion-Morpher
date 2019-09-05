package com.eudycontreras.motionmorpherlibrary.properties

import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
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
            startValue = if (field.size > 0) field[0] else MIN_OFFSET
            endValue = if (field.size > 0) field[field.size - 1] else MIN_OFFSET
        }

    var relativeSum: Float = MIN_OFFSET
        protected set

    var absoluteSum: Float = MIN_OFFSET
        protected set

    var startValue: Float = MIN_OFFSET
        protected set

    var endValue: Float = MIN_OFFSET
        protected set
}
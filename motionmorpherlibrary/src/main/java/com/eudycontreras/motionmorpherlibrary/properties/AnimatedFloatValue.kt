package com.eudycontreras.motionmorpherlibrary.properties

import android.animation.TimeInterpolator
import com.eudycontreras.motionmorpherlibrary.MAX_OFFSET
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.lerp
import com.google.android.material.math.MathUtils
import kotlin.math.abs

/**
 * Class which represent animateable float values.
 * This class holds information about the start and end
 * value of a float property.
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */
class AnimatedFloatValue(
    override val propertyName: String,
    fromValue: Float = MIN_OFFSET,
    toValue: Float = MIN_OFFSET
) : AnimatedValue<Float>() {

    constructor(
        propertyName: String,
        fromValue: Float,
        toValue: Float,
        startOffset: Float,
        endOffset: Float
    ):this(propertyName, fromValue, toValue) {
        this.interpolateOffsetStart = startOffset
        this.interpolateOffsetEnd = endOffset
    }

    var add: Float = MIN_OFFSET
    var multiply: Float = MAX_OFFSET

    override var interpolator: TimeInterpolator? = null

    override var fromValue: Float = fromValue
        set(value) {
            field = value
            difference = abs(fromValue - toValue)
        }

    override var toValue: Float = toValue
        set(value) {
            field = value
            difference = abs(fromValue - toValue)
        }

    var difference: Float = abs(fromValue - toValue)
        private set

    var differenceRatio: Float = toValue / fromValue
        private set

    fun lerp(fraction: Float): Float = lerp(fromValue, toValue, fraction)

    fun set(value: AnimatedFloatValue) {
        super.set(value)
    }

    fun copy(other: AnimatedFloatValue) {
        this.difference = other.difference
        this.fromValue = other.fromValue
        this.toValue = other.toValue
        this.interpolator = other.interpolator
    }

    override fun toString(): String {
        return "$fromValue -> $toValue"
    }
}
package com.eudycontreras.motionmorpherlibrary.properties.AnimatedValues

import android.animation.TimeInterpolator
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.lerp
import com.eudycontreras.motionmorpherlibrary.properties.AnimatedValue
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
): AnimatedValue<Float>() {

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

    var differenceRatio: Float = if (fromValue == MIN_OFFSET) MIN_OFFSET else toValue / fromValue
        private set

    fun lerp(fraction: Float): Float = lerp(fromValue, toValue, fraction)

    fun set(value: AnimatedFloatValue) {
        super.set(value)
    }

    fun copy(other: AnimatedFloatValue) {
        this.fromValue = other.fromValue
        this.toValue = other.toValue
        this.interpolator = other.interpolator
        this.difference = other.difference
        this.differenceRatio = other.differenceRatio
    }

    override fun clone(): AnimatedValue<Float> {
       val values = AnimatedFloatValue(
           propertyName,
           fromValue,
           toValue
       ).let {
           it.durationOffsetStart = durationOffsetStart
           it.durationOffsetEnd = durationOffsetEnd
           it.interpolateOffsetStart = interpolateOffsetStart
           it.interpolateOffsetEnd = interpolateOffsetEnd
           it.interpolator = interpolator?.let { it::class.java.newInstance() }
           it.differenceRatio = differenceRatio
           it.difference = difference
           it
       }
        return values
    }

    override fun toString(): String {
        return "$fromValue -> $toValue"
    }
}
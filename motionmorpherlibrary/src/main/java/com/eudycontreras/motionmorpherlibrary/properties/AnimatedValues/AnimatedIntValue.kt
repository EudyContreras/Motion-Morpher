package com.eudycontreras.motionmorpherlibrary.properties.AnimatedValues

import android.animation.TimeInterpolator
import com.eudycontreras.motionmorpherlibrary.lerp
import com.eudycontreras.motionmorpherlibrary.properties.AnimatedValue
import kotlin.math.abs

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

class AnimatedIntValue(
    override val propertyName: String,
    fromValue: Int,
    toValue: Int
) : AnimatedValue<Int>() {

    constructor(
        propertyName: String,
        fromValue: Int,
        toValue: Int,
        startOffset: Float,
        endOffset: Float
    ):this(propertyName, fromValue, toValue) {
        this.interpolateOffsetStart = startOffset
        this.interpolateOffsetEnd = endOffset
    }

    var add: Int = 0
    var multiply: Int = 1

    override var interpolator: TimeInterpolator? = null

    override var fromValue: Int = fromValue
        set(value) {
            field = value
            difference = abs(fromValue - toValue)
        }

    override var toValue: Int = toValue
        set(value) {
            field = value
            difference = abs(fromValue - toValue)
        }

    var difference: Int = abs(fromValue - toValue)
        private set

    var differenceRatio: Int = if (fromValue == 0) 0 else (toValue / fromValue).toInt()
        private set

    fun lerp(fraction: Float): Int = lerp(fromValue, toValue, fraction)

    fun set(value: AnimatedIntValue) {
        super.set(value)
    }

    fun copy(other: AnimatedIntValue) {
        this.multiply = other.multiply
        this.add = other.add
        this.difference = other.difference
        this.fromValue = other.fromValue
        this.toValue = other.toValue
        this.interpolator = other.interpolator
    }

    override fun clone(): AnimatedValue<Int> {
        val values = AnimatedIntValue(
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
}

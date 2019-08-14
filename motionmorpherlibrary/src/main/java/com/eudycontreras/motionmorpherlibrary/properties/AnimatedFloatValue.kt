package com.eudycontreras.motionmorpherlibrary.properties

import android.animation.TimeInterpolator
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import kotlin.math.abs

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

    override var add: Float? = null
    override var multiply: Float? = null
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

    fun copy(other: AnimatedFloatValue) {
        this.multiply = other.multiply
        this.add = other.add
        this.difference = other.difference
        this.fromValue = other.fromValue
        this.toValue = other.toValue
        this.interpolator = other.interpolator
    }

    override fun toString(): String {
        return "$fromValue -> $toValue"
    }
}
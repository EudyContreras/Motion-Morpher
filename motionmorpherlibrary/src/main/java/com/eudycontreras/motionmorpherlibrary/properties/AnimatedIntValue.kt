package com.eudycontreras.motionmorpherlibrary.properties

import android.animation.TimeInterpolator
import kotlin.math.abs

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

    override var add: Int? = null
    override var multiply: Int? = null
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

    fun copy(other: AnimatedIntValue) {
        this.multiply = other.multiply
        this.add = other.add
        this.difference = other.difference
        this.fromValue = other.fromValue
        this.toValue = other.toValue
        this.interpolator = other.interpolator
    }
}

package com.eudycontreras.motionmorpherlibrary.properties

import android.animation.TimeInterpolator
import com.eudycontreras.motionmorpherlibrary.MAX_OFFSET
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET

abstract class AnimatedValue<T> {

    abstract var fromValue: T
    abstract var toValue: T

    abstract var add: T?
    abstract var multiply: T?

    abstract var interpolator: TimeInterpolator?

    abstract val propertyName: String

    open var interpolateOffsetStart: Float = MIN_OFFSET
    open var interpolateOffsetEnd: Float = MAX_OFFSET

    open var durationOffsetStart: Float = MIN_OFFSET
    open var durationOffsetEnd: Float = MAX_OFFSET

    open val canInterpolate: Boolean
        get() = fromValue != toValue

    open fun set(value: T) {
        this.fromValue = value
        this.toValue = value
    }

    open fun flip() {
        val temp: T = fromValue
        fromValue = toValue
        toValue = temp
    }

    open fun copy(other: AnimatedValue<T>) {
        this.multiply = other.multiply
        this.add = other.add
        this.fromValue = other.fromValue
        this.toValue = other.toValue
        this.interpolator = other.interpolator
    }

    class AnimatedValueImpl<T>(
        override val propertyName: String,
        override var fromValue: T,
        override var toValue: T
    ) : AnimatedValue<T>() {

        override var add: T? = null
        override var multiply: T? = null
        override var interpolator: TimeInterpolator? = null
    }

    companion object {

        fun <T> instance(propertyName: String, fromValue: T, toValue: T): AnimatedValueImpl<T> {
            return AnimatedValueImpl(propertyName, fromValue, toValue)
        }

        fun ofFloat(propertyName: String, fromValue: Float, toValue: Float): AnimatedFloatValue {
            return AnimatedFloatValue(propertyName, fromValue, toValue)
        }

        fun ofInt(propertyName: String, fromValue: Int, toValue: Int): AnimatedIntValue {
            return AnimatedIntValue(propertyName, fromValue, toValue)
        }

        const val X = "x"
        const val Y = "y"
        const val COLOR = "color"
        const val ALPHA = "alpha"
        const val WIDTH = "resize"
        const val HEIGHT = "height"
        const val MARGIN = "margin"
        const val PADDING = "padding"
        const val CORNERS = "corners"
        const val SCALE_X = "scale_x"
        const val SCALE_Y = "scale_y"
        const val ROTATION = "rotate"
        const val ROTATION_X = "rotation_x"
        const val ROTATION_Y = "rotation_y"
        const val POSITION_X = "position_x"
        const val POSITION_Y = "position_Y"
        const val TRANSLATION_X = "translation_x"
        const val TRANSLATION_Y = "translation_Y"
        const val TRANSLATION_Z = "translation_z"
    }
}
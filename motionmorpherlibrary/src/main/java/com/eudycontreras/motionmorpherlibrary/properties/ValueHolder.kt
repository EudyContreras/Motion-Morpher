package com.eudycontreras.motionmorpherlibrary.properties

import android.animation.TimeInterpolator
import com.eudycontreras.motionmorpherlibrary.MAX_OFFSET
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.enumerations.Interpolation

open class ValueHolder<T>(
        type: String,
        var interpolator: TimeInterpolator? = null,
        values: Array<T>
    ) {

    open var interpolateOffsetStart: Float = MIN_OFFSET
    open var interpolateOffsetEnd: Float = MAX_OFFSET

    open var durationOffsetStart: Float = MIN_OFFSET
    open var durationOffsetEnd: Float = MAX_OFFSET

    open var values: Array<T> = values
        set(value) {
            field = value
            canInterpolate = field.isNotEmpty() && field.distinct().size > 1
        }

    var interpolation: Interpolation = Interpolation.DEDICATED

    var canInterpolate: Boolean = false
        protected set

    var increase: Float = Float.MIN_VALUE

    var multiplier: Float = Float.MIN_VALUE

    var shareInterpolate: Boolean = false

    var propertyName: String = type
        private set

    fun set(values: Array<T>) {
        this.values = values
    }

    fun reverse() {
        this.values.reverse()
    }

    fun copy(other: ValueHolder<T>) {
        this.canInterpolate = other.canInterpolate
        this.multiplier = other.multiplier
        this.increase = other.increase
        this.propertyName = other.propertyName
        this.values = other.values
        this.interpolator = other.interpolator
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ValueHolder<*>) return false

        if (!values.contentEquals(other.values)) return false
        if (increase != other.increase) return false
        if (multiplier != other.multiplier) return false
        if (propertyName != other.propertyName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = values.contentHashCode()
        result = 31 * result + increase.hashCode()
        result = 31 * result + multiplier.hashCode()
        result = 31 * result + propertyName.hashCode()
        return result
    }

    companion object {
            const val COLOR = "color_values"
            const val ALPHA = "alpha_values"
            const val WIDTH = "resize_values"
            const val HEIGHT = "height_values"
            const val MARGIN = "margin_values"
            const val PADDING = "padding_values"
            const val CORNERS = "corners_values"
            const val SCALE_X = "scale_x_values"
            const val SCALE_Y = "scale_y_values"
            const val ROTATION = "rotate_values"
            const val ROTATION_X = "rotation_x_values"
            const val ROTATION_Y = "rotation_y_values"
            const val POSITION_X = "position_x_values"
            const val POSITION_Y = "position_Y_values"
            const val TRANSLATION_X = "translation_x_values"
            const val TRANSLATION_Y = "translation_Y_values"
            const val TRANSLATION_Z = "translation_z_values"
        }
    }
package com.eudycontreras.motionmorpherlibrary.properties

import android.animation.TimeInterpolator

open class ValueMap<T>(
        type: String,
        var fromValue: T,
        var toValue: T,
        var add: T? = null,
        var interpolator: TimeInterpolator? = null
    ) {

    open val canInterpolate: Boolean
        get() = fromValue != toValue

    var by: Float = 1f

    val interpolate: Boolean
        get() = fromValue != toValue

    fun set(value: T) {
        this.fromValue = value
        this.toValue = value
    }

    var type: String = type
        private set

    fun flip() {
        val temp: T = fromValue
        fromValue = toValue
        toValue = temp
    }

    fun copy(other: ValueMap<T>) {
        this.by = other.by
        this.type = other.type
        this.fromValue = other.fromValue
        this.toValue = other.toValue
        this.interpolator = other.interpolator
    }

    fun copyTo(other: ValueMap<T>) {
        this.by = other.by
        this.type = other.type
        this.toValue = other.toValue
        this.interpolator = other.interpolator
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ValueMap<*>) return false

        if (fromValue != other.fromValue) return false
        if (toValue != other.toValue) return false
        if (add != other.add) return false
        if (by != other.by) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fromValue?.hashCode() ?: 0
        result = 31 * result + (toValue?.hashCode() ?: 0)
        result = 31 * result + (add?.hashCode() ?: 0)
        result = 31 * result + by.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    companion object {
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
package com.eudycontreras.motionmorpherlibrary.properties

import android.animation.TimeInterpolator

open class ValueMap<T>(
        type: String,
        var fromValue: T,
        var toValue: T,
        var add: T? = null,
        var interpolator: TimeInterpolator? = null
    ) {

    var by: Float = 1f

    val interpolate: Boolean
        get() = fromValue != toValue

    fun set(value: T) {
        this.fromValue = value
        this.toValue = value
    }

    var type: String = type
        private set

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
        companion object {
            const val COLOR = "color"
            const val ALPHA = "alpha"
            const val WIDTH = "resize"
            const val HEIGHT = "height"
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
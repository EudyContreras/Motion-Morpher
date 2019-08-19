package com.eudycontreras.motionmorpherlibrary.properties

import android.animation.TimeInterpolator

class AnimatedValueArray<T>(
        val propertyName: String,
        values: Array<T>
    ) {
        var interpolator: TimeInterpolator? = null

        var values: Array<T> = values
            set(value) {
                field = value
                canInterpolate = field.isNotEmpty() && field.distinct().size > 1
            }

        var canInterpolate: Boolean = false
            private set

        var increase: Float = Float.MIN_VALUE

        var multiplier: Float = Float.MIN_VALUE

        var shareInterpolate: Boolean = false

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
            this.values = other.values
            this.interpolator = other.interpolator
        }
    }
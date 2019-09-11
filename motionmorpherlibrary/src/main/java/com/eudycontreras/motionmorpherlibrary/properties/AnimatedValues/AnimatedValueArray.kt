package com.eudycontreras.motionmorpherlibrary.properties.AnimatedValues

import android.animation.TimeInterpolator
import com.eudycontreras.motionmorpherlibrary.interfaces.Cloneable

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

open class AnimatedValueArray<T>(
    val propertyName: String,
    values: Array<T>
) : Cloneable<AnimatedValueArray<T>> {
    var interpolator: TimeInterpolator? = null

    open var values: Array<T> = values
        set(value) {
            field = value
            canInterpolate = field.isNotEmpty() && field.distinct().size > 1
        }

    var canInterpolate: Boolean = false
        protected set

    var increase: Float = Float.MIN_VALUE

    var multiplier: Float = Float.MIN_VALUE

    var shareInterpolate: Boolean = false

    fun set(values: Array<T>) {
        this.values = values.copyOf()
    }

    fun reverse() {
        this.values.reverse()
    }

    fun copy(other: AnimatedValueArray<T>) {
        this.canInterpolate = other.canInterpolate
        this.multiplier = other.multiplier
        this.increase = other.increase
        this.values = other.values
        this.interpolator = other.interpolator
    }

    override fun clone(): AnimatedValueArray<T> {
        val value = AnimatedValueArray<T>(
            propertyName,
            values
        ).let {
            it.increase = increase
            it.multiplier = multiplier
            it.shareInterpolate = shareInterpolate
            it.interpolator = interpolator?.let { it::class.java.newInstance() }
            it
        }
        return value
    }
}
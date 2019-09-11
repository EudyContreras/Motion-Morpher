package com.eudycontreras.motionmorpherlibrary.properties.AnimatedValues

import android.animation.TimeInterpolator
import com.eudycontreras.motionmorpherlibrary.properties.AnimatedValue
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since September 11 2019
 */

class AnimateCornerRadii(
    override val propertyName: String,
    override var fromValue: CornerRadii,
    override var toValue: CornerRadii,
    override var interpolator: TimeInterpolator? = null
) : AnimatedValue<CornerRadii>() {

    override fun set(value: CornerRadii) {
        this.fromValue = value
        this.toValue = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun set(value: AnimatedValue<*>) {
        this.fromValue = (value.toValue as CornerRadii).clone()
        this.toValue = (value.toValue as CornerRadii).clone()
    }

    override fun flip() {
        val temp: CornerRadii = fromValue
        fromValue = toValue
        toValue = temp
    }

    override fun copy(other: AnimatedValue<CornerRadii>) {
        this.fromValue = other.fromValue
        this.toValue = other.toValue
        this.interpolator = other.interpolator
    }

    override fun clone(): AnimatedValue<CornerRadii> {
        val value = AnimateCornerRadii(
            propertyName,
            fromValue.clone(),
            toValue.clone(),
            interpolator
        ).let {
            it.durationOffsetStart = durationOffsetStart
            it.durationOffsetEnd = durationOffsetEnd
            it.interpolateOffsetStart = interpolateOffsetStart
            it.interpolateOffsetEnd = interpolateOffsetEnd
            it.interpolator = interpolator?.let { it::class.java.newInstance() }
            it
        }
        return value
    }
}
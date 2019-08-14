package com.eudycontreras.motionmorpherlibrary.interactions

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import com.eudycontreras.motionmorpherlibrary.MAX_OFFSET
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.enumerations.AnimationType
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.properties.AnimationStagger


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 12 2019
 */

abstract class Interaction {

    open var duration: Long = 0L

    open var amountMultiplier: Float = MAX_OFFSET
    open var stretchMultiplier: Float = MAX_OFFSET

    open var inInterpolator: TimeInterpolator? = null
    open var outInterpolator: TimeInterpolator? = null

    open var animationStagger: AnimationStagger? = null

    private var animator: ValueAnimator = ValueAnimator.ofFloat(MIN_OFFSET, MAX_OFFSET)

    open fun play(animationType: AnimationType, duration: Long = this.duration): Animator {
        this.animationStagger?.let {
            applyStagger(it, animationType)
        }
        this.duration = duration
        this.animator = ValueAnimator.ofFloat(MIN_OFFSET, MAX_OFFSET)
        this.animator
            .setDuration(duration)
            .addUpdateListener {
                val fraction = it.animatedValue as Float
                animate(fraction, animationType)
            }

        this.animator.start()
        return animator
    }

    abstract fun animate(fraction: Float, animationType: AnimationType)
    abstract fun buildInteraction(startView: MorphLayout, endView: MorphLayout)
    abstract fun applyStagger(animationStagger: AnimationStagger, animationType: AnimationType)
    abstract fun createInteraction(startView: MorphLayout, endView: MorphLayout)
    abstract fun applyInteraction(fraction: Float, animationType: AnimationType)
}
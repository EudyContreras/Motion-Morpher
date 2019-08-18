package com.eudycontreras.motionmorpherlibrary.interactions

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import com.eudycontreras.motionmorpherlibrary.MAX_OFFSET
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.Morpher
import com.eudycontreras.motionmorpherlibrary.enumerations.AnimationType
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.listeners.MorphAnimationListener
import com.eudycontreras.motionmorpherlibrary.properties.AnimationStagger


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 12 2019
 */

abstract class Interaction {

    open var duration: Long = 0L

    open var amountMultiplier: Float = MAX_OFFSET

    open var inInterpolator: TimeInterpolator? = null
    open var outInterpolator: TimeInterpolator? = null

    open var animationStaggerOut: AnimationStagger? = null
    open var animationStaggerIn: AnimationStagger? = null

    private var animator: ValueAnimator = ValueAnimator.ofFloat(MIN_OFFSET, MAX_OFFSET)

    protected var morphUpdater: ((Float) -> Unit)? = null

    internal var morpher: Morpher? = null

    open fun play(animationType: AnimationType, duration: Long = this.duration) {
        playWith(animationType, duration, null, null, null)
    }

    internal fun playWith(animationType: AnimationType, duration: Long, updater: ((Float) -> Unit)?, onStart: (() -> Unit)?, onEnd: (() -> Unit)?) {
        this.morphUpdater = updater
        this.duration = duration

        when (animationType) {
            AnimationType.REVEAL -> {
                this.animationStaggerOut?.let {
                    applyStagger(it, animationType)
                }
            }
            AnimationType.CONCEAL -> {
                this.animationStaggerIn?.let {
                    applyStagger(it, animationType)
                }
            }
        }
        this.animator = ValueAnimator.ofFloat(MIN_OFFSET, MAX_OFFSET)
        this.animator.duration = duration
        this.animator.addListener(MorphAnimationListener(onStart, onEnd))
        this.animator.addUpdateListener {
                val fraction = it.animatedValue as Float
                animate(fraction, animationType)
            }

        this.animator.start()
    }

    abstract fun animate(fraction: Float, animationType: AnimationType)
    abstract fun buildInteraction(startView: MorphLayout, endView: MorphLayout)
    abstract fun applyStagger(animationStagger: AnimationStagger, animationType: AnimationType)
}
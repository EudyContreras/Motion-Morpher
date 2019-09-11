package com.eudycontreras.motionmorpherlibrary.interactions

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import com.eudycontreras.motionmorpherlibrary.*
import com.eudycontreras.motionmorpherlibrary.enumerations.AnimationType
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.listeners.MorphAnimationListener
import com.eudycontreras.motionmorpherlibrary.properties.AnimatedValues.AnimatedFloatValue
import com.eudycontreras.motionmorpherlibrary.properties.AnimationStagger


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 12 2019
 */

abstract class Interaction {

    open var duration: Long = MIN_DURATION

    open var amountMultiplier: Float = MAX_OFFSET

    open var inInterpolator: TimeInterpolator? = null
    open var outInterpolator: TimeInterpolator? = null

    open var animationStaggerOut: AnimationStagger? = null
    open var animationStaggerIn: AnimationStagger? = null

    private var animator: ValueAnimator = ValueAnimator.ofFloat(MIN_OFFSET, MAX_OFFSET)

    protected var morphUpdater: ((Float) -> Unit)? = null

    internal var morpher: Morpher? = null

    internal fun playWith(
        animationType: AnimationType,
        duration: Long,
        dimPropertyInto: AnimatedFloatValue,
        dimPropertyFrom: AnimatedFloatValue,
        dimUpdater: BackgroundDimListener,
        updater: ((Float) -> Unit)?,
        onStart: (() -> Unit)?,
        onEnd: (() -> Unit)?
    ) {
        this.morphUpdater = updater
        this.duration = duration

        when (animationType) {
            AnimationType.REVEAL -> {
                applyStagger(animationStaggerOut, animationType)
            }
            AnimationType.CONCEAL -> {
                applyStagger(animationStaggerIn, animationType)
            }
        }
        this.animator = ValueAnimator.ofFloat(MIN_OFFSET, MAX_OFFSET)
        this.animator.duration = duration
        this.animator.addListener(MorphAnimationListener(onStart, onEnd))
        this.animator.addUpdateListener {
            val fraction = it.animatedValue as Float
            animate(fraction, animationType)
            when (animationType) {
                AnimationType.REVEAL -> {
                    val dimFraction = mapRange(
                        fraction,
                        dimPropertyInto.interpolateOffsetStart,
                        dimPropertyInto.interpolateOffsetEnd,
                        MIN_OFFSET,
                        MAX_OFFSET
                    )

                    dimUpdater?.invoke(dimPropertyInto.fromValue + (dimPropertyInto.toValue - dimPropertyInto.fromValue) * dimFraction)
                }
                AnimationType.CONCEAL -> {
                    val dimFraction = mapRange(
                        fraction,
                        dimPropertyFrom.interpolateOffsetStart,
                        dimPropertyFrom.interpolateOffsetEnd,
                        MIN_OFFSET,
                        MAX_OFFSET
                    )

                    dimUpdater?.invoke(dimPropertyFrom.fromValue + (dimPropertyFrom.toValue - dimPropertyFrom.fromValue) * dimFraction)
                }
            }
        }
        this.animator.start()
    }

    abstract fun animate(fraction: Float, animationType: AnimationType)
    abstract fun buildInteraction(startView: MorphLayout, endView: MorphLayout)
    abstract fun applyStagger(animationStagger: AnimationStagger?, animationType: AnimationType)
}
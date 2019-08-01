package com.eudycontreras.motionmorpherlibrary

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import androidx.annotation.ColorInt
import androidx.core.animation.addListener
import com.eudycontreras.motionmorpherlibrary.enumerations.Corner
import com.eudycontreras.motionmorpherlibrary.enumerations.Dimension
import com.eudycontreras.motionmorpherlibrary.extensions.clamp
import com.eudycontreras.motionmorpherlibrary.extensions.toStateList
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.properties.*
import com.eudycontreras.motionmorpherlibrary.utilities.ColorUtility
import kotlin.math.abs


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 30 2019
 */

class Choreographer {

    private var animatorSet: ArrayList<ChoreographyControl> = ArrayList()

    private var listener: ChoreographyListener = ChoreographyListener()

    private var built: Boolean = false

    private lateinit var headChoreography: Choreography
    private lateinit var tailChoreography: Choreography

    fun addListener(listener: ChoreographyListener): Choreographer {
        this.listener = listener
        return this
    }

    fun animate(vararg view: MorphLayout): Choreography {
        this.headChoreography = Choreography(this, *view)
        this.headChoreography.offset = 1f
        return headChoreography
    }

    internal fun animateAfter(choreography: Choreography, offset: Float, vararg view: MorphLayout): Choreography {
        tailChoreography =  Choreography(this, *view).apply {
            this.parent = choreography
            this.offset = offset
            this.delay = (choreography.duration * offset).toLong()
            choreography.child = this
        }
        return tailChoreography
    }

    internal fun thenAnimate(choreography: Choreography, vararg view: MorphLayout): Choreography {
        tailChoreography = Choreography(this, *view).apply {
            this.parent = choreography
            this.offset = 1f
            choreography.child = this
        }
        return tailChoreography
    }

    internal fun alsoAnimate(choreography: Choreography, vararg view: MorphLayout): Choreography {
        tailChoreography = Choreography(this, *view).apply {
            this.parent = choreography
            choreography.child = this
        }
        return tailChoreography
    }

    internal fun andAnimate(choreography: Choreography, vararg view: MorphLayout): Choreography {
        tailChoreography = choreography.clone(*view).apply {
            this.views = view
            this.parent = choreography
            this.child = null
            choreography.child = this
        }
        return tailChoreography
    }

    internal fun andAnimateAfter(choreography: Choreography, offset: Float, vararg view: MorphLayout): Choreography {
        tailChoreography = choreography.clone(*view).apply {
            this.views = view
            this.parent = choreography
            this.offset = offset
            this.delay = (choreography.duration * offset).toLong()
            this.child = null
            choreography.child = this
        }
        return tailChoreography
    }

    internal fun build(): Choreographer {

        var totalDuration: Long = 0
        var totalDelay: Long = 0

        var temp: Choreography? = headChoreography

        while (temp != null) {
            totalDuration += (temp.duration.toFloat() * (temp.child?.offset?: 1f)).toLong()
            totalDelay += ((temp.parent?.duration?: 0L).toFloat() * temp.offset).toLong()
            val animator = ValueAnimator.ofFloat(0f, 1f)

            val current = temp

            if (temp.reverse) {
                animator.repeatMode = ValueAnimator.REVERSE
                animator.repeatCount = 1
            }

            val updateListener: ValueAnimator.AnimatorUpdateListener = ValueAnimator.AnimatorUpdateListener {
                val fraction = (it.animatedValue as Float).clamp(0f, 1f)

                animate(current, fraction)
            }

            val control = ChoreographyControl(animator, current)

            if (current.doneAction != null) {

                val listener: (Animator) -> Unit = {
                    current.doneAction?.invoke(current)
                }

                animator.addListener(onEnd = listener)
                control.endListener = listener
            }

            animator.addUpdateListener(updateListener)
            animator.duration = current.duration
            animator.startDelay = totalDelay

            control.mTotalDuration = current.duration
            control.mRemainingDuration = totalDuration
            control.updateListener = updateListener

            animatorSet.add(control)

            applyMultipliers(temp)
            applyInterpolators(temp)

            temp = temp.child
        }

        built = true

        return this
    }

    private fun applyInterpolators(choreography: Choreography) {
        choreography.alpha.interpolator = choreography.alpha.interpolator ?: choreography.interpolator

        choreography.width.interpolator = choreography.width.interpolator ?: choreography.interpolator
        choreography.height.interpolator = choreography.height.interpolator ?: choreography.interpolator

        choreography.scaleX.interpolator = choreography.scaleX.interpolator ?: choreography.interpolator
        choreography.scaleY.interpolator = choreography.scaleY.interpolator ?: choreography.interpolator

        choreography.rotation.interpolator = choreography.rotation.interpolator ?: choreography.interpolator
        choreography.rotationX.interpolator = choreography.rotationX.interpolator ?: choreography.interpolator
        choreography.rotationY.interpolator = choreography.rotationY.interpolator ?: choreography.interpolator

        choreography.translateX.interpolator = choreography.translateX.interpolator ?: choreography.interpolator
        choreography.translateY.interpolator = choreography.translateY.interpolator ?: choreography.interpolator
        choreography.translateZ.interpolator = choreography.translateZ.interpolator ?: choreography.interpolator

        choreography.cornerRadii.interpolator = choreography.cornerRadii.interpolator ?: choreography.interpolator

        choreography.color.interpolator = choreography.color.interpolator ?: choreography.interpolator
    }

    private fun applyMultipliers(choreography: Choreography) {
        if (choreography.width.by != MAX_OFFSET) {
            choreography.width.toValue = choreography.width.fromValue * choreography.width.by
        }
        if (choreography.height.by != MAX_OFFSET) {
            choreography.height.toValue = choreography.height.fromValue * choreography.height.by
        }
        if (choreography.scaleX.by != MAX_OFFSET) {
            choreography.scaleX.toValue = choreography.scaleX.fromValue * choreography.scaleX.by
        }
        if (choreography.scaleY.by != MAX_OFFSET) {
            choreography.scaleY.toValue = choreography.scaleY.fromValue * choreography.scaleY.by
        }
    }

    /*internal fun build(): Choreographer {
        var totalDuration: Long = 0

        var temp: Choreography? = headChoreography

        while (temp != null) {
            totalDuration += (temp.duration.toFloat() * (temp.child?.offset?: 1f)).toLong()
            temp = temp.child
        }

        temp = headChoreography
        var lastDuration = 0L

        while (temp != null) {
            val offsetStart: Float = (temp.offset * lastDuration) / totalDuration.toFloat()
            val offsetEnd: Float = temp.duration.toFloat() / totalDuration.toFloat()

            temp.fractionOffsetStart = offsetStart.clamp(0f, 1f)
            temp.fractionOffsetEnd = (offsetEnd + offsetStart).clamp(0f, 1f)

            lastDuration += temp.duration
            temp = temp.child
        }

        animator.duration = totalDuration
        animator.startDelay = headChoreography.delay
        animator.addListener(
            onStart = {
                listener.startListener?.invoke()
            },
            onEnd = {
                listener.endListener?.invoke()
            }
        )

        animator.addUpdateListener {
            val fraction = (it.animatedValue as Float).clamp(0f, 1f)

            var current: Choreography? = headChoreography

            control.mElapsedDuration  = (totalDuration * fraction).toLong()
            control.mRemainingDuration  = totalDuration - control.mElapsedDuration

            while (current != null) {
                if (current.done) {
                    current = current.child
                    continue
                }

                val interpolatedFraction = mapRange(fraction, current.fractionOffsetStart, current.fractionOffsetEnd, 0f, 1f, 0f, 1f)

                animate(current, interpolatedFraction)
                current = current.child
            }

            listener.fractionListener?.invoke(control.mRemainingDuration, fraction)
        }

        control.mTotalDuration = totalDuration
        control.mRemainingDuration = totalDuration

        built = true

        return this
    }*/

    fun animate(choreography: Choreography, fraction: Float) {
        val views = choreography.views

        if (views.size == 1) {
            animate(views[0], choreography, fraction)
            return
        }

        for (view in views) {
            animate(view, choreography, fraction)
        }
    }

    private fun animate(view: MorphLayout, choreography: Choreography, fraction: Float) {

        val alphaFraction = choreography.alpha.interpolator?.getInterpolation(fraction) ?: fraction

        val scaleXFraction = choreography.scaleX.interpolator?.getInterpolation(fraction) ?: fraction
        val scaleYFraction = choreography.scaleY.interpolator?.getInterpolation(fraction) ?: fraction

        val rotateFraction = choreography.rotation.interpolator?.getInterpolation(fraction) ?: fraction
        val rotateXFraction = choreography.rotationX.interpolator?.getInterpolation(fraction) ?: fraction
        val rotateYFraction = choreography.rotationY.interpolator?.getInterpolation(fraction) ?: fraction

        val translateXFraction = choreography.translateX.interpolator?.getInterpolation(fraction) ?: fraction
        val translateYFraction = choreography.translateY.interpolator?.getInterpolation(fraction) ?: fraction
        val translateZFraction = choreography.translateZ.interpolator?.getInterpolation(fraction) ?: fraction

        view.morphAlpha = choreography.alpha.fromValue + (choreography.alpha.toValue - choreography.alpha.fromValue) * alphaFraction

        view.morphScaleX = choreography.scaleX.fromValue + (choreography.scaleX.toValue - choreography.scaleX.fromValue) * scaleXFraction
        view.morphScaleY = choreography.scaleY.fromValue + (choreography.scaleY.toValue - choreography.scaleY.fromValue) * scaleYFraction

        view.morphRotation = choreography.rotation.fromValue + (choreography.rotation.toValue - choreography.rotation.fromValue) * rotateFraction
        view.morphRotationX = choreography.rotationX.fromValue + (choreography.rotationX.toValue - choreography.rotationX.fromValue) * rotateXFraction
        view.morphRotationY = choreography.rotationY.fromValue + (choreography.rotationY.toValue - choreography.rotationY.fromValue) * rotateYFraction

        view.morphTranslationX = choreography.translateX.fromValue + (choreography.translateX.toValue - choreography.translateX.fromValue) * translateXFraction
        view.morphTranslationY = choreography.translateY.fromValue + (choreography.translateY.toValue - choreography.translateY.fromValue) * translateYFraction
        view.morphTranslationZ = choreography.translateZ.fromValue + (choreography.translateZ.toValue - choreography.translateZ.fromValue) * translateZFraction

        if (view.mutateCorners && view.hasGradientDrawable()) {
            val cornersFraction = choreography.cornerRadii.interpolator?.getInterpolation(fraction) ?: fraction

            view.updateCorners(0, choreography.cornerRadii.fromValue[0] + (choreography.cornerRadii.toValue[0] - choreography.cornerRadii.fromValue[0]) * cornersFraction)
            view.updateCorners(1, choreography.cornerRadii.fromValue[1] + (choreography.cornerRadii.toValue[1] - choreography.cornerRadii.fromValue[1]) * cornersFraction)
            view.updateCorners(2, choreography.cornerRadii.fromValue[2] + (choreography.cornerRadii.toValue[2] - choreography.cornerRadii.fromValue[2]) * cornersFraction)
            view.updateCorners(3, choreography.cornerRadii.fromValue[3] + (choreography.cornerRadii.toValue[3] - choreography.cornerRadii.fromValue[3]) * cornersFraction)
            view.updateCorners(4, choreography.cornerRadii.fromValue[4] + (choreography.cornerRadii.toValue[4] - choreography.cornerRadii.fromValue[4]) * cornersFraction)
            view.updateCorners(5, choreography.cornerRadii.fromValue[5] + (choreography.cornerRadii.toValue[5] - choreography.cornerRadii.fromValue[5]) * cornersFraction)
            view.updateCorners(6, choreography.cornerRadii.fromValue[6] + (choreography.cornerRadii.toValue[6] - choreography.cornerRadii.fromValue[6]) * cornersFraction)
            view.updateCorners(7, choreography.cornerRadii.fromValue[7] + (choreography.cornerRadii.toValue[7] - choreography.cornerRadii.fromValue[7]) * cornersFraction)
        }

        if (choreography.color.fromValue != choreography.color.toValue) {
            view.morphStateList = ColorUtility.interpolateColor(fraction, choreography.color.fromValue, choreography.color.toValue).toStateList()
        }

        if (choreography.width.fromValue != choreography.width.toValue || choreography.height.fromValue != choreography.height.toValue) {
            val widthFraction = choreography.width.interpolator?.getInterpolation(fraction) ?: fraction
            val heightFraction = choreography.height.interpolator?.getInterpolation(fraction) ?: fraction

            view.morphWidth = choreography.width.fromValue + (choreography.width.toValue - choreography.width.fromValue) * widthFraction
            view.morphHeight = choreography.height.fromValue + (choreography.height.toValue - choreography.height.fromValue) * heightFraction

            view.updateLayout()
        }

        if (fraction >= 1f) {
            choreography.done = true
            choreography.doneAction?.invoke(choreography)
        }
    }

    fun start(): ArrayList<ChoreographyControl> {
        animatorSet.forEach { it.start() }
        return animatorSet
    }

    fun reset(): Choreographer {
        predecessors(tailChoreography) {
            it.resetProperties()
        }
        return this
    }

    fun reset(view: MorphLayout): Choreographer {
        predecessors(tailChoreography) {
            it.views.forEach {v ->
                if (view == v) {
                    it.resetProperties()
                }
            }
        }
        return this
    }

    fun resetWithAnimation(view: MorphLayout): Choreographer {
        predecessors(tailChoreography) { chor ->
            chor.views.forEach {v ->
                if (view == v) {
                    animatorSet.first { it.choreography == chor }.reverse()
                }
            }
        }
        return this
    }

    fun predecessors(choreography: Choreography, iterator: (choreography: Choreography) -> Unit) {
        var temp: Choreography? = choreography
        while (temp != null) {
            iterator.invoke(temp)
            temp = temp.parent
        }
    }

    fun sucessors(choreography: Choreography, iterator: (choreography: Choreography) -> Unit) {
        var temp: Choreography? = choreography
        while (temp != null) {
            iterator.invoke(temp)
            temp = temp.child
        }
    }

    data class ChoreographyListener(
        var fractionListener: ((remainingTime: Long, fraction: Float) -> Unit)? = null,
        var endListener: Action = null,
        var startListener: Action = null,
        var reverseListener: Action = null
    )

    inner class ChoreographyControl(
        internal var animator: ValueAnimator,
        internal var choreography: Choreography
    ) {

        internal var mTotalDuration: Long = 0L
        internal var mRemainingDuration: Long = 0L
        internal var mElapsedDuration: Long = 0L

        internal var endListener: ((Animator) -> Unit)? = null

        internal lateinit var updateListener: ValueAnimator.AnimatorUpdateListener

        fun pause() {
            animator.pause()
        }

        fun resume() {
            animator.resume()
        }

        fun cancel() {
            animator.cancel()
        }

        fun reverse() {
            val animator = ValueAnimator.ofFloat(1f, 0f)
            endListener?.let { animator.addListener(it) }

            animator.interpolator = this.animator.interpolator
            animator.duration = this.mTotalDuration
            animator.addUpdateListener(this.updateListener)
            animator.start()

            this.animator = animator
        }

        internal fun start(): ChoreographyControl {
            animator.start()
            return this
        }

        fun getTotalDuration(): Long {
            return mTotalDuration
        }

        fun getRemainingDuration(): Long  {
            return mRemainingDuration
        }

        fun getElapsedDuration(): Long  {
            return mElapsedDuration
        }
    }

    class Choreography (
        var choreographer: Choreographer,
        internal vararg var views: MorphLayout
    ) {

        internal var done: Boolean = false
        internal var reverse: Boolean = false

        internal var delay: Long = 0L
        internal var offset: Float = 0F
        internal var interval: Long = 0L
        internal var duration: Long = 0L
        internal var childStagger: Float = 0F

        internal var fractionOffsetStart: Float = MIN_OFFSET
        internal var fractionOffsetEnd: Float = MAX_OFFSET

        internal var scaleX: FloatValues = FloatValues(ValueMap.SCALE_X, MAX_OFFSET, MAX_OFFSET)
        internal var scaleY: FloatValues = FloatValues(ValueMap.SCALE_Y, MAX_OFFSET, MAX_OFFSET)

        internal var rotation: FloatValues = FloatValues(ValueMap.ROTATION, MIN_OFFSET, MIN_OFFSET)
        internal var rotationX: FloatValues = FloatValues(ValueMap.ROTATION_X, MIN_OFFSET, MIN_OFFSET)
        internal var rotationY: FloatValues = FloatValues(ValueMap.ROTATION_Y, MIN_OFFSET, MIN_OFFSET)

        internal var positionX: FloatValues = FloatValues(ValueMap.POSITION_X, MIN_OFFSET, MIN_OFFSET)
        internal var positionY: FloatValues = FloatValues(ValueMap.POSITION_Y, MIN_OFFSET, MIN_OFFSET)

        internal var translateX: FloatValues = FloatValues(ValueMap.TRANSLATION_X, MIN_OFFSET, MIN_OFFSET)
        internal var translateY: FloatValues = FloatValues(ValueMap.TRANSLATION_Y, MIN_OFFSET, MIN_OFFSET)
        internal var translateZ: FloatValues = FloatValues(ValueMap.TRANSLATION_Z, MIN_OFFSET, MIN_OFFSET)

        internal var width: FloatValues = FloatValues(ValueMap.WIDTH, MIN_OFFSET, MIN_OFFSET)
        internal var height: FloatValues = FloatValues(ValueMap.HEIGHT, MIN_OFFSET, MIN_OFFSET)

        internal var alpha: FloatValues = FloatValues(ValueMap.ALPHA, MAX_OFFSET, MAX_OFFSET)

        internal var color: IntValues = IntValues(ValueMap.COLOR, 0x000000, 0x000000)

        internal var cornerRadii: ValueMap<CornerRadii> = ValueMap(ValueMap.CORNERS, CornerRadii(), CornerRadii())

        internal var doneAction: ChoreographerAction = null

        internal var interpolator: TimeInterpolator? = null

        internal var parent: Choreography? = null
        internal var child: Choreography? = null

        init {
            if (views.isNotEmpty()) {
                views[0].let {
                    this.color.set(it.morphColor)
                    this.alpha.set(it.morphAlpha)
                    this.scaleX.set(it.morphScaleX)
                    this.scaleY.set(it.morphScaleY)
                    this.rotation.set(it.morphRotation)
                    this.rotationX.set(it.morphRotationX)
                    this.rotationY.set(it.morphRotationY)
                    this.translateX.set(it.morphTranslationX)
                    this.translateY.set(it.morphTranslationY)
                    this.translateZ.set(it.morphTranslationZ)
                    this.positionX.set(it.morphX)
                    this.positionY.set(it.morphY)
                    this.width.set(it.morphWidth)
                    this.height.set(it.morphHeight)
                    this.cornerRadii.fromValue = it.morphCornerRadii.getCopy()
                    this.cornerRadii.toValue = it.morphCornerRadii.getCopy()
                }
            }
        }

        fun resetProperties() {
            if (views.isNotEmpty()) {
                views.forEach {
                    it.morphX = positionX.fromValue
                    it.morphY = positionY.fromValue
                    it.morphStateList = color.fromValue.toStateList()
                    it.morphAlpha = alpha.fromValue
                    it.morphScaleX = scaleX.fromValue
                    it.morphScaleY = scaleY.fromValue
                    it.morphRotation = rotation.fromValue
                    it.morphRotationX = rotationX.fromValue
                    it.morphRotationY = rotationY.fromValue
                    it.morphTranslationX = translateX.fromValue
                    it.morphTranslationY = translateY.fromValue
                    it.morphTranslationZ = translateZ.fromValue
                    it.morphWidth = width.fromValue
                    it.morphHeight = height.fromValue
                    if (it.mutateCorners && it.hasGradientDrawable()) {
                        it.morphCornerRadii = cornerRadii.fromValue.getCopy()
                    }
                    it.updateLayout()
                }
            }
        }

        fun positionAt(view: MorphLayout, interpolator: TimeInterpolator? = null): Choreography {

            val translateX = abs(view.windowLocationX.toFloat() - this.views[0].windowLocationX.toFloat())
            val translateY = abs(view.windowLocationY.toFloat() - this.views[0].windowLocationY.toFloat())

            this.translateX.toValue = if (view.windowLocationX > this.views[0].windowLocationX) translateX else -translateX
            this.translateY.toValue = if (view.windowLocationY > this.views[0].windowLocationY) translateY else -translateY

            this.translateX.interpolator = interpolator
            this.translateY.interpolator = interpolator

            return this
        }

        fun positionAt(bounds: Bounds, interpolator: TimeInterpolator? = null): Choreography {

            val translateX = abs(bounds.x.toFloat() - this.views[0].windowLocationX.toFloat())
            val translateY = abs(bounds.y.toFloat() - this.views[0].windowLocationY.toFloat())

            this.translateX.toValue = if (bounds.x > this.views[0].windowLocationX) translateX else -translateX
            this.translateY.toValue = if (bounds.y > this.views[0].windowLocationY) translateY else -translateY

            this.translateX.interpolator = interpolator
            this.translateY.interpolator = interpolator

            return this
        }

        fun positionX(positionX: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.translateX.toValue = positionX
            this.translateX.interpolator = interpolator
            return this
        }

        fun positionX(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.positionX.fromValue = fromValue
            this.positionX.toValue = toValue
            this.positionX.interpolator = interpolator
            return this
        }

        fun positionX(values: FloatValues): Choreography {
            this.positionX.copy(values)
            return this
        }

        fun positionY(positionY: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.positionY.toValue = positionY
            this.positionY.interpolator = interpolator
            return this
        }

        fun positionY(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.positionY.fromValue = fromValue
            this.positionY.toValue = toValue
            this.positionY.interpolator = interpolator
            return this
        }

        fun positionY(values: FloatValues): Choreography {
            this.positionY.copy(values)
            return this
        }

        fun positionZ(values: FloatValues): Choreography {
            this.translateZ.copy(values)
            return this
        }

        fun translateX(translationX: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.translateX.toValue = translationX
            this.translateX.interpolator = interpolator
            return this
        }

        fun translateX(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.translateX.fromValue = fromValue
            this.translateX.toValue = toValue
            this.translateX.interpolator = interpolator
            return this
        }

        fun translateX(values: FloatValues): Choreography {
            this.translateX.copy(values)
            return this
        }

        fun translateY(translationY: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.translateY.toValue = translationY
            this.translateY.interpolator = interpolator
            return this
        }

        fun translateY(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.translateY.fromValue = fromValue
            this.translateY.toValue = toValue
            this.translateY.interpolator = interpolator
            return this
        }

        fun translateY(values: FloatValues): Choreography {
            this.translateY.copy(values)
            return this
        }

        fun translateZ(translationZ: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.translateZ.toValue = translationZ
            this.translateZ.interpolator = interpolator
            return this
        }

        fun translateZ(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.translateZ.fromValue = fromValue
            this.translateZ.toValue = toValue
            this.translateZ.interpolator = interpolator
            return this
        }

        fun translateZ(values: FloatValues): Choreography {
            this.translateZ.copy(values)
            return this
        }

        fun opacity(opacity: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.alpha.toValue = opacity
            this.alpha.interpolator = interpolator
            return this
        }

        fun opacity(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.alpha.fromValue = fromValue
            this.alpha.toValue = toValue
            this.alpha.interpolator = interpolator
            return this
        }

        fun opacity(values: FloatValues): Choreography {
            this.alpha.copy(values)
            return this
        }

        fun opacity(opacity: Int, interpolator: TimeInterpolator? = null): Choreography {
            return this.opacity(opacity.clamp(0, 100) / 100f, interpolator)
        }

        fun opacity(fromValue: Int, toValue: Int, interpolator: TimeInterpolator? = null): Choreography {
            return this.opacity(fromValue.clamp(0, 100) / 100f, toValue.clamp(0, 100) / 100f, interpolator)
        }

        fun cornerRadius(corner: Corner = Corner.ALL, radius: Float, interpolator: TimeInterpolator? = null): Choreography {
            cornerRadii.interpolator = interpolator
            when (corner) {
                Corner.TOP_LEFT ->  {
                    cornerRadii.toValue[0] = radius
                    cornerRadii.toValue[1] = radius
                }
                Corner.TOP_RIGHT -> {
                    cornerRadii.toValue[2] = radius
                    cornerRadii.toValue[3] = radius
                }
                Corner.BOTTOM_RIGHT -> {
                    cornerRadii.toValue[4] = radius
                    cornerRadii.toValue[5] = radius
                }
                Corner.BOTTOM_LEFT -> {
                    cornerRadii.toValue[6] = radius
                    cornerRadii.toValue[7] = radius
                }
                Corner.ALL -> {
                    cornerRadii.toValue[0] = radius
                    cornerRadii.toValue[1] = radius
                    cornerRadii.toValue[2] = radius
                    cornerRadii.toValue[3] = radius
                    cornerRadii.toValue[4] = radius
                    cornerRadii.toValue[5] = radius
                    cornerRadii.toValue[6] = radius
                    cornerRadii.toValue[7] = radius
                }
            }
            return this
        }

        fun cornerRadius(corner: Corner = Corner.ALL, fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            cornerRadii.interpolator = interpolator
            when (corner) {
                Corner.TOP_LEFT ->  {
                    cornerRadii.fromValue[0] = fromValue
                    cornerRadii.fromValue[1] = fromValue

                    cornerRadii.toValue[0] = toValue
                    cornerRadii.toValue[1] = toValue
                }
                Corner.TOP_RIGHT -> {
                    cornerRadii.fromValue[2] = fromValue
                    cornerRadii.fromValue[3] = fromValue

                    cornerRadii.toValue[2] = toValue
                    cornerRadii.toValue[3] = toValue
                }
                Corner.BOTTOM_RIGHT -> {
                    cornerRadii.fromValue[4] = fromValue
                    cornerRadii.fromValue[5] = fromValue

                    cornerRadii.toValue[4] = toValue
                    cornerRadii.toValue[5] = toValue
                }
                Corner.BOTTOM_LEFT -> {
                    cornerRadii.fromValue[6] = fromValue
                    cornerRadii.fromValue[7] = fromValue

                    cornerRadii.toValue[6] = toValue
                    cornerRadii.toValue[7] = toValue
                }
                Corner.ALL -> {
                    cornerRadii.fromValue[0] = fromValue
                    cornerRadii.fromValue[1] = fromValue
                    cornerRadii.fromValue[2] = fromValue
                    cornerRadii.fromValue[3] = fromValue
                    cornerRadii.fromValue[4] = fromValue
                    cornerRadii.fromValue[5] = fromValue
                    cornerRadii.fromValue[6] = fromValue
                    cornerRadii.fromValue[7] = fromValue

                    cornerRadii.toValue[0] = toValue
                    cornerRadii.toValue[1] = toValue
                    cornerRadii.toValue[2] = toValue
                    cornerRadii.toValue[3] = toValue
                    cornerRadii.toValue[4] = toValue
                    cornerRadii.toValue[5] = toValue
                    cornerRadii.toValue[6] = toValue
                    cornerRadii.toValue[7] = toValue
                }
            }
            return this
        }

        fun cornerRadius(valueMap: ValueMap<CornerRadii>): Choreography {
            cornerRadii.copy(valueMap)
            return this
        }

        fun addRotation(): Choreography {
            return this
        }

        fun rotateBy(): Choreography {
            return this
        }

        fun rotate(rotation: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotation.toValue = rotation
            this.rotation.interpolator = interpolator
            return this
        }

        fun rotate(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotation.fromValue = fromValue
            this.rotation.toValue = toValue
            this.rotation.interpolator = interpolator
            return this
        }

        fun rotate(values: FloatValues): Choreography {
            this.rotation.copy(values)
            return this
        }

        fun addRotationX(): Choreography {
            return this
        }

        fun rotateXBy(): Choreography {
            return this
        }

        fun rotateX(rotationX: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationX.toValue = rotationX
            this.rotationX.interpolator = interpolator
            return this
        }

        fun rotateX(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationX.fromValue = fromValue
            this.rotationX.toValue = toValue
            this.rotationX.interpolator = interpolator
            return this
        }

        fun rotateX(values: FloatValues): Choreography {
            this.rotationX.copy(values)
            return this
        }

        fun addRotationY(): Choreography {
            return this
        }

        fun rotateYBy(): Choreography {
            return this
        }

        fun rotateY(rotationY: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationY.toValue = rotationY
            this.rotationY.interpolator = interpolator
            return this
        }

        fun rotateY(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationY.fromValue = fromValue
            this.rotationY.toValue = toValue
            this.rotationY.interpolator = interpolator
            return this
        }

        fun rotateY(values: FloatValues): Choreography {
            this.rotationY.copy(values)
            return this
        }

        fun addScaleX(): Choreography {
            return this
        }

        fun scaleXBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleX.by = multiplier
            this.scaleX.interpolator = interpolator
            return this
        }

        fun scaleX(scaleX: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleX.toValue = scaleX
            this.scaleX.interpolator = interpolator
            return this
        }

        fun scaleX(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleX.fromValue = fromValue
            this.scaleX.toValue = toValue
            this.scaleX.interpolator = interpolator
            return this
        }

        fun scaleX(values: FloatValues): Choreography {
            this.scaleX.copy(values)
            return this
        }


        fun addScaleY(): Choreography {
            return this
        }

        fun scaleYBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleY.by = multiplier
            this.scaleY.interpolator = interpolator
            return this
        }

        fun scaleY(scaleY: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleY.toValue = scaleY
            this.scaleY.interpolator = interpolator
            return this
        }

        fun scaleY(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleY.fromValue = fromValue
            this.scaleY.toValue = toValue
            this.scaleY.interpolator = interpolator
            return this
        }

        fun scaleY(values: FloatValues): Choreography {
            this.scaleY.copy(values)
            return this
        }


        fun addScale(): Choreography {
            return this
        }

        fun scaleBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleX.by = multiplier
            this.scaleY.by = multiplier
            this.scaleX.interpolator = interpolator
            this.scaleY.interpolator = interpolator
            return this
        }

        fun scale(scale: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleX(scale, interpolator)
            this.scaleY(scale, interpolator)
            return this
        }

        fun scale(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleX(fromValue, toValue, interpolator)
            this.scaleY(fromValue, toValue, interpolator)
            return this
        }


        fun addToSize(dimension: Dimension, delta: Float): Choreography {
            return this
        }

        fun resizeTo(view: MorphLayout, interpolator: TimeInterpolator? = null): Choreography {
            this.width.toValue = view.morphWidth
            this.height.toValue = view.morphHeight

            this.width.interpolator = interpolator
            this.height.interpolator = interpolator
            return this
        }

        fun resizeTo(bounds: Bounds, interpolator: TimeInterpolator? = null): Choreography {
            this.width.toValue = bounds.width
            this.height.toValue = bounds.height

            this.width.interpolator = interpolator
            this.height.interpolator = interpolator

            return this
        }

        fun resizeBy(dimension: Dimension, multiplier: Float, interpolator: TimeInterpolator? = null): Choreography {
            when(dimension) {
                Dimension.WIDTH -> {
                    this.width.by = multiplier
                    this.width.interpolator = interpolator
                }
                Dimension.HEIGHT ->  {
                    this.height.by = multiplier
                    this.height.interpolator = interpolator
                }
                Dimension.BOTH -> {
                    this.width.by = multiplier
                    this.width.interpolator = interpolator
                    this.height.by = multiplier
                    this.height.interpolator = interpolator
                }
            }

            return this
        }

        fun resize(dimension: Dimension, value: Float, interpolator: TimeInterpolator? = null): Choreography {
            when(dimension) {
                Dimension.WIDTH -> {
                    this.width.toValue = value
                    this.width.interpolator = interpolator
                }
                Dimension.HEIGHT ->  {
                    this.height.toValue = value
                    this.height.interpolator = interpolator
                }
                Dimension.BOTH -> {
                    this.width.toValue = value
                    this.height.toValue = value
                    this.width.interpolator = interpolator
                    this.height.interpolator = interpolator
                }
            }
            return this
        }

        fun resize(dimension: Dimension, fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            when(dimension) {
                Dimension.WIDTH -> {
                    this.width.fromValue = fromValue
                    this.width.toValue = toValue
                    this.width.interpolator = interpolator
                }
                Dimension.HEIGHT ->  {
                    this.height.fromValue = fromValue
                    this.height.toValue = toValue
                    this.height.interpolator = interpolator
                }
                Dimension.BOTH -> {
                    this.width.fromValue = fromValue
                    this.width.toValue = toValue
                    this.width.interpolator = interpolator

                    this.height.fromValue = fromValue
                    this.height.toValue = toValue
                    this.height.interpolator = interpolator
                }
            }
            return this
        }

        fun resize(dimension: Dimension, values: FloatValues): Choreography {
            when(dimension) {
                Dimension.WIDTH -> {
                    this.width.copy(values)
                }
                Dimension.HEIGHT ->  {
                    this.height.copy(values)
                }
                Dimension.BOTH -> {
                    this.width.copy(values)
                    this.height.copy(values)
                }
            }
            return this
        }

        fun color(color: Color, interpolator: TimeInterpolator? = null): Choreography {
            this.color.toValue = color.toColor()
            this.color.interpolator = interpolator
            return this
        }

        fun color(fromValue: Color, toValue: Color, interpolator: TimeInterpolator? = null): Choreography {
            this.color.fromValue = fromValue.toColor()
            this.color.toValue = toValue.toColor()
            this.color.interpolator = interpolator
            return this
        }

        fun color(valueMap: ValueMap<Color>): Choreography {
            this.color.fromValue = valueMap.fromValue.toColor()
            this.color.toValue = valueMap.toValue.toColor()
            this.color.interpolator = valueMap.interpolator
            return this
        }

        fun color(@ColorInt color: Int, interpolator: TimeInterpolator? = null): Choreography{
            this.color.toValue = color
            this.color.interpolator = interpolator
            return this
        }

        fun color(@ColorInt fromValue: Int, @ColorInt toValue: Int, interpolator: TimeInterpolator? = null): Choreography{
            this.color.fromValue = fromValue
            this.color.toValue = toValue
            this.color.interpolator = interpolator
            return this
        }

        fun interpolator(interpolator: TimeInterpolator?): Choreography {
            this.interpolator = interpolator
            return this
        }

        fun withDuration(duration: Long): Choreography {
            this.duration = duration
            return this
        }

        fun withDelay(delay: Long): Choreography {
            this.delay = delay
            return this
        }

        fun whenDone(action: ChoreographerAction): Choreography {
            this.doneAction = action
            return this
        }

        fun withInterval(time: Long): Choreography {
            return this
        }

        fun childStagger(stagger: Float): Choreography {
            return this
        }

        fun thenReverse(): Choreography {
            this.reverse = true
            return this
        }

        fun animateAfter(offset: Float, vararg view: MorphLayout): Choreography {
            return choreographer.animateAfter(this, offset, *view)
        }

        fun thenAnimate(vararg view: MorphLayout): Choreography {
            return choreographer.thenAnimate(this, *view)
        }

        fun alsoAnimate(vararg view: MorphLayout): Choreography {
            return choreographer.alsoAnimate(this, *view)
        }

        fun andAnimate(vararg view: MorphLayout): Choreography {
            return choreographer.andAnimate(this, *view)
        }

        fun andAnimateAfter(offset: Float, vararg view: MorphLayout): Choreography {
            return choreographer.andAnimateAfter(this, offset, *view)
        }

        fun build(): Choreographer {
            return choreographer.build()
        }

        fun clone(vararg view: MorphLayout): Choreography {
            val choreography = Choreography(choreographer, *view)

            choreography.color.copy(this.color)
            choreography.width.copy(this.width)
            choreography.height.copy(this.height)
            choreography.alpha.copy(this.alpha)
            choreography.scaleX.copy(this.scaleX)
            choreography.scaleY.copy(this.scaleY)
            choreography.rotation.copy(this.rotation)
            choreography.rotationX.copy(this.rotationX)
            choreography.rotationY.copy(this.rotationY)
            choreography.positionX.copy(this.positionX)
            choreography.positionY.copy(this.positionY)
            choreography.translateX.copy(this.translateX)
            choreography.translateY.copy(this.translateY)
            choreography.translateZ.copy(this.translateZ)
            choreography.cornerRadii.copy(this.cornerRadii)
            choreography.duration = this.duration
            choreography.parent = null
            choreography.child = null

            return choreography
        }

        fun toMatchBounds(): Choreography {
            return this
        }

        fun toMatchShape(): Choreography {
            return this
        }
    }

    companion object {
        const val MAX_OFFSET = 1f
        const val MIN_OFFSET = 0f
    }
}
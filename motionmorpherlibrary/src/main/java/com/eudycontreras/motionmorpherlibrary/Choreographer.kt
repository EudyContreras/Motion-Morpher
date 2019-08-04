package com.eudycontreras.motionmorpherlibrary

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.util.Log
import androidx.annotation.ColorInt
import androidx.core.animation.addListener
import com.eudycontreras.motionmorpherlibrary.enumerations.*
import com.eudycontreras.motionmorpherlibrary.extensions.clamp
import com.eudycontreras.motionmorpherlibrary.extensions.toStateList
import com.eudycontreras.motionmorpherlibrary.helpers.CurvedTranslationHelper
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

    private var animators: ArrayList<ChoreographyControl> = ArrayList()

    private var listener: ChoreographyListener = ChoreographyListener()

    private var arcTranslator: CurvedTranslationHelper = CurvedTranslationHelper()

    private var built: Boolean = false

    private var defaultDuration: Long = 0L

    private lateinit var headChoreography: Choreography
    private lateinit var tailChoreography: Choreography

    fun withDefaultDuration(duration: Long): Choreographer {
        this.defaultDuration = duration
        return this
    }

    fun addListener(listener: ChoreographyListener): Choreographer {
        this.listener = listener
        return this
    }


    fun animate(vararg view: MorphLayout): Choreography {
        this.headChoreography = Choreography(this, *view)
        this.headChoreography.offset = 1f
        return headChoreography
    }

    internal fun animateAfter(choreography: Choreography, offset: Float, vararg views: MorphLayout): Choreography {

        tailChoreography =  Choreography(this, *views).apply {
            this.parent = choreography
            this.offset = offset
            this.delay = (choreography.duration * offset).toLong()
            choreography.child = this
        }
        return tailChoreography
    }

    internal fun thenAnimate(choreography: Choreography, vararg views: MorphLayout): Choreography {
        var properties: Map<String, ValueMap<*>>? = null

        predecessors(choreography) { control, chor ->
            loopA@ for(viewA in chor.views) {
                loopB@ for(viewB in views) {
                    if (viewB == viewA) {
                        properties = chor.properties()
                        control.breakTraversal()
                        break@loopB
                    }
                }
                if (properties != null) {
                    break@loopA
                }
            }
        }
        tailChoreography = Choreography(this, *views).apply {
            this.setStartProperties(properties)
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

    internal fun reverseAnimate(choreography: Choreography, vararg views: MorphLayout): Choreography {
        var oldChoreography: Choreography? = null

        predecessors(choreography) { control, _choreography ->
            loopA@ for(viewA in _choreography.views) {
                loopB@ for(viewB in views) {
                    if (viewB == viewA) {
                        oldChoreography = _choreography
                        control.breakTraversal()
                        break@loopB
                    }

                    if (oldChoreography != null)
                        break@loopA
                }
            }
        }

        tailChoreography = (oldChoreography?.clone(*views) ?: Choreography(this, *views)).apply {
            this.reverseValues()
            this.views = views
            this.offset = 1f
            this.reverseToStartState = true
            this.parent = choreography
            this.child = null
            choreography.child = this
        }
        return tailChoreography
    }

    internal fun andReverseAnimate(choreography: Choreography, vararg views: MorphLayout): Choreography {
        var oldChoreography: Choreography? = null

        predecessors(choreography) { control, _choreography ->
            loopA@ for(viewA in _choreography.views) {
                loopB@ for(viewB in views) {
                    if (viewB == viewA) {
                        oldChoreography = _choreography
                        control.breakTraversal()
                        break@loopB
                    }

                    if (oldChoreography != null)
                        break@loopA
                }
            }
        }

        tailChoreography = (oldChoreography?.clone(*views) ?: Choreography(this, *views)).apply {
            this.reverseValues()
            this.views = views
            this.reverseToStartState = true
            this.parent = choreography
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
            val current = temp

            applyMultipliers(current)
            applyInterpolators(current)

            totalDuration += (current.duration.toFloat() * (current.child?.offset?: 1f)).toLong()
            totalDelay += ((current.parent?.duration?: 0L).toFloat() * current.offset).toLong()
            totalDelay += current.delay

            val start: Float = if (current.reverseToStartState) MAX_OFFSET else MIN_OFFSET
            val end: Float = if (current.reverseToStartState) MIN_OFFSET else MAX_OFFSET

            val animator = ValueAnimator.ofFloat(start, end)

            if (current.reverse) {
                animator.repeatMode = ValueAnimator.REVERSE
                animator.repeatCount = 1
            }

            val updateListener: ValueAnimator.AnimatorUpdateListener = ValueAnimator.AnimatorUpdateListener {
                val fraction = it.animatedFraction.clamp(MIN_OFFSET, MAX_OFFSET)

                if (current.reverseToStartState) {
                    Log.d("CURRENT REVERSING:: ", fraction.toString())
                }
                animate(current, fraction)
            }

            val control = ChoreographyControl(animator, current)

            val endListener: (Animator) -> Unit = {
                current.doneAction?.invoke(current)
            }

            val startListener: (Animator) -> Unit = {
                current.recomputeFromValues()
            }

            animator.addListener(
                onStart = startListener,
                onEnd = endListener
            )

            animator.addUpdateListener(updateListener)

            animator.duration = current.duration
            animator.startDelay = totalDelay

            control.endListener = endListener
            control.mStartDelay = totalDelay
            control.mDuration = current.duration
            control.mRemainingDuration = totalDuration
            control.updateListener = updateListener

            animators.add(control)

            temp = current.child
        }

        built = true

        return this
    }

    private fun applyInterpolators(choreography: Choreography) {
        choreography.color.interpolator = choreography.color.interpolator ?: choreography.interpolator

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

        choreography.positionX.interpolator = choreography.positionX.interpolator ?: choreography.interpolator
        choreography.positionY.interpolator = choreography.positionY.interpolator ?: choreography.interpolator

        choreography.paddings.interpolator = choreography.paddings.interpolator ?: choreography.interpolator

        choreography.margings.interpolator = choreography.margings.interpolator ?: choreography.interpolator

        choreography.cornerRadii.interpolator = choreography.cornerRadii.interpolator ?: choreography.interpolator
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
        if (choreography.rotation.by != MAX_OFFSET) {
            choreography.rotation.toValue = choreography.rotation.fromValue * choreography.rotation.by
        }
        if (choreography.rotationX.by != MAX_OFFSET) {
            choreography.rotationX.toValue = choreography.rotationX.fromValue * choreography.rotationX.by
        }
        if (choreography.rotationY.by != MAX_OFFSET) {
            choreography.rotationY.toValue = choreography.rotationY.fromValue * choreography.rotationY.by
        }
    }

    /*internal fun build(): Choreographer {
        var totalDuration: Long = 0

        var temp: Choreography? = headChoreography

        while (temp != null) {
            totalDuration += (temp.defaultDuration.toFloat() * (temp.child?.offset?: 1f)).toLong()
            temp = temp.child
        }

        temp = headChoreography
        var lastDuration = 0L

        while (temp != null) {
            val offsetStart: Float = (temp.offset * lastDuration) / totalDuration.toFloat()
            val offsetEnd: Float = temp.defaultDuration.toFloat() / totalDuration.toFloat()

            temp.fractionOffsetStart = offsetStart.clamp(0f, 1f)
            temp.fractionOffsetEnd = (offsetEnd + offsetStart).clamp(0f, 1f)

            lastDuration += temp.defaultDuration
            temp = temp.child
        }

        animator.defaultDuration = totalDuration
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

        control.mDuration = totalDuration
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

    private var boundsChanged: Boolean = false

    private fun animate(view: MorphLayout, choreography: Choreography, fraction: Float) {

        val alphaFraction = choreography.alpha.interpolator?.getInterpolation(fraction) ?: fraction

        val scaleXFraction = choreography.scaleX.interpolator?.getInterpolation(fraction) ?: fraction
        val scaleYFraction = choreography.scaleY.interpolator?.getInterpolation(fraction) ?: fraction

        val rotateFraction = choreography.rotation.interpolator?.getInterpolation(fraction) ?: fraction
        val rotateXFraction = choreography.rotationX.interpolator?.getInterpolation(fraction) ?: fraction
        val rotateYFraction = choreography.rotationY.interpolator?.getInterpolation(fraction) ?: fraction

        val translateZFraction = choreography.translateZ.interpolator?.getInterpolation(fraction) ?: fraction

        view.morphAlpha = choreography.alpha.fromValue + (choreography.alpha.toValue - choreography.alpha.fromValue) * alphaFraction

        view.morphScaleX = choreography.scaleX.fromValue + (choreography.scaleX.toValue - choreography.scaleX.fromValue) * scaleXFraction
        view.morphScaleY = choreography.scaleY.fromValue + (choreography.scaleY.toValue - choreography.scaleY.fromValue) * scaleYFraction

        view.morphRotation = choreography.rotation.fromValue + (choreography.rotation.toValue - choreography.rotation.fromValue) * rotateFraction

        view.morphRotationX = choreography.rotationX.fromValue + (choreography.rotationX.toValue - choreography.rotationX.fromValue) * rotateXFraction
        view.morphRotationY = choreography.rotationY.fromValue + (choreography.rotationY.toValue - choreography.rotationY.fromValue) * rotateYFraction

        if (choreography.positionX.canInterpolate || choreography.positionY.canInterpolate) {
            val positionXFraction = choreography.positionX.interpolator?.getInterpolation(fraction) ?: fraction
            val positionYFraction = choreography.positionY.interpolator?.getInterpolation(fraction) ?: fraction

            view.morphTranslationX = choreography.positionX.fromValue + (choreography.positionX.toValue - choreography.positionX.fromValue) * positionXFraction
            view.morphTranslationY = choreography.positionY.fromValue + (choreography.positionY.toValue - choreography.positionY.fromValue) * positionYFraction
        } else {
            val translateXFraction = choreography.translateX.interpolator?.getInterpolation(fraction) ?: fraction
            val translateYFraction = choreography.translateY.interpolator?.getInterpolation(fraction) ?: fraction

            view.morphTranslationX = choreography.translateX.fromValue + (choreography.translateX.toValue - choreography.translateX.fromValue) * translateXFraction
            view.morphTranslationY = choreography.translateY.fromValue + (choreography.translateY.toValue - choreography.translateY.fromValue) * translateYFraction
        }

        view.morphTranslationZ = choreography.translateZ.fromValue + (choreography.translateZ.toValue - choreography.translateZ.fromValue) * translateZFraction

        if (view.mutateCorners && view.hasGradientDrawable() && choreography.cornerRadii.canInterpolate) {
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

        if (choreography.color.canInterpolate) {
            val colorFraction = choreography.color.interpolator?.getInterpolation(fraction) ?: fraction

            view.morphStateList = ColorUtility.interpolateColor(colorFraction, choreography.color.fromValue, choreography.color.toValue).toStateList()
        }

        if (choreography.margings.canInterpolate) {
            val marginFraction = choreography.margings.interpolator?.getInterpolation(fraction) ?: fraction

            view.morphMargings.top = choreography.margings.fromValue.top + (choreography.margings.toValue.top - choreography.margings.fromValue.top) * marginFraction
            view.morphMargings.start = choreography.margings.fromValue.start + (choreography.margings.toValue.start - choreography.margings.fromValue.start) * marginFraction
            view.morphMargings.end = choreography.margings.fromValue.end + (choreography.margings.toValue.end - choreography.margings.fromValue.end) * marginFraction
            view.morphMargings.bottom = choreography.margings.fromValue.bottom + (choreography.margings.toValue.bottom - choreography.margings.fromValue.bottom) * marginFraction

            boundsChanged = true
        }

        if (choreography.paddings.canInterpolate) {
            val paddingFraction = choreography.paddings.interpolator?.getInterpolation(fraction) ?: fraction

            view.morphPaddings.top = choreography.paddings.fromValue.top + (choreography.paddings.toValue.top - choreography.paddings.fromValue.top) * paddingFraction
            view.morphPaddings.start = choreography.paddings.fromValue.start + (choreography.paddings.toValue.start - choreography.paddings.fromValue.start) * paddingFraction
            view.morphPaddings.end = choreography.paddings.fromValue.end + (choreography.paddings.toValue.end - choreography.paddings.fromValue.end) * paddingFraction
            view.morphPaddings.bottom = choreography.paddings.fromValue.bottom + (choreography.paddings.toValue.bottom - choreography.paddings.fromValue.bottom) * paddingFraction
        }

        if (choreography.width.canInterpolate || choreography.height.canInterpolate) {
            val widthFraction = choreography.width.interpolator?.getInterpolation(fraction) ?: fraction
            val heightFraction = choreography.height.interpolator?.getInterpolation(fraction) ?: fraction

            view.morphWidth = choreography.width.fromValue + (choreography.width.toValue - choreography.width.fromValue) * widthFraction
            view.morphHeight = choreography.height.fromValue + (choreography.height.toValue - choreography.height.fromValue) * heightFraction

            boundsChanged = true
        }

        if (fraction >= 1f) {
            choreography.done = true
            choreography.doneAction?.invoke(choreography)
        }

        if (boundsChanged) {
            view.updateLayout()
        }

        boundsChanged = false
    }

    fun start(): ArrayList<ChoreographyControl> {
        animators.forEach { it.start() }
        return animators
    }

    fun reset(): Choreographer {
        predecessors(tailChoreography) { _, choreography ->
            choreography.resetProperties()
        }
        return this
    }

    fun reset(view: MorphLayout): Choreographer {
        predecessors(tailChoreography) { _, choreography ->
            choreography.views.forEach {v ->
                if (view == v) {
                    choreography.resetProperties()
                }
            }
        }
        return this
    }

    fun resetWithAnimation(view: MorphLayout): Choreographer {
        predecessors(tailChoreography) { _, choreography ->
            choreography.views.forEach {v ->
                if (view == v) {
                    animators.first { it.choreography == choreography }.reverse()
                }
            }
        }
        return this
    }

    fun predecessors(choreography: Choreography, iterator: (control: TraverseControl, choreography: Choreography) -> Unit) {
        val traverseControl= TraverseControl()
        var temp: Choreography? = choreography
        while (temp != null) {
            iterator.invoke(traverseControl, temp)
            if (traverseControl.breakTraverse) {
                break
            }
            temp = temp.parent
        }

        if (temp == null) {
            temp = choreography
        }
    }

    fun sucessors(choreography: Choreography, iterator: (choreography: Choreography) -> Unit) {
        val traverseControl= TraverseControl()

        var temp: Choreography? = choreography
        while (temp != null) {
            iterator.invoke(temp)
            if (traverseControl.breakTraverse) {
                break
            }
            temp = temp.child
        }

        if (temp == null) {
            temp = choreography
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

        internal var mDuration: Long = 0L
        internal var mStartDelay: Long = 0L
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
            val animator = ValueAnimator.ofFloat(MAX_OFFSET, MIN_OFFSET)

            endListener?.let { animator.addListener(it) }

            animator.interpolator = this.animator.interpolator
            animator.duration = this.mDuration
            animator.addUpdateListener(this.updateListener)
            animator.start()

            this.animator = animator
        }

        internal fun start(): ChoreographyControl {
            animator.start()
            return this
        }

        fun getTotalDuration(): Long {
            return mDuration
        }

        fun getRemainingDuration(): Long  {
            return mRemainingDuration
        }

        fun getElapsedDuration(): Long  {
            return mElapsedDuration
        }

        fun getStartDelay(): Long  {
            return mStartDelay
        }
    }

    class Choreography (
        var choreographer: Choreographer,
        internal vararg var views: MorphLayout
    ) {

        internal var done: Boolean = false
        internal var reverse: Boolean = false
        internal var reverseToStartState: Boolean = false

        internal var delay: Long = 0L
        internal var offset: Float = 0F
        internal var interval: Long = 0L
        internal var duration: Long = choreographer.defaultDuration
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

        internal var paddings: ValueMap<Paddings> = ValueMap(ValueMap.PADDING, Paddings(), Paddings())
        internal var margings: ValueMap<Margings> = ValueMap(ValueMap.MARGIN, Margings(), Margings())

        internal var translateX: FloatValues = FloatValues(ValueMap.TRANSLATION_X, MIN_OFFSET, MIN_OFFSET)
        internal var translateY: FloatValues = FloatValues(ValueMap.TRANSLATION_Y, MIN_OFFSET, MIN_OFFSET)
        internal var translateZ: FloatValues = FloatValues(ValueMap.TRANSLATION_Z, MIN_OFFSET, MIN_OFFSET)

        internal var width: FloatValues = FloatValues(ValueMap.WIDTH, MIN_OFFSET, MIN_OFFSET)
        internal var height: FloatValues = FloatValues(ValueMap.HEIGHT, MIN_OFFSET, MIN_OFFSET)

        internal var alpha: FloatValues = FloatValues(ValueMap.ALPHA, MAX_OFFSET, MAX_OFFSET)

        internal var color: IntValues = IntValues(ValueMap.COLOR, 0x000000, 0x000000)

        internal var cornerRadii: ValueMap<CornerRadii> = ValueMap(ValueMap.CORNERS, CornerRadii(), CornerRadii())

        internal var arcTranslatorX: Boolean = false
        internal var arcTranslatorY: Boolean = false

        internal var doneAction: ChoreographerAction = null

        internal var interpolator: TimeInterpolator? = null

        internal var parent: Choreography? = null
        internal var child: Choreography? = null

        init {
            applyDefaultValues()
        }

        internal fun properties(): Map<String, ValueMap<*>> {
            val properties: HashMap<String, ValueMap<*>> = HashMap()
            properties[scaleX.type] = scaleX
            properties[scaleY.type] = scaleY
            properties[rotation.type] = rotation
            properties[rotationX.type] = rotationX
            properties[rotationY.type] = rotationY
            properties[positionX.type] = positionX
            properties[positionY.type] = positionY
            properties[paddings.type] = paddings
            properties[margings.type] = margings
            properties[translateX.type] = translateX
            properties[translateY.type] = translateY
            properties[translateZ.type] = translateZ
            properties[width.type] = width
            properties[height.type] = height
            properties[alpha.type] = alpha
            properties[color.type] = color
            properties[cornerRadii.type] = cornerRadii
            return properties
        }
/*

        internal fun setProperties(properties: Map<String, ValueMap<*>>) {
            scaleX = properties[scaleX.type] as FloatValues
            scaleY = properties[scaleY.type] as FloatValues
            rotation = properties[rotation.type] as FloatValues
            rotationX = properties[rotationX.type] as FloatValues
            rotationY = properties[rotationY.type] as FloatValues
            positionX = properties[positionX.type] as FloatValues
            positionY = properties[positionY.type] as FloatValues
            paddings = properties[paddings.type] as ValueMap<Paddings>
            margings = properties[margings.type] as ValueMap<Margings>
            translateX = properties[translateX.type] as FloatValues
            translateY = properties[translateY.type] as FloatValues
            translateZ = properties[translateZ.type] as FloatValues
            width = properties[width.type] as FloatValues
            height = properties[height.type] as FloatValues
            alpha = properties[alpha.type] as FloatValues
            color = properties[color.type] as IntValues
            cornerRadii = properties[cornerRadii.type] as ValueMap<CornerRadii>
        }
*/

        internal fun setStartProperties(properties: Map<String, ValueMap<*>>?) {
            if (properties == null)
                return

            scaleX.set(properties[scaleX.type]?.toValue as Float)
            scaleY.set(properties[scaleY.type]?.toValue as Float)
            rotation.set(properties[rotation.type]?.toValue as Float)
            rotationX.set(properties[rotationX.type]?.toValue as Float)
            rotationY.set(properties[rotationY.type]?.toValue as Float)
            positionX.set(properties[positionX.type]?.toValue as Float)
            positionY.set(properties[positionY.type]?.toValue as Float)
            paddings.set(properties[paddings.type]?.toValue as Paddings)
            margings.set(properties[margings.type]?.toValue as Margings)
            translateX.set(properties[translateX.type]?.toValue as Float)
            translateY.set(properties[translateY.type]?.toValue as Float)
            translateZ.set(properties[translateZ.type]?.toValue as Float)
            width.set(properties[width.type]?.toValue as Float)
            height.set(properties[height.type]?.toValue as Float)
            alpha.set(properties[alpha.type]?.toValue as Float)
            color.set(properties[color.type]?.toValue as Int)
            cornerRadii.set(properties[cornerRadii.type]?.toValue as CornerRadii)
        }

        internal fun recomputeFromValues() {
            /* if (views.isNotEmpty()) {
                 views[0].let {
                     this.color.fromValue = it.morphColor
                     this.alpha.fromValue = (it.morphAlpha)
                     this.scaleX.fromValue = (it.morphScaleX)
                     this.scaleY.fromValue = (it.morphScaleY)
                     this.rotation.fromValue = (it.morphRotation)
                     this.rotationX.fromValue = (it.morphRotationX)
                     this.rotationY.fromValue = (it.morphRotationY)
                     this.translateX.fromValue = (it.morphTranslationX)
                     this.translateY.fromValue = (it.morphTranslationY)
                     this.translateZ.fromValue = (it.morphTranslationZ)
                     this.positionX.fromValue = (it.morphX)
                     this.positionY.fromValue = (it.morphY)
                     this.width.fromValue = (it.morphWidth)
                     this.height.fromValue = (it.morphHeight)
                     this.margings.fromValue = it.morphMargings.getCopy()
                     this.paddings.fromValue = it.morphPaddings.getCopy()
                     this.cornerRadii.fromValue = it.morphCornerRadii.getCopy()
                 }
             }*/
        }

        internal fun reverseValues() {
            this.color.flip()
            this.alpha.flip()
            this.scaleX.flip()
            this.scaleY.flip()
            this.rotation.flip()
            this.rotationX.flip()
            this.rotationY.flip()
            this.translateX.flip()
            this.translateY.flip()
            this.translateZ.flip()
            this.positionX.flip()
            this.positionY.flip()
            this.width.flip()
            this.height.flip()
            this.margings.flip()
            this.paddings.flip()
            this.cornerRadii.flip()
        }

        private fun applyDefaultValues() {
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
                    this.positionX.set(it.morphTranslationX)
                    this.positionY.set(it.morphTranslationY)
                    this.width.set(it.morphWidth)
                    this.height.set(it.morphHeight)
                    this.margings.fromValue = it.morphMargings.getCopy()
                    this.margings.toValue = it.morphMargings.getCopy()
                    this.paddings.fromValue = it.morphPaddings.getCopy()
                    this.paddings.toValue = it.morphPaddings.getCopy()
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
                    if (positionX.canInterpolate) {
                        it.morphTranslationX = positionX.fromValue
                    } else {
                        it.morphTranslationX = translateX.fromValue
                    }
                    if (positionY.canInterpolate) {
                        it.morphTranslationY = positionY.fromValue
                    } else {
                        it.morphTranslationY = translateY.fromValue
                    }
                    it.morphTranslationZ = translateZ.fromValue
                    it.morphWidth = width.fromValue
                    it.morphHeight = height.fromValue
                    it.morphMargings = margings.fromValue
                    it.morphPaddings = paddings.fromValue
                    if (it.mutateCorners && it.hasGradientDrawable()) {
                        it.morphCornerRadii = cornerRadii.fromValue.getCopy()
                    }
                    it.updateLayout()
                }
            }
        }

        fun positionAt(bounds: Bounds, interpolator: TimeInterpolator? = null): Choreography {
            val startX: Float = bounds.x + ((bounds.width / 2f) - (views[0].morphWidth / 2f))
            val startY: Float = bounds.y + ((bounds.height / 2f) - (views[0].morphHeight / 2f))

            val endX: Float = views[0].windowLocationX.toFloat()
            val endY: Float = views[0].windowLocationY.toFloat()

            val translationX: Float = abs(endX - startX)
            val translationY: Float = abs(endY - startY)

            this.translateX.toValue =  if (startX < endX) -translationX else translationX
            this.translateY.toValue =  if (startY < endY) -translationY else translationY

            this.translateX.interpolator = interpolator
            this.translateY.interpolator = interpolator

            return this
        }

        fun positionX(positionX: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.positionX.toValue = positionX
            this.positionX.interpolator = interpolator
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

        fun arcTranslateTo(coordinates: Coordinates, interpolator: TimeInterpolator? = null): Choreography {
            return arcTranslateTo(coordinates.x, coordinates.y, interpolator)
        }

        fun arcTranslateTo(translationX: Float, translationY: Float, interpolator: TimeInterpolator? = null): Choreography {
            translateX.toValue = translationX
            translateY.toValue = translationY
            translateX.interpolator = interpolator
            translateY.interpolator = interpolator
            choreographer.arcTranslator.setControlPoint(Coordinates(translationX, translateY.fromValue))
            choreographer.arcTranslator.setStartPoint(Coordinates(translateX.fromValue, translateY.fromValue))
            choreographer.arcTranslator.setEndPoint(Coordinates(translateX.toValue, translateY.toValue))
            this.arcTranslatorX = true
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

        fun cornerRadiusTo(corners: CornerRadii, interpolator: TimeInterpolator? = null): Choreography {
            cornerRadii.interpolator = interpolator
            cornerRadii.toValue = corners
            return this
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

        fun addRotation(delta: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotation.add = delta
            this.rotation.interpolator = interpolator
            return this
        }

        fun rotateBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotation.by = multiplier
            this.rotation.interpolator = interpolator
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

        fun addRotationX(delta: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationX.add = delta
            this.rotationX.interpolator = interpolator
            return this
        }

        fun rotateXBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationX.by = multiplier
            this.rotationX.interpolator = interpolator
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

        fun addRotationY(delta: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationY.add = delta
            this.rotationY.interpolator = interpolator
            return this
        }

        fun rotateYBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationY.by = multiplier
            this.rotationY.interpolator = interpolator
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

        fun scaleTo(bounds: Bounds, interpolator: TimeInterpolator? = null): Choreography {
            return scaleTo(bounds.dimension(), interpolator)
        }

        fun scaleTo(dimension: Dimension, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleX.toValue = dimension.width / this.width.fromValue
            this.scaleY.toValue = dimension.height / this.height.fromValue

            this.scaleX.interpolator = interpolator
            this.scaleY.interpolator = interpolator

            return this
        }

        fun addScaleX(value: Float, interpolator: TimeInterpolator?): Choreography {
            this.scaleX.add = value
            this.scaleX.interpolator = interpolator
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

        fun addScaleY(value: Float, interpolator: TimeInterpolator?): Choreography {
            this.scaleY.add = value
            this.scaleY.interpolator = interpolator
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

        fun addScale(value: Float, interpolator: TimeInterpolator?): Choreography {
            addScaleX(value, interpolator)
            addScaleY(value, interpolator)
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

        fun addToSize(measurement: Measurement, delta: Float): Choreography {
            when(measurement) {
                Measurement.WIDTH -> {
                    this.width.toValue = width.toValue + delta
                }
                Measurement.HEIGHT -> {
                    this.height.toValue = height.toValue + delta
                }
                Measurement.BOTH -> {
                    this.width.toValue = width.toValue + delta
                    this.height.toValue = height.toValue + delta
                }
            }

            this.width.interpolator = interpolator
            this.height.interpolator = interpolator
            return this
        }


        fun boundsTo(bounds: Bounds, interpolator: TimeInterpolator? = null): Choreography {
            resizeTo(bounds, interpolator)
            positionAt(bounds, interpolator)
            return this
        }

        fun resizeTo(bounds: Bounds, interpolator: TimeInterpolator? = null): Choreography {
            return resizeTo(bounds.dimension(), interpolator)
        }

        fun resizeTo(dimension: Dimension, interpolator: TimeInterpolator? = null): Choreography {
            this.width.toValue = dimension.width
            this.height.toValue = dimension.height

            this.width.interpolator = interpolator
            this.height.interpolator = interpolator

            return this
        }

        fun resizeBy(measurement: Measurement, multiplier: Float, interpolator: TimeInterpolator? = null): Choreography {
            when(measurement) {
                Measurement.WIDTH -> {
                    this.width.by = multiplier
                    this.width.interpolator = interpolator
                }
                Measurement.HEIGHT ->  {
                    this.height.by = multiplier
                    this.height.interpolator = interpolator
                }
                Measurement.BOTH -> {
                    this.width.by = multiplier
                    this.width.interpolator = interpolator
                    this.height.by = multiplier
                    this.height.interpolator = interpolator
                }
            }
            return this
        }

        fun resize(measurement: Measurement, value: Float, interpolator: TimeInterpolator? = null): Choreography {
            when(measurement) {
                Measurement.WIDTH -> {
                    this.width.toValue = value
                    this.width.interpolator = interpolator
                }
                Measurement.HEIGHT ->  {
                    this.height.toValue = value
                    this.height.interpolator = interpolator
                }
                Measurement.BOTH -> {
                    this.width.toValue = value
                    this.height.toValue = value
                    this.width.interpolator = interpolator
                    this.height.interpolator = interpolator
                }
            }
            return this
        }

        fun resize(measurement: Measurement, fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            when(measurement) {
                Measurement.WIDTH -> {
                    this.width.fromValue = fromValue
                    this.width.toValue = toValue
                    this.width.interpolator = interpolator
                }
                Measurement.HEIGHT ->  {
                    this.height.fromValue = fromValue
                    this.height.toValue = toValue
                    this.height.interpolator = interpolator
                }
                Measurement.BOTH -> {
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

        fun resize(measurement: Measurement, values: FloatValues): Choreography {
            when(measurement) {
                Measurement.WIDTH -> {
                    this.width.copy(values)
                }
                Measurement.HEIGHT ->  {
                    this.height.copy(values)
                }
                Measurement.BOTH -> {
                    this.width.copy(values)
                    this.height.copy(values)
                }
            }
            return this
        }

        fun margin(margin: Margin, value: Float, interpolator: TimeInterpolator? = null): Choreography {
            when(margin) {
                Margin.TOP -> {
                    margings.toValue.top = value
                }
                Margin.START -> {
                    margings.toValue.start = value
                }
                Margin.END -> {
                    margings.toValue.end = value
                }
                Margin.BOTTOM -> {
                    margings.toValue.bottom = value
                }
                Margin.ALL -> {
                    margings.toValue.top = value
                    margings.toValue.start = value
                    margings.toValue.end = value
                    margings.toValue.bottom = value
                }
            }
            margings.interpolator = interpolator
            return this
        }

        fun margin(margin: Margin, valueFrom: Float, valueTo: Float, interpolator: TimeInterpolator? = null): Choreography {
            when(margin) {
                Margin.TOP -> {
                    margings.fromValue.top = valueFrom
                    margings.toValue.top = valueTo
                }
                Margin.START -> {
                    margings.fromValue.start = valueFrom
                    margings.toValue.start = valueTo
                }
                Margin.END -> {
                    margings.fromValue.end = valueFrom
                    margings.toValue.end = valueTo
                }
                Margin.BOTTOM -> {
                    margings.fromValue.bottom = valueFrom
                    margings.toValue.bottom = valueTo
                }
                Margin.ALL -> {
                    margings.fromValue.top = valueFrom
                    margings.toValue.top = valueTo

                    margings.fromValue.start = valueFrom
                    margings.toValue.start = valueTo

                    margings.fromValue.end = valueFrom
                    margings.toValue.end = valueTo

                    margings.fromValue.bottom = valueFrom
                    margings.toValue.bottom = valueTo
                }
            }
            margings.interpolator = interpolator
            return this
        }

        fun margin(margin: ValueMap<Margings>): Choreography {
            this.margings.copy(margin)
            return this
        }

        fun padding(padding: Padding, value: Float, interpolator: TimeInterpolator? = null): Choreography {
            when(padding) {
                Padding.TOP -> {
                    paddings.toValue.top = value
                }
                Padding.START -> {
                    paddings.toValue.start = value
                }
                Padding.END -> {
                    paddings.toValue.end = value
                }
                Padding.BOTTOM -> {
                    paddings.toValue.bottom = value
                }
                Padding.ALL -> {
                    paddings.toValue.top = value
                    paddings.toValue.start = value
                    paddings.toValue.end = value
                    paddings.toValue.bottom = value
                }
            }
            paddings.interpolator = interpolator
            return this
        }

        fun padding(padding: Padding, valueFrom: Float, valueTo: Float, interpolator: TimeInterpolator? = null): Choreography {
            when(padding) {
                Padding.TOP -> {
                    paddings.fromValue.top = valueFrom
                    paddings.toValue.top = valueTo
                }
                Padding.START -> {
                    paddings.fromValue.start = valueFrom
                    paddings.toValue.start = valueTo
                }
                Padding.END -> {
                    paddings.fromValue.end = valueFrom
                    paddings.toValue.end = valueTo
                }
                Padding.BOTTOM -> {
                    paddings.fromValue.bottom = valueFrom
                    paddings.toValue.bottom = valueTo
                }
                Padding.ALL -> {
                    paddings.fromValue.top = valueFrom
                    paddings.toValue.top = valueTo

                    paddings.fromValue.start = valueFrom
                    paddings.toValue.start = valueTo

                    paddings.fromValue.end = valueFrom
                    paddings.toValue.end = valueTo

                    paddings.fromValue.bottom = valueFrom
                    paddings.toValue.bottom = valueTo
                }
            }
            paddings.interpolator = interpolator
            return this
        }

        fun padding(paddings: ValueMap<Paddings>): Choreography {
            this.paddings.copy(paddings)
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

        fun anchorTo(anchor: Anchor, root: MorphLayout, interpolator: TimeInterpolator? = null): Choreography{
            val bounds = root.viewBounds

            var startX: Float = bounds.x.toFloat()
            var startY: Float = bounds.y.toFloat()

            var endX: Float = views[0].windowLocationX.toFloat()
            var endY: Float = views[0].windowLocationY.toFloat()

            when (anchor) {
                Anchor.TOP -> {
                    val translationY: Float = abs(endY - startY)

                    this.positionY.toValue = -translationY
                    this.positionY.interpolator = interpolator
                }
                Anchor.LEFT -> {
                    val translationX: Float = abs(endX - startX)

                    this.positionX.toValue = -translationX
                    this.positionX.interpolator = interpolator
                }
                Anchor.RIGHT -> {
                    val translationX: Float = abs(endX - startX)

                    this.positionX.toValue = translationX
                    this.positionX.interpolator = interpolator
                }
                Anchor.BOTTOM -> {
                    val translationY: Float = abs(endY - startY)

                    this.positionY.toValue = translationY
                    this.positionY.interpolator = interpolator
                }
                Anchor.CENTER -> {
                    startX = bounds.x.toFloat() + (bounds.width / 2)
                    startY = bounds.y.toFloat() + (bounds.height / 2)

                    endX = views[0].windowLocationX.toFloat()
                    endY = views[0].windowLocationY.toFloat()

                    val translationX: Float = abs(endX - startX)
                    val translationY: Float = abs(endY - startY)

                    this.positionX.toValue = (if (endX > startX) -translationX else translationX) - (width.fromValue / 2)
                    this.positionY.toValue = (if (endY > startY) -translationY else translationY) - (height.fromValue / 2)

                    this.positionX.interpolator = interpolator
                    this.positionY.interpolator = interpolator
                }
                Anchor.TOP_LEFT -> {
                    anchorTo(Anchor.TOP, root, interpolator)
                    anchorTo(Anchor.LEFT, root, interpolator)
                }
                Anchor.TOP_RIGHT -> {
                    anchorTo(Anchor.TOP, root, interpolator)
                    anchorTo(Anchor.RIGHT, root, interpolator)
                }
                Anchor.BOTTOM_RIGHT -> {
                    anchorTo(Anchor.BOTTOM, root, interpolator)
                    anchorTo(Anchor.RIGHT, root, interpolator)
                }
                Anchor.BOTTOM_LEFT -> {
                    anchorTo(Anchor.BOTTOM, root, interpolator)
                    anchorTo(Anchor.RIGHT, root, interpolator)
                }
            }
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

        fun withStartDelay(delay: Long): Choreography {
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

        private fun moveInRelationTo(){

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

        fun reverseAnimate(vararg view: MorphLayout): Choreography {
            return choreographer.reverseAnimate(this, *view)
        }

        fun andReverseAnimate(vararg view: MorphLayout): Choreography {
            return choreographer.andReverseAnimate(this, *view)
        }

        fun build(): Choreographer {
            return choreographer.build()
        }

        fun copyProps(other: Choreography) {
            color.copy(other.color)
            width.copy(other.width)
            height.copy(other.height)
            alpha.copy(other.alpha)
            scaleX.copy(other.scaleX)
            scaleY.copy(other.scaleY)
            rotation.copy(other.rotation)
            rotationX.copy(other.rotationX)
            rotationY.copy(other.rotationY)
            positionX.copy(other.positionX)
            positionY.copy(other.positionY)
            translateX.copy(other.translateX)
            translateY.copy(other.translateY)
            translateZ.copy(other.translateZ)
            cornerRadii.copy(other.cornerRadii)
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

        fun backgroundTo(background: Background): Choreography {
            return this
        }
    }

    class TraverseControl {
        internal var breakTraverse: Boolean = false

        fun breakTraversal() {
            breakTraverse = true
        }
    }

    companion object {
        const val MAX_OFFSET = 1f
        const val MIN_OFFSET = 0f
    }
}
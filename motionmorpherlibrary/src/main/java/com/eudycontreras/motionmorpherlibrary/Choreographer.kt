package com.eudycontreras.motionmorpherlibrary

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import androidx.annotation.ColorInt
import androidx.core.animation.addListener
import com.eudycontreras.motionmorpherlibrary.enumerations.*
import com.eudycontreras.motionmorpherlibrary.extensions.clamp
import com.eudycontreras.motionmorpherlibrary.extensions.toStateList
import com.eudycontreras.motionmorpherlibrary.helpers.CurvedTranslationHelper
import com.eudycontreras.motionmorpherlibrary.helpers.StretchAnimationHelper
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.observable.PropertyChangeObservable
import com.eudycontreras.motionmorpherlibrary.properties.*
import com.eudycontreras.motionmorpherlibrary.utilities.ColorUtility
import com.eudycontreras.motionmorpherlibrary.utilities.binding.Bindable
import kotlin.math.abs


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 30 2019
 */
class Choreographer(context: Context): PropertyChangeObservable() {

    //TODO("Different animation durations for properties")

    private lateinit var headChoreography: Choreography
    private lateinit var tailChoreography: Choreography

    private val handler: Handler by lazy {
        Handler()
    }

    private var listener: ChoreographyListener = ChoreographyListener()

    private var defaultInterpolator: TimeInterpolator? = null

    private var boundsChanged: Boolean = false

    private var defaultDuration: Long = 0L

    private var built: Boolean = false

    val morpher: Morpher = Morpher(context)

    var totalDuration: Long = 0L
        private set

    private val arcTranslator: CurvedTranslationHelper by lazy {
        CurvedTranslationHelper()
    }

    private var translationXListener: ViewPropertyValueListener = { view, value ->
        view.morphTranslationX = value
    }

    private var translationYListener: ViewPropertyValueListener = { view, value ->
        view.morphTranslationY = value
    }

    fun withDefaultDuration(duration: Long): Choreographer {
        this.defaultDuration = duration
        return this
    }

    fun withDefaultInterpolator(interpolator: TimeInterpolator): Choreographer {
        this.defaultInterpolator = interpolator
        return this
    }

    fun addListener(listener: ChoreographyListener): Choreographer {
        this.listener = listener
        return this
    }

    fun animator(view: MorphLayout): ViewPropertyAnimator {
        return view.animator()
    }

    fun animate(vararg view: MorphLayout): Choreography {
        this.headChoreography = Choreography(this, *view)
        this.headChoreography.offset = 1f
        return headChoreography
    }

    internal fun animateAfter(choreography: Choreography, offset: Float, vararg views: MorphLayout): Choreography {
        val properties = getProperties(choreography, *views)

        tailChoreography =  Choreography(this, *views).apply {
            this.setStartProperties(properties)
            this.parent = choreography
            this.offset = offset
            this.delay = (choreography.duration * offset).toLong()
            choreography.child = this
        }
        return tailChoreography
    }

    internal fun thenAnimate(choreography: Choreography, vararg views: MorphLayout): Choreography {
        val properties = getProperties(choreography, *views)

        tailChoreography = Choreography(this, *views).apply {
            this.setStartProperties(properties)
            this.parent = choreography
            this.offset = 1f
            choreography.child = this
        }
        return tailChoreography
    }

    internal fun alsoAnimate(choreography: Choreography, vararg views: MorphLayout): Choreography {
        val properties = getProperties(choreography, *views)

        tailChoreography = Choreography(this, *views).apply {
            this.setStartProperties(properties)
            this.parent = choreography
            choreography.child = this
        }
        return tailChoreography
    }

    internal fun andAnimate(choreography: Choreography, vararg views: MorphLayout): Choreography {
        tailChoreography = choreography.clone(*views).apply {
            this.views = views
            this.parent = choreography
            this.child = null
            choreography.child = this
        }
        return tailChoreography
    }

    internal fun andAnimateAfter(choreography: Choreography, offset: Float, vararg views: MorphLayout): Choreography {
        tailChoreography = choreography.clone(*views).apply {
            this.views = views
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
            this.flipValues()
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
            this.flipValues()
            this.views = views
            this.reverseToStartState = true
            this.parent = choreography
            this.child = null
            choreography.child = this
        }
        return tailChoreography
    }


    fun animateChildrenOf(vararg view: MorphLayout): Choreography {
        return headChoreography
    }

    internal fun animateChildrenOfAfter(choreography: Choreography, offset: Float, vararg views: MorphLayout): Choreography {
        return tailChoreography
    }

    internal fun thenAnimateChildrenOf(choreography: Choreography, vararg views: MorphLayout): Choreography {
        return tailChoreography
    }

    internal fun alsoAnimateChildrenOf(choreography: Choreography, vararg views: MorphLayout): Choreography {
        return tailChoreography
    }

    private fun getProperties(choreography: Choreography, vararg views: MorphLayout): Map<String, AnimatedValue<*>>? {
        var properties: Map<String, AnimatedValue<*>>? = null

        predecessors(choreography) { control, _choreography ->
            loopA@ for(viewA in _choreography.views) {
                loopB@ for(viewB in views) {
                    if (viewB == viewA) {
                        properties = _choreography.properties()
                        control.breakTraversal()
                        break@loopB
                    }
                }
                if (properties != null) {
                    break@loopA
                }
            }
        }
        return properties
    }


    fun bindTo(bindable: Bindable<Float>) {

    }

    fun transitionTo(fraction: Float){

    }

    fun startAfter(delay: Long) {
        handler.postDelayed({
            start()
        }, delay)
    }

    fun start() {
        successors(headChoreography) { _, choreography ->
            choreography.control.cancel()
            choreography.control.start()
        }
    }

    fun startFor(choreography: Choreography) {
        successors(headChoreography) { _, _choreography ->
            if (_choreography == choreography) {
                choreography.control.cancel()
                choreography.control.start()
            }
        }
    }

    fun clear(): Choreographer {
        if (!::tailChoreography.isInitialized)
            return this

        predecessors(tailChoreography) { _, choreography ->
            choreography.control.cancel()
        }

        headChoreography.parent = null
        headChoreography.child = null
        headChoreography.views = emptyArray()
        headChoreography.resetProperties()

        tailChoreography.parent = null
        tailChoreography.child = null
        tailChoreography.views = emptyArray()
        tailChoreography.resetProperties()

        built = false

        return this
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

    fun resetWithAnimation(view: MorphLayout, duration: Long = defaultDuration): Choreographer {
        predecessors(tailChoreography) { _, choreography ->
            choreography.views.forEach {v ->
                if (view == v) {
                    choreography
                        .reverseAnimate(view)
                        .withDuration(duration)
                    startFor(choreography)
                }
            }
        }
        return this
    }

    private fun predecessors(choreography: Choreography, iterator: (control: TraverseControl, choreography: Choreography) -> Unit) {
        val traverseControl= TraverseControl()
        var temp: Choreography? = choreography
        while (temp != null) {
            iterator.invoke(traverseControl, temp)
            if (traverseControl.breakTraverse) {
                break
            }
            temp = temp.parent
        }
    }

    private fun successors(choreography: Choreography, iterator: (control: TraverseControl, choreography: Choreography) -> Unit) {
        val traverseControl = TraverseControl()
        var temp: Choreography? = choreography
        while (temp != null) {
            iterator.invoke(traverseControl, temp)
            if (traverseControl.breakTraverse) {
                break
            }
            temp = temp.child
        }
    }

    internal fun build(): Choreographer {

        var totalDuration: Long = 0
        var totalDelay: Long = 0

        var temp: Choreography? = headChoreography

        while (temp != null) {
            val current = temp

            applyInterpolators(current)
            applyMultipliers(current)
            applyAdders(current)

            totalDuration += (current.duration.toFloat() * (current.child?.offset?: MAX_OFFSET)).toLong()
            totalDelay += ((current.parent?.duration?: 0L).toFloat() * current.offset).toLong()
            totalDelay += current.delay

            val start: Float = if (current.reverseToStartState) MAX_OFFSET else MIN_OFFSET
            val end: Float = if (current.reverseToStartState) MIN_OFFSET else MAX_OFFSET

            current.control = ChoreographyControl(current, start, end)

            if (current.reverse) {
                current.control.repeatMode = ChoreographyControl.REVERSE
                current.control.repeatCount = 1
            }

            val updateListener: ValueAnimator.AnimatorUpdateListener = ValueAnimator.AnimatorUpdateListener {
                val fraction = it.animatedFraction.clamp(MIN_OFFSET, MAX_OFFSET)

                animate(current, fraction, current.duration, it.currentPlayTime)
                current.offsetListener?.invoke(fraction)
            }

            val endListener: (Animator) -> Unit = {
                current.doneAction?.invoke(current)
                current.isRunning = false
            }

            val startListener: (Animator) -> Unit = {
                current.startAction?.invoke(current)
                current.isRunning = true
            }

            current.control.mDuration = current.duration
            current.control.mStartDelay = totalDelay
            current.control.updateListener = updateListener
            current.control.startListener = startListener
            current.control.endListener = endListener
            current.control.build()

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

        choreography.translateXValues.interpolator = choreography.translateXValues.interpolator ?: choreography.interpolator
        choreography.translateYValues.interpolator = choreography.translateYValues.interpolator ?: choreography.interpolator
    }

    private fun applyMultipliers(choreography: Choreography) {
        if (choreography.width.multiply != null) {
            choreography.width.toValue = choreography.width.fromValue * choreography.width.multiply!!
        }
        if (choreography.height.multiply != null) {
            choreography.height.toValue = choreography.height.fromValue * choreography.height.multiply!!
        }
        if (choreography.scaleX.multiply != null) {
            choreography.scaleX.toValue = choreography.scaleX.fromValue * choreography.scaleX.multiply!!
        }
        if (choreography.scaleY.multiply != null) {
            choreography.scaleY.toValue = choreography.scaleY.fromValue * choreography.scaleY.multiply!!
        }
        if (choreography.rotation.multiply != null) {
            choreography.rotation.toValue = choreography.rotation.fromValue * choreography.rotation.multiply!!
        }
        if (choreography.rotationX.multiply != null) {
            choreography.rotationX.toValue = choreography.rotationX.fromValue * choreography.rotationX.multiply!!
        }
        if (choreography.rotationY.multiply != null) {
            choreography.rotationY.toValue = choreography.rotationY.fromValue * choreography.rotationY.multiply!!
        }
    }

    private fun applyAdders(choreography: Choreography) {
        if (choreography.width.add != null) {
            choreography.width.toValue = choreography.width.fromValue + choreography.width.add!!
        }
        if (choreography.height.add != null) {
            choreography.height.toValue = choreography.height.fromValue + choreography.height.add!!
        }
        if (choreography.scaleX.add != null) {
            choreography.scaleX.toValue = choreography.scaleX.fromValue + choreography.scaleX.add!!
        }
        if (choreography.scaleY.add != null) {
            choreography.scaleY.toValue = choreography.scaleY.fromValue + choreography.scaleY.add!!
        }
        if (choreography.rotation.add != null) {
            choreography.rotation.toValue = choreography.rotation.fromValue + choreography.rotation.add!!
        }
        if (choreography.rotationX.add != null) {
            choreography.rotationX.toValue = choreography.rotationX.fromValue + choreography.rotationX.add!!
        }
        if (choreography.rotationY.add != null) {
            choreography.rotationY.toValue = choreography.rotationY.fromValue + choreography.rotationY.add!!
        }
    }

    fun animate(choreography: Choreography, fraction: Float, duration: Long, currentPlayTime: Long) {
        val views = choreography.views

        if (views.size == 1) {
            animate(views[0], choreography, fraction, duration, currentPlayTime)
            return
        }

        for (view in views) {
            animate(view, choreography, fraction, duration, currentPlayTime)
        }
    }

    private fun animate(view: MorphLayout, choreography: Choreography, fraction: Float, duration: Long, currentPlayTime: Long) {

        val alphaFraction = choreography.alpha.interpolator?.getInterpolation(fraction) ?: fraction

        val scaleXFraction = choreography.scaleX.interpolator?.getInterpolation(fraction) ?: fraction
        val scaleYFraction = choreography.scaleY.interpolator?.getInterpolation(fraction) ?: fraction

        val rotateFraction = choreography.rotation.interpolator?.getInterpolation(fraction) ?: fraction
        val rotateXFraction = choreography.rotationX.interpolator?.getInterpolation(fraction) ?: fraction
        val rotateYFraction = choreography.rotationY.interpolator?.getInterpolation(fraction) ?: fraction

        val translateZFraction = choreography.translateZ.interpolator?.getInterpolation(fraction) ?: fraction

        view.morphPivotX = choreography.pivotPoint.x
        view.morphPivotY = choreography.pivotPoint.y

        view.morphAlpha = choreography.alpha.fromValue + (choreography.alpha.toValue - choreography.alpha.fromValue) * alphaFraction

        view.morphScaleX = choreography.scaleX.fromValue + (choreography.scaleX.toValue - choreography.scaleX.fromValue) * scaleXFraction
        view.morphScaleY = choreography.scaleY.fromValue + (choreography.scaleY.toValue - choreography.scaleY.fromValue) * scaleYFraction

        view.morphRotation = choreography.rotation.fromValue + (choreography.rotation.toValue - choreography.rotation.fromValue) * rotateFraction

        view.morphRotationX = choreography.rotationX.fromValue + (choreography.rotationX.toValue - choreography.rotationX.fromValue) * rotateXFraction
        view.morphRotationY = choreography.rotationY.fromValue + (choreography.rotationY.toValue - choreography.rotationY.fromValue) * rotateYFraction

        view.morphTranslationZ = choreography.translateZ.fromValue + (choreography.translateZ.toValue - choreography.translateZ.fromValue) * translateZFraction

        if (choreography.positionX.canInterpolate || choreography.positionY.canInterpolate) {
            val positionXFraction = choreography.positionX.interpolator?.getInterpolation(fraction) ?: fraction
            val positionYFraction = choreography.positionY.interpolator?.getInterpolation(fraction) ?: fraction

            if (choreography.useArcTranslator) {

                val arcTranslationX = arcTranslator.getCurvedTranslationX(positionXFraction, choreography.positionX.fromValue, choreography.positionX.toValue, choreography.positionX.toValue)
                val arcTranslationY = arcTranslator.getCurvedTranslationY(positionYFraction, choreography.positionY.fromValue, choreography.positionY.toValue, choreography.positionY.fromValue)

                view.morphTranslationX = arcTranslationX.toFloat()
                view.morphTranslationY = arcTranslationY.toFloat()
            } else {
                view.morphTranslationX = choreography.positionX.fromValue + (choreography.positionX.toValue - choreography.positionX.fromValue) * positionXFraction
                view.morphTranslationY = choreography.positionY.fromValue + (choreography.positionY.toValue - choreography.positionY.fromValue) * positionYFraction

                choreography.stretch?.let {
                    StretchAnimationHelper.applyStretch(view, choreography.positionY, it, view.morphTranslationY)
                }
            }

        } else if (choreography.translateX.canInterpolate || choreography.translateY.canInterpolate) {
            val translateXFraction = choreography.translateX.interpolator?.getInterpolation(fraction) ?: fraction
            val translateYFraction = choreography.translateY.interpolator?.getInterpolation(fraction) ?: fraction

            if (choreography.useArcTranslator) {
                val arcTranslationX = arcTranslator.getCurvedTranslationX(translateXFraction, choreography.translateX.fromValue, choreography.translateX.toValue, choreography.translateX.toValue)
                val arcTranslationY = arcTranslator.getCurvedTranslationY(translateYFraction, choreography.translateY.fromValue, choreography.translateY.toValue, choreography.translateY.fromValue)

                view.morphTranslationX = arcTranslationX.toFloat()
                view.morphTranslationY = arcTranslationY.toFloat()
            } else {
                view.morphTranslationX = choreography.translateX.fromValue + (choreography.translateX.toValue - choreography.translateX.fromValue) * translateXFraction
                view.morphTranslationY = choreography.translateY.fromValue + (choreography.translateY.toValue - choreography.translateY.fromValue) * translateYFraction
            }
        } else {
            if(choreography.translateXValues.canInterpolate) {
                animateThroughPoints(choreography.translateXValues, view, currentPlayTime, duration, translationXListener)
            }

            if(choreography.translateYValues.canInterpolate) {
                animateThroughPoints(choreography.translateYValues, view, currentPlayTime, duration, translationYListener)
            }
        }

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

    private fun animateThroughPoints(valueHolder: FloatValueHolder, view: MorphLayout, playTime: Long, duration: Long, listener: ViewPropertyValueListener) {
        val values = valueHolder.values
        val pointDuration = duration / values.size

        for(index in 0 until values.size - 1) {

            val timeStart: Long = index * pointDuration
            val timeEnd: Long = (index + 1) * pointDuration

            if (playTime < timeStart)
                continue

            val start: Float = values[index]
            val end: Float = values[index + 1]

            val mapFraction = mapRange(playTime.toFloat(), timeStart.toFloat(), timeEnd.toFloat(), MIN_OFFSET, MAX_OFFSET, MIN_OFFSET, MAX_OFFSET)

            val valueXFraction = valueHolder.interpolator?.getInterpolation(mapFraction) ?: mapFraction

            listener(view,start + (end - start) * valueXFraction)
        }
    }

    data class ChoreographyListener(
        var fractionListener: ((remainingTime: Long, fraction: Float) -> Unit)? = null,
        var endListener: Action = null,
        var startListener: Action = null,
        var reverseListener: Action = null
    )

    class ChoreographyControl(
        internal var choreography: Choreography,
        internal var startOffset: Float,
        internal var endOffset: Float
    ) {

        internal var repeatMode: Int = RESTART
        internal var repeatCount: Int = 0
        internal var mDuration: Long = 0L
        internal var mStartDelay: Long = 0L

        internal lateinit  var endListener: (Animator) -> Unit
        internal lateinit var startListener: (Animator) -> Unit

        internal lateinit var updateListener: ValueAnimator.AnimatorUpdateListener

        internal var animator = ValueAnimator.ofFloat(startOffset, endOffset)

        fun pause() {
            animator.pause()
        }

        fun resume() {
            animator.resume()
        }

        fun cancel() {
            animator.cancel()
        }

        internal fun build() {
            animator.addListener(
                onStart = startListener,
                onEnd = endListener
            )
            animator.setFloatValues(startOffset, endOffset)
            animator.addUpdateListener(updateListener)
            animator.duration = mDuration
            animator.startDelay = mStartDelay
            animator.repeatCount = repeatCount
            animator.repeatMode = repeatMode
        }

        fun reverse(duration: Long?) {
            animator  = ValueAnimator.ofFloat(MAX_OFFSET, MIN_OFFSET)

            animator.addListener( onEnd = endListener)

            animator.duration = duration ?: this.mDuration
            animator.addUpdateListener(this.updateListener)
            animator.start()
        }

        internal fun start(): ChoreographyControl {
            animator.start()
            return this
        }

        fun getTotalDuration(): Long {
            return mDuration
        }

        fun getRemainingDuration(): Long  {
            return mDuration - animator.currentPlayTime
        }

        fun getElapsedDuration(): Long  {
            return animator.currentPlayTime
        }

        fun getStartDelay(): Long  {
            return mStartDelay
        }

        companion object {
            const val RESTART = 1
            const val REVERSE = 2
            const val INFINITE = -1
        }
    }

    class Choreography (
        var choreographer: Choreographer,
        internal vararg var views: MorphLayout
    ) {

        var isRunning: Boolean = false
            internal set

        internal var done: Boolean = false
        internal var reverse: Boolean = false
        internal var reverseToStartState: Boolean = false
        internal var useArcTranslator: Boolean = false

        internal var delay: Long = 0L
        internal var offset: Float = 0F
        internal var interval: Long = 0L
        internal var duration: Long = 0L
        internal var childStagger: Float = MIN_OFFSET

        internal var pivotValueX: Float = 0.5f
        internal var pivotValueY: Float = 0.5f

        internal var fractionOffsetStart: Float = MIN_OFFSET
        internal var fractionOffsetEnd: Float = MAX_OFFSET

        internal var pivotValueTypeX: Pivot = Pivot.RELATIVE_TO_SELF
        internal var pivotValueTypeY: Pivot = Pivot.RELATIVE_TO_SELF

        internal val scaleX: AnimatedFloatValue = AnimatedFloatValue(ValueMap.SCALE_X, MAX_OFFSET, MAX_OFFSET)
        internal val scaleY: AnimatedFloatValue = AnimatedFloatValue(ValueMap.SCALE_Y, MAX_OFFSET, MAX_OFFSET)

        internal val rotation: AnimatedFloatValue = AnimatedFloatValue(ValueMap.ROTATION, MIN_OFFSET, MIN_OFFSET)
        internal val rotationX: AnimatedFloatValue = AnimatedFloatValue(ValueMap.ROTATION_X, MIN_OFFSET, MIN_OFFSET)
        internal val rotationY: AnimatedFloatValue = AnimatedFloatValue(ValueMap.ROTATION_Y, MIN_OFFSET, MIN_OFFSET)

        internal val positionX: AnimatedFloatValue = AnimatedFloatValue(ValueMap.POSITION_X, MIN_OFFSET, MIN_OFFSET)
        internal val positionY: AnimatedFloatValue = AnimatedFloatValue(ValueMap.POSITION_Y, MIN_OFFSET, MIN_OFFSET)

        internal val translateX: AnimatedFloatValue = AnimatedFloatValue(ValueMap.TRANSLATION_X, MIN_OFFSET, MIN_OFFSET)
        internal val translateY: AnimatedFloatValue = AnimatedFloatValue(ValueMap.TRANSLATION_Y, MIN_OFFSET, MIN_OFFSET)
        internal val translateZ: AnimatedFloatValue = AnimatedFloatValue(ValueMap.TRANSLATION_Z, MIN_OFFSET, MIN_OFFSET)

        internal var translateXValues: FloatValueHolder = FloatValueHolder(ValueHolder.TRANSLATION_X)
        internal var translateYValues: FloatValueHolder = FloatValueHolder(ValueHolder.TRANSLATION_Y)

        internal val width: AnimatedFloatValue = AnimatedFloatValue(ValueMap.WIDTH, MIN_OFFSET, MIN_OFFSET)
        internal val height: AnimatedFloatValue = AnimatedFloatValue(ValueMap.HEIGHT, MIN_OFFSET, MIN_OFFSET)

        internal val alpha: AnimatedFloatValue = AnimatedFloatValue(ValueMap.ALPHA, MAX_OFFSET, MAX_OFFSET)

        internal val color: AnimatedIntValue = AnimatedIntValue(ValueMap.COLOR, DEFAULT_COLOR, DEFAULT_COLOR)

        internal val cornerRadii: AnimatedValue<CornerRadii> = AnimatedValue.instance(ValueMap.CORNERS, CornerRadii(), CornerRadii())

        internal val paddings: AnimatedValue<Paddings> = AnimatedValue.instance(ValueMap.PADDING, Paddings(), Paddings())
        internal val margings: AnimatedValue<Margings> = AnimatedValue.instance(ValueMap.MARGIN, Margings(), Margings())

        internal var offsetListener: TransitionOffsetListener = null

        internal val pivotPoint: Coordinates = Coordinates()

        internal val controlPoint: Coordinates? = null

        internal var doneAction: ChoreographerAction = null
        internal var startAction: ChoreographerAction = null

        internal var interpolator: TimeInterpolator? = null

        internal var viewParentSize: Dimension = Dimension()

        internal var stretch: Stretch? = null

        lateinit var control: ChoreographyControl
        internal var parent: Choreography? = null
        internal var child: Choreography? = null

        init {
            applyDefaultValues()
        }

        internal fun properties(): Map<String, AnimatedValue<*>> {
            val properties: HashMap<String, AnimatedValue<*>> = HashMap()
            properties[scaleX.propertyName] = scaleX
            properties[scaleY.propertyName] = scaleY
            properties[rotation.propertyName] = rotation
            properties[rotationX.propertyName] = rotationX
            properties[rotationY.propertyName] = rotationY
            properties[positionX.propertyName] = positionX
            properties[positionY.propertyName] = positionY
            properties[paddings.propertyName] = paddings
            properties[margings.propertyName] = margings
            properties[translateX.propertyName] = translateX
            properties[translateY.propertyName] = translateY
            properties[translateZ.propertyName] = translateZ
            properties[width.propertyName] = width
            properties[height.propertyName] = height
            properties[alpha.propertyName] = alpha
            properties[color.propertyName] = color
            properties[cornerRadii.propertyName] = cornerRadii
            return properties
        }

        internal fun propertyHolders(): Map<String, ValueHolder<*>> {
            val properties: HashMap<String, ValueHolder<*>> = HashMap()
            properties[translateX.propertyName] = translateXValues
            properties[translateY.propertyName] = translateYValues
            return properties
        }

        internal fun setStartProperties(properties: Map<String, AnimatedValue<*>>?) {
            if (properties == null)
                return

            scaleX.set(properties[scaleX.propertyName]?.toValue as Float)
            scaleY.set(properties[scaleY.propertyName]?.toValue as Float)
            rotation.set(properties[rotation.propertyName]?.toValue as Float)
            rotationX.set(properties[rotationX.propertyName]?.toValue as Float)
            rotationY.set(properties[rotationY.propertyName]?.toValue as Float)
            positionX.set(properties[positionX.propertyName]?.toValue as Float)
            positionY.set(properties[positionY.propertyName]?.toValue as Float)
            paddings.set(properties[paddings.propertyName]?.toValue as Paddings)
            margings.set(properties[margings.propertyName]?.toValue as Margings)
            translateX.set(properties[translateX.propertyName]?.toValue as Float)
            translateY.set(properties[translateY.propertyName]?.toValue as Float)
            translateZ.set(properties[translateZ.propertyName]?.toValue as Float)
            width.set(properties[width.propertyName]?.toValue as Float)
            height.set(properties[height.propertyName]?.toValue as Float)
            alpha.set(properties[alpha.propertyName]?.toValue as Float)
            color.set(properties[color.propertyName]?.toValue as Int)
            cornerRadii.set(properties[cornerRadii.propertyName]?.toValue as CornerRadii)
        }

        @Suppress("UNCHECKED_CAST")
        internal fun setStartPropertyHolders(properties: Map<String, ValueHolder<*>>?) {
            if (properties == null)
                return

            translateXValues.values = properties.getValue(translateXValues.propertyName).values as Array<Float>
            translateYValues.values = properties.getValue(translateXValues.propertyName).values as Array<Float>
        }

        internal fun flipValues() {
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
            this.translateXValues.values.reverse()
            this.translateYValues.values.reverse()
        }

        private fun applyDefaultValues() {
            this.duration = choreographer.defaultDuration
            this.interpolator = choreographer.defaultInterpolator

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
                    this.pivotPoint.x = it.morphPivotX
                    this.pivotPoint.y = it.morphPivotY

                    if (it.getView().parent != null) {
                        it.getView().parent?.let { parent ->
                            val viewGroup = parent as ViewGroup
                            viewParentSize.width = viewGroup.width.toFloat()
                            viewParentSize.height = viewGroup.height.toFloat()
                        }
                    } else {
                        viewParentSize.width = width.toValue
                        viewParentSize.height = height.toValue
                    }
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

        fun withPivot(pivotValues: FloatPoint, type: Pivot = Pivot.RELATIVE_TO_SELF): Choreography {
            return withPivot(pivotValues.x, pivotValues.y, type)
        }

        fun withPivot(pivotX: Float, pivotY: Float, type: Pivot = Pivot.RELATIVE_TO_SELF): Choreography {
            pivotValueX = pivotX
            pivotValueY = pivotY
            pivotValueTypeX = type
            pivotValueTypeY = type
            pivotPoint.x = resolveSize(pivotValueTypeX, pivotValueX, width.fromValue, viewParentSize.width)
            pivotPoint.y = resolveSize(pivotValueTypeY, pivotValueY, height.fromValue, viewParentSize.height)
            return this
        }

        fun withPivotX(pivotX: Float, type: Pivot = Pivot.RELATIVE_TO_SELF): Choreography {
            pivotValueX = pivotX
            pivotValueTypeX = type
            pivotPoint.x = resolveSize(pivotValueTypeX, pivotValueX, width.fromValue, viewParentSize.width)
            return this
        }

        fun withPivotY(pivotY: Float, type: Pivot = Pivot.RELATIVE_TO_SELF): Choreography {
            pivotValueY = pivotY
            pivotValueTypeY = type
            pivotPoint.y = resolveSize(pivotValueTypeY, pivotValueY, height.fromValue, viewParentSize.height)
            return this
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

        fun xPositionTo(positionX: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.positionX.toValue = positionX
            this.positionX.interpolator = interpolator
            return this
        }

        fun xPositionFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.positionX.fromValue = fromValue
            this.positionX.toValue = toValue
            this.positionX.interpolator = interpolator
            return this
        }

        fun xPosition(values: AnimatedFloatValue): Choreography {
            this.positionX.copy(values)
            return this
        }

        fun yPositionTo(positionY: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.positionY.toValue = positionY
            this.positionY.interpolator = interpolator
            return this
        }

        fun yPositionFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.positionY.fromValue = fromValue
            this.positionY.toValue = toValue
            this.positionY.interpolator = interpolator
            return this
        }

        fun yPosition(values: AnimatedFloatValue): Choreography {
            this.positionY.copy(values)
            return this
        }

        fun xTranslateTo(translationX: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.translateX.toValue = translationX
            this.translateX.interpolator = interpolator
            return this
        }

        fun xTranslateFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.translateX.fromValue = fromValue
            this.translateX.toValue = toValue
            this.translateX.interpolator = interpolator
            return this
        }

        fun xTranslate(values: AnimatedFloatValue): Choreography {
            this.translateX.copy(values)
            return this
        }

        fun yTranslateTo(translationY: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.translateY.toValue = translationY
            this.translateY.interpolator = interpolator
            return this
        }

        fun yTranslateFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.translateY.fromValue = fromValue
            this.translateY.toValue = toValue
            this.translateY.interpolator = interpolator
            return this
        }

        fun yTranslate(values: AnimatedFloatValue): Choreography {
            this.translateY.copy(values)
            return this
        }

        fun zTranslateTo(translationZ: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.translateZ.toValue = translationZ
            this.translateZ.interpolator = interpolator
            return this
        }

        fun zTranslateFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.translateZ.fromValue = fromValue
            this.translateZ.toValue = toValue
            this.translateZ.interpolator = interpolator
            return this
        }

        fun zTranslate(values: AnimatedFloatValue): Choreography {
            this.translateZ.copy(values)
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
            useArcTranslator = true
            return this
        }

        fun translateBetween(vararg values: FloatPoint): Choreography {
            translateXValues.values = values.map { it.x }.toTypedArray()
            translateYValues.values = values.map { it.y }.toTypedArray()
            return this
        }

        fun translateBetween(interpolator: TimeInterpolator? = null, vararg values: FloatPoint): Choreography {
            translateXValues.values = values.map { it.x }.toTypedArray()
            translateYValues.values = values.map { it.y }.toTypedArray()
            translateXValues.interpolator = interpolator
            translateYValues.interpolator = interpolator
            return this
        }

        fun xTranslateBetween(value: Float, percentages: IntArray): Choreography {
            val mapped = percentages.map { it / 100f }.toTypedArray().toFloatArray()
            return xTranslateBetween(value, mapped)
        }

        fun xTranslateBetween(value: Float, percentages: FloatArray): Choreography {
            val output: ArrayList<Float> = ArrayList()
            for (percentage in percentages) {
                output.add(value * percentage)
            }
            translateXValues.values = output.toTypedArray()
            return this
        }

        fun xTranslateBetween(vararg values: Float): Choreography {
            translateXValues.values = values.toTypedArray()
            return this
        }

        fun xTranslateBetween(interpolator: TimeInterpolator? = null, vararg values: Float): Choreography {
            translateXValues.values = values.toTypedArray()
            translateXValues.interpolator = interpolator
            return this
        }

        fun yTranslateBetween(vararg values: Float): Choreography {
            translateYValues.values = values.toTypedArray()
            return this
        }

        fun yTranslateBetween(interpolator: TimeInterpolator? = null, vararg values: Float): Choreography {
            translateYValues.values = values.toTypedArray()
            translateYValues.interpolator = interpolator
            return this
        }

        fun alphaTo(opacity: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.alpha.toValue = opacity
            this.alpha.interpolator = interpolator
            return this
        }

        fun alphaFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.alpha.fromValue = fromValue
            this.alpha.toValue = toValue
            this.alpha.interpolator = interpolator
            return this
        }

        fun alpha(values: AnimatedFloatValue): Choreography {
            this.alpha.copy(values)
            return this
        }

        fun alphaTo(opacity: Int, interpolator: TimeInterpolator? = null): Choreography {
            return this.alphaTo(opacity.clamp(0, 100) / 100f, interpolator)
        }

        fun alphaFrom(fromValue: Int, toValue: Int, interpolator: TimeInterpolator? = null): Choreography {
            return this.alphaFrom(fromValue.clamp(0, 100) / 100f, toValue.clamp(0, 100) / 100f, interpolator)
        }

        fun cornerRadiusTo(corners: CornerRadii, interpolator: TimeInterpolator? = null): Choreography {
            cornerRadii.interpolator = interpolator
            cornerRadii.toValue = corners
            return this
        }

        fun cornerRadiusTo(corner: Corner = Corner.ALL, radius: Float, interpolator: TimeInterpolator? = null): Choreography {
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

        fun cornerRadiusFrom(corner: Corner = Corner.ALL, fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
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

        fun cornerRadius(valueMap: AnimatedValue<CornerRadii>): Choreography {
            cornerRadii.copy(valueMap)
            return this
        }

        fun addRotation(delta: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotation.add = delta
            this.rotation.interpolator = interpolator
            return this
        }

        fun rotateBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotation.multiply = multiplier
            this.rotation.interpolator = interpolator
            return this
        }

        fun rotateTo(rotation: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotation.toValue = rotation
            this.rotation.interpolator = interpolator
            return this
        }

        fun rotateFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotation.fromValue = fromValue
            this.rotation.toValue = toValue
            this.rotation.interpolator = interpolator
            return this
        }

        fun rotate(values: AnimatedFloatValue): Choreography {
            this.rotation.copy(values)
            return this
        }

        fun xRotateAdd(delta: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationX.add = delta
            this.rotationX.interpolator = interpolator
            return this
        }

        fun xRotateBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationX.multiply = multiplier
            this.rotationX.interpolator = interpolator
            return this
        }

        fun xRotate(rotationX: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationX.toValue = rotationX
            this.rotationX.interpolator = interpolator
            return this
        }

        fun xRotateFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationX.fromValue = fromValue
            this.rotationX.toValue = toValue
            this.rotationX.interpolator = interpolator
            return this
        }

        fun xRotate(values: AnimatedFloatValue): Choreography {
            this.rotationX.copy(values)
            return this
        }

        fun yRotateAdd(delta: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationY.add = delta
            this.rotationY.interpolator = interpolator
            return this
        }

        fun yRotateBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationY.multiply = multiplier
            this.rotationY.interpolator = interpolator
            return this
        }

        fun yRotateTo(rotationY: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationY.toValue = rotationY
            this.rotationY.interpolator = interpolator
            return this
        }

        fun yRotateFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationY.fromValue = fromValue
            this.rotationY.toValue = toValue
            this.rotationY.interpolator = interpolator
            return this
        }

        fun yRotate(values: AnimatedFloatValue): Choreography {
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

        fun xScaleAdd(value: Float, interpolator: TimeInterpolator?): Choreography {
            this.scaleX.add = value
            this.scaleX.interpolator = interpolator
            return this
        }

        fun xScaleBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleX.multiply = multiplier
            this.scaleX.interpolator = interpolator
            return this
        }

        fun xScaleTo(scaleX: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleX.toValue = scaleX
            this.scaleX.interpolator = interpolator
            return this
        }

        fun xScaleFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleX.fromValue = fromValue
            this.scaleX.toValue = toValue
            this.scaleX.interpolator = interpolator
            return this
        }

        fun xScale(values: AnimatedFloatValue): Choreography {
            this.scaleX.copy(values)
            return this
        }

        fun yScaleAdd(value: Float, interpolator: TimeInterpolator?): Choreography {
            this.scaleY.add = value
            this.scaleY.interpolator = interpolator
            return this
        }

        fun yScaleBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleY.multiply = multiplier
            this.scaleY.interpolator = interpolator
            return this
        }

        fun yScaleTo(scaleY: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleY.toValue = scaleY
            this.scaleY.interpolator = interpolator
            return this
        }

        fun yScaleFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleY.fromValue = fromValue
            this.scaleY.toValue = toValue
            this.scaleY.interpolator = interpolator
            return this
        }

        fun yScale(values: AnimatedFloatValue): Choreography {
            this.scaleY.copy(values)
            return this
        }

        fun addScale(value: Float, interpolator: TimeInterpolator?): Choreography {
            xScaleAdd(value, interpolator)
            yScaleAdd(value, interpolator)
            return this
        }

        fun scaleBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleX.multiply = multiplier
            this.scaleY.multiply = multiplier
            this.scaleX.interpolator = interpolator
            this.scaleY.interpolator = interpolator
            return this
        }

        fun scaleTo(scale: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.xScaleTo(scale, interpolator)
            this.yScaleTo(scale, interpolator)
            return this
        }

        fun scaleTo(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.xScaleFrom(fromValue, toValue, interpolator)
            this.yScaleFrom(fromValue, toValue, interpolator)
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
                    this.width.multiply = multiplier
                    this.width.interpolator = interpolator
                }
                Measurement.HEIGHT ->  {
                    this.height.multiply = multiplier
                    this.height.interpolator = interpolator
                }
                Measurement.BOTH -> {
                    this.width.multiply = multiplier
                    this.width.interpolator = interpolator
                    this.height.multiply = multiplier
                    this.height.interpolator = interpolator
                }
            }
            return this
        }

        fun resizeTo(measurement: Measurement, value: Float, interpolator: TimeInterpolator? = null): Choreography {
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

        fun resizeFrom(measurement: Measurement, fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
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

        fun resize(measurement: Measurement, values: AnimatedFloatValue): Choreography {
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

        fun marginTo(margin: Margin, value: Float, interpolator: TimeInterpolator? = null): Choreography {
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

        fun marginFrom(margin: Margin, valueFrom: Float, valueTo: Float, interpolator: TimeInterpolator? = null): Choreography {
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

        fun margin(margin: AnimatedValue<Margings>): Choreography {
            this.margings.copy(margin)
            return this
        }

        fun paddingTo(padding: Padding, value: Float, interpolator: TimeInterpolator? = null): Choreography {
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

        fun paddingFrom(padding: Padding, valueFrom: Float, valueTo: Float, interpolator: TimeInterpolator? = null): Choreography {
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

        fun padding(paddings: AnimatedValue<Paddings>): Choreography {
            this.paddings.copy(paddings)
            return this
        }

        fun colorTo(color: Color, interpolator: TimeInterpolator? = null): Choreography {
            this.color.toValue = color.toColor()
            this.color.interpolator = interpolator
            return this
        }

        fun colorFrom(fromValue: Color, toValue: Color, interpolator: TimeInterpolator? = null): Choreography {
            this.color.fromValue = fromValue.toColor()
            this.color.toValue = toValue.toColor()
            this.color.interpolator = interpolator
            return this
        }

        fun color(valueMap: AnimatedValue<Color>): Choreography {
            this.color.fromValue = valueMap.fromValue.toColor()
            this.color.toValue = valueMap.toValue.toColor()
            this.color.interpolator = valueMap.interpolator
            return this
        }

        fun colorTo(@ColorInt color: Int, interpolator: TimeInterpolator? = null): Choreography{
            this.color.toValue = color
            this.color.interpolator = interpolator
            return this
        }

        fun colorFrom(@ColorInt fromValue: Int, @ColorInt toValue: Int, interpolator: TimeInterpolator? = null): Choreography{
            this.color.fromValue = fromValue
            this.color.toValue = toValue
            this.color.interpolator = interpolator
            return this
        }

        fun anchorArcTo(anchor: Anchor, root: MorphLayout, interpolator: TimeInterpolator? = null): Choreography{
            this.useArcTranslator = true
            return anchorTo(anchor, root, interpolator)
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

        fun withOffsetListener(offsetListener: TransitionOffsetListener): Choreography {
            this.offsetListener = offsetListener
            return this
        }

        fun withInterpolator(interpolator: TimeInterpolator?): Choreography {
            this.interpolator = interpolator
            return this
        }

        fun withDuration(duration: Long): Choreography {
            this.duration = duration
            return this
        }

        fun withStretch(stretch: Stretch): Choreography {
            this.stretch = stretch
            return this
        }

        fun withStartDelay(delay: Long): Choreography {
            this.delay = delay
            return this
        }

        fun withStagger(offset: Float): Choreography {
            return this
        }

        fun withStagger(delay: Long): Choreography {
            return this
        }

        fun whenDone(action: ChoreographerAction): Choreography {
            this.doneAction = action
            return this
        }

        fun onStart(action: ChoreographerAction): Choreography {
            this.startAction = action
            return this
        }

        fun withControlPoint(point: Coordinates): Choreography {
            return this
        }

        fun withInterval(time: Long): Choreography {
            return this
        }

        fun childStagger(stagger: Float): Choreography {
            return this
        }

        fun revealWith(reveal: Reveal): Choreography {
            return this
        }

        fun revealFrom(centerX: Float, centerY: Float): Choreography {
            return this
        }

        fun revealFrom(anchor: Anchor): Choreography {
            return this
        }

        fun thenReverse(): Choreography {
            this.reverse = true
            return this
        }

        private fun moveInRelationTo(){

        }

        fun animateAfter(offset: Float, vararg view: MorphLayout = this.views): Choreography {
            return choreographer.animateAfter(this, offset, *view)
        }

        fun thenAnimate(vararg view: MorphLayout = this.views): Choreography {
            return choreographer.thenAnimate(this, *view)
        }

        fun alsoAnimate(vararg view: MorphLayout = this.views): Choreography {
            return choreographer.alsoAnimate(this, *view)
        }

        fun andAnimate(vararg view: MorphLayout = this.views): Choreography {
            return choreographer.andAnimate(this, *view)
        }

        fun andAnimateAfter(offset: Float, vararg view: MorphLayout = this.views): Choreography {
            return choreographer.andAnimateAfter(this, offset, *view)
        }

        fun reverseAnimate(vararg view: MorphLayout = this.views): Choreography {
            return choreographer.reverseAnimate(this, *view)
        }

        fun andReverseAnimate(vararg view: MorphLayout = this.views): Choreography {
            return choreographer.andReverseAnimate(this, *view)
        }

        fun animateChildrenOf(view: MorphLayout, staggerOffset: Float): Choreography {
            return this
        }

        fun animateChildrenOf(view: MorphLayout, staggerDuration: Long): Choreography {
            return this
        }

        fun build(): Choreographer {
            return choreographer.build()
        }

        fun start() {
            if (choreographer.built) {
                choreographer.start()
            } else {
                choreographer.build().start()
            }
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
            translateXValues.copy(other.translateXValues)
            translateYValues.copy(other.translateYValues)
            pivotPoint.copy(other.pivotPoint)
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
            choreography.translateXValues.copy(this.translateXValues)
            choreography.translateYValues.copy(this.translateYValues)
            choreography.pivotPoint.copy(this.pivotPoint)
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
        fun resolveSize(type: Pivot, value: Float, size: Float, parentSize: Float): Float {
            return when (type) {
                Pivot.ABSOLUTE -> value
                Pivot.RELATIVE_TO_SELF -> size * value
                Pivot.RELATIVE_TO_PARENT -> parentSize * value
                Pivot.BASE_ON_PARENT -> parentSize * value
                Pivot.BASE_ON_VIEW -> parentSize * value
            }
        }
    }
}
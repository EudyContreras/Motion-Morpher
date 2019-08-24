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
import com.eudycontreras.motionmorpherlibrary.helpers.ArcTranslationHelper
import com.eudycontreras.motionmorpherlibrary.helpers.StretchAnimationHelper
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.layouts.MorphView
import com.eudycontreras.motionmorpherlibrary.observable.PropertyChangeObservable
import com.eudycontreras.motionmorpherlibrary.properties.*
import com.eudycontreras.motionmorpherlibrary.utilities.ColorUtility
import com.eudycontreras.motionmorpherlibrary.utilities.RevealUtility
import kotlin.math.abs

/**
 * Class which mangages and creates complex choreographies. The choreographer
 * can be use for sequencing animations in any way desired. It allows the building of
 * complex animation choreographies that can be stored and run at a later time.
 *
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 30 2019
 */
class Choreographer(context: Context): PropertyChangeObservable() {

    //TODO("Make the views held by this into weak references to avoid memory leaks")
    //TODO("Different animation durations for properties")

    private lateinit var headChoreography: Choreography
    private lateinit var tailChoreography: Choreography

    private var defaultInterpolator: TimeInterpolator? = null

    private var boundsChanged: Boolean = false

    private var defaultDuration: Long = MIN_DURATION

    private var built: Boolean = false

    /**
     * Returns an instance of the Morpher which can be used for
     * [Morpher] starting view into and from ending view.
     */
    val morpher: Morpher = Morpher(context)

    /**
     * The total duration which the [Choreographer] will take in order to
     * animate all the created choreographies.
     */
    var totalDuration: Long = MIN_DURATION
        private set

    private val handler: Handler by lazy {
        Handler()
    }

    private val arcTranslator: ArcTranslationHelper by lazy {
        ArcTranslationHelper()
    }

    private var translationXListener: ViewPropertyValueListener = { view, value ->
        view.morphTranslationX = value
    }

    private var translationYListener: ViewPropertyValueListener = { view, value ->
        view.morphTranslationY = value
    }

    /**
     * Assigns a default duration to use when the [Choreography] to animate has
     * no defined duration.
     * @param duration the default duration to use.
     */
    fun withDefaultDuration(duration: Long): Choreographer {
        this.defaultDuration = duration
        return this
    }

    /**
     * Assigns a default easing [TimeInterpolator] to use when the [Choreography] to animate has
     * no defined interpolator.
     * @param interpolator the default interpolator to use
     */
    fun withDefaultInterpolator(interpolator: TimeInterpolator): Choreographer {
        this.defaultInterpolator = interpolator
        return this
    }

    /**
     * Returns the property animator of the given [MorphLayout] in order to perform
     * stock property animations.
     * @param view The view to which the property animator belongs to
     * @return the animator to use for stock property animations.
     */
    fun animator(view: MorphLayout): ViewPropertyAnimator {
        return view.animator()
    }

    /**
     * Creates the initial head [Choreography] for the given [MorphLayout].
     * @param views the views to which the choreography belongs to
     * @return the created choreography.
     */
    fun animate(vararg views: MorphLayout): Choreography {
        this.headChoreography = Choreography(this, *views)
        this.headChoreography.offset = MAX_OFFSET
        return headChoreography
    }

    /**
     * Creates a [Choreography] for the given [MorphLayout]. With a specified parent choreography.
     * The choreography will play after the specified offset.
     * @param choreography the parent choreography of the current.
     * @param views the views to which the choreography belongs to.
     * @param offset the time offset to use. The current choreography will play after the
     * parent choreography has animated to the specified offset.
     * @return the created choreography.
     */
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

    /**
     * Creates a [Choreography] for the given [MorphLayout]. With a specified parent choreography.
     * The choreography will play after parent choreography is done animating.
     * @param choreography the parent choreography of the current.
     * @param views the views to which the choreography belongs to.
     * @return the created choreography.
     */
    internal fun thenAnimate(choreography: Choreography, vararg views: MorphLayout): Choreography {
        val properties = getProperties(choreography, *views)

        tailChoreography = Choreography(this, *views).apply {
            this.setStartProperties(properties)
            this.parent = choreography
            this.offset = MAX_OFFSET
            choreography.child = this
        }
        return tailChoreography
    }

    /**
     * Creates a [Choreography] for the given [MorphLayout]. With a specified parent choreography.
     * @param choreography the parent choreography of the current.
     * @param views the views to which the choreography belongs to.
     * @return the created choreography.
     */
    internal fun alsoAnimate(choreography: Choreography, vararg views: MorphLayout): Choreography {
        val properties = getProperties(choreography, *views)

        tailChoreography = Choreography(this, *views).apply {
            this.setStartProperties(properties)
            this.parent = choreography
            choreography.child = this
        }
        return tailChoreography
    }

    /**
     * Creates a [Choreography] for the given [MorphLayout]. With a specified parent choreography.
     * The choreography is a clone of the parent choreography and will play together with its parent
     * @param choreography the parent choreography of the current.
     * @param views the views to which the choreography belongs to.
     * @return the created choreography.
     */
    internal fun andAnimate(choreography: Choreography, vararg views: MorphLayout): Choreography {
        tailChoreography = choreography.clone(*views).apply {
            this.views = views
            this.parent = choreography
            this.child = null
            choreography.child = this
        }
        return tailChoreography
    }

    /**
     * Creates a [Choreography] for the given [MorphLayout]. With a specified parent choreography.
     * The choreography is a clone of the parent choreography and will play after the specified offset.
     * @param choreography the parent choreography of the current.
     * @param views the views to which the choreography belongs to.
     * @param offset the time offset to use. The current choreography will play after the
     * parent choreography has animated to the specified offset.
     * @return the created choreography.
     */
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

    /**
     * Reverse animates the specified choreography to its initial state for the specified views.
     * The reverse animation will occur after the parent animation is done animating.
     * @param choreography the parent choreography of the current.
     * @param views the views to which the choreography belongs to.
     * @return the created choreography.
     */
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
            this.offset = MAX_OFFSET
            this.reverseToStartState = true
            this.parent = choreography
            this.child = null
            choreography.child = this
        }
        return tailChoreography
    }

    /**
     * Reverse animates the specified [Choreography] to its initial state for the specified [MorphLayout].
     * The reverse animation will occur together with its parent.
     * @param choreography the parent choreography of the current.
     * @param views the views to which the choreography belongs to.
     * @return the created choreography.
     */
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

    /**
     * Creates and animation [Choreography] for the children of the specified [MorphLayout]. The
     * the animation can optionally play with a specified animation stagger.
     * @param stagger the stagger to for animating the children
     * @param views the views to which the choreography belongs to.
     * @return the created choreography.
     */
    fun animateChildrenOf(vararg views: MorphLayout, stagger: AnimationStagger? = null): Choreography {
        this.headChoreography = Choreography(this, *views)
        this.headChoreography.offset = MAX_OFFSET
        this.headChoreography.stagger = stagger
        return headChoreography
    }

    /**
     * Creates and animation [Choreography] for the children of the specified [MorphLayout]. The
     * the animation can optionally play with a specified animation stagger. The animation
     * will play after the specified offset.
     * @param stagger the stagger to for animating the children
     * @param views the views to which the choreography belongs to.
     * @param offset the time offset to use. The current choreography will play after the
     * parent choreography has animated to the specified offset.
     * @return the created choreography.
     */
    internal fun animateChildrenOfAfter(choreography: Choreography, offset: Float, stagger: AnimationStagger? = null, vararg views: MorphLayout): Choreography {
        val properties = getProperties(choreography, *views)

        tailChoreography =  Choreography(this, *views).apply {
            this.setStartProperties(properties)
            this.parent = choreography
            this.offset = offset
            this.stagger = stagger
            this.delay = (choreography.duration * offset).toLong()
            choreography.child = this
        }
        return tailChoreography
    }

    /**
     * Creates and animation [Choreography] for the children of the specified [MorphLayout]. The
     * the animation can optionally play with a specified animation stagger. The animation
     * will play after its parent.
     * @param stagger the stagger to for animating the children
     * @param views the views to which the choreography belongs to.
     * @return the created choreography.
     */
    internal fun thenAnimateChildrenOf(choreography: Choreography, stagger: AnimationStagger? = null, vararg views: MorphLayout): Choreography {
        val properties = getProperties(choreography, *views)

        tailChoreography = Choreography(this, *views).apply {
            this.setStartProperties(properties)
            this.parent = choreography
            this.offset = MAX_OFFSET
            this.stagger = stagger
            choreography.child = this
        }
        return tailChoreography
    }

    /**
     * Creates and animation [Choreography] for the children of the specified [MorphLayout]. The
     * the animation can optionally play with a specified animation stagger. The animation
     * will play together with its parent.
     * @param stagger the stagger to for animating the children
     * @param views the views to which the choreography belongs to.
     * @return the created choreography.
     */
    internal fun alsoAnimateChildrenOf(choreography: Choreography, stagger: AnimationStagger? = null, vararg views: MorphLayout): Choreography {
        val properties = getProperties(choreography, *views)

        tailChoreography = Choreography(this, *views).apply {
            this.setStartProperties(properties)
            this.parent = choreography
            this.stagger = stagger
            choreography.child = this
        }
        return tailChoreography
    }

    /**
     * Creates and animation [Choreography] for the children of the specified [MorphLayout]. The choreography is
     * a direct clone of its parent. The animation can optionally play with a specified animation stagger.
     * The animation will play together with its parent.
     * @param stagger the stagger to for animating the children
     * @param views the views to which the choreography belongs to.
     * @return the created choreography.
     */
    internal fun andAnimateChildrenOf(choreography: Choreography, stagger: AnimationStagger? = null, vararg views: MorphLayout): Choreography {
        tailChoreography = choreography.clone(*views).apply {
            this.views = views
            this.parent = choreography
            this.child = null
            this.stagger = stagger
            choreography.child = this
        }
        return tailChoreography
    }

    /**
     * Fetches a map of the properties for the given views. The passed [Choreography] is traversed from
     * bottom to top in order to find the choreography which belongs to specified [MorphLayout]. When the choreography
     * is found a map created containing all the animation properties.
     * @param choreography The choreography to traverse from.
     * @param views The views to which the choreography requested belongs to.
     * @return a map of the animation properties with their respective property name.
     */
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

    /**
     * Starts the choreographies of this [Choreographer] after the specified delay.
     * @param delay The amount delay to wait for before the animations start.
     * @see start
     */
    fun startAfter(delay: Long) {
        handler.postDelayed({
            start()
        }, delay)
    }

    /**
     * Starts the choreographies of this [Choreographer].
     */
    fun start() {
        successors(headChoreography) { _, choreography ->
            choreography.control.cancel()
            choreography.control.start()
        }
    }

    /**
     * Starts the animation for the specified [Choreography].
     * @param choreography The choreography which is to be started/play.
     */
    fun startFor(choreography: Choreography) {
        successors(headChoreography) { _, _choreography ->
            if (_choreography == choreography) {
                choreography.control.cancel()
                choreography.control.start()
            }
        }
    }

    /**
     * Clears the choreographies for this [Choreographer].
     * @return the choreographer
     */
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

    /**
     * Resets all the choreographies for this [Choreographer].
     * @return the choreographer
     */
    fun reset(): Choreographer {
        predecessors(tailChoreography) { _, choreography ->
            choreography.resetProperties()
        }
        return this
    }

    /**
     * Resets all the [Choreography] for the specified [MorphLayout]
     * @param view The view which will have its choreography reset.
     * @return the choreographer
     */
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

    /**
     * Resets the [Choreography] for the specified [MorphLayout] with the specified duration.
     * @param view the view to which the reset choreography belongs to
     * @param duration the duration the for the animaation reset
     * @return the choreographer being used
     */
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

    /**
     * Helper method used for traversing the predecessors of the given [Choreography].
     * @param choreography The [Choreography] to traverse.
     * @param iterator The iterator which gives access to the traversal control
     * and the current choreography being traversed.
     */
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

    /**
     * Helper method used for traversing the successors of the given [Choreography].
     * @param choreography The [Choreography] to traverse.
     * @param iterator The iterator which gives access to the traversal control
     * and the current choreography being traversed.
     */
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

    /**
     * Builds the [Choreographer] by applying the desired values of each [Choreography]
     * and prepares the choreographies to be played. The build process is called prior
     * to the start of the choreographer. This process allows for the heavy process of building
     * a choreography to be done prior to the point at which it will be played.
     * @return The choreographer being used.
     */
    internal fun build(): Choreographer {

        var totalDuration: Long = MIN_DURATION
        var totalDelay: Long = MIN_DURATION

        var temp: Choreography? = headChoreography

        while (temp != null) {
            val current = temp

            applyInterpolators(current)
            applyMultipliers(current)
            applyReveal(current)
            applyAdders(current)

            totalDuration += (current.duration.toFloat() * (current.child?.offset?: MAX_OFFSET)).toLong()
            totalDelay += ((current.parent?.duration?: MIN_DURATION).toFloat() * current.offset).toLong()
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

    /**
     * Applies the reveal for the specified choreography. The reveal
     * is applied to the [Choreography] if it contains a specified [Reveal].
     * @param choreography The choreography to which its reveal is applied to.
     */
    private fun applyReveal(choreography: Choreography) {
        choreography.reveal?.let {
            it.duration = choreography.duration
            it.interpolator = it.interpolator ?: choreography.interpolator
        }
    }

    /**
     * Applies the easing [TimeInterpolator] for the specified [Choreography]. The interpolator
     * is applied the *Choreography* if it contains a specified Interpolator. If the *Choreography*
     * @param choreography The choreography to which its reveal is applied to.
     */
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

    /**
     * Applies the multiplies for [AnimatedValue] toValue property for each of the animation properties
     * of the specified [Choreography].
     * @param choreography The choreography to which its multipliers are applied to.
     */
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

    /**
     * Applies the adders for [AnimatedValue] toValue property for each of the animation properties
     * of the specified [Choreography].
     * @param choreography The choreography to which its adders are applied to.
     */
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

    /**
     * Animates the specified [Choreography] to specified animation fraction. The total duration
     * and the current playtime must be known.
     * @param choreography The choreography to be animated.
     * @param fraction The fraction to animate to.
     * @param duration The duration of the animation.
     * @param currentPlayTime The current playtime of the animation.
     */
    private fun animate(choreography: Choreography, fraction: Float, duration: Long, currentPlayTime: Long) {
        val views = choreography.views

        if (views.size == 1) {
            animate(views[0], choreography, fraction, duration, currentPlayTime)
            return
        }

        for (view in views) {
            animate(view, choreography, fraction, duration, currentPlayTime)
        }
    }

    /**
     * Animates the specified [MorphLayout] with the specified [Choreography] and to specified animation fraction.
     * The total duration and the current playtime must be known. Each property is animated using its respective from and to
     * values and the specified [TimeInterpolator] if any has been specified. This is the function where the animation
     * happens.
     * @param view The morph layout to be animated
     * @param choreography The choreography to be animated.
     * @param fraction The fraction to animate to.
     * @param duration The duration of the animation.
     * @param currentPlayTime The current playtime of the animation.
     */
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

                val controlX = choreography.controlPoint?.x ?: choreography.positionX.toValue
                val controlY = choreography.controlPoint?.y ?: choreography.positionY.fromValue

                val arcTranslationX = arcTranslator.getCurvedTranslationX(positionXFraction, choreography.positionX.fromValue, choreography.positionX.toValue, controlX)
                val arcTranslationY = arcTranslator.getCurvedTranslationY(positionYFraction, choreography.positionY.fromValue, choreography.positionY.toValue, controlY)

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
                val controlX = choreography.controlPoint?.x ?: choreography.positionX.toValue
                val controlY = choreography.controlPoint?.y ?: choreography.positionY.fromValue

                val arcTranslationX = arcTranslator.getCurvedTranslationX(translateXFraction, choreography.translateX.fromValue, choreography.translateX.toValue, controlX)
                val arcTranslationY = arcTranslator.getCurvedTranslationY(translateYFraction, choreography.translateY.fromValue, choreography.translateY.toValue, controlY)

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

        if (fraction >= MAX_OFFSET) {
            choreography.done = true
            choreography.doneAction?.invoke(choreography)
        }

        if (boundsChanged) {
            view.updateLayout()
        }

        boundsChanged = false
    }

    /**
     * Animates the specified [MorphLayout] with the values specified on the  [AnimatedFloatValueArray] to specified animation playtime.
     * The total duration must be known and a [ViewPropertyValueListener] is used in order to notify the progression of the values.
     * @param valueHolder The value holder containing the values to animate between
     * @param view The morph layout to animate
     * @param playTime The current playtime of the animation.
     * @param duration The duration of the animation.
     * @param listener The value progression listener
     */
    private fun animateThroughPoints(valueHolder: AnimatedFloatValueArray, view: MorphLayout, playTime: Long, duration: Long, listener: ViewPropertyValueListener) {
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

    /**
     * Class which creates a choreography progression listener.
     * The listener receives notifications upon the end, start and reversal of
     * a choreography animation.
     * @param fractionListener The progression listener
     * @param endListener The listener notified when the animation ends.
     * @param startListener The listener notified when the animation starts.
     * @param reverseListener The listener notified when the animation reverses.
     */
    data class ChoreographyListener(
        var fractionListener: ((remainingTime: Long, fraction: Float) -> Unit)? = null,
        var endListener: Action = null,
        var startListener: Action = null,
        var reverseListener: Action = null
    )

    /**
     * Class containing information about the properties used for animating a [Choreography]
     * Each choreography uses an animation control for its core animation.
     * @param choreography The choreography to which the control belongs to.
     * @param startOffset The offset to which control will start the animation of the choreography
     * @param endOffset The offset to which control will end the animation of the choreography
     */
    class ChoreographyControl(
        internal var choreography: Choreography,
        internal var startOffset: Float,
        internal var endOffset: Float
    ) {

        internal var repeatCount: Int = 0
        internal var repeatMode: Int = RESTART
        internal var mDuration: Long = MIN_DURATION
        internal var mStartDelay: Long = MIN_DURATION

        internal lateinit  var endListener: (Animator) -> Unit
        internal lateinit var startListener: (Animator) -> Unit

        internal lateinit var updateListener: ValueAnimator.AnimatorUpdateListener

        internal var animator = ValueAnimator.ofFloat(startOffset, endOffset)

        /**
         * Pauses the animation used by this control.
         */
        fun pause() {
            animator.pause()
        }

        /**
         * Resumes the animation used by this control.
         */
        fun resume() {
            animator.resume()
        }

        /**
         * Cancels the animation used by this control.
         */
        fun cancel() {
            animator.cancel()
        }

        /**
         * Builds the animation used by this control.
         */
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

        /**
         * Reverses the animation used by this control with
         * the specified duration.
         * @param duration The total duration the reversal animation will last.
         */
        fun reverse(duration: Long?) {
            animator  = ValueAnimator.ofFloat(MAX_OFFSET, MIN_OFFSET)

            animator.addListener( onEnd = endListener)

            animator.duration = duration ?: this.mDuration
            animator.addUpdateListener(this.updateListener)
            animator.start()
        }

        /**
         * Starts the animation used by this control and returns
         * and instance of itself.
         * @return This [ChoreographyControl]
         */
        internal fun start(): ChoreographyControl {
            choreography.reveal?.let {
                RevealUtility.circularReveal(
                    centerX = it.centerX.toInt(),
                    centerY = it.centerY.toInt(),
                    startRadius = it.radiusStart,
                    resultView = it.view,
                    interpolator = it.interpolator,
                    duration = mDuration,
                    startDelay = mStartDelay
                )
            }
            animator.start()
            return this
        }

        /**
         * Returns the total duration of the animation of this control.
         * @return the animation duration in milliseconds
         */
        fun getTotalDuration(): Long {
            return mDuration
        }

        /**
         * Returns the remaining duration of the animation of this control.
         * @return the remaining animation duration in milliseconds
         */
        fun getRemainingDuration(): Long  {
            return mDuration - animator.currentPlayTime
        }

        /**
         * Returns the total time passed of the animation of this control.
         * @return the animation time passed in milliseconds
         */
        fun getElapsedDuration(): Long  {
            return animator.currentPlayTime
        }

        /**
         * Returns the the start delay of the animation of this control.
         * @return the animation start delay in milliseconds
         */
        fun getStartDelay(): Long  {
            return mStartDelay
        }

        companion object {
            const val RESTART = 1
            const val REVERSE = 2
            const val INFINITE = -1
        }
    }

    /**
     * Class which holds all the data on how to animate a [MorphLayout] in order
     * successfully create the desired animation sequence. The choreography is gives
     * its [Choreographer] instructions on how to animated and holds values for animateable
     * properties a the specified views.
     * @param choreographer The [Choreographer] used for animating this choreography
     * @param views the views which this choreography will animate. This assumes the premise that
     * all views have similar layout properties.
     */
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

        internal var delay: Long = MIN_DURATION
        internal var offset: Float = MIN_OFFSET
        internal var interval: Long = MIN_DURATION
        internal var duration: Long = MIN_DURATION

        internal var pivotValueX: Float = 0.5f
        internal var pivotValueY: Float = 0.5f

        internal var fractionOffsetStart: Float = MIN_OFFSET
        internal var fractionOffsetEnd: Float = MAX_OFFSET

        internal var pivotValueTypeX: Pivot = Pivot.RELATIVE_TO_SELF
        internal var pivotValueTypeY: Pivot = Pivot.RELATIVE_TO_SELF

        internal val scaleX: AnimatedFloatValue = AnimatedFloatValue(AnimatedValue.SCALE_X, MAX_OFFSET, MAX_OFFSET)
        internal val scaleY: AnimatedFloatValue = AnimatedFloatValue(AnimatedValue.SCALE_Y, MAX_OFFSET, MAX_OFFSET)

        internal val rotation: AnimatedFloatValue = AnimatedFloatValue(AnimatedValue.ROTATION, MIN_OFFSET, MIN_OFFSET)
        internal val rotationX: AnimatedFloatValue = AnimatedFloatValue(AnimatedValue.ROTATION_X, MIN_OFFSET, MIN_OFFSET)
        internal val rotationY: AnimatedFloatValue = AnimatedFloatValue(AnimatedValue.ROTATION_Y, MIN_OFFSET, MIN_OFFSET)

        internal val positionX: AnimatedFloatValue = AnimatedFloatValue(AnimatedValue.POSITION_X, MIN_OFFSET, MIN_OFFSET)
        internal val positionY: AnimatedFloatValue = AnimatedFloatValue(AnimatedValue.POSITION_Y, MIN_OFFSET, MIN_OFFSET)

        internal val translateX: AnimatedFloatValue = AnimatedFloatValue(AnimatedValue.TRANSLATION_X, MIN_OFFSET, MIN_OFFSET)
        internal val translateY: AnimatedFloatValue = AnimatedFloatValue(AnimatedValue.TRANSLATION_Y, MIN_OFFSET, MIN_OFFSET)
        internal val translateZ: AnimatedFloatValue = AnimatedFloatValue(AnimatedValue.TRANSLATION_Z, MIN_OFFSET, MIN_OFFSET)

        internal val width: AnimatedFloatValue = AnimatedFloatValue(AnimatedValue.WIDTH, MIN_OFFSET, MIN_OFFSET)
        internal val height: AnimatedFloatValue = AnimatedFloatValue(AnimatedValue.HEIGHT, MIN_OFFSET, MIN_OFFSET)

        internal val alpha: AnimatedFloatValue = AnimatedFloatValue(AnimatedValue.ALPHA, MAX_OFFSET, MAX_OFFSET)

        internal val color: AnimatedIntValue = AnimatedIntValue(AnimatedValue.COLOR, DEFAULT_COLOR, DEFAULT_COLOR)

        internal val cornerRadii: AnimatedValue<CornerRadii> = AnimatedValue.instance(AnimatedValue.CORNERS, CornerRadii(), CornerRadii())

        internal val paddings: AnimatedValue<Paddings> = AnimatedValue.instance(AnimatedValue.PADDING, Paddings(), Paddings())
        internal val margings: AnimatedValue<Margings> = AnimatedValue.instance(AnimatedValue.MARGIN, Margings(), Margings())

        internal var translateXValues: AnimatedFloatValueArray = AnimatedFloatValueArray(AnimatedValue.TRANSLATION_X)
        internal var translateYValues: AnimatedFloatValueArray = AnimatedFloatValueArray(AnimatedValue.TRANSLATION_Y)

        internal var offsetListener: TransitionOffsetListener = null

        internal var viewParentSize: Dimension = Dimension()

        internal val pivotPoint: Coordinates = Coordinates()

        internal var controlPoint: Coordinates? = null

        internal var doneAction: ChoreographerAction = null
        internal var startAction: ChoreographerAction = null

        internal var interpolator: TimeInterpolator? = null

        internal var stagger: AnimationStagger? = null

        internal var stretch: Stretch? = null

        internal var reveal: Reveal? = null

        lateinit var control: ChoreographyControl
        internal var parent: Choreography? = null
        internal var child: Choreography? = null

        init {
            applyDefaultValues()
        }

        /***
         * A map of the properties or [AnimatedValue] of this [Choreography]
         * @return a map containing of the properties accessed by their respective names.
         */
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

        /**
         * A map containing of property sets or [AnimatedValueArray]
         * @return a map containing of the properties sets accessed by their respective names.
         */
        internal fun propertyHolders(): Map<String, AnimatedValueArray<*>> {
            val properties: HashMap<String, AnimatedValueArray<*>> = HashMap()
            properties[translateX.propertyName] = translateXValues
            properties[translateY.propertyName] = translateYValues
            return properties
        }

        /**
         * Sets the values for all properties using the given map of properties.
         * @param properties Map of all the properties to be set.
         */
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

        /**
         * Sets the values for all properties sets using the given map of properties sets.
         * @param properties Map of all the properties sets to be set.
         */
        @Suppress("UNCHECKED_CAST")
        internal fun setStartPropertyHolders(properties: Map<String, AnimatedValueArray<*>>?) {
            if (properties == null)
                return

            translateXValues.values = properties.getValue(translateXValues.propertyName).values as Array<Float>
            translateYValues.values = properties.getValue(translateXValues.propertyName).values as Array<Float>
        }

        /**
         * Flips the from and to values of each of the animation properties.
         */
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

        /**
         * Applies the default values for each of the animation properties.
         * the values are assigned using the values of all the animateable properties
         * of the views to be animated by this [Choreography]
         */
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

        /**
         * Resets each of the views held by this [Choreography] to their initial
         * pre animation state.
         */
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

        // NEEDS TESTING
        /**
         * Animates the views of this [Choreography] to the left of the specified [MorphLayout] with
         * the specified margin. Optionally uses the specified [TimeInterpolator] if any is present.
         * @param otherView The layout to which the choreography will animate its views to the left of
         * @param margin The margin to use between the choreography views and the specified layout.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun toLeftOf(otherView: MorphLayout, margin: Float = MIN_OFFSET, interpolator: TimeInterpolator? = null): Choreography {
            val view = views[0]
            val startX: Float = view.windowLocationX.toFloat()
            val startY: Float = view.windowLocationY.toFloat()

            val endX: Float = otherView.windowLocationX.toFloat() - (margin + view.morphWidth)
            val endY: Float = otherView.windowLocationY.toFloat()

            val differenceX: Float = endX - startX
            val differenceY: Float = endY - startY

            this.translateX.toValue = differenceX
            this.translateY.toValue = differenceY

            this.translateX.interpolator = interpolator
            this.translateY.interpolator = interpolator

            return this
        }

        // NEEDS TESTING
        /**
         * Animates the views of this [Choreography] to the right of the specified [MorphLayout] with
         * the specified margin. Optionally uses the specified [TimeInterpolator] if any is present.
         * @param otherView The layout to which the choreography will animate its views to the right of
         * @param margin The margin to use between the choreography views and the specified layout.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun toRightOf(otherView: MorphView, margin: Float = MIN_OFFSET, interpolator: TimeInterpolator? = null): Choreography {
            val view = views[0]
            val startX: Float = view.windowLocationX.toFloat()
            val startY: Float = view.windowLocationY.toFloat()

            val endX: Float = otherView.windowLocationX.toFloat() + (margin + view.morphHeight)
            val endY: Float = otherView.windowLocationY.toFloat()

            val differenceX: Float = endX - startX
            val differenceY: Float = endY - startY

            this.translateX.toValue = differenceX
            this.translateY.toValue = differenceY

            this.translateX.interpolator = interpolator
            this.translateY.interpolator = interpolator

            return this
        }

        // NEEDS TESTING
        /**
         * Animates the views of this [Choreography] to the top of the specified [MorphLayout] with
         * the specified margin. Optionally uses the specified [TimeInterpolator] if any is present.
         * @param otherView The layout to which the choreography will animate its views to the top of
         * @param margin The margin to use between the choreography views and the specified layout.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun toTopOf(otherView: MorphView, margin: Float = MIN_OFFSET, interpolator: TimeInterpolator? = null): Choreography {
            val view = views[0]
            val startX: Float = view.windowLocationX.toFloat()
            val startY: Float = view.windowLocationY.toFloat()

            val endX: Float = otherView.windowLocationX.toFloat()
            val endY: Float = otherView.windowLocationY.toFloat() - (margin + view.morphHeight)

            val differenceX: Float = endX - startX
            val differenceY: Float = endY - startY

            this.translateX.toValue = differenceX
            this.translateY.toValue = differenceY

            this.translateX.interpolator = interpolator
            this.translateY.interpolator = interpolator

            return this
        }

        // NEEDS TESTING
        /**
         * Animates the views of this [Choreography] to the bottm of the specified [MorphLayout] with
         * the specified margin. Optionally uses the specified [TimeInterpolator] if any is present.
         * @param otherView The layout to which the choreography will animate its views to the bottom of
         * @param margin The margin to use between the choreography views and the specified layout.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun toBottomOf(otherView: MorphView, margin: Float = MIN_OFFSET, interpolator: TimeInterpolator? = null): Choreography {
            val view = views[0]
            val startX: Float = view.windowLocationX.toFloat()
            val startY: Float = view.windowLocationY.toFloat()

            val endX: Float = otherView.windowLocationX.toFloat()
            val endY: Float = otherView.windowLocationY.toFloat() + (margin + view.morphHeight)

            val differenceX: Float = endX - startX
            val differenceY: Float = endY - startY

            this.translateX.toValue = differenceX
            this.translateY.toValue = differenceY

            this.translateX.interpolator = interpolator
            this.translateY.interpolator = interpolator

            return this
        }

        /**
         * Animates the views of this [Choreography] to the specified bounds.
         * @param bounds The bounds to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
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

        /**
         * Animates the views of this [Choreography] to the specified x position.
         * @param positionX the position to which the X position value is to be animated to
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun xPositionTo(positionX: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.positionX.toValue = positionX
            this.positionX.interpolator = interpolator
            return this
        }

        /**
         * Animates the views of this [Choreography] to the specified X position from the specified X position.
         * @param fromValue the position from which the X position value is to be animated from
         * @param toValue the position to which the X position value is to be animated to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun xPositionFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.positionX.fromValue = fromValue
            this.positionX.toValue = toValue
            this.positionX.interpolator = interpolator
            return this
        }

        /**
         * Animates the views of this [Choreography] to the specified X position value property
         * @param value The property to use for animating the X value of this choreography
         * @return this choreography.
         */
        fun xPosition(value: AnimatedFloatValue): Choreography {
            this.positionX.copy(value)
            return this
        }

        /**
         * Animates the views of this [Choreography] to the specified Y position.
         * @param positionY the position to which the Y position value is to be animated to
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun yPositionTo(positionY: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.positionY.toValue = positionY
            this.positionY.interpolator = interpolator
            return this
        }

        /**
         * Animates the views of this [Choreography] to the specified Y position from the specified Y position.
         * @param fromValue the position from which the Y position value is to be animated from
         * @param toValue the position to which the Y position value is to be animated to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun yPositionFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.positionY.fromValue = fromValue
            this.positionY.toValue = toValue
            this.positionY.interpolator = interpolator
            return this
        }

        /**
         * Animates the views of this [Choreography] to the specified Y position value property.
         * The position.
         * @param value The property to use for animating the Y value of this choreography
         * @return this choreography.
         */
        fun yPosition(value: AnimatedFloatValue): Choreography {
            this.positionY.copy(value)
            return this
        }

        /**
         * Animates the views of this [Choreography] to the specified X translation.
         * @param translationX the position to which the X translation value is to be animated to
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun xTranslateTo(translationX: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.translateX.toValue = translationX
            this.translateX.interpolator = interpolator
            return this
        }

        /**
         * Animates the views of this [Choreography] to the specified X translation from the specified X translation.
         * @param fromValue the position from which the X translation value is to be animated from
         * @param toValue the position to which the X translation value is to be animated to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun xTranslateFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.translateX.fromValue = fromValue
            this.translateX.toValue = toValue
            this.translateX.interpolator = interpolator
            return this
        }

        /**
         * Animates the views of this [Choreography] to the specified X translation value property
         * @param value The property to use for animating the X value of this choreography
         * @return this choreography.
         */
        fun xTranslate(value: AnimatedFloatValue): Choreography {
            this.translateX.copy(value)
            return this
        }

        /**
         * Animates the views of this [Choreography] to the specified Y translation.
         * @param translationY the position to which the Y translation value is to be animated to
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun yTranslateTo(translationY: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.translateY.toValue = translationY
            this.translateY.interpolator = interpolator
            return this
        }

        /**
         * Animates the views of this [Choreography] to the specified Y translation from the specified Y translation.
         * @param fromValue the position from which the Y translation value is to be animated from
         * @param toValue the position to which the Y translation value is to be animated to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun yTranslateFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.translateY.fromValue = fromValue
            this.translateY.toValue = toValue
            this.translateY.interpolator = interpolator
            return this
        }

        /**
         * Animates the views of this [Choreography] to the specified Y translation value property
         * @param value The property to use for animating the Y value of this choreography
         * @return this choreography.
         */
        fun yTranslate(value: AnimatedFloatValue): Choreography {
            this.translateY.copy(value)
            return this
        }

        /**
         * Animates the views of this [Choreography] to the specified Z translation.
         * @param translationZ the position to which the Z translation value is to be animated to
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun zTranslateTo(translationZ: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.translateZ.toValue = translationZ
            this.translateZ.interpolator = interpolator
            return this
        }

        /**
         * Animates the views of this [Choreography] to the specified Z translation from the specified Z translation.
         * @param fromValue the position from which the Z translation value is to be animated from
         * @param toValue the position to which the Z translation value is to be animated to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun zTranslateFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.translateZ.fromValue = fromValue
            this.translateZ.toValue = toValue
            this.translateZ.interpolator = interpolator
            return this
        }

        /**
         * Animates the views of this [Choreography] to the specified Z translation value property
         * @param value The property to use for animating the Z value of this choreography
         * @return this choreography.
         */
        fun zTranslate(value: AnimatedFloatValue): Choreography {
            this.translateZ.copy(value)
            return this
        }

        /**
         * Animates the views of this [Choreography] to the specified coordinates using arc translation
         * The control point is auto calculated if no control point has been specified.
         * @param coordinates The coordinates to arc translate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography
         */
        fun arcTranslateTo(coordinates: Coordinates, interpolator: TimeInterpolator? = null): Choreography {
            return arcTranslateTo(coordinates.x, coordinates.y, interpolator)
        }
        /**
         * Animates the views of this [Choreography] to the specified X and Y translation values using arc translation
         * The control point is auto calculated if no control point has been specified.
         * @param translationX The x translation amount to arc translate to.
         * @param translationY The x translation amount to arc translate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography
         */
        fun arcTranslateTo(translationX: Float, translationY: Float, interpolator: TimeInterpolator? = null): Choreography {
            translateX.toValue = translationX
            translateY.toValue = translationY
            translateX.interpolator = interpolator
            translateY.interpolator = interpolator
            useArcTranslator = true
            return this
        }

        /**
         * Animates the views of this [Choreography] between the specified points see: [FloatPoint].
         * Uses the default interpolator if any is present
         * @return this choreography
         */
        fun translateBetween(vararg values: FloatPoint): Choreography {
            translateXValues.values = values.map { it.x }.toTypedArray()
            translateYValues.values = values.map { it.y }.toTypedArray()
            translateXValues.interpolator = interpolator
            translateYValues.interpolator = interpolator
            return this
        }

        /**
         * Animates the views of this [Choreography] between the specified points see: [FloatPoint].
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography
         */
        fun translateBetween(interpolator: TimeInterpolator? = null, vararg values: FloatPoint): Choreography {
            translateXValues.values = values.map { it.x }.toTypedArray()
            translateYValues.values = values.map { it.y }.toTypedArray()
            translateXValues.interpolator = interpolator
            translateYValues.interpolator = interpolator
            return this
        }

        /**
         * Animates the views of this [Choreography] between the X translation values created
         * by mapping to the specified percentages. Int based percentages are used.
         * Ex: 0%, 50%, 120%
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography
         */
        fun xTranslateBetween(value: Float, percentages: IntArray, interpolator: TimeInterpolator? = null): Choreography {
            val mapped = percentages.map { it / 100f }.toTypedArray().toFloatArray()
            return xTranslateBetween(value, mapped, interpolator)
        }

        /**
         * Animates the views of this [Choreography] between the X translation values created
         * by mapping to the specified percentages. Float based percentages are used where
         * 0.5f equals 50% and so on.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography
         */
        fun xTranslateBetween(value: Float, percentages: FloatArray, interpolator: TimeInterpolator? = null): Choreography {
            val output: ArrayList<Float> = ArrayList()
            for (percentage in percentages) {
                output.add(value * percentage)
            }
            translateXValues.interpolator = interpolator
            translateXValues.values = output.toTypedArray()
            return this
        }

        /**
         * Animates the views of this [Choreography] between the specified X translation values.
         * Uses the default interpolator if any is present.
         * @param values the values to translate between.
         * @return this choreography.
         */
        fun xTranslateBetween(vararg values: Float): Choreography {
            translateXValues.values = values.toTypedArray()
            return this
        }

        /**
         * Animates the views of this [Choreography] between the specified X translation values.
         * @param values the values to translate between.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun xTranslateBetween(interpolator: TimeInterpolator? = null, vararg values: Float): Choreography {
            translateXValues.values = values.toTypedArray()
            translateXValues.interpolator = interpolator
            return this
        }

        /**
         * Animates the views of this [Choreography] between the Y translation values created
         * by mapping to the specified percentages. Int based percentages are used.
         * Ex: 0%, 50%, 120%
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography
         */
        fun yTranslateBetween(value: Float, percentages: IntArray, interpolator: TimeInterpolator? = null): Choreography {
            val mapped = percentages.map { it / 100f }.toTypedArray().toFloatArray()
            return yTranslateBetween(value, mapped, interpolator)
        }

        /**
         * Animates the views of this [Choreography] between the Y translation values created
         * by mapping to the specified percentages. Float based percentages are used where
         * 0.5f equals 50% and so on.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography
         */
        fun yTranslateBetween(value: Float, percentages: FloatArray, interpolator: TimeInterpolator? = null): Choreography {
            val output: ArrayList<Float> = ArrayList()
            for (percentage in percentages) {
                output.add(value * percentage)
            }
            translateYValues.interpolator = interpolator
            translateYValues.values = output.toTypedArray()
            return this
        }

        /**
         * Animates the views of this [Choreography] between the specified Y translation values.
         * Uses the default interpolator if any is present.
         * @param values the values to translate between.
         * @return this choreography.
         */
        fun yTranslateBetween(vararg values: Float): Choreography {
            translateYValues.values = values.toTypedArray()
            return this
        }

        /**
         * Animates the views of this [Choreography] between the specified Y translation values.
         * @param values the values to translate between.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun yTranslateBetween(interpolator: TimeInterpolator? = null, vararg values: Float): Choreography {
            translateYValues.values = values.toTypedArray()
            translateYValues.interpolator = interpolator
            return this
        }

        /**
         * Animates the alpha value of the views of this [Choreography] to the specified alpha value.
         * @param alpha The alpha value to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun alphaTo(alpha: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.alpha.toValue = alpha
            this.alpha.interpolator = interpolator
            return this
        }

        /**
         * Animates the alpha value of the views of this [Choreography] from the specified alpha value
         * to the specified alpha value.
         * @param fromValue The alpha value to animate from.
         * @param toValue The alpha value to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun alphaFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.alpha.fromValue = fromValue
            this.alpha.toValue = toValue
            this.alpha.interpolator = interpolator
            return this
        }

        /**
         * Animates the alpha value of the views of this [Choreography] to the specified alpha value.
         * The alpha value is specified as a percentage where 50 is 50 percent opacity
         * @param alpha The alpha value to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun alphaTo(alpha: Int, interpolator: TimeInterpolator? = null): Choreography {
            return this.alphaTo(alpha.clamp(0, 100) / 100f, interpolator)
        }

        /**
         * Animates the alpha value of the views of this [Choreography] from the specified alpha value
         * to the specified alpha value. The alpha value is specified as a percentage where 50 is 50 percent opacity.
         * @param fromValue The alpha value to animate from.
         * @param toValue The alpha value to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun alphaFrom(fromValue: Int, toValue: Int, interpolator: TimeInterpolator? = null): Choreography {
            return this.alphaFrom(fromValue.clamp(0, 100) / 100f, toValue.clamp(0, 100) / 100f, interpolator)
        }

        /**
         * Animates the alpha value of the views of this [Choreography] using the specified animated
         * alpha value property. See [AnimatedFloatValue]
         * @param value The property to use for this animation.
         * @return this choreography.
         */
        fun alpha(value: AnimatedFloatValue): Choreography {
            this.alpha.copy(value)
            return this
        }

        /**
         * Animates the corner radius of the views of this [Choreography] to the specified [CornerRadii].
         * @param corners The corner radius value to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun cornerRadiusTo(corners: CornerRadii, interpolator: TimeInterpolator? = null): Choreography {
            cornerRadii.interpolator = interpolator
            cornerRadii.toValue = corners
            return this
        }

        /**
         * Animates the corner radius of the specified corners of the views of this [Choreography] to the specified value.
         * @param corner The corner which value is to be animated.
         * @param radius The radius to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
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

        /**
         * Animates the corner radius of the specified corners of the views of this [Choreography] from the specified value.
         * to the specified value
         * @param corner The corner which value is to be animated.
         * @param fromValue The radius to animate from.
         * @param toValue The radius to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
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

        /**
         * Animates the corner radius of the specified corners of the views of this [Choreography] from the specified value.
         * to the specified value
         * @param cornerValue The corner property to use fo this animation.
         * @return this choreography.
         */
        fun cornerRadius(cornerValue: AnimatedValue<CornerRadii>): Choreography {
            cornerRadii.copy(cornerValue)
            return this
        }

        /**
         * Animates the views of this [Choreography] to the computed rotation value created
         * by adding the specified delta to the current rotation value. This causes the rotation value
         * to be increased/decreased with the specified amount.
         * @param delta The amount to add to the current rotation value
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun addRotation(delta: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotation.add = delta
            this.rotation.interpolator = interpolator
            return this
        }

        /**
         * Animates the views of this [Choreography] to the computed rotation value created
         * by multiplying the specified multiplier with the current rotation value. This causes the rotation value
         * to be increased/decreased with the specified amount.
         * @param multiplier The amount to multiply the current rotation value by
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun rotateBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotation.multiply = multiplier
            this.rotation.interpolator = interpolator
            return this
        }

        /**
         * Animates the rotation value of the views of this [Choreography] to the specified rotation value.
         * @param rotation The rotation value to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun rotateTo(rotation: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotation.toValue = rotation
            this.rotation.interpolator = interpolator
            return this
        }

        /**
         * Animates the rotation value of the views of this [Choreography] from the specified rotation value
         * to the specified rotation value
         * @param fromValue The rotation value to animate from
         * @param toValue The rotation value to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun rotateFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotation.fromValue = fromValue
            this.rotation.toValue = toValue
            this.rotation.interpolator = interpolator
            return this
        }

        /**
         * Animates the rotation value of the views of this [Choreography] using the specified animated
         * rotation value property. See [AnimatedFloatValue]
         * @param value The property to use for this animation.
         * @return this choreography.
         */
        fun rotate(value: AnimatedFloatValue): Choreography {
            this.rotation.copy(value)
            return this
        }

        /**
         * Animates the views of this [Choreography] to the computed X rotation value created
         * by adding the specified delta to the current X rotation value. This causes the X rotation value
         * to be increased/decreased with the specified amount.
         * @param delta The amount to add to the current X rotation value
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun xRotateAdd(delta: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationX.add = delta
            this.rotationX.interpolator = interpolator
            return this
        }

        /**
         * Animates the views of this [Choreography] to the computed X rotation value created
         * by multiplying the specified multiplier with the current X rotation value. This causes the X rotation value
         * to be increased/decreased with the specified amount.
         * @param multiplier The amount to multiply the current X rotation value by
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun xRotateBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationX.multiply = multiplier
            this.rotationX.interpolator = interpolator
            return this
        }

        /**
         * Animates the X rotation value of the views of this [Choreography] to the specified X rotation value.
         * @param rotationX The X rotation value to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun xRotateTo(rotationX: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationX.toValue = rotationX
            this.rotationX.interpolator = interpolator
            return this
        }

        /**
         * Animates the X rotation value of the views of this [Choreography] from the specified X rotation value
         * to the specified X rotation value
         * @param fromValue The X rotation value to animate from
         * @param toValue The X rotation value to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun xRotateFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationX.fromValue = fromValue
            this.rotationX.toValue = toValue
            this.rotationX.interpolator = interpolator
            return this
        }

        /**
         * Animates the X rotation value of the views of this [Choreography] using the specified animated
         * X rotation value property. See [AnimatedFloatValue]
         * @param value The property to use for this animation.
         * @return this choreography.
         */
        fun xRotate(value: AnimatedFloatValue): Choreography {
            this.rotationX.copy(value)
            return this
        }

        /**
         * Animates the views of this [Choreography] to the computed Y rotation value created
         * by adding the specified delta to the current Y rotation value. This causes the Y rotation value
         * to be increased/decreased with the specified amount.
         * @param delta The amount to add to the current Y rotation value
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun yRotateAdd(delta: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationY.add = delta
            this.rotationY.interpolator = interpolator
            return this
        }

        /**
         * Animates the views of this [Choreography] to the computed Y rotation value created
         * by multiplying the specified multiplier with the current Y rotation value. This causes the Y rotation value
         * to be increased/decreased with the specified amount.
         * @param multiplier The amount to multiply the current Y rotation value by
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun yRotateBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationY.multiply = multiplier
            this.rotationY.interpolator = interpolator
            return this
        }

        /**
         * Animates the Y rotation value of the views of this [Choreography] to the specified Y rotation value.
         * @param rotationY The Y rotation value to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun yRotateTo(rotationY: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationY.toValue = rotationY
            this.rotationY.interpolator = interpolator
            return this
        }

        /**
         * Animates the Y rotation value of the views of this [Choreography] from the specified Y rotation value
         * to the specified Y rotation value
         * @param fromValue The Y rotation value to animate from
         * @param toValue The Y rotation value to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun yRotateFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationY.fromValue = fromValue
            this.rotationY.toValue = toValue
            this.rotationY.interpolator = interpolator
            return this
        }

        /**
         * Animates the Y rotation value of the views of this [Choreography] using the specified animated
         * X rotation value property. See [AnimatedFloatValue]
         * @param value The property to use for this animation.
         * @return this choreography.
         */
        fun yRotate(value: AnimatedFloatValue): Choreography {
            this.rotationY.copy(value)
            return this
        }

        /**
         * Animates the scale value of the views of this [Choreography] to the specified [Bounds] value.
         * @param bounds The bounds dimension to scale animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun scaleTo(bounds: Bounds, interpolator: TimeInterpolator? = null): Choreography {
            return scaleTo(bounds.dimension(), interpolator)
        }

        /**
         * Animates the scale value of the views of this [Choreography] to the specified [Dimension] value.
         * @param dimension The dimension to scale animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun scaleTo(dimension: Dimension, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleX.toValue = dimension.width / this.width.fromValue
            this.scaleY.toValue = dimension.height / this.height.fromValue

            this.scaleX.interpolator = interpolator
            this.scaleY.interpolator = interpolator

            return this
        }

        /**
         * Animates the views of this [Choreography] to the computed scale value created
         * by adding the specified delta to the current X and Y scale value. This causes the scale values
         * to be increased/decreased with the specified amount.
         * @param delta The amount to add to the current X and Y scale value
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun scaleAdd(delta: Float, interpolator: TimeInterpolator?): Choreography {
            xScaleAdd(delta, interpolator)
            yScaleAdd(delta, interpolator)
            return this
        }

        /**
         * Animates the views of this [Choreography] to the computed scale value created
         * by multiplying the specified delta to the current X and Y scale value. This causes the scale values
         * to be increased/decreased with the specified amount.
         * @param multiplier The amount to add to the current X and Y scale value
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun scaleBy(multiplier: Float, interpolator: TimeInterpolator?): Choreography {
            xScaleBy(multiplier, interpolator)
            yScaleBy(multiplier, interpolator)
            return this
        }

        /**
         * Animates the X and Y scale value of the views of this [Choreography] to the specified scale value.
         * @param scale The scale amount to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun scaleTo(scale: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.xScaleTo(scale, interpolator)
            this.yScaleTo(scale, interpolator)
            return this
        }

        /**
         * Animates the X and Y scale value of the views of this [Choreography] from the specified scale value
         * to the specified scale value
         * @param fromValue The scale amount to animate from
         * @param toValue The scale amount to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun scaleFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.xScaleFrom(fromValue, toValue, interpolator)
            this.yScaleFrom(fromValue, toValue, interpolator)
            return this
        }

        /**
         * Animates the views of this [Choreography] to the computed X scale value created
         * by adding the specified delta to the current X scale value. This causes the scale value
         * to be increased/decreased with the specified amount.
         * @param delta The amount to add to the current X scale value
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun xScaleAdd(delta: Float, interpolator: TimeInterpolator?): Choreography {
            this.scaleX.add = delta
            this.scaleX.interpolator = interpolator
            return this
        }

        /**
         * Animates the views of this [Choreography] to the computed X scale value created
         * by multiplying the specified delta to the current X scale value. This causes the scale value
         * to be increased/decreased with the specified amount.
         * @param multiplier The amount to add to the current X scale value
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun xScaleBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleX.multiply = multiplier
            this.scaleX.interpolator = interpolator
            return this
        }

        /**
         * Animates the X scale value of the views of this [Choreography] to the specified X scale value.
         * @param scaleX The scale amount to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun xScaleTo(scaleX: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleX.toValue = scaleX
            this.scaleX.interpolator = interpolator
            return this
        }

        /**
         * Animates the X scale value of the views of this [Choreography] from the specified X scale value
         * to the specified X scale value
         * @param fromValue The scale amount to animate from
         * @param toValue The scale amount to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun xScaleFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleX.fromValue = fromValue
            this.scaleX.toValue = toValue
            this.scaleX.interpolator = interpolator
            return this
        }

        /**
         * Animates the X scale value of the views of this [Choreography] using the specified animated
         * X scale value property. See [AnimatedFloatValue]
         * @param value The property to use for this animation.
         * @return this choreography.
         */
        fun xScale(value: AnimatedFloatValue): Choreography {
            this.scaleX.copy(value)
            return this
        }

        /**
         * Animates the views of this [Choreography] to the computed Y scale value created
         * by adding the specified delta to the current Y scale value. This causes the scale value
         * to be increased/decreased with the specified amount.
         * @param delta The amount to add to the current X scale value
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun yScaleAdd(delta: Float, interpolator: TimeInterpolator?): Choreography {
            this.scaleY.add = delta
            this.scaleY.interpolator = interpolator
            return this
        }

        /**
         * Animates the views of this [Choreography] to the computed Y scale value created
         * by multiplying the specified delta to the current Y scale value. This causes the scale value
         * to be increased/decreased with the specified amount.
         * @param multiplier The amount to add to the current Y scale value
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun yScaleBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleY.multiply = multiplier
            this.scaleY.interpolator = interpolator
            return this
        }

        /**
         * Animates the Y scale value of the views of this [Choreography] to the specified Y scale value.
         * @param scaleY The scale amount to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun yScaleTo(scaleY: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleY.toValue = scaleY
            this.scaleY.interpolator = interpolator
            return this
        }

        /**
         * Animates the Y scale value of the views of this [Choreography] from the specified Y scale value
         * to the specified Y scale value
         * @param fromValue The scale amount to animate from
         * @param toValue The scale amount to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun yScaleFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.scaleY.fromValue = fromValue
            this.scaleY.toValue = toValue
            this.scaleY.interpolator = interpolator
            return this
        }

        /**
         * Animates the Y scale value of the views of this [Choreography] using the specified animated
         * Y scale value property. See [AnimatedFloatValue]
         * @param value The property to use for this animation.
         * @return this choreography.
         */
        fun yScale(value: AnimatedFloatValue): Choreography {
            this.scaleY.copy(value)
            return this
        }

        /**
         * Animate the [Bounds] (Dimensions and Coordinates) of the views of this [Choreography] using the
         * specified bounds.
         * @param bounds The bounds to animate to.
         * @param interpolator the interpolator to use for this animation
         */
        fun boundsTo(bounds: Bounds, interpolator: TimeInterpolator? = null): Choreography {
            resizeTo(bounds, interpolator)
            positionAt(bounds, interpolator)
            return this
        }

        /**
         * Animate the size (Width and/or Height properties) of the views of this [Choreography] using the
         * specified [Measurement] with the specified delta. The delta is the amount to be added to the dimension
         * which is to be animated.
         * @param measurement The dimension to resize.
         * @param delta The amount to add to the current size
         * @param interpolator the interpolator to use for this animation
         */
        fun addToSize(measurement: Measurement, delta: Float, interpolator: TimeInterpolator? = null): Choreography {
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

        /**
         * Animates the views of this [Choreography] to the computed size (Width, Height) value created
         * by multiplying the specified delta with the current width and height values. This causes the size value
         * to be increased/decreased with the specified amount for the specified [Measurement].
         * @param measurement The dimension to resize.
         * @param multiplier The amount to add to the current Y scale value
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
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

        /**
         * Animates the size (Width, Height) values of the views of this [Choreography] to the specified [Bounds] value.
         * @param bounds The bounds to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun resizeTo(bounds: Bounds, interpolator: TimeInterpolator? = null): Choreography {
            return resizeTo(bounds.dimension(), interpolator)
        }

        /**
         * Animates the size (Width, Height) values of the views of this [Choreography] to the specified [Dimension] value.
         * @param dimension The dimension to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun resizeTo(dimension: Dimension, interpolator: TimeInterpolator? = null): Choreography {
            this.width.toValue = dimension.width
            this.height.toValue = dimension.height

            this.width.interpolator = interpolator
            this.height.interpolator = interpolator

            return this
        }

        /**
         * Animates the size (Width and/or Height) values of the views of this [Choreography] to the specified [Dimension] value
         * based on the specified [Measurement]
         * @param measurement The dimension to resize.
         * @param value The value to animate to the specified dimension to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
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

        /**
         * Animates the size (Width and/or Height) values of the views of this [Choreography] from the specified [Dimension] value
         * to the specified [Dimension] value based on the specified [Measurement]
         * @param measurement The dimension to resize.
         * @param fromValue The value from which to animate specified dimension from.
         * @param toValue The value to animate the specified dimension to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
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

        /**
         * Animates the size (Width and/or Height) values of the views of this [Choreography] with the specified Size animation
         * property value based on the specified [Measurement]
         * @param measurement The dimension to resize.
         * @param value The property to use for this animation.
         * @return this choreography.
         */
        fun resize(measurement: Measurement, value: AnimatedFloatValue): Choreography {
            when(measurement) {
                Measurement.WIDTH -> {
                    this.width.copy(value)
                }
                Measurement.HEIGHT ->  {
                    this.height.copy(value)
                }
                Measurement.BOTH -> {
                    this.width.copy(value)
                    this.height.copy(value)
                }
            }
            return this
        }

        /**
         * Animates the specified [Margin] value of the views of this [Choreography] to the specified value
         * @param margin The margin to animate.
         * @param value The value to animate the specified margin to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
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

        /**
         * Animates the specified [Margin] value of the views of this [Choreography] from the specified value
         * to the specified value
         * @param margin The margin to animate.
         * @param valueFrom The value to animate the specified margin from.
         * @param valueTo The value to animate the specified margin to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
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

        /**
         * Animates the specified [Margin] value of the views of this [Choreography] with the specified animation
         * value property. See [AnimatedValue]
         * @param margin The property to use for this animation.
         * @return this choreography.
         */
        fun margin(margin: AnimatedValue<Margings>): Choreography {
            this.margings.copy(margin)
            return this
        }

        /**
         * Animates the specified [Padding] value of the views of this [Choreography] to the specified value
         * @param padding The padding to animate.
         * @param value The value to animate the specified padding to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
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

        /**
         * Animates the specified [Padding] value of the views of this [Choreography] from the specified value
         * to the specified value
         * @param padding The padding to animate.
         * @param valueFrom The value to animate the specified padding from.
         * @param valueTo The value to animate the specified padding to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
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

        /**
         * Animates the specified [Padding] value of the views of this [Choreography] with the specified animation
         * value property. See [AnimatedValue]
         * @param padding The property to use for this animation.
         * @return this choreography.
         */
        fun padding(padding: AnimatedValue<Paddings>): Choreography {
            this.paddings.copy(padding)
            return this
        }

        /**
         * Animates the color value of the views of this [Choreography] to the specified color value.
         * @param color The color value to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun colorTo(color: Color, interpolator: TimeInterpolator? = null): Choreography {
            this.color.toValue = color.toColor()
            this.color.interpolator = interpolator
            return this
        }

        /**
         * Animates the color value of the views of this [Choreography] from the specified color value
         * to the specified color value
         * @param fromValue The color to animate from
         * @param toValue The color to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun colorFrom(fromValue: Color, toValue: Color, interpolator: TimeInterpolator? = null): Choreography {
            this.color.fromValue = fromValue.toColor()
            this.color.toValue = toValue.toColor()
            this.color.interpolator = interpolator
            return this
        }

        /**
         * Animates the color value of the views of this [Choreography] using the specified animated
         * color value property. See [AnimatedValue]
         * @param value The property to use for this animation.
         * @return this choreography.
         */
        fun color(value: AnimatedValue<Color>): Choreography {
            this.color.fromValue = value.fromValue.toColor()
            this.color.toValue = value.toValue.toColor()
            this.color.interpolator = value.interpolator
            return this
        }

        /**
         * Animates the color value of the views of this [Choreography] to the specified color value.
         * @param color The color value to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun colorTo(@ColorInt color: Int, interpolator: TimeInterpolator? = null): Choreography{
            this.color.toValue = color
            this.color.interpolator = interpolator
            return this
        }

        /**
         * Animates the color value of the views of this [Choreography] from the specified color value
         * to the specified color value
         * @param fromValue The color to animate from
         * @param toValue The color to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun colorFrom(@ColorInt fromValue: Int, @ColorInt toValue: Int, interpolator: TimeInterpolator? = null): Choreography{
            this.color.fromValue = fromValue
            this.color.toValue = toValue
            this.color.interpolator = interpolator
            return this
        }

        /**
         * Arc animates the position of the views of this [Choreography] to the specified position of the specified [Anchor].
         * in relation to the specified view: [MorphLayout]. If no arc translation control point has been specified it will
         * then been computed upon building. If a margin offset is used the the views will position at the
         * anchor point with the given margin offset.
         * @param anchor The position to animate the position to
         * @param view The view to animate relative to.
         * @param margin The offset distance to add from the absolute anchor to the animated views
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun anchorArcTo(anchor: Anchor, view: MorphLayout, margin: Float = MIN_OFFSET, interpolator: TimeInterpolator? = null): Choreography{
            this.useArcTranslator = true
            return anchorTo(anchor, view, margin, interpolator)
        }

        /**
         * Animates the position of the views of this [Choreography] to the specified position of the specified [Anchor].
         * in relation to the specified view: [MorphLayout]. If a margin offset is used the the views will position at the
         * anchor point with the given margin offset.
         * @param anchor The position to animate the position to
         * @param view The view to animate relative to.
         * @param margin The offset distance to add from the absolute anchor to the animated views
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun anchorTo(anchor: Anchor, view: MorphLayout, margin: Float = MIN_OFFSET, interpolator: TimeInterpolator? = null): Choreography{
            val bounds = view.viewBounds

            var startX: Float = bounds.x.toFloat()
            var startY: Float = bounds.y.toFloat()

            var endX: Float = views[0].windowLocationX.toFloat()
            var endY: Float = views[0].windowLocationY.toFloat()

            when (anchor) {
                Anchor.TOP -> {
                    val translationY: Float = abs(endY - startY)

                    this.positionY.toValue = -(translationY - margin)
                    this.positionY.interpolator = interpolator
                }
                Anchor.LEFT -> {
                    val translationX: Float = abs(endX - startX)

                    this.positionX.toValue = -(translationX - margin)
                    this.positionX.interpolator = interpolator
                }
                Anchor.RIGHT -> {
                    val translationX: Float = abs(endX - startX)

                    this.positionX.toValue = (translationX - margin)
                    this.positionX.interpolator = interpolator
                }
                Anchor.BOTTOM -> {
                    val translationY: Float = abs(endY - startY)

                    this.positionY.toValue = (translationY - margin)
                    this.positionY.interpolator = interpolator
                }
                Anchor.CENTER -> {
                    startX = bounds.x.toFloat() + (bounds.width / 2)
                    startY = bounds.y.toFloat() + (bounds.height / 2)

                    endX = views[0].windowLocationX.toFloat()
                    endY = views[0].windowLocationY.toFloat()

                    val translationX: Float = abs(endX - startX) - margin
                    val translationY: Float = abs(endY - startY) - margin

                    this.positionX.toValue = (if (endX > startX) -translationX else translationX) - (width.fromValue / 2)
                    this.positionY.toValue = (if (endY > startY) -translationY else translationY) - (height.fromValue / 2)

                    this.positionX.interpolator = interpolator
                    this.positionY.interpolator = interpolator
                }
                Anchor.TOP_LEFT -> {
                    anchorTo(Anchor.TOP, view, margin, interpolator)
                    anchorTo(Anchor.LEFT, view, margin, interpolator)
                }
                Anchor.TOP_RIGHT -> {
                    anchorTo(Anchor.TOP, view, margin, interpolator)
                    anchorTo(Anchor.RIGHT, view, margin, interpolator)
                }
                Anchor.BOTTOM_RIGHT -> {
                    anchorTo(Anchor.BOTTOM, view, margin, interpolator)
                    anchorTo(Anchor.RIGHT, view, margin, interpolator)
                }
                Anchor.BOTTOM_LEFT -> {
                    anchorTo(Anchor.BOTTOM, view, margin, interpolator)
                    anchorTo(Anchor.RIGHT, view, margin, interpolator)
                }
            }
            return this
        }

        /**
         * Animates the background of the views of this [Choreography] to the specified [Background]
         * @param background the background to animate the current to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun backgroundTo(background: Background, interpolator: TimeInterpolator? = null): Choreography {
            throw NotImplementedError("This function is not yet implemented")
            return this
        }

        /**
         * Specifies the pivot offset values see: [FloatPoint] to use for rotate and scale animations
         * in this [Choreography]. The pivot will be computed given the specified [Pivot] relation.
         * @param pivotPoint The offset points to use for pivoting.
         * @param type The relation to use when computing the pivot.
         * @return this choreography.
         */
        fun withPivot(pivotPoint: FloatPoint, type: Pivot = Pivot.RELATIVE_TO_SELF): Choreography {
            return withPivot(pivotPoint.x, pivotPoint.y, type)
        }

        /**
         * Specifies the pivot point X and pivot point Y to use for rotate and scale animations in.
         * this [Choreography]. The pivot will be computed given the specified [Pivot] relation.
         * @param pivotX The offset pivot X point to use.
         * @param pivotY The offset pivot Y point to use.
         * @param type The relation to use when computing the pivot.
         * @return this choreography.
         */
        fun withPivot(pivotX: Float, pivotY: Float, type: Pivot = Pivot.RELATIVE_TO_SELF): Choreography {
            withPivotX(pivotX, type)
            withPivotX(pivotY, type)
            return this
        }

        /**
         * Specifies the pivot point X to use for rotate and scale animations in this [Choreography].
         * The pivot will be computed given the specified [Pivot] relation.
         * @param pivotX The offset pivot X point to use.
         * @param type The relation to use when computing the pivot.
         * @return this choreography.
         */
        fun withPivotX(pivotX: Float, type: Pivot = Pivot.RELATIVE_TO_SELF): Choreography {
            pivotValueX = pivotX
            pivotValueTypeX = type
            pivotPoint.x = resolvePivot(pivotValueTypeX, pivotValueX, width.fromValue, viewParentSize.width)
            return this
        }

        /**
         * Specifies the pivot point Y to use for rotate and scale animations in this [Choreography].
         * The pivot will be computed given the specified [Pivot] relation.
         * @param pivotY The offset pivot Y point to use.
         * @param type The relation to use when computing the pivot.
         * @return this choreography.
         */
        fun withPivotY(pivotY: Float, type: Pivot = Pivot.RELATIVE_TO_SELF): Choreography {
            pivotValueY = pivotY
            pivotValueTypeY = type
            pivotPoint.y = resolvePivot(pivotValueTypeY, pivotValueY, height.fromValue, viewParentSize.height)
            return this
        }

        /**
         * Specifies [TransitionOffsetListener] to use for this [Choreography]. The listener
         * is notified by the progress of the animation being perform by this choreography with
         * a percent fraction from 0f to 1f
         * @param offsetListener The listener to notify.
         * @return this choreography.
         */
        fun withOffsetListener(offsetListener: TransitionOffsetListener): Choreography {
            this.offsetListener = offsetListener
            return this
        }

        /**
         * Specifies the default [TimeInterpolator] to use for this [Choreography]. The interpolator
         * will be used when the property being animated has no defined interpolator of its own.
         * @param interpolator The interpolator to use for this choreography.
         * @return this choreography.
         */
        fun withInterpolator(interpolator: TimeInterpolator?): Choreography {
            this.interpolator = interpolator
            return this
        }

        /**
         * Specifies the duration of the animation for this [Choreography]. Based on how this choreography was
         * created, if no duration is specified this choreography will use the duration of its parent.
         * In other cases the duration will be set to the default animation of the [Choreographer]
         * @param duration The duration of the choreography animation
         * @return this choreography.
         */
        fun withDuration(duration: Long): Choreography {
            this.duration = duration
            return this
        }

        /**
         * Specifies a [Stretch] property to use for when animating the translation or position properties
         * of this [Choreography].
         * @param stretch The stretch property to use for stretching and squashing the views
         * being animated by this choreography upon translation.
         */
        fun withStretch(stretch: Stretch): Choreography {
            this.stretch = stretch
            return this
        }

        /**
         * Specifies the start delay of the animation for this [Choreography].
         * @param delay The delay of the choreography animation
         * @return this choreography.
         */
        fun withStartDelay(delay: Long): Choreography {
            this.delay = delay
            return this
        }

        /**
         * Specifies the stagger value to use for animating through the views for this [Choreography].
         * The duration of the animation will remain intact but the higher the stagger offset the faster
         * the animation for each individual view will be. See: [AnimationStagger]
         * @param offset The offset to use. The offset indicates at what point through the animation
         * of the previous view should the animation of the current view start. When incremental stagger
         * is used the value will range between a threshold.
         * @param multiplier The stagger multiplier to use. The multiplier determines the range of the offset
         * a value of 1f means full offset.
         * @param type The [Stagger] type to use.
         * @return this choreography.
         */
        fun withStagger(offset: Float = MIN_OFFSET, multiplier: Float = MAX_OFFSET, type: Stagger = Stagger.LINEAR): Choreography {
            this.stagger = AnimationStagger(staggerOffset = offset, staggerMultiplier = multiplier, type = type)
            return this
        }

        /**
         * Specifies the stagger animation see: [AnimationStagger] to use for animating through the views for this [Choreography].
         * The duration of the animation will remain intact but the higher the stagger offset the faster
         * the animation for each individual view will be.
         * @param stagger The instruction to use for creating the stagger effect.
         * @return this choreography.
         */
        fun withStagger(stagger: AnimationStagger): Choreography {
            this.stagger = stagger
            return this
        }

        /**
         * Specifies the action that should be executed upon the end of the animation of this [Choreography]
         * @param action The end action to execute.
         * @return this choreography.
         */
        fun whenDone(action: ChoreographerAction): Choreography {
            this.doneAction = action
            return this
        }

        /**
         * Specifies the action that should be executed upon the start of the animation of this [Choreography]
         * @param action The start action to execute.
         * @return this choreography.
         */
        fun onStart(action: ChoreographerAction): Choreography {
            this.startAction = action
            return this
        }

        /**
         * Specifies the control X and Y value to use for arc translation. If arc
         * translation is used. The translation will use the specified control point created
         * with the specified X and Y values.
         * @param x The x coordinate of the control point.
         * @param y The y coordinate of the control point.
         * @return this choreography.
         */
        fun withControlPoint(x: Float, y: Float): Choreography {
            this.controlPoint = Coordinates(x, y)
            return this
        }

        /**
         * Specifies the control point value to use for arc translation. If arc
         * translation is used the translation will use the specified control point
         * @param point The control point to use for arc translations.
         * @return this choreography.
         */
        fun withControlPoint(point: Coordinates): Choreography {
            this.controlPoint = point
            return this
        }

        /**
         * Specifies the control point value to use for arc translation. If arc
         * translation is used the translation will use the specified control point
         * @param point The control point to use for arc translations.
         * @return this choreography.
         */
        fun withControlPoint(point: FloatPoint): Choreography {
            this.controlPoint = Coordinates(point.x, point.y)
            return this
        }

        /**
         * Specifies the circular [Reveal] to use for revealing the views of this [Choreography]
         * @param reveal contains information on how to reveal the views.
         * @return this choreography.
         */
        fun revealWith(reveal: Reveal): Choreography {
            this.reveal = reveal
            return this
        }

        /**
         * Specifies the circular reveal to use for revealing the views of this [Choreography]. The
         * reveal will happen with the radius and center point of the specified view.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun revealWith(view: MorphLayout, interpolator: TimeInterpolator? = null): Choreography {
            this.reveal = Reveal(view.getView(), views[0].getView())
            this.reveal?.interpolator = interpolator
            return this
        }

        /**
         * Specifies the circular reveal to use for revealing the views of this [Choreography]. The
         * reveal will happen with the specified center coordinates and radius.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun revealFrom(centerX: Float, centerY: Float, radius: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.reveal = Reveal(centerX, centerY, radius, views[0].getView())
            this.reveal?.interpolator = interpolator
            return this
        }

        /**
         * Specifies the circular reveal to use for revealing the views of this [Choreography]. The
         * reveal will happen with the specified center coordinates and radius.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun revealFrom(coordinates: Coordinates, radius: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.reveal = Reveal(coordinates.x, coordinates.y, radius, views[0].getView())
            this.reveal?.interpolator = interpolator
            return this
        }

        /**
         * Specifies that the animation perform by this [Choreography] will reverse upon finish.
         * @return this choreography.
         */
        fun thenReverse(): Choreography {
            this.reverse = true
            return this
        }

        /**
         * Creates a [Choreography] for the given views which will start at the duration
         * offset of its parent. A value of 0.5f indicates that this choreography will play when
         * the animation of its parent is half way through. If no views have been specified the
         * views of the previous choreography will be used.
         * @param offset The offset at which this choreography will start animating.
         * @param views The views which will be animated by this choreography.
         * @return this choreography.
         */
        fun animateAfter(offset: Float, vararg views: MorphLayout = this.views): Choreography {
            return choreographer.animateAfter(this, offset, *views)
        }

        /**
         * Creates a [Choreography] for the given views which will start directly the animation of
         * its parent choreography is over. If no views have been specified the views of the previous
         * choreography will be used.
         * @param views The views which will be animated by this choreography.
         * @return this choreography.
         */
        fun thenAnimate(vararg views: MorphLayout = this.views): Choreography {
            return choreographer.thenAnimate(this, *views)
        }

        /**
         * Creates a [Choreography] for the given views which will start directly at the same time
         * as its parent. If no views have been specified the views of the previous choreography will be used.
         * @param views The views which will be animated by this choreography.
         * @return this choreography.
         */
        fun alsoAnimate(vararg views: MorphLayout = this.views): Choreography {
            return choreographer.alsoAnimate(this, *views)
        }

        /**
         * Creates a [Choreography] for the given views which will start directly at the same time
         * as its parent with the same properties as its parent unless specified otherwise. If no views
         * have been specified the views of the previous choreography will be used.
         * @param views The views which will be animated by this choreography.
         * @return this choreography.
         */
        fun andAnimate(vararg views: MorphLayout = this.views): Choreography {
            return choreographer.andAnimate(this, *views)
        }

        /**
         * Creates a [Choreography] for the given views which will start directly after the specified duration
         * offset of its parent with the same properties as its parent unless specified otherwise. If no views
         * have been specified the views of the previous choreography will be used.
         * @param views The views which will be animated by this choreography.
         * @return this choreography.
         */
        fun andAnimateAfter(offset: Float, vararg views: MorphLayout = this.views): Choreography {
            return choreographer.andAnimateAfter(this, offset, *views)
        }

        /**
         * Creates a [Choreography] for the given views which will reverse the last choreography which was
         * assign to the same views if any. If the views have not been part of a previous choreography this
         * will do nothing. The animation will play upon the end of the animation of its parent.
         * If no views have been specified the views of the previous choreography will be used.
         * @param views The views which will be animated by this choreography.
         * @return this choreography.
         */
        fun reverseAnimate(vararg views: MorphLayout = this.views): Choreography {
            return choreographer.reverseAnimate(this, *views)
        }

        /**
         * Creates a [Choreography] for the given views which will reverse the last choreography which was
         * assign to the same views if any. If the views have not been part of a previous choreography this
         * will do nothing. The animation will play at the same time as its parent and will clone its parents properties.
         * If no views have been specified the views of the previous choreography will be used.
         * @param views The views which will be animated by this choreography.
         * @return this choreography.
         */
        fun andReverseAnimate(vararg view: MorphLayout = this.views): Choreography {
            return choreographer.andReverseAnimate(this, *view)
        }

        /**
         * Creates a [Choreography] for the given children of the specified view which will start at the duration
         * offset of its parent. A value of 0.5f indicates that this choreography will play when
         * the animation of its parent is half way through. If a stagger is specified the views will be animated
         * with the specified stagger.
         * @param offset The offset at which this choreography will start animating.
         * @param view The view which children will be animated by this choreography.
         * @param stagger The stagger to use when animating the children. See [AnimationStagger]
         * @return this choreography.
         */
        fun animateChildrenOfAfter(view: MorphLayout, offset: Float, stagger: AnimationStagger? = null): Choreography {
            val children = view.getChildren().map { if (it is MorphLayout) it else MorphView.makeMorphable(it) }.toList().toTypedArray()
            return choreographer.animateChildrenOfAfter(this, offset, stagger, *children)
        }

        /**
         * Creates a [Choreography] for the given children of the specified view which will start when the animation
         * of the parent choreography is over. If a stagger is specified the views will be animated
         * with the specified stagger.
         * @param view The view which children will be animated by this choreography.
         * @param stagger The stagger to use when animating the children. See [AnimationStagger]
         * @return this choreography.
         */
        fun thenAnimateChildrenOf(view: MorphLayout, stagger: AnimationStagger? = null): Choreography {
            val children = view.getChildren().map { if (it is MorphLayout) it else MorphView.makeMorphable(it) }.toList().toTypedArray()
            return choreographer.thenAnimateChildrenOf(this, stagger, *children)
        }

        /**
         * Creates a [Choreography] for the given children of the specified view which will start when the animation
         * of the parent choreography starts. If a stagger is specified the views will be animated
         * with the specified stagger.
         * @param view The view which children will be animated by this choreography.
         * @param stagger The stagger to use when animating the children. See [AnimationStagger]
         * @return this choreography.
         */
        fun alsoAnimateChildrenOf(view: MorphLayout, stagger: AnimationStagger? = null): Choreography {
            val children = view.getChildren().map { if (it is MorphLayout) it else MorphView.makeMorphable(it) }.toList().toTypedArray()
            return choreographer.alsoAnimateChildrenOf(this, stagger, *children)
        }

        /**
         * Creates a [Choreography] for the given children of the specified view which will start when the animation
         * of the parent choreography starts. The properties of the parent choreography will be used by this choreography.
         * If a stagger is specified the views will be animated with the specified stagger.
         * @param view The view which children will be animated by this choreography.
         * @param stagger The stagger to use when animating the children. See [AnimationStagger]
         * @return this choreography.
         */
        fun andAnimateChildrenOf(view: MorphLayout, stagger: AnimationStagger? = null): Choreography {
            val children = view.getChildren().map { if (it is MorphLayout) it else MorphView.makeMorphable(it) }.toList().toTypedArray()
            return choreographer.andAnimateChildrenOf(this, stagger, *children)
        }

        /**
         * A call to this function will build the current and all the previously appended choreographies.
         * This function must be called prior to starting the [Choreography] animation. Note that a
         * call to this function will not only built the current choreography but also all its predecessors.
         * A built choreography can be saved to played at a later time. The ability to build a
         * choreography helps to get rid of overhead.
         * @return the [Choreographer] which will animate this choreography.
         */
        fun build(): Choreographer {
            return choreographer.build()
        }

        /**
         * A call to this function will start the current and all the previously appended choreographies.
         * call to this function will not only start the current choreography but also all its predecessors.
         * If the choreographies are not yet build they will also be built.
         * choreography helps to get rid of overhead.
         */
        fun start() {
            if (choreographer.built) {
                choreographer.start()
            } else {
                choreographer.build().start()
            }
        }

        /**
         * Copies the properties of the give [Choreography] into the current
         */
        private fun copyProps(other: Choreography) {
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

        /**
         * Creates a clone of this [Choreography] with the specifies views
         * @param views the view to build a choreography with.
         * @return the cloned choreography
         */
        fun clone(vararg views: MorphLayout): Choreography {
            val choreography = Choreography(choreographer, *views)

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
    }

    /**
     * Class which controls whether a traversal should break traversion.
     * Allows for stopping a traversal to continue from outside of the traversal
     * function.
     */
    class TraverseControl {
        internal var breakTraverse: Boolean = false

        /**
         * Breaks the traversal this control is bound to
         */
        fun breakTraversal() {
            breakTraverse = true
        }
    }

    private companion object {
        /**
         * Resolves the pivot for a given type and value
         * @param type specifies in relation to what the pivot should be computed
         * @param value the value to map the size of the view to
         * @param parentSize the size of the parent of the view whose pivot
         * is being resolved
         */
        fun resolvePivot(type: Pivot, value: Float, size: Float, parentSize: Float): Float {
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
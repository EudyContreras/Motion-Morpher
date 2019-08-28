package com.eudycontreras.motionmorpherlibrary

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.animation.addListener
import com.eudycontreras.motionmorpherlibrary.enumerations.*
import com.eudycontreras.motionmorpherlibrary.extensions.asMorphable
import com.eudycontreras.motionmorpherlibrary.extensions.clamp
import com.eudycontreras.motionmorpherlibrary.extensions.identity
import com.eudycontreras.motionmorpherlibrary.extensions.toStateList
import com.eudycontreras.motionmorpherlibrary.helpers.ArcTranslationHelper
import com.eudycontreras.motionmorpherlibrary.helpers.StretchAnimationHelper
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.layouts.MorphView
import com.eudycontreras.motionmorpherlibrary.properties.*
import com.eudycontreras.motionmorpherlibrary.utilities.ColorUtility
import com.eudycontreras.motionmorpherlibrary.utilities.RevealUtility
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.abs

/**
 * Class which manages and creates complex choreographies. The choreographer
 * can be use for sequencing animations in any way desired. It allows the building of
 * complex animation choreographies that can be stored and run at a later time.
 *
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 30 2019
 */
class Choreographer(context: Context) {

    //TODO("Make the morphViews held by this into weak references to avoid memory leaks")
    //TODO("Different animation durations for properties")

    private lateinit var headChoreography: Choreography
    private lateinit var tailChoreography: Choreography

    private val morphViewPool: HashMap<Int, MorphLayout> = HashMap()

    private var interpolator: TimeInterpolator? = null

    private var boundsChanged: Boolean = false

    private var defaultDuration: Long = MIN_DURATION

    private var defaultPivot: Point<Float>? = null

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

    /**
     * Determines whether the descendants of a choreography should inherit
     * its parents non animation value properties such as:
     *
     * - [Choreography.duration][Choreography] The choreography animation duration.
     * - [Choreography.pivotPoint][Choreography] The choreography pivot point for scale and rotate animations.
     * - [Choreography.interpolator][Choreography] The choreography ease interpolator.
     * - [Choreography.controlPoint][Choreography] The choreography control point for arc animations
     *
     *
     * Default value: `TRUE`
     */
    var allowInheritance: Boolean = true
        private  set

    private val handler: Handler by lazy {
        Handler()
    }

    private val arcTranslator: ArcTranslationHelper by lazy {
        ArcTranslationHelper()
    }

    private var scaleXListener: ViewPropertyValueListener = { view, value ->
        view.morphScaleX = value
    }

    private var scaleYListener: ViewPropertyValueListener = { view, value ->
        view.morphScaleY = value
    }

    private var rotationListener: ViewPropertyValueListener = { view, value ->
        view.morphRotation = value
    }

    private var rotationXListener: ViewPropertyValueListener = { view, value ->
        view.morphRotationX = value
    }

    private var rotationYListener: ViewPropertyValueListener = { view, value ->
        view.morphRotationY = value
    }

    private var translationXListener: ViewPropertyValueListener = { view, value ->
        view.morphTranslationX = value
    }

    private var translationYListener: ViewPropertyValueListener = { view, value ->
        view.morphTranslationY = value
    }

    private var translationZListener: ViewPropertyValueListener = { view, value ->
        view.morphTranslationZ = value
    }

    /**
     * Operator functions that adds the specified [Choreographer] to this
     * choreographer by appending the choreography chain contained within the
     * added choreographer.
     * @param other The choreographer to be added.
     * @return this choreographer.
     */
    operator fun Choreographer.plus(other: Choreographer): Choreographer {
        return this.append(other, MAX_OFFSET)
    }

    /**
     * Operator functions that adds the specified [Choreographer] to this
     * choreographer by appending the choreography chain contained within the
     * added choreographer.
     * @param other The choreographer to be added.
     */
    operator fun Choreographer.plusAssign(other: Choreographer) {
        this.append(other, MAX_OFFSET)
    }

    /**
     * Determines whether the descendants of a [Choreography] should inherit
     * its parents non animation value properties such as:
     *
     * - [Choreography.duration][Choreography] The choreography animation duration.
     * - [Choreography.pivotPoint][Choreography] The choreography pivot point for scale and rotate animations.
     * - [Choreography.interpolator][Choreography] The choreography ease interpolator.
     * - [Choreography.controlPoint][Choreography] The choreography control point for arc animations
     *
     * Default value: `TRUE`
     *
     * @return this choreographer.
     */
    fun allowChildInheritance(allow: Boolean = true): Choreographer {
        this.allowInheritance = allow
        return this
    }

    /**
     * Assigns a default duration to use when the [Choreography] to animate has
     * no defined duration.
     * @param duration the default duration to use.
     * @return this choreographer.
     */
    fun withDefaultDuration(duration: Long): Choreographer {
        this.defaultDuration = duration
        return this
    }

    /**
     * Assigns a default pivot point to use when the [Choreography] to animate has
     * no defined pivot point.
     * @param point the default pivot point to use.
     * @return this choreographer.
     */
    fun withDefaultPivot(point: Point<Float>): Choreographer {
        this.defaultPivot = point
        return this
    }

    /**
     * Assigns default pivot point coordinates to use when the [Choreography] to animate has
     * no defined pivot point.
     * @param x the default pivot point X to use.
     * @param y the default pivot point Y to use.
     * @return this choreographer.
     */
    fun withDefaultPivot(x: Float, y: Float): Choreographer {
        this.defaultPivot = FloatPoint(x, y)
        return this
    }

    /**
     * Assigns a default easing [TimeInterpolator] to use when the [Choreography] to animate has
     * no defined interpolator.
     * @param interpolator the default interpolator to use
     * @return this choreographer.
     */
    fun withDefaultInterpolator(interpolator: TimeInterpolator): Choreographer {
        this.interpolator = interpolator
        return this
    }

    /**
     * Creates the initial head [Choreography] for the given [MorphLayout].
     * @param views the morphViews to which the choreography belongs to
     * @return the created choreography.
     */
    fun animate(vararg views: View?): Choreography {
        val morphViews = getViews(views)
        this.headChoreography = Choreography(this, *morphViews)
        this.headChoreography.offset = MAX_OFFSET
        return headChoreography
    }


    /**
     * Creates the initial head [Choreography] for the given [MorphLayout].
     * @param morphViews the morphViews to which the choreography belongs to
     * @return the created choreography.
     */
    fun animate(vararg morphViews: MorphLayout): Choreography {
        this.headChoreography = Choreography(this, *morphViews)
        this.headChoreography.offset = MAX_OFFSET
        return headChoreography
    }

    /**
     * Creates a [Choreography] for the given [MorphLayout]. With a specified parent choreography.
     * The choreography will play after the specified offset.
     * @param choreography the parent choreography of the current.
     * @param views the morphViews to which the choreography belongs to.
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
            if (allowInheritance) {
                this.duration = choreography.duration
                this.interpolator = choreography.interpolator
                this.pivotPoint = choreography.pivotPoint
                this.controlPoint = choreography.controlPoint
            }
            choreography.child = this
        }
        return tailChoreography
    }

    /**
     * Creates a [Choreography] for the given [MorphLayout]. With a specified parent choreography.
     * The choreography will play after parent choreography is done animating.
     * @param choreography the parent choreography of the current.
     * @param views the morphViews to which the choreography belongs to.
     * @return the created choreography.
     */
    internal fun thenAnimate(choreography: Choreography, vararg views: MorphLayout): Choreography {
        val properties = getProperties(choreography, *views)

        tailChoreography = Choreography(this, *views).apply {
            this.setStartProperties(properties)
            this.parent = choreography
            this.offset = MAX_OFFSET

            if (allowInheritance) {
                this.duration = choreography.duration
                this.interpolator = choreography.interpolator
                this.pivotPoint = choreography.pivotPoint
                this.controlPoint = choreography.controlPoint
            }
            choreography.child = this
        }
        return tailChoreography
    }

    /**
     * Creates a [Choreography] for the given [MorphLayout]. With a specified parent choreography.
     * @param choreography the parent choreography of the current.
     * @param views the morphViews to which the choreography belongs to.
     * @return the created choreography.
     */
    internal fun alsoAnimate(choreography: Choreography, vararg views: MorphLayout): Choreography {
        val properties = getProperties(choreography, *views)

        tailChoreography = Choreography(this, *views).apply {
            this.setStartProperties(properties)
            this.parent = choreography
            choreography.child = this
            if (allowInheritance) {
                this.duration = choreography.duration
                this.interpolator = choreography.interpolator
                this.pivotPoint = choreography.pivotPoint
                this.controlPoint = choreography.controlPoint
            }
        }
        return tailChoreography
    }

    /**
     * Creates a [Choreography] for the given [MorphLayout]. With a specified parent choreography.
     * The choreography is a clone of the parent choreography and will play together with its parent
     * @param choreography the parent choreography of the current.
     * @param views the morphViews to which the choreography belongs to.
     * @return the created choreography.
     */
    internal fun andAnimate(choreography: Choreography, vararg views: MorphLayout): Choreography {
        tailChoreography = choreography.clone(*views).apply {
            this.morphViews = views
            this.parent = choreography
            this.child = null
            if (allowInheritance) {
                this.duration = choreography.duration
                this.interpolator = choreography.interpolator
                this.pivotPoint = choreography.pivotPoint
                this.controlPoint = choreography.controlPoint
            }
            choreography.child = this
        }
        return tailChoreography
    }

    /**
     * Creates a [Choreography] for the given [MorphLayout]. With a specified parent choreography.
     * The choreography is a clone of the parent choreography and will play after the specified offset.
     * @param choreography the parent choreography of the current.
     * @param views the morphViews to which the choreography belongs to.
     * @param offset the time offset to use. The current choreography will play after the
     * parent choreography has animated to the specified offset.
     * @return the created choreography.
     */
    internal fun andAnimateAfter(choreography: Choreography, offset: Float, vararg views: MorphLayout): Choreography {
        tailChoreography = choreography.clone(*views).apply {
            this.morphViews = views
            this.parent = choreography
            this.offset = offset
            this.child = null
            if (allowInheritance) {
                this.duration = choreography.duration
                this.interpolator = choreography.interpolator
                this.pivotPoint = choreography.pivotPoint
                this.controlPoint = choreography.controlPoint
            }
            choreography.child = this
        }
        return tailChoreography
    }

    /**
     * Reverse animates the specified choreography to its initial state for the specified morphViews.
     * The reverse animation will occur after the parent animation is done animating.
     * @param choreography the parent choreography of the current.
     * @param views the morphViews to which the choreography belongs to.
     * @return the created choreography.
     */
    internal fun reverseAnimate(choreography: Choreography, vararg views: MorphLayout): Choreography {
        var oldChoreography: Choreography? = null

        predecessors(choreography) { control, _choreography ->
            loopA@ for(viewA in _choreography.morphViews) {
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
     * @param views the morphViews to which the choreography belongs to.
     * @return the created choreography.
     */
    internal fun andReverseAnimate(choreography: Choreography, vararg views: MorphLayout): Choreography {
        var oldChoreography: Choreography? = null

        predecessors(choreography) { control, _choreography ->
            loopA@ for(viewA in _choreography.morphViews) {
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
     * @param views the morphViews to which the choreography belongs to.
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
     * @param views the morphViews to which the choreography belongs to.
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
            if (allowInheritance) {
                this.duration = choreography.duration
                this.interpolator = choreography.interpolator
                this.pivotPoint = choreography.pivotPoint
                this.controlPoint = choreography.controlPoint
            }
            choreography.child = this
        }
        return tailChoreography
    }

    /**
     * Creates and animation [Choreography] for the children of the specified [MorphLayout]. The
     * the animation can optionally play with a specified animation stagger. The animation
     * will play after its parent.
     * @param stagger the stagger to for animating the children
     * @param views the morphViews to which the choreography belongs to.
     * @return the created choreography.
     */
    internal fun thenAnimateChildrenOf(choreography: Choreography, stagger: AnimationStagger? = null, vararg views: MorphLayout): Choreography {
        val properties = getProperties(choreography, *views)

        tailChoreography = Choreography(this, *views).apply {
            this.setStartProperties(properties)
            this.parent = choreography
            this.offset = MAX_OFFSET
            this.stagger = stagger
            if (allowInheritance) {
                this.duration = choreography.duration
                this.interpolator = choreography.interpolator
                this.pivotPoint = choreography.pivotPoint
                this.controlPoint = choreography.controlPoint
            }
            choreography.child = this
        }
        return tailChoreography
    }

    /**
     * Creates and animation [Choreography] for the children of the specified [MorphLayout]. The
     * the animation can optionally play with a specified animation stagger. The animation
     * will play together with its parent.
     * @param stagger the stagger to for animating the children
     * @param views the morphViews to which the choreography belongs to.
     * @return the created choreography.
     */
    internal fun alsoAnimateChildrenOf(choreography: Choreography, stagger: AnimationStagger? = null, vararg views: MorphLayout): Choreography {
        val properties = getProperties(choreography, *views)

        tailChoreography = Choreography(this, *views).apply {
            this.setStartProperties(properties)
            this.parent = choreography
            this.stagger = stagger
            if (allowInheritance) {
                this.duration = choreography.duration
                this.interpolator = choreography.interpolator
                this.pivotPoint = choreography.pivotPoint
                this.controlPoint = choreography.controlPoint
            }
            choreography.child = this
        }
        return tailChoreography
    }

    /**
     * Creates and animation [Choreography] for the children of the specified [MorphLayout]. The choreography is
     * a direct clone of its parent. The animation can optionally play with a specified animation stagger.
     * The animation will play together with its parent.
     * @param stagger the stagger to for animating the children
     * @param views the morphViews to which the choreography belongs to.
     * @return the created choreography.
     */
    internal fun andAnimateChildrenOf(choreography: Choreography, stagger: AnimationStagger? = null, vararg views: MorphLayout): Choreography {
        tailChoreography = choreography.clone(*views).apply {
            this.morphViews = views
            this.parent = choreography
            this.child = null
            this.stagger = stagger
            if (allowInheritance) {
                this.duration = choreography.duration
                this.interpolator = choreography.interpolator
                this.pivotPoint = choreography.pivotPoint
                this.controlPoint = choreography.controlPoint
            }
            choreography.child = this
        }
        return tailChoreography
    }

    /**
     * Fetches a map of the properties for the given morphViews. The passed [Choreography] is traversed from
     * bottom to top in order to find the choreography which belongs to specified [MorphLayout]. When the choreography
     * is found a map created containing all the animation properties.
     * @param choreography The choreography to traverse from.
     * @param views The morphViews to which the choreography requested belongs to.
     * @return a map of the animation properties with their respective property name.
     */
    private fun getProperties(choreography: Choreography, vararg views: MorphLayout): Map<String, AnimatedValue<*>>? {
        var properties: Map<String, AnimatedValue<*>>? = null

        predecessors(choreography) { control, _choreography ->
            loopA@ for(viewA in _choreography.morphViews) {
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
        if (!built) {
            build()
            built = false
        }
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
        headChoreography.morphViews = emptyArray()
        headChoreography.resetProperties()

        tailChoreography.parent = null
        tailChoreography.child = null
        tailChoreography.morphViews = emptyArray()
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
            choreography.morphViews.forEach { v ->
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
            choreography.morphViews.forEach { v ->
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
     * Traverses the successors of the specified [Choreography] in order to
     * find the head. If the choreography has no child the specified
     * choreography is returned.
     * @param choreography the choreography to traverse.
     */
    private tailrec fun getTail(choreography: Choreography): Choreography {
        if (choreography.child != null) {
            return getTail(choreography.child!!)
        }
        return choreography
    }

    /**
     * Traverses the predecessors of the specified [Choreography] in order to
     * find the head. If the choreography has no parent the specified
     * choreography is returned.
     * @param choreography the choreography to traverse.
     */
    private tailrec fun getHead(choreography: Choreography): Choreography {
        if (choreography.parent != null) {
            return getHead(choreography.parent!!)
        }
        return choreography
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
     * Appends the head [Choreography] of the specified [Choreographer] to the
     * tail choreography of this Choreographer. The appended choreography will become part
     * of this choreographer and will play based on the properties which
     * were given upon creation and its creation method.
     *
     * - **Note:** When a choreography chain is appended the choreographer must be rebuilt, meaning you must call the
     * [Choreographer.build] function even when it has already been called.
     *
     * @param choreographer the choreographer whose head choreography is to be appended.
     * @param offset the new appended head will be added with a default offset of 1f, meaning
     * that the appended sequence will play after the end of the previous. This parameter specifies
     * at what offset to play the animation.
     * @return this choreographer.
     */
    fun append(choreographer: Choreographer, offset: Float = MAX_OFFSET): Choreographer {
        this.tailChoreography.child = choreographer.headChoreography
        choreographer.headChoreography.parent = this.tailChoreography
        choreographer.headChoreography.offset = offset
        this.tailChoreography = choreographer.tailChoreography
        return this
    }


    /**
     * Prepends the tail [Choreography] of the specified [Choreographer] to the
     * head choreography of this Choreographer. The prepended choreography will become part
     * of this choreographer and will play based on the properties which
     * were given upon creation and its creation method.
     *
     * - **Note:** When a choreography chain is prepended the choreographer must be rebuilt, meaning you must call the
     * [Choreographer.build] function even when it has already been called.
     *
     * @param choreographer the choreographer whose tail choreography is to be prepended.
     * @param offset the offset at which the old head should start animating. Usually
     * the offset for the head is 0f. The default value for this parameter is 1f which means
     * that the old head will start animating after the animation from the tail of the prepended
     * choreography chain is done animating.
     * @return this choreographer.
     */
    fun prepend(choreographer: Choreographer, offset: Float = MAX_OFFSET): Choreographer {
        val head = this.headChoreography
        val tail = choreographer.tailChoreography

        head.offset = offset
        tail.child = head
        head.parent = tail

        this.headChoreography = choreographer.headChoreography
        return this
    }

    /**
     * Builds the [Choreographer] by applying the desired values of each [Choreography]
     * and prepares the choreographies to be played. The build process is called prior
     * to the start of the choreographer and each of its choreographies. This process allows
     * for the heavy process of building a choreography to be done prior to the point at
     * which it will be played. The build process traverses the choreography link from head
     * to root and it calculates the durations and start times of each of the choreographies and
     * it defines an animation control to each one. See: [ChoreographyControl]
     * @return The choreographer being used.
     */
    internal fun build(): Choreographer {

        var totalDuration: Long = MIN_DURATION
        var totalDelay: Long = MIN_DURATION

        successors(headChoreography) { _, current ->
            applyInterpolators(current)
            applyReveal(current)
            applyConceal(current)

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

                current.progressListener?.invoke(fraction)
                current.offsetTrigger?.listenTo(fraction)
            }

            val endListener: (Animator) -> Unit = {
                current.doneAction?.invoke(current)
                current.isRunning = false
            }

            val startListener: (Animator) -> Unit = {
                current.startAction?.invoke(current)
                current.isRunning = true

                current.reveal?.let {
                    RevealUtility.circularReveal(it)
                }
                current.conceal?.let {
                    RevealUtility.circularConceal(it)
                }
            }

            current.control.mDuration = current.duration
            current.control.mStartDelay = totalDelay
            current.control.updateListener = updateListener
            current.control.startListener = startListener
            current.control.endListener = endListener
            current.control.build()
        }

        built = true

        return this
    }

    /**
     *
     */
    fun transitionTo(percentage: Float) {
        //TODO("Implement this somehow")
    }

    /**
     * Applies the reveal for the specified [Choreography]. The reveal
     * is applied to the choreography if it contains a specified [Reveal].
     * @param choreography The choreography to which its reveal is applied to.
     */
    private fun applyReveal(choreography: Choreography) {
        choreography.reveal?.let {
            it.duration = choreography.duration
            it.interpolator = it.interpolator ?: choreography.interpolator
            if (it.centerX == Float.MIN_VALUE) {
                it.centerX = choreography.pivotPoint.x
            }
            if (it.centerY == Float.MIN_VALUE) {
                it.centerY = choreography.pivotPoint.y
            }
        }
    }

    /**
     * Applies the conceal for the specified [Choreography]. The conceal
     * is applied to the choreography if it contains a specified [Conceal].
     * @param choreography The choreography to which its conceal is applied to.
     */
    private fun applyConceal(choreography: Choreography) {
        choreography.conceal?.let {
            it.duration = choreography.duration
            it.interpolator = it.interpolator ?: choreography.interpolator
            if (it.centerX == Float.MIN_VALUE) {
                it.centerX = choreography.pivotPoint.x
            }
            if (it.centerY == Float.MIN_VALUE) {
                it.centerY = choreography.pivotPoint.y
            }
        }
    }

    /**
     * Applies the easing [TimeInterpolator] for the specified [Choreography]. The interpolator
     * is applied the *Choreography* if it contains a specified Interpolator. If the *Choreography*
     * @param choreography The choreography to which the interpolator is applied to.
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

        choreography.scaleXValues.interpolator = choreography.scaleXValues.interpolator ?: choreography.interpolator
        choreography.scaleYValues.interpolator = choreography.scaleYValues.interpolator ?: choreography.interpolator

        choreography.rotationValues.interpolator = choreography.rotationValues.interpolator ?: choreography.interpolator
        choreography.rotationXValues.interpolator = choreography.rotationXValues.interpolator ?: choreography.interpolator
        choreography.rotationYValues.interpolator = choreography.rotationYValues.interpolator ?: choreography.interpolator

        choreography.translateXValues.interpolator = choreography.translateXValues.interpolator ?: choreography.interpolator
        choreography.translateYValues.interpolator = choreography.translateYValues.interpolator ?: choreography.interpolator
        choreography.translateZValues.interpolator = choreography.translateZValues.interpolator ?: choreography.interpolator
    }

    /**
     * Applies the multiplies for [AnimatedValue] toValue property for each of the animation properties
     * of the specified [Choreography].
     * @param choreography The choreography to which its multipliers are applied to.
     */
    private fun applyMultipliers(choreography: Choreography) {
        if (choreography.width.multiply != MAX_OFFSET) {
            choreography.width.toValue = choreography.width.fromValue * choreography.width.multiply
        }
        if (choreography.height.multiply != MAX_OFFSET) {
            choreography.height.toValue = choreography.height.fromValue * choreography.height.multiply
        }
        if (choreography.scaleX.multiply != MAX_OFFSET) {
            choreography.scaleX.toValue = choreography.scaleX.fromValue * choreography.scaleX.multiply
        }
        if (choreography.scaleY.multiply != MAX_OFFSET) {
            choreography.scaleY.toValue = choreography.scaleY.fromValue * choreography.scaleY.multiply
        }
        if (choreography.rotation.multiply != MAX_OFFSET) {
            choreography.rotation.toValue = choreography.rotation.fromValue * choreography.rotation.multiply
        }
        if (choreography.rotationX.multiply != MAX_OFFSET) {
            choreography.rotationX.toValue = choreography.rotationX.fromValue * choreography.rotationX.multiply
        }
        if (choreography.rotationY.multiply != MAX_OFFSET) {
            choreography.rotationY.toValue = choreography.rotationY.fromValue * choreography.rotationY.multiply
        }
    }

    /**
     * Applies the adders for [AnimatedValue] toValue property for each of the animation properties
     * of the specified [Choreography].
     * @param choreography The choreography to which its adders are applied to.
     */
    internal fun applyAdders(choreography: Choreography) {
        if (choreography.width.add != MIN_OFFSET) {
            choreography.width.toValue = choreography.width.fromValue + choreography.width.add
        }
        if (choreography.height.add != MIN_OFFSET) {
            choreography.height.toValue = choreography.height.fromValue + choreography.height.add
        }
        if (choreography.scaleX.add != MIN_OFFSET) {
            choreography.scaleX.toValue = choreography.scaleX.fromValue + choreography.scaleX.add
        }
        if (choreography.scaleY.add != MIN_OFFSET) {
            choreography.scaleY.toValue = choreography.scaleY.fromValue + choreography.scaleY.add
        }
        if (choreography.rotation.add != MIN_OFFSET) {
            choreography.rotation.toValue = choreography.rotation.fromValue + choreography.rotation.add
        }
        if (choreography.rotationX.add != MIN_OFFSET) {
            choreography.rotationX.toValue = choreography.rotationX.fromValue + choreography.rotationX.add
        }
        if (choreography.rotationY.add != MIN_OFFSET) {
            choreography.rotationY.toValue = choreography.rotationY.fromValue + choreography.rotationY.add
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
        val views = choreography.morphViews

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

        if (choreography.scaleX.canInterpolate) {
            view.morphScaleX = choreography.scaleX.fromValue + (choreography.scaleX.toValue - choreography.scaleX.fromValue) * scaleXFraction
        } else {
            if(choreography.scaleXValues.canInterpolate) {
                animateThroughPoints(choreography.scaleXValues, view, currentPlayTime, duration, scaleXListener)
            }
        }

        if (choreography.scaleY.canInterpolate) {
            view.morphScaleY = choreography.scaleY.fromValue + (choreography.scaleY.toValue - choreography.scaleY.fromValue) * scaleYFraction
        } else {
            if(choreography.scaleYValues.canInterpolate) {
                animateThroughPoints(choreography.scaleYValues, view, currentPlayTime, duration, scaleYListener)
            }
        }

        if (choreography.rotation.canInterpolate) {
            view.morphRotation = choreography.rotation.fromValue + (choreography.rotation.toValue - choreography.rotation.fromValue) * rotateFraction
        } else {
            if(choreography.rotationValues.canInterpolate) {
                animateThroughPoints(choreography.rotationValues, view, currentPlayTime, duration, rotationListener)
            }
        }

        if (choreography.rotationX.canInterpolate) {
            view.morphRotationX = choreography.rotationX.fromValue + (choreography.rotationX.toValue - choreography.rotationX.fromValue) * rotateXFraction
        } else {
            if(choreography.rotationXValues.canInterpolate) {
                animateThroughPoints(choreography.rotationXValues, view, currentPlayTime, duration, rotationXListener)
            }
        }

        if (choreography.rotationY.canInterpolate) {
            view.morphRotationY = choreography.rotationY.fromValue + (choreography.rotationY.toValue - choreography.rotationY.fromValue) * rotateYFraction
        } else {
            if(choreography.rotationYValues.canInterpolate) {
                animateThroughPoints(choreography.rotationYValues, view, currentPlayTime, duration, rotationYListener)
            }
        }

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
        }

        if (choreography.translateX.canInterpolate || choreography.translateY.canInterpolate) {
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

        if (choreography.translateZ.canInterpolate) {
            view.morphTranslationZ = choreography.translateZ.fromValue + (choreography.translateZ.toValue - choreography.translateZ.fromValue) * translateZFraction
        } else {
            if (choreography.translateZValues.canInterpolate) {
                animateThroughPoints(choreography.translateZValues, view, currentPlayTime, duration, translationZListener)
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
     * Animates the specified [MorphLayout] with the values specified on the [AnimatedFloatValueArray] to specified animation playtime.
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
     * properties a the specified morphViews.
     * @param choreographer The [Choreographer] used for animating this choreography
     * @param morphViews the morphViews which this choreography will animate. This assumes the premise that
     * all morphViews have similar layout properties.
     */
    class Choreography (
        var choreographer: Choreographer,
        internal vararg var morphViews: MorphLayout
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

        internal var scaleXValues: AnimatedFloatValueArray = AnimatedFloatValueArray(AnimatedValue.SCALE_X)
        internal var scaleYValues: AnimatedFloatValueArray = AnimatedFloatValueArray(AnimatedValue.SCALE_Y)

        internal var rotationValues: AnimatedFloatValueArray = AnimatedFloatValueArray(AnimatedValue.ROTATION)
        internal var rotationXValues: AnimatedFloatValueArray = AnimatedFloatValueArray(AnimatedValue.ROTATION_X)
        internal var rotationYValues: AnimatedFloatValueArray = AnimatedFloatValueArray(AnimatedValue.ROTATION_Y)

        internal var translateXValues: AnimatedFloatValueArray = AnimatedFloatValueArray(AnimatedValue.TRANSLATION_X)
        internal var translateYValues: AnimatedFloatValueArray = AnimatedFloatValueArray(AnimatedValue.TRANSLATION_Y)
        internal var translateZValues: AnimatedFloatValueArray = AnimatedFloatValueArray(AnimatedValue.TRANSLATION_Z)

        internal var progressListener: TransitionProgressListener = null

        internal var offsetTrigger: OffsetTrigger? = null

        internal var viewParentSize: Dimension = Dimension()

        internal var pivotPoint: Coordinates = Coordinates()

        internal var controlPoint: Coordinates? = null

        internal var doneAction: ChoreographerAction = null
        internal var startAction: ChoreographerAction = null

        /**
         * The easing interpolator used by this [Choreography]
         */
        internal var interpolator: TimeInterpolator? = null

        internal var stagger: AnimationStagger? = null

        internal var stretch: Stretch? = null

        internal var reveal: Reveal? = null
        internal var conceal: Conceal? = null

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
            properties[scaleXValues.propertyName] = scaleXValues
            properties[scaleYValues.propertyName] = scaleYValues
            properties[rotationValues.propertyName] = rotationValues
            properties[rotationXValues.propertyName] = rotationXValues
            properties[rotationYValues.propertyName] = rotationYValues
            properties[translateXValues.propertyName] = translateXValues
            properties[translateYValues.propertyName] = translateYValues
            properties[translateZValues.propertyName] = translateZValues
            return properties
        }

        /**
         * Sets the values for all properties using the given map of properties.
         * @param properties Map of all the properties to be set.
         */
        @Suppress("UNCHECKED_CAST")
        internal fun setStartProperties(properties: Map<String, AnimatedValue<*>>?) {
            if (properties == null)
                return

            scaleX.set(properties[scaleX.propertyName] as AnimatedFloatValue)
            scaleY.set(properties[scaleY.propertyName] as AnimatedFloatValue)
            rotation.set(properties[rotation.propertyName] as AnimatedFloatValue)
            rotationX.set(properties[rotationX.propertyName] as AnimatedFloatValue)
            rotationY.set(properties[rotationY.propertyName] as AnimatedFloatValue)
            positionX.set(properties[positionX.propertyName] as AnimatedFloatValue)
            positionY.set(properties[positionY.propertyName] as AnimatedFloatValue)
            paddings.set(properties.getValue(paddings.propertyName) as AnimatedValue<Padding>)
            margings.set(properties.getValue(margings.propertyName) as AnimatedValue<Margin>)
            translateX.set(properties[translateX.propertyName] as AnimatedFloatValue)
            translateY.set(properties[translateY.propertyName] as AnimatedFloatValue)
            translateZ.set(properties[translateZ.propertyName] as AnimatedFloatValue)
            width.set(properties[width.propertyName] as AnimatedFloatValue)
            height.set(properties[height.propertyName] as AnimatedFloatValue)
            alpha.set(properties[alpha.propertyName] as AnimatedFloatValue)
            color.set(properties.getValue(color.propertyName) as AnimatedValue<Color>)
            cornerRadii.set(properties.getValue(cornerRadii.propertyName) as AnimatedValue<CornerRadii>)
        }

        /**
         * Sets the values for all properties sets using the given map of properties sets.
         * @param properties Map of all the properties sets to be set.
         */
        @Suppress("UNCHECKED_CAST")
        internal fun setStartPropertyHolders(properties: Map<String, AnimatedValueArray<*>>?) {
            if (properties == null)
                return

            scaleXValues.values = properties.getValue(scaleXValues.propertyName).values as Array<Float>
            scaleYValues.values = properties.getValue(scaleYValues.propertyName).values as Array<Float>
            rotationValues.values = properties.getValue(rotationValues.propertyName).values as Array<Float>
            rotationXValues.values = properties.getValue(rotationXValues.propertyName).values as Array<Float>
            rotationYValues.values = properties.getValue(rotationYValues.propertyName).values as Array<Float>
            translateXValues.values = properties.getValue(translateXValues.propertyName).values as Array<Float>
            translateYValues.values = properties.getValue(translateYValues.propertyName).values as Array<Float>
            translateZValues.values = properties.getValue(translateZValues.propertyName).values as Array<Float>
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
            this.scaleXValues.values.reverse()
            this.scaleYValues.values.reverse()
            this.rotationValues.values.reverse()
            this.rotationXValues.values.reverse()
            this.rotationYValues.values.reverse()
            this.translateXValues.values.reverse()
            this.translateYValues.values.reverse()
            this.translateZValues.values.reverse()
        }

        /**
         * Applies the default values for each of the animation properties.
         * the values are assigned using the values of all the animateable properties
         * of the morphViews to be animated by this [Choreography]
         */
        private fun applyDefaultValues() {
            if (morphViews.isNotEmpty()) {
                morphViews[0].let {
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
            this.duration = choreographer.defaultDuration
            this.interpolator = choreographer.interpolator
            this.choreographer.defaultPivot?.let {
                this.pivotPoint.x = resolvePivot(Pivot.RELATIVE_TO_SELF, it.x, width.fromValue, viewParentSize.width)
                this.pivotPoint.y = resolvePivot(Pivot.RELATIVE_TO_SELF, it.y, height.fromValue, viewParentSize.height)
            }
        }

        /**
         * Resets each of the morphViews held by this [Choreography] to their initial
         * pre animation state.
         */
        fun resetProperties() {
            if (morphViews.isNotEmpty()) {
                morphViews.forEach {
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
         * Animates the morphViews of this [Choreography] to the left of the specified [MorphLayout] with
         * the specified margin. Optionally uses the specified [TimeInterpolator] if any is present.
         * @param otherView The layout to which the choreography will animate its morphViews to the left of
         * @param margin The margin to use between the choreography morphViews and the specified layout.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun toLeftOf(otherView: MorphLayout, margin: Float = MIN_OFFSET, interpolator: TimeInterpolator? = null): Choreography {
            val view = morphViews[0]
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
         * Animates the morphViews of this [Choreography] to the right of the specified [MorphLayout] with
         * the specified margin. Optionally uses the specified [TimeInterpolator] if any is present.
         * @param otherView The layout to which the choreography will animate its morphViews to the right of
         * @param margin The margin to use between the choreography morphViews and the specified layout.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun toRightOf(otherView: MorphView, margin: Float = MIN_OFFSET, interpolator: TimeInterpolator? = null): Choreography {
            val view = morphViews[0]
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
         * Animates the morphViews of this [Choreography] to the top of the specified [MorphLayout] with
         * the specified margin. Optionally uses the specified [TimeInterpolator] if any is present.
         * @param otherView The layout to which the choreography will animate its morphViews to the top of
         * @param margin The margin to use between the choreography morphViews and the specified layout.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun toTopOf(otherView: MorphView, margin: Float = MIN_OFFSET, interpolator: TimeInterpolator? = null): Choreography {
            val view = morphViews[0]
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
         * Animates the morphViews of this [Choreography] to the bottm of the specified [MorphLayout] with
         * the specified margin. Optionally uses the specified [TimeInterpolator] if any is present.
         * @param otherView The layout to which the choreography will animate its morphViews to the bottom of
         * @param margin The margin to use between the choreography morphViews and the specified layout.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun toBottomOf(otherView: MorphView, margin: Float = MIN_OFFSET, interpolator: TimeInterpolator? = null): Choreography {
            val view = morphViews[0]
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
         * Animates the morphViews of this [Choreography] to the specified bounds.
         * @param bounds The bounds to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun positionAt(bounds: Bounds, interpolator: TimeInterpolator? = null): Choreography {
            val startX: Float = bounds.x + ((bounds.width / 2f) - (morphViews[0].morphWidth / 2f))
            val startY: Float = bounds.y + ((bounds.height / 2f) - (morphViews[0].morphHeight / 2f))

            val endX: Float = morphViews[0].windowLocationX.toFloat()
            val endY: Float = morphViews[0].windowLocationY.toFloat()

            val translationX: Float = abs(endX - startX)
            val translationY: Float = abs(endY - startY)

            this.translateX.toValue =  if (startX < endX) -translationX else translationX
            this.translateY.toValue =  if (startY < endY) -translationY else translationY

            this.translateX.interpolator = interpolator
            this.translateY.interpolator = interpolator

            return this
        }

        /**
         * Animates the morphViews of this [Choreography] to the specified x position.
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
         * Animates the morphViews of this [Choreography] to the specified X position from the specified X position.
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
         * Animates the morphViews of this [Choreography] to the specified X position value property
         * @param value The property to use for animating the X value of this choreography
         * @return this choreography.
         */
        fun xPosition(value: AnimatedFloatValue): Choreography {
            this.positionX.copy(value)
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] to the specified Y position.
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
         * Animates the morphViews of this [Choreography] to the specified Y position from the specified Y position.
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
         * Animates the morphViews of this [Choreography] to the specified Y position value property.
         * The position.
         * @param value The property to use for animating the Y value of this choreography
         * @return this choreography.
         */
        fun yPosition(value: AnimatedFloatValue): Choreography {
            this.positionY.copy(value)
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the specified points see: [FloatPoint].
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
         * Animates the morphViews of this [Choreography] between the specified points see: [FloatPoint].
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
         * Animates the morphViews of this [Choreography] between the X translation values created
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
         * Animates the morphViews of this [Choreography] between the X translation values created
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
         * Animates the morphViews of this [Choreography] between the specified X translation values.
         * Uses the default interpolator if any is present.
         * @param values the values to translate between.
         * @return this choreography.
         */
        fun xTranslateBetween(vararg values: Float): Choreography {
            translateXValues.values = values.toTypedArray()
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the specified X translation values.
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
         * Animates the morphViews of this [Choreography] to the specified X translation.
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
         * Animates the morphViews of this [Choreography] to the specified X translation from the specified X translation.
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
         * Animates the morphViews of this [Choreography] to the specified X translation value property
         * @param value The property to use for animating the X value of this choreography
         * @return this choreography.
         */
        fun xTranslate(value: AnimatedFloatValue): Choreography {
            this.translateX.copy(value)
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the Y translation values created
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
         * Animates the morphViews of this [Choreography] between the Y translation values created
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
         * Animates the morphViews of this [Choreography] between the specified Y translation values.
         * Uses the default interpolator if any is present.
         * @param values the values to translate between.
         * @return this choreography.
         */
        fun yTranslateBetween(vararg values: Float): Choreography {
            translateYValues.values = values.toTypedArray()
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the specified Y translation values.
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
         * Animates the morphViews of this [Choreography] to the specified Y translation.
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
         * Animates the morphViews of this [Choreography] to the specified Y translation from the specified Y translation.
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
         * Animates the morphViews of this [Choreography] to the specified Y translation value property
         * @param value The property to use for animating the Y value of this choreography
         * @return this choreography.
         */
        fun yTranslate(value: AnimatedFloatValue): Choreography {
            this.translateY.copy(value)
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the Z translation values created
         * by mapping to the specified percentages. Int based percentages are used.
         * Ex: 0%, 50%, 120%
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography
         */
        fun zTranslateBetween(value: Float, percentages: IntArray, interpolator: TimeInterpolator? = null): Choreography {
            val mapped = percentages.map { it / 100f }.toTypedArray().toFloatArray()
            return zTranslateBetween(value, mapped, interpolator)
        }

        /**
         * Animates the morphViews of this [Choreography] between the Z translation values created
         * by mapping to the specified percentages. Float based percentages are used where
         * 0.5f equals 50% and so on.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography
         */
        fun zTranslateBetween(value: Float, percentages: FloatArray, interpolator: TimeInterpolator? = null): Choreography {
            val output: ArrayList<Float> = ArrayList()
            for (percentage in percentages) {
                output.add(value * percentage)
            }
            translateZValues.interpolator = interpolator
            translateZValues.values = output.toTypedArray()
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the specified Z translation values.
         * Uses the default interpolator if any is present.
         * @param values the values to translate between.
         * @return this choreography.
         */
        fun zTranslateBetween(vararg values: Float): Choreography {
            translateZValues.values = values.toTypedArray()
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the specified Z translation values.
         * @param values the values to translate between.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun zTranslateBetween(interpolator: TimeInterpolator? = null, vararg values: Float): Choreography {
            translateZValues.values = values.toTypedArray()
            translateZValues.interpolator = interpolator
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] to the specified Z translation.
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
         * Animates the morphViews of this [Choreography] to the specified Z translation from the specified Z translation.
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
         * Animates the morphViews of this [Choreography] to the specified Z translation value property
         * @param value The property to use for animating the Z value of this choreography
         * @return this choreography.
         */
        fun zTranslate(value: AnimatedFloatValue): Choreography {
            this.translateZ.copy(value)
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] to the specified coordinates using arc translation
         * The control point is auto calculated if no control point has been specified.
         * @param coordinates The coordinates to arc translate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography
         */
        fun arcTranslateTo(coordinates: Coordinates, interpolator: TimeInterpolator? = null): Choreography {
            return arcTranslateTo(coordinates.x, coordinates.y, interpolator)
        }
        /**
         * Animates the morphViews of this [Choreography] to the specified X and Y translation values using arc translation
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
         * Animates the alpha value of the morphViews of this [Choreography] to the specified alpha value.
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
         * Animates the alpha value of the morphViews of this [Choreography] from the specified alpha value
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
         * Animates the alpha value of the morphViews of this [Choreography] to the specified alpha value.
         * The alpha value is specified as a percentage where 50 is 50 percent opacity
         * @param alpha The alpha value to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun alphaTo(alpha: Int, interpolator: TimeInterpolator? = null): Choreography {
            return this.alphaTo(alpha.clamp(0, 100) / 100f, interpolator)
        }

        /**
         * Animates the alpha value of the morphViews of this [Choreography] from the specified alpha value
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
         * Animates the alpha value of the morphViews of this [Choreography] using the specified animated
         * alpha value property. See [AnimatedFloatValue]
         * @param value The property to use for this animation.
         * @return this choreography.
         */
        fun alpha(value: AnimatedFloatValue): Choreography {
            this.alpha.copy(value)
            return this
        }

        /**
         * Animates the corner radius of the morphViews of this [Choreography] to the specified [CornerRadii].
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
         * Animates the corner radius of the specified corners of the morphViews of this [Choreography] to the specified value.
         * @param corners The corners which value is to be animated.
         * @param radius The radius to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun cornerRadiusTo(corners: CornersSet, radius: Float, interpolator: TimeInterpolator? = null): Choreography {
            for (corner in corners) {
                cornerRadiusTo(corner, radius, interpolator)
            }
            return this
        }

        /**
         * Animates the corner radius of the specified corners of the morphViews of this [Choreography] to the specified value.
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
         * Animates the corner radius of the specified corners of the morphViews of this [Choreography] from the specified value.
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
         * Animates the corner radius of the specified corners of the morphViews of this [Choreography] from the specified value.
         * to the specified value
         * @param cornerValue The corner property to use fo this animation.
         * @return this choreography.
         */
        fun cornerRadius(cornerValue: AnimatedValue<CornerRadii>): Choreography {
            cornerRadii.copy(cornerValue)
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the rotation values created
         * by mapping to the specified percentages. Int based percentages are used.
         * Ex: 0%, 50%, 120%
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography
         */
        fun rotateBetween(value: Float, percentages: IntArray, interpolator: TimeInterpolator? = null): Choreography {
            val mapped = percentages.map { it / 100f }.toTypedArray().toFloatArray()
            return rotateBetween(value, mapped, interpolator)
        }

        /**
         * Animates the morphViews of this [Choreography] between the rotation values created
         * by mapping to the specified percentages. Float based percentages are used where
         * 0.5f equals 50% and so on.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography
         */
        fun rotateBetween(value: Float, percentages: FloatArray, interpolator: TimeInterpolator? = null): Choreography {
            val output: ArrayList<Float> = ArrayList()
            for (percentage in percentages) {
                output.add(value * percentage)
            }
            rotationValues.interpolator = interpolator
            rotationValues.values = output.toTypedArray()
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the specified rotation values.
         * Uses the default interpolator if any is present.
         * @param values the values to rotate between.
         * @return this choreography.
         */
        fun rotateBetween(vararg values: Float): Choreography {
            rotationValues.values = values.toTypedArray()
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the specified rotation values.
         * @param values the values to rotate between.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun rotateBetween(interpolator: TimeInterpolator? = null, vararg values: Float): Choreography {
            rotationValues.values = values.toTypedArray()
            rotationValues.interpolator = interpolator
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] to the computed rotation value created
         * by adding the specified delta to the current rotation value. This causes the rotation value
         * to be increased/decreased with the specified amount.
         * @param delta The amount to add to the current rotation value
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun addRotation(delta: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotation.add += delta
            this.rotation.interpolator = interpolator
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] to the computed rotation value created
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
         * Animates the rotation value of the morphViews of this [Choreography] to the specified rotation value.
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
         * Animates the rotation value of the morphViews of this [Choreography] from the specified rotation value
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
         * Animates the rotation value of the morphViews of this [Choreography] using the specified animated
         * rotation value property. See [AnimatedFloatValue]
         * @param value The property to use for this animation.
         * @return this choreography.
         */
        fun rotate(value: AnimatedFloatValue): Choreography {
            this.rotation.copy(value)
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the X rotation values created
         * by mapping to the specified percentages. Int based percentages are used.
         * Ex: 0%, 50%, 120%
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography
         */
        fun xRotateBetween(value: Float, percentages: IntArray, interpolator: TimeInterpolator? = null): Choreography {
            val mapped = percentages.map { it / 100f }.toTypedArray().toFloatArray()
            return xRotateBetween(value, mapped, interpolator)
        }

        /**
         * Animates the morphViews of this [Choreography] between the X rotation values created
         * by mapping to the specified percentages. Float based percentages are used where
         * 0.5f equals 50% and so on.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography
         */
        fun xRotateBetween(value: Float, percentages: FloatArray, interpolator: TimeInterpolator? = null): Choreography {
            val output: ArrayList<Float> = ArrayList()
            for (percentage in percentages) {
                output.add(value * percentage)
            }
            rotationXValues.interpolator = interpolator
            rotationXValues.values = output.toTypedArray()
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the specified X rotation values.
         * Uses the default interpolator if any is present.
         * @param values the values to X rotate between.
         * @return this choreography.
         */
        fun xRotateBetween(vararg values: Float): Choreography {
            rotationXValues.values = values.toTypedArray()
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the specified X rotation values.
         * @param values the values to X rotate between.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun xRotateBetween(interpolator: TimeInterpolator? = null, vararg values: Float): Choreography {
            rotationXValues.values = values.toTypedArray()
            rotationXValues.interpolator = interpolator
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] to the computed X rotation value created
         * by adding the specified delta to the current X rotation value. This causes the X rotation value
         * to be increased/decreased with the specified amount.
         * @param delta The amount to add to the current X rotation value
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun xRotateAdd(delta: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationX.add += delta
            this.rotationX.interpolator = interpolator
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] to the computed X rotation value created
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
         * Animates the X rotation value of the morphViews of this [Choreography] to the specified X rotation value.
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
         * Animates the X rotation value of the morphViews of this [Choreography] from the specified X rotation value
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
         * Animates the X rotation value of the morphViews of this [Choreography] using the specified animated
         * X rotation value property. See [AnimatedFloatValue]
         * @param value The property to use for this animation.
         * @return this choreography.
         */
        fun xRotate(value: AnimatedFloatValue): Choreography {
            this.rotationX.copy(value)
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the Y rotation values created
         * by mapping to the specified percentages. Int based percentages are used.
         * Ex: 0%, 50%, 120%
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography
         */
        fun yRotateBetween(value: Float, percentages: IntArray, interpolator: TimeInterpolator? = null): Choreography {
            val mapped = percentages.map { it / 100f }.toTypedArray().toFloatArray()
            return yRotateBetween(value, mapped, interpolator)
        }

        /**
         * Animates the morphViews of this [Choreography] between the Y rotation values created
         * by mapping to the specified percentages. Float based percentages are used where
         * 0.5f equals 50% and so on.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography
         */
        fun yRotateBetween(value: Float, percentages: FloatArray, interpolator: TimeInterpolator? = null): Choreography {
            val output: ArrayList<Float> = ArrayList()
            for (percentage in percentages) {
                output.add(value * percentage)
            }
            rotationYValues.interpolator = interpolator
            rotationYValues.values = output.toTypedArray()
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the specified Y rotation values.
         * Uses the default interpolator if any is present.
         * @param values the values to Y rotate between.
         * @return this choreography.
         */
        fun yRotateBetween(vararg values: Float): Choreography {
            rotationYValues.values = values.toTypedArray()
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the specified Y rotation values.
         * @param values the values to Y rotate between.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun yRotateBetween(interpolator: TimeInterpolator? = null, vararg values: Float): Choreography {
            rotationYValues.values = values.toTypedArray()
            rotationYValues.interpolator = interpolator
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] to the computed Y rotation value created
         * by adding the specified delta to the current Y rotation value. This causes the Y rotation value
         * to be increased/decreased with the specified amount.
         * @param delta The amount to add to the current Y rotation value
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun yRotateAdd(delta: Float, interpolator: TimeInterpolator? = null): Choreography {
            this.rotationY.add += delta
            this.rotationY.interpolator = interpolator
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] to the computed Y rotation value created
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
         * Animates the Y rotation value of the morphViews of this [Choreography] to the specified Y rotation value.
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
         * Animates the Y rotation value of the morphViews of this [Choreography] from the specified Y rotation value
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
         * Animates the Y rotation value of the morphViews of this [Choreography] using the specified animated
         * X rotation value property. See [AnimatedFloatValue]
         * @param value The property to use for this animation.
         * @return this choreography.
         */
        fun yRotate(value: AnimatedFloatValue): Choreography {
            this.rotationY.copy(value)
            return this
        }

        /**
         * Animates the scale value of the morphViews of this [Choreography] to the specified [Bounds] value.
         * @param bounds The bounds dimension to scale animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun scaleTo(bounds: Bounds, interpolator: TimeInterpolator? = null): Choreography {
            return scaleTo(bounds.dimension(), interpolator)
        }

        /**
         * Animates the scale value of the morphViews of this [Choreography] to the specified [Dimension] value.
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
         * Animates the morphViews of this [Choreography] to the computed scale value created
         * by adding the specified delta to the current X and Y scale value. This causes the scale values
         * to be increased/decreased with the specified amount.
         * @param delta The amount to add to the current X and Y scale value
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun scaleAdd(delta: Float, interpolator: TimeInterpolator? = null): Choreography {
            xScaleAdd(delta, interpolator)
            yScaleAdd(delta, interpolator)
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] to the computed scale value created
         * by multiplying the specified delta to the current X and Y scale value. This causes the scale values
         * to be increased/decreased with the specified amount.
         * @param multiplier The amount to add to the current X and Y scale value
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun scaleBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography {
            xScaleBy(multiplier, interpolator)
            yScaleBy(multiplier, interpolator)
            return this
        }

        /**
         * Animates the X and Y scale value of the morphViews of this [Choreography] to the specified scale value.
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
         * Animates the X and Y scale value of the morphViews of this [Choreography] from the specified scale value
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
         * Animates the morphViews of this [Choreography] between the scale X values created
         * by mapping to the specified percentages. Int based percentages are used.
         * Ex: 0%, 50%, 120%
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography
         */
        fun xScaleBetween(value: Float, percentages: IntArray, interpolator: TimeInterpolator? = null): Choreography {
            val mapped = percentages.map { it / 100f }.toTypedArray().toFloatArray()
            return xScaleBetween(value, mapped, interpolator)
        }

        /**
         * Animates the morphViews of this [Choreography] between the scale X values created
         * by mapping to the specified percentages. Float based percentages are used where
         * 0.5f equals 50% and so on.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography
         */
        fun xScaleBetween(value: Float, percentages: FloatArray, interpolator: TimeInterpolator? = null): Choreography {
            val output: ArrayList<Float> = ArrayList()
            for (percentage in percentages) {
                output.add(value * percentage)
            }
            scaleXValues.interpolator = interpolator
            scaleXValues.values = output.toTypedArray()
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the specified scale X values.
         * Uses the default interpolator if any is present.
         * @param values the values to scale X between.
         * @return this choreography.
         */
        fun xScaleBetween(vararg values: Float): Choreography {
            scaleXValues.values = values.toTypedArray()
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the specified scale X values.
         * @param values the values to scale X between.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun xScaleBetween(interpolator: TimeInterpolator? = null, vararg values: Float): Choreography {
            scaleXValues.values = values.toTypedArray()
            scaleXValues.interpolator = interpolator
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] to the computed X scale value created
         * by adding the specified delta to the current X scale value. This causes the scale value
         * to be increased/decreased with the specified amount.
         * @param delta The amount to add to the current X scale value
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun xScaleAdd(delta: Float, interpolator: TimeInterpolator?): Choreography {
            this.scaleX.add += delta
            this.scaleX.interpolator = interpolator
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] to the computed X scale value created
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
         * Animates the X scale value of the morphViews of this [Choreography] to the specified X scale value.
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
         * Animates the X scale value of the morphViews of this [Choreography] from the specified X scale value
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
         * Animates the X scale value of the morphViews of this [Choreography] using the specified animated
         * X scale value property. See [AnimatedFloatValue]
         * @param value The property to use for this animation.
         * @return this choreography.
         */
        fun xScale(value: AnimatedFloatValue): Choreography {
            this.scaleX.copy(value)
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the scale Y values created
         * by mapping to the specified percentages. Int based percentages are used.
         * Ex: 0%, 50%, 120%
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography
         */
        fun yScaleBetween(value: Float, percentages: IntArray, interpolator: TimeInterpolator? = null): Choreography {
            val mapped = percentages.map { it / 100f }.toTypedArray().toFloatArray()
            return yScaleBetween(value, mapped, interpolator)
        }

        /**
         * Animates the morphViews of this [Choreography] between the scale Y values created
         * by mapping to the specified percentages. Float based percentages are used where
         * 0.5f equals 50% and so on.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography
         */
        fun yScaleBetween(value: Float, percentages: FloatArray, interpolator: TimeInterpolator? = null): Choreography {
            val output: ArrayList<Float> = ArrayList()
            for (percentage in percentages) {
                output.add(value * percentage)
            }
            scaleYValues.interpolator = interpolator
            scaleYValues.values = output.toTypedArray()
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the specified scale Y values.
         * Uses the default interpolator if any is present.
         * @param values the values to scale Y between.
         * @return this choreography.
         */
        fun yScaleBetween(vararg values: Float): Choreography {
            scaleYValues.values = values.toTypedArray()
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] between the specified scale Y values.
         * @param values the values to scale Y between.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun yScaleBetween(interpolator: TimeInterpolator? = null, vararg values: Float): Choreography {
            scaleYValues.values = values.toTypedArray()
            scaleYValues.interpolator = interpolator
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] to the computed Y scale value created
         * by adding the specified delta to the current Y scale value. This causes the scale value
         * to be increased/decreased with the specified amount.
         * @param delta The amount to add to the current X scale value
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun yScaleAdd(delta: Float, interpolator: TimeInterpolator?): Choreography {
            this.scaleY.add += delta
            this.scaleY.interpolator = interpolator
            return this
        }

        /**
         * Animates the morphViews of this [Choreography] to the computed Y scale value created
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
         * Animates the Y scale value of the morphViews of this [Choreography] to the specified Y scale value.
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
         * Animates the Y scale value of the morphViews of this [Choreography] from the specified Y scale value
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
         * Animates the Y scale value of the morphViews of this [Choreography] using the specified animated
         * Y scale value property. See [AnimatedFloatValue]
         * @param value The property to use for this animation.
         * @return this choreography.
         */
        fun yScale(value: AnimatedFloatValue): Choreography {
            this.scaleY.copy(value)
            return this
        }

        /**
         * Animate the [Bounds] (Dimensions and Coordinates) of the morphViews of this [Choreography] using the
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
         * Animate the size (Width and/or Height properties) of the morphViews of this [Choreography] using the
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
         * Animates the morphViews of this [Choreography] to the computed size (Width, Height) value created
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
         * Animates the size (Width, Height) values of the morphViews of this [Choreography] to the specified [Bounds] value.
         * @param bounds The bounds to animate to.
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun resizeTo(bounds: Bounds, interpolator: TimeInterpolator? = null): Choreography {
            return resizeTo(bounds.dimension(), interpolator)
        }

        /**
         * Animates the size (Width, Height) values of the morphViews of this [Choreography] to the specified [Dimension] value.
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
         * Animates the size (Width and/or Height) values of the morphViews of this [Choreography] to the specified [Dimension] value
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
         * Animates the size (Width and/or Height) values of the morphViews of this [Choreography] from the specified [Dimension] value
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
         * Animates the size (Width and/or Height) values of the morphViews of this [Choreography] with the specified Size animation
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
         * Animates the specified [Margin] value of the morphViews of this [Choreography] to the specified value
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
         * Animates the specified [Margin] value of the morphViews of this [Choreography] from the specified value
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
         * Animates the specified [Margin] value of the morphViews of this [Choreography] with the specified animation
         * value property. See [AnimatedValue]
         * @param margin The property to use for this animation.
         * @return this choreography.
         */
        fun margin(margin: AnimatedValue<Margings>): Choreography {
            this.margings.copy(margin)
            return this
        }

        /**
         * Animates the specified [Padding] value of the morphViews of this [Choreography] to the specified value
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
         * Animates the specified [Padding] value of the morphViews of this [Choreography] from the specified value
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
         * Animates the specified [Padding] value of the morphViews of this [Choreography] with the specified animation
         * value property. See [AnimatedValue]
         * @param padding The property to use for this animation.
         * @return this choreography.
         */
        fun padding(padding: AnimatedValue<Paddings>): Choreography {
            this.paddings.copy(padding)
            return this
        }

        /**
         * Animates the color value of the morphViews of this [Choreography] to the specified color value.
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
         * Animates the color value of the morphViews of this [Choreography] from the specified color value
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
         * Animates the color value of the morphViews of this [Choreography] using the specified animated
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
         * Animates the color value of the morphViews of this [Choreography] to the specified color value.
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
         * Animates the color value of the morphViews of this [Choreography] from the specified color value
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
         * Arc animates the position of the morphViews of this [Choreography] to the specified position of the specified [Anchor].
         * in relation to the specified view: [MorphLayout]. If no arc translation control point has been specified it will
         * then been computed upon building. If a margin offset is used the the morphViews will position at the
         * anchor point with the given margin offset.
         * @param anchor The position to animate the position to
         * @param view The view to animate relative to.
         * @param margin The offset distance to add from the absolute anchor to the animated morphViews
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun anchorArcTo(anchor: Anchor, view: MorphLayout, margin: Float = MIN_OFFSET, interpolator: TimeInterpolator? = null): Choreography{
            this.useArcTranslator = true
            return anchorTo(anchor, view, margin, interpolator)
        }

        /**
         * Animates the position of the morphViews of this [Choreography] to the specified position of the specified [Anchor].
         * in relation to the specified view: [MorphLayout]. If a margin offset is used the the morphViews will position at the
         * anchor point with the given margin offset.
         * @param anchor The position to animate the position to
         * @param view The view to animate relative to.
         * @param margin The offset distance to add from the absolute anchor to the animated morphViews
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun anchorTo(anchor: Anchor, view: MorphLayout, margin: Float = MIN_OFFSET, interpolator: TimeInterpolator? = null): Choreography{
            val bounds = view.viewBounds

            var startX: Float = bounds.x.toFloat()
            var startY: Float = bounds.y.toFloat()

            var endX: Float = morphViews[0].windowLocationX.toFloat()
            var endY: Float = morphViews[0].windowLocationY.toFloat()

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

                    endX = morphViews[0].windowLocationX.toFloat()
                    endY = morphViews[0].windowLocationY.toFloat()

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
         * Animates the background of the morphViews of this [Choreography] to the specified [Background]
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
            withPivotY(pivotY, type)
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
         * Specifies a [TransitionProgressListener] to use for this [Choreography]. The listener
         * is notified by the progress of the animation being perform by this choreography with
         * a percent fraction from 0f to 1f
         * @param progressListener The listener to notify.
         * @return this choreography.
         */
        fun withProgressListener(progressListener: TransitionProgressListener): Choreography {
            this.progressListener = progressListener
            return this
        }

        /**
         * Specifies an [OffsetTrigger] to use for this [Choreography]. The trigger will execute
         * its specified event: [OffsetTrigger.triggerAction] when the animation has reached the
         * specified trigger offset: [OffsetTrigger.percentage]. A trigger can only be activated
         * once.
         * @param offsetTrigger The trigger to use.
         * @return this choreography.
         */
        fun withOffsetTrigger(offsetTrigger: OffsetTrigger): Choreography {
            this.offsetTrigger = offsetTrigger
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
         * @param stretch The stretch property to use for stretching and squashing the morphViews
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
         * Specifies the stagger value to use for animating through the morphViews for this [Choreography].
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
         * Specifies the stagger animation see: [AnimationStagger] to use for animating through the morphViews for this [Choreography].
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
         * Specifies the circular reveal to use for revealing the morphViews of this [Choreography]. The
         * reveal will happen with the radius and center point of the specified view.
         * @param view the view from which the reveal will happen.
         * @param interpolator the interpolator to use for this animation.
         * @param onEnd the action to perform at the end of the reveal.
         * @return this choreography.
         */
        fun revealFrom(view: View, interpolator: TimeInterpolator? = null, onEnd: Action = null): Choreography {
            this.reveal = Reveal(view, morphViews[0].getView())
            this.reveal?.interpolator = interpolator
            this.reveal?.onEnd = onEnd
            return this
        }

        /**
         * Specifies the circular reveal to use for revealing the morphViews of this [Choreography]. The
         * reveal will happen with the specified center coordinates and radius.
         * @param centerX the initial horizontal center coordinate of the reveal
         * @param centerY the initial vertical center coordinate of the reveal
         * @param radius the initial radius of the reveal
         * @param interpolator the interpolator to use for this animation.
         * @param onEnd the action to perform at the end of the reveal.
         * @return this choreography.
         */
        fun revealFrom(centerX: Float, centerY: Float, radius: Float, interpolator: TimeInterpolator? = null, onEnd: Action = null): Choreography {
            this.reveal = Reveal(centerX, centerY, radius, morphViews[0].getView())
            this.reveal?.interpolator = interpolator
            this.reveal?.onEnd = onEnd
            return this
        }

        /**
         * Specifies the circular reveal to use for revealing the morphViews of this [Choreography]. The
         * reveal will happen with the specified center coordinates and radius.
         * @param coordinates the location from where the reveal will happen.
         * @param radius the initial radius of the reveal
         * @param interpolator the interpolator to use for this animation.
         * @param onEnd the action to perform at the end of the reveal.
         * @return this choreography.
         */
        fun revealFrom(coordinates: Coordinates, radius: Float, interpolator: TimeInterpolator? = null, onEnd: Action = null): Choreography {
            this.reveal = Reveal(coordinates.x, coordinates.y, radius, morphViews[0].getView())
            this.reveal?.interpolator = interpolator
            this.reveal?.onEnd = onEnd
            return this
        }

        /**
         * Specifies the circular reveal to use for revealing the morphViews of this [Choreography]. The
         * reveal will happen with the specified relative offsets and radius.
         * @param offsetX the x location offset within the view. 0.5f == the horizontal center of the view
         * @param offsetY the Y location offset within the view. 0.5f == the vertical center of the view
         * @param radius the initial radius of the reveal
         * @param interpolator the interpolator to use for this animation.
         * @param onEnd the action to perform at the end of the reveal.
         * @return this choreography.
         */
        fun revealWith(offsetX: Float, offsetY: Float, radius: Float, interpolator: TimeInterpolator? = null, onEnd: Action = null): Choreography {
            val morphView = morphViews[0]
            val centerX = (morphView.morphWidth * offsetX)
            val centerY = (morphView.morphHeight * offsetY)
            this.reveal = Reveal(centerX, centerY, radius, morphView.getView())
            this.reveal?.interpolator = interpolator
            this.reveal?.onEnd = onEnd
            return this
        }

        /**
         * Specifies the circular reveal to use for revealing the morphViews of this [Choreography]. The
         * reveal will happen from the pivot location if any has been specified, otherwise the reveal will happen at
         * the center of the view being revealed.
         * @param interpolator the interpolator to use for this animation.
         * @param radius the initial radius of the reveal
         * @param onEnd the action to perform at the end of the reveal.
         * @return this choreography.
         */
        fun revealWith(radius: Float, interpolator: TimeInterpolator? = null, onEnd: Action = null): Choreography {
            val morphView = morphViews[0]
            this.reveal = Reveal(radius, morphView.getView())
            this.reveal?.interpolator = interpolator
            this.reveal?.onEnd = onEnd
            return this
        }

        /**
         * Specifies the circular [Reveal] to use for revealing the morphViews of this [Choreography]
         * @param reveal contains information on how to reveal the morphViews.
         * @return this choreography.
         */
        fun withReveal(reveal: Reveal): Choreography {
            this.reveal = reveal
            return this
        }

        /**
         * Specifies the circular conceal to use for concealing the morphViews of this [Choreography]. The
         * conceal will happen towards the radius and center point of the specified view.
         * @param view the view to which the conceal will happen.
         * @param interpolator the interpolator to use for this animation.
         * @param onEnd the action to perform at the end of the conceal.
         * @return this choreography.
         */
        fun concealFrom(view: View, interpolator: TimeInterpolator? = null, onEnd: Action = null): Choreography {
            this.conceal = Conceal(morphViews[0].getView(), view)
            this.conceal?.interpolator = interpolator
            this.conceal?.onEnd = onEnd
            return this
        }

        /**
         * Specifies the circular conceal to use for concealing the morphViews of this [Choreography]. The
         * conceal will happen towards the specified center coordinates and radius.
         * @param centerX the ending horizontal center coordinate of the conceal
         * @param centerY the ending vertical center coordinate of the conceal
         * @param radius the ending radius of the conceal
         * @param interpolator the interpolator to use for this animation.
         * @param onEnd the action to perform at the end of the conceal.
         * @return this choreography.
         */
        fun concealTo(centerX: Float, centerY: Float, radius: Float, interpolator: TimeInterpolator? = null, onEnd: Action = null): Choreography {
            this.conceal = Conceal(centerX, centerY, radius, morphViews[0].getView())
            this.conceal?.interpolator = interpolator
            this.conceal?.onEnd = onEnd
            return this
        }

        /**
         * Specifies the circular conceal to use for concealing the morphViews of this [Choreography]. The
         * conceal will happen towards the specified center coordinates and radius.
         * @param coordinates the location to where the conceal will end.
         * @param radius the ending radius of the conceal
         * @param interpolator the interpolator to use for this animation.
         * @param onEnd the action to perform at the end of the conceal.
         * @return this choreography.
         */
        fun concealFrom(coordinates: Coordinates, radius: Float, interpolator: TimeInterpolator? = null, onEnd: Action = null): Choreography {
            this.conceal = Conceal(coordinates.x, coordinates.y, radius, morphViews[0].getView())
            this.conceal?.interpolator = interpolator
            this.conceal?.onEnd = onEnd
            return this
        }

        /**
         * Specifies the circular conceal to use concealing the morphViews of this [Choreography]. The
         * conceal will happen towards the specified relative offsets and radius.
         * @param offsetX the x location offset within the view. 0.5f == the horizontal center of the view
         * @param offsetY the Y location offset within the view. 0.5f == the vertical center of the view
         * @param radius the ending radius of the conceal
         * @param interpolator the interpolator to use for this animation.
         * @param onEnd the action to perform at the end of the conceal.
         * @return this choreography.
         */
        fun concealWith(offsetX: Float, offsetY: Float, radius: Float, interpolator: TimeInterpolator? = null, onEnd: Action = null): Choreography {
            val morphView = morphViews[0]
            val centerX = (morphView.morphWidth * offsetX)
            val centerY = (morphView.morphHeight * offsetY)
            this.conceal = Conceal(centerX, centerY, radius, morphView.getView())
            this.conceal?.interpolator = interpolator
            this.conceal?.onEnd = onEnd
            return this
        }

        /**
         * Specifies the circular conceal to use for concealing the morphViews of this [Choreography]. The
         * conceal will happen towards the pivot location if any has been specified, otherwise the conceal will happen at
         * towards the center of the view being concealed.
         * @param interpolator the interpolator to use for this animation.
         * @param radius the ending radius of the conceal
         * @param onEnd the action to perform at the end of the conceal.
         * @return this choreography.
         */
        fun concealWith(radius: Float, interpolator: TimeInterpolator? = null, onEnd: Action = null): Choreography {
            val morphView = morphViews[0]
            this.conceal = Conceal(radius, morphView.getView())
            this.conceal?.interpolator = interpolator
            this.conceal?.onEnd = onEnd
            return this
        }

        /**
         * Specifies the circular [Conceal] to use for concealing the morphViews of this [Choreography]
         * @param conceal contains information on how to conceal the morphViews.
         * @return this choreography.
         */
        fun withConceal(conceal: Conceal): Choreography {
            this.conceal = conceal
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
         * Creates a [Choreography] for the latest given morphViews which will start at the duration
         * offset of its parent. An offset of 0.5f indicates that this choreography will play when
         * the animation of its parent is half way through. If no morphViews have been specified the
         * morphViews of the previous choreography will be used.
         * @param offset The offset at which this choreography will start animating.
         * @return this choreography.
         */
        fun after(offset: Float): Choreography {
            choreographer.applyAdders(this)
            choreographer.applyMultipliers(this)
            return choreographer.animateAfter(this, offset, *morphViews)
        }

        /**
         * Creates a [Choreography] for the given view which will start at the duration
         * offset of its parent. A value of 0.5f indicates that this choreography will play when
         * the animation of its parent is half way through. If no view have been specified the
         * morphViews of the previous choreography will be used.
         * @param offset The offset at which this choreography will start animating.
         * @param views The views which will be animated by this choreography.
         * @return this choreography.
         */
        fun animateAfter(offset: Float, vararg views: View? = emptyArray()): Choreography {
            val morphViews = choreographer.getViews(views, this.morphViews)
            return animateAfter(offset, *morphViews)
        }

        /**
         * Creates a [Choreography] for the given morphViews which will start at the duration
         * offset of its parent. A value of 0.5f indicates that this choreography will play when
         * the animation of its parent is half way through. If no morphViews have been specified the
         * morphViews of the previous choreography will be used.
         * @param offset The offset at which this choreography will start animating.
         * @param morphViews The morph layouts, see: [MorphLayout] which will be animated by this choreography.
         * @return this choreography.
         */
        fun animateAfter(offset: Float, vararg morphViews: MorphLayout = this.morphViews): Choreography {
            choreographer.applyAdders(this)
            choreographer.applyMultipliers(this)
            return choreographer.animateAfter(this, offset, *morphViews)
        }

        /**
         * Creates a [Choreography] for the last given morphViews which will start directly the animation of
         * its parent choreography is over. If no morphViews have been specified the morphViews of the previous
         * choreography will be used.
         * @return this choreography.
         */
        fun then(): Choreography {
            choreographer.applyAdders(this)
            choreographer.applyMultipliers(this)
            return choreographer.thenAnimate(this, *morphViews)
        }

        /**
         * Creates a [Choreography] for the given views which will start directly the animation of
         * its parent choreography is over. If no views have been specified the morphViews of the previous
         * choreography will be used.
         * @param views The views which will be animated by this choreography.
         * @return this choreography.
         */
        fun thenAnimate(vararg views: View? = emptyArray()): Choreography {
            val morphViews = choreographer.getViews(views, this.morphViews)
            choreographer.applyAdders(this)
            choreographer.applyMultipliers(this)
            return choreographer.thenAnimate(this, *morphViews)
        }

        /**
         * Creates a [Choreography] for the last given morphViews which will start directly at the same time
         * as its parent. If no morphViews have been specified the morphViews of the previous choreography will be used.
         * @return this choreography.
         */
        fun also(): Choreography {
            choreographer.applyAdders(this)
            choreographer.applyMultipliers(this)
            return choreographer.alsoAnimate(this, *morphViews)
        }

        /**
         * Creates a [Choreography] for the given views which will start directly at the same time
         * as its parent. If no views have been specified the morphViews of the previous choreography will be used.
         * @param views The views which will be animated by this choreography.
         * @return this choreography.
         */
        fun alsoAnimate(vararg views: View? = emptyArray()): Choreography {
            val morphViews = choreographer.getViews(views, this.morphViews)
            return alsoAnimate(*morphViews)
        }

        /**
         * Creates a [Choreography] for the given morphViews which will start directly at the same time
         * as its parent. If no morphViews have been specified the morphViews of the previous choreography will be used.
         * @param morphViews The morph layouts, see: [MorphLayout] which will be animated by this choreography.
         * @return this choreography.
         */
        fun alsoAnimate(vararg morphViews: MorphLayout = this.morphViews): Choreography {
            choreographer.applyAdders(this)
            choreographer.applyMultipliers(this)
            return choreographer.alsoAnimate(this, *morphViews)
        }

        /**
         * Creates a [Choreography] for the given views which will start directly at the same time
         * as its parent with the same properties as its parent unless specified otherwise. If no views
         * have been specified the morphViews of the previous choreography will be used.
         * @param views The views which will be animated by this choreography.
         * @return this choreography.
         */
        fun andAnimate(vararg views: View? = emptyArray()): Choreography {
            val morphViews = choreographer.getViews(views, this.morphViews)
            return andAnimate(*morphViews)
        }

        /**
         * Creates a [Choreography] for the given morphViews which will start directly at the same time
         * as its parent with the same properties as its parent unless specified otherwise. If no morphViews
         * have been specified the morphViews of the previous choreography will be used.
         * @param morphViews The morph layouts, see: [MorphLayout] which will be animated by this choreography.
         * @return this choreography.
         */
        fun andAnimate(vararg morphViews: MorphLayout = this.morphViews): Choreography {
            choreographer.applyAdders(this)
            choreographer.applyMultipliers(this)
            return choreographer.andAnimate(this, *morphViews)
        }

        /**
         * Creates a [Choreography] for the given views which will start directly after the specified duration
         * offset of its parent with the same properties as its parent unless specified otherwise. If no views
         * have been specified the morphViews of the previous choreography will be used.
         * @param views The views which will be animated by this choreography.
         * @return this choreography.
         */
        fun andAnimateAfter(offset: Float, vararg views: View? = emptyArray()): Choreography {
            val morphViews = choreographer.getViews(views, this.morphViews)
            return andAnimateAfter(offset, *morphViews)
        }

        /**
         * Creates a [Choreography] for the given morphViews which will start directly after the specified duration
         * offset of its parent with the same properties as its parent unless specified otherwise. If no morphViews
         * have been specified the morphViews of the previous choreography will be used.
         * @param morphViews The morph layouts, see: [MorphLayout] which will be animated by this choreography.
         * @return this choreography.
         */
        fun andAnimateAfter(offset: Float, vararg morphViews: MorphLayout = this.morphViews): Choreography {
            choreographer.applyAdders(this)
            choreographer.applyMultipliers(this)
            return choreographer.andAnimateAfter(this, offset, *morphViews)
        }

        /**
         * Creates a [Choreography] for the given views which will reverse the last choreography which was
         * assign to the same views if any. If the views have not been part of a previous choreography this
         * will do nothing. The animation will play upon the end of the animation of its parent.
         * If no views have been specified the morphViews of the previous choreography will be used.
         * @param views The views which will be animated by this choreography.
         * @return this choreography.
         */
        fun reverseAnimate(vararg views: View? = emptyArray()): Choreography {
            val morphViews = choreographer.getViews(views, this.morphViews)
            return reverseAnimate(*morphViews)
        }

        /**
         * Creates a [Choreography] for the given morphViews which will reverse the last choreography which was
         * assign to the same morphViews if any. If the morphViews have not been part of a previous choreography this
         * will do nothing. The animation will play upon the end of the animation of its parent.
         * If no morphViews have been specified the morphViews of the previous choreography will be used.
         * @param morphViews The morph layouts, see: [MorphLayout] which will be animated by this choreography.
         * @return this choreography.
         */
        fun reverseAnimate(vararg morphViews: MorphLayout = this.morphViews): Choreography {
            choreographer.applyAdders(this)
            choreographer.applyMultipliers(this)
            return choreographer.reverseAnimate(this, *morphViews)
        }

        /**
         * Creates a [Choreography] for the given views which will reverse the last choreography which was
         * assign to the same views if any. If the views have not been part of a previous choreography this
         * will do nothing. The animation will play at the same time as its parent and will clone its parents properties.
         * If no views have been specified the morphViews of the previous choreography will be used.
         * @param views The views which will be animated by this choreography.
         * @return this choreography.
         */
        fun andReverseAnimate(vararg views: View? = emptyArray()): Choreography {
            val morphViews = choreographer.getViews(views, this.morphViews)
            return andReverseAnimate(*morphViews)
        }

        /**
         * Creates a [Choreography] for the given morphViews which will reverse the last choreography which was
         * assign to the same morphViews if any. If the morphViews have not been part of a previous choreography this
         * will do nothing. The animation will play at the same time as its parent and will clone its parents properties.
         * If no morphViews have been specified the morphViews of the previous choreography will be used.
         * @param morphViews The morph layouts, see: [MorphLayout] which will be animated by this choreography.
         * @return this choreography.
         */
        fun andReverseAnimate(vararg morphViews: MorphLayout = this.morphViews): Choreography {
            choreographer.applyAdders(this)
            choreographer.applyMultipliers(this)
            return choreographer.andReverseAnimate(this, *morphViews)
        }

        /**
         * Creates a [Choreography] for the given children of the specified view which will start at the duration
         * offset of its parent. A value of 0.5f indicates that this choreography will play when
         * the animation of its parent is half way through. If a stagger is specified the morphViews will be animated
         * with the specified stagger.
         * @param offset The offset at which this choreography will start animating.
         * @param view The morph layouts, see: [MorphLayout] which children will be animated by this choreography.
         * @param stagger The stagger to use when animating the children. See [AnimationStagger]
         * @return this choreography.
         */
        fun animateChildrenOfAfter(view: MorphLayout, offset: Float, stagger: AnimationStagger? = null): Choreography {
            //TODO("Deal with the costly conversion")
            val children = view.getChildren().map { if (it is MorphLayout) it else MorphView.makeMorphable(it) }.toList().toTypedArray()
            return choreographer.animateChildrenOfAfter(this, offset, stagger, *children)
        }

        /**
         * Creates a [Choreography] for the given children of the specified view which will start when the animation
         * of the parent choreography is over. If a stagger is specified the morphViews will be animated
         * with the specified stagger.
         * @param view The morph layouts, see: [MorphLayout] which children will be animated by this choreography.
         * @param stagger The stagger to use when animating the children. See [AnimationStagger]
         * @return this choreography.
         */
        fun thenAnimateChildrenOf(view: MorphLayout, stagger: AnimationStagger? = null): Choreography {
            //TODO("Deal with the costly conversion")
            val children = view.getChildren().map { if (it is MorphLayout) it else MorphView.makeMorphable(it) }.toList().toTypedArray()
            return choreographer.thenAnimateChildrenOf(this, stagger, *children)
        }

        /**
         * Creates a [Choreography] for the given children of the specified view which will start when the animation
         * of the parent choreography starts. If a stagger is specified the morphViews will be animated
         * with the specified stagger.
         * @param view The morph layouts, see: [MorphLayout] which children will be animated by this choreography.
         * @param stagger The stagger to use when animating the children. See [AnimationStagger]
         * @return this choreography.
         */
        fun alsoAnimateChildrenOf(view: MorphLayout, stagger: AnimationStagger? = null): Choreography {
            //TODO("Deal with the costly conversion")
            val children = view.getChildren().map { if (it is MorphLayout) it else MorphView.makeMorphable(it) }.toList().toTypedArray()
            return choreographer.alsoAnimateChildrenOf(this, stagger, *children)
        }

        /**
         * Creates a [Choreography] for the given children of the specified view which will start when the animation
         * of the parent choreography starts. The properties of the parent choreography will be used by this choreography.
         * If a stagger is specified the morphViews will be animated with the specified stagger.
         * @param view The morph layouts, see: [MorphLayout] which children will be animated by this choreography.
         * @param stagger The stagger to use when animating the children. See [AnimationStagger]
         * @return this choreography.
         */
        fun andAnimateChildrenOf(view: MorphLayout, stagger: AnimationStagger? = null): Choreography {
            //TODO("Deal with the costly conversion")
            val children = view.getChildren().map { if (it is MorphLayout) it else MorphView.makeMorphable(it) }.toList().toTypedArray()
            return choreographer.andAnimateChildrenOf(this, stagger, *children)
        }

       /* *//**
         * Appends the specified [Choreography] to the tail choreography
         * of this [Choreographer]. The appended choreography will become part
         * of this choreographer and will play based on the properties which
         * were given upon creation and its creation method.
         *
         * - **Note:** When a choreography chain is appended the choreographer must be rebuilt, meaning you must call the
         * [Choreographer.build] function even when it has already been called.
         * - **Note:** This function returns the tail of the appended choreography.
         *
         * @param choreography the choreography to be appended.
         * @param returnCurrent if set to true the append choreography will be returned
         * otherwise the tail of the appended choreography will be return if it exists.
         * **Default:** *False* If this is set to *true* the current appended choreography
         * is returned and all of its predecessors will be dereferenced.
         *
         * @return this tail or the currently appended choreography.
         *//*
        fun append(choreography: Choreography, returnCurrent: Boolean = false): Choreography {
            this.child = choreography
            choreography.parent = this

            if (!returnCurrent) {
                return choreographer.getTail(choreography)
            }
            return choreography
        }

        *//**
         * Prepends the specified [Choreography] to this choreography.
         * The prepended choreography will become part of this choreographer
         * and it will play based on the properties which were given upon
         * creation and its creation method.
         *
         * - **Note:** When a choreography chain is prepended the choreographer must be rebuilt,
         * meaning you must call the [Choreographer.build] function even when
         * it has already been called.
         *
         * @param choreography the choreography to be prepended.
         * @return this choreography.
         *//*
        fun prepend(choreography: Choreography): Choreography {
            this.parent?.let {
                it.child = choreography
            }
            val tail = choreographer.getTail(choreography)

            tail.child = this
            this.parent = choreography

            return this
        }*/

        /**
         * A call to this function will build the current and all the previously appended choreographies.
         * This function must be called prior to starting the [Choreography] animation. Note that a
         * call to this function will not only built the current choreography but also all its predecessors.
         * A built choreography can be saved to played at a later time. The ability to build a
         * choreography helps to get rid of overhead.
         * @return the [Choreographer] which will animate this choreography.
         */
        fun build(): Choreographer {
            choreographer.applyAdders(this)
            choreographer.applyMultipliers(this)
            return choreographer.build()
        }

        /**
         * A call to this function will start the current and all the previously appended choreographies.
         * call to this function will not only start the current choreography but also all its predecessors.
         * If the choreographies are not yet build they will also be built.
         * choreography helps to get rid of overhead.
         */
        fun start() {
            choreographer.start()
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
            scaleXValues.copy(other.scaleXValues)
            scaleYValues.copy(other.scaleYValues)
            rotationValues.copy(other.rotationValues)
            rotationXValues.copy(other.rotationXValues)
            rotationYValues.copy(other.rotationYValues)
            translateXValues.copy(other.translateXValues)
            translateYValues.copy(other.translateYValues)
            translateZValues.copy(other.translateZValues)
            pivotPoint.copy(other.pivotPoint)
        }

        /**
         * Creates a clone of this [Choreography] with the specifies morphViews
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
            choreography.scaleXValues.copy(this.scaleXValues)
            choreography.scaleYValues.copy(this.scaleYValues)
            choreography.rotationValues.copy(this.rotationValues)
            choreography.rotationXValues.copy(this.rotationXValues)
            choreography.rotationYValues.copy(this.rotationYValues)
            choreography.translateXValues.copy(this.translateXValues)
            choreography.translateYValues.copy(this.translateYValues)
            choreography.translateZValues.copy(this.translateZValues)
            choreography.pivotPoint.copy(this.pivotPoint)
            choreography.duration = this.duration
            choreography.parent = null
            choreography.child = null

            return choreography
        }
    }

    private fun getViews(views: Array<out View?>, morphViews: Array<out MorphLayout>): Array<out MorphLayout> {
        return if (views.isNotEmpty()) {
            getViews(views)
        } else {
            morphViews
        }
    }

    private fun getViews(views: Array<out View?>): Array<out MorphLayout> {
        val newMorphViews = LinkedList<MorphLayout>()

        for(view in views) {
            if (view == null)
                continue

            val id = view.identity()
            if (!morphViewPool.containsKey(id)) {
                val morphView = if (view is MorphLayout) view else view.asMorphable()
                newMorphViews.add(morphView)
                morphViewPool[id] = morphView
            } else {
                newMorphViews.add(morphViewPool.getValue(id))
            }
        }
        return newMorphViews.toTypedArray()
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
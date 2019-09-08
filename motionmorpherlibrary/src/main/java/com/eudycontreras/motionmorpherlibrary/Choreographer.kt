package com.eudycontreras.motionmorpherlibrary

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.animation.addListener
import androidx.core.view.children
import com.eudycontreras.motionmorpherlibrary.enumerations.*
import com.eudycontreras.motionmorpherlibrary.extensions.*
import com.eudycontreras.motionmorpherlibrary.helpers.ArcTranslationHelper
import com.eudycontreras.motionmorpherlibrary.helpers.StaggerAnimationHelper
import com.eudycontreras.motionmorpherlibrary.helpers.StretchAnimationHelper
import com.eudycontreras.motionmorpherlibrary.layouts.MorphView
import com.eudycontreras.motionmorpherlibrary.listeners.AnimationProgressListener
import com.eudycontreras.motionmorpherlibrary.properties.*
import com.eudycontreras.motionmorpherlibrary.utilities.ColorUtility
import com.eudycontreras.motionmorpherlibrary.utilities.RevealUtility
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.abs
import android.graphics.drawable.ColorDrawable
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.properties.AnimatedValue
import com.eudycontreras.motionmorpherlibrary.properties.AnimatedValueArray
import android.os.Build
import android.os.Handler
import com.eudycontreras.motionmorpherlibrary.drawables.ParticleEffectDrawable
import com.eudycontreras.motionmorpherlibrary.particles.effects.RippleEffect
import com.google.android.material.ripple.RippleUtils


/**
 * Class which manages and creates complex choreographies. The choreographer
 * can be use for sequencing animations in any way desired. It allows the building of
 * complex animation choreographies that can then be stored and ran at a later time.
 * A [Choreography] is an animation sequence which happens for one or more views where
 * any property can be animated simultaneously. The choreographer holds a single animator
 * which animates the whole sequence of choreographies with an animated value from 0f to 1f.
 * This allows mapping choreographies to scroll and other interactions a fraction is available.
 *
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 30 2019
 */
class Choreographer(context: Context) {

    //TODO(" Figure out a way to determine stretch direction ")

    private lateinit var headChoreography: Choreography
    private lateinit var tailChoreography: Choreography

    private var animator: ValueAnimator = ValueAnimator.ofFloat(MIN_OFFSET, MAX_OFFSET)

    private var particleEffect: ParticleEffectDrawable = ParticleEffectDrawable()

    private val defaultInterpolator: TimeInterpolator = AccelerateDecelerateInterpolator()

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
     * animate all of its choreographies.
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
     * Default value: `TRUE`
     */
    var allowInheritance: Boolean = true
        private  set

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
     * added choreographer to this one.
     *
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
     * @return This choreographer.
     */
    fun allowChildInheritance(allow: Boolean = true): Choreographer {
        this.allowInheritance = allow
        return this
    }

    /**
     * Assigns a default duration to use. When the [Choreography] to animate has
     * no defined duration its own the default [duration] is used.
     *
     * @param duration The default duration to use.
     * @throws IllegalArgumentException Thrown when the offset is negative.
     * @return This choreographer.
     */
    fun withDefaultDuration(duration: Long): Choreographer {
        require(duration >= MIN_DURATION) { "Choreographer cannot have negative durations: $duration" }
        this.defaultDuration = duration
        return this
    }

    /**
     * Assigns a default pivot point to use. When the [Choreography] to animate has
     * no defined pivot point the default pivot coordinates will be used.
     *
     * @param point The default pivot point to use.
     * @return This choreographer.
     */
    fun withDefaultPivot(point: Point<Float>): Choreographer {
        this.defaultPivot = point
        return this
    }

    /**
     * Assigns a default pivot point location to use. When the [Choreography] to animate has
     * no defined pivot point of its own, the default pivot coordinates will be used.
     *
     * @param x The default pivot point X to use.
     * @param y The default pivot point Y to use.
     * @return This choreographer.
     */
    fun withDefaultPivot(x: Float, y: Float): Choreographer {
        this.defaultPivot = FloatPoint(x, y)
        return this
    }

    /**
     * Assigns a default easing [TimeInterpolator] to use. When the [Choreography] to animate has
     * no defined interpolator of its own this interpolator will be used..
     *
     * @param interpolator The default interpolator to use
     * @return This choreographer.
     */
    fun withDefaultInterpolator(interpolator: TimeInterpolator): Choreographer {
        this.interpolator = interpolator
        return this
    }

    /**
     * Creates the initial head [Choreography] for the given [View]. The head
     * is the first choreography to be played in a choreography sequence.
     *
     * @param views The view which are to be animated by this choreography.
     * @param block The encapsulation block of the created choreography
     * @return This choreographer.
     */
    fun animate(vararg views: View?, block: Choreography.() -> Unit): Choreographer {
        return animate(morphViews = *getViews(views), block = block)
    }

    /**
     * Creates the initial head [Choreography] for the given [View]. The head
     * is the first choreography to be played in a choreography sequence.
     *
     * @param morphViews The view which are to be animated by this choreography.
     * @param block The encapsulation block of the created choreography
     * @return This choreographer.
     */
    fun animate(vararg morphViews: MorphLayout, block: Choreography.() -> Unit): Choreographer {
        this.headChoreography = Choreography(this, *morphViews)
        this.headChoreography.offset = MAX_OFFSET
        this.tailChoreography = headChoreography

        applyAdders(tailChoreography)
        applyMultipliers(tailChoreography)
        block(headChoreography)

        return this
    }

    /**
     * Creates a [Choreography] for the given [MorphLayout]. With a specified parent choreography.
     * The choreography will play after the specified offset.
     *
     * @param choreography The parent choreography of the current.
     * @param views The morphViews to which the choreography belongs to.
     * @param offset The time offset to use. The current choreography will play after the
     * parent choreography has animated to the specified offset.
     * @throws IllegalArgumentException Thrown when the offset is negative.
     * @return The created choreography.
     */
    private fun animateFor(choreography: Choreography, offset: Float, allowInheritance: Boolean = this.allowInheritance, vararg views: MorphLayout): Choreographer {
        val properties = getProperties(choreography, *views)

        require(offset >= MIN_OFFSET) { "A duration offset may not be less than zero: $offset" }

        tailChoreography =  Choreography(this, *views).apply {
            this.setStartProperties(properties)
            this.parent = choreography
            this.offset = offset
            this.offsetDelayAlpha = (this.offset * choreography.duration).toLong()
            this.offsetDelayDelta = ((MAX_OFFSET - this.offset) * choreography.duration).toLong()
            if (allowInheritance) {
                this.duration = choreography.duration
                this.interpolator = choreography.interpolator
                this.pivotPoint = choreography.pivotPoint
                this.controlPoint = choreography.controlPoint
            }
            choreography.child = this
        }
        return this
    }

    /**
     * Reverse animates the specified [Choreography] to its initial state for the specified morphViews.
     * The reverse animation will occur after the parent animation is done animating. Only the latest
     * choreography for the specified view will be reversed.
     *
     * @param choreography The parent choreography of the current.
     * @param views The morphViews to which the choreography belongs to.
     * @throws IllegalArgumentException Thrown when the offset is negative.
     * @return The created choreography.
     */
    internal fun reverseAnimateFor(choreography: Choreography, offset: Float, vararg views: MorphLayout): Choreographer {
        var oldChoreography: Choreography? = null

        require(offset >= MIN_OFFSET) { "A duration offset may not be less than zero: $offset" }

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
            this.offset = offset
            this.reverseToStartState = true
            this.parent = choreography
            this.child = null
            choreography.child = this
        }
        return this
    }

    /**
     * Creates a [Choreography] for the latest given views which will start after the specified
     * duration offset relative to its parent. An offset of 0.5f indicates that this choreography will play when
     * the animation of its parent is 50 percent done.
     *
     * @param offset The offset at which this choreography will start animating.
     * @param block The encapsulation block of the created choreography
     * @return This choreographer.
     */
    fun after(offset: Float, block: Choreography.() -> Unit): Choreographer {
        return animateAfter(offset = offset, morphViews = *tailChoreography.morphViews, block = block)
    }

    /**
     * Creates a [Choreography] for the specified views which will start after the specified
     * duration offset relative to its parent. An offset of 0.5f indicates that this choreography will play when
     * the animation of its parent is 50 percent done. If no views have been specified the
     * views of the previous choreography will be used.
     *
     * @param offset The offset at which this choreography will start animating.
     * @param block The encapsulation block of the created choreography
     * @return This choreographer.
     */
    fun animateAfter(offset: Float, vararg views: View? = emptyArray(), block: Choreography.() -> Unit): Choreographer {
        val morphViews = getViews(views, tailChoreography.morphViews)
        return animateAfter(offset = offset, morphViews = *morphViews, block = block)
    }

    /**
     * Creates a [Choreography] for the specified views which will start after the specified
     * duration offset relative to its parent. An offset of 0.5f indicates that this choreography will play when
     * the animation of its parent is 50 percent done. If no views have been specified the
     * views of the previous choreography will be used.
     *
     * @param offset The offset at which this choreography will start animating.
     * @param block The encapsulation block of the created choreography
     * @return This choreographer.
     */
    fun animateAfter(offset: Float, vararg morphViews: MorphLayout = tailChoreography.morphViews, block: Choreography.() -> Unit): Choreographer {
        applyAdders(tailChoreography)
        applyMultipliers(tailChoreography)
        animateFor(tailChoreography, offset, allowInheritance, *morphViews)
        block(tailChoreography)
        return this
    }

    /**
     * Creates a [Choreography] for the views of the last choreography. The choreography will start directly
     * after the animation of its parent choreography is done.
     * @param block The encapsulation block of the created choreography.
     * @return This choreographer.
     */
    fun then(block: Choreography.() -> Unit): Choreographer {
        return thenAnimate(morphViews = *tailChoreography.morphViews, block = block)
    }

    /**
     * Creates a [Choreography] for the views of the last choreography. The choreography will start directly
     * after the animation of its parent choreography is done. If no views have been specified the views of the previous
     * choreography will be used. See: [View]
     *
     * @param views The views which will be animated by this choreography.
     * @param block The encapsulation block of the created choreography.
     * @return this choreographer.
     */
    fun thenAnimate(vararg views: View? = emptyArray(), block: Choreography.() -> Unit): Choreographer {
        return thenAnimate(morphViews = *getViews(views, tailChoreography.morphViews), block = block)
    }

    /**
     * Creates a [Choreography] for the views of the last choreography. The choreography will start directly
     * after the animation of its parent choreography is done. If no views have been specified the views of the previous
     * choreography will be used. See: [MorphLayout]
     *
     * @param views The views which will be animated by this choreography.
     * @param block The encapsulation block of the created choreography.
     * @return This choreographer.
     */
    fun thenAnimate(vararg morphViews: MorphLayout = emptyArray(), block: Choreography.() -> Unit): Choreographer {
        applyAdders(tailChoreography)
        applyMultipliers(tailChoreography)
        animateFor(tailChoreography, MAX_OFFSET, allowInheritance, *morphViews)
        block(tailChoreography)
        return this
    }

    /**
     * Creates a [Choreography] for the views of the last choreography which will start its animation
     * at the same time as its parent choreography unless a start delay is specified.
     *
     * @param block The encapsulation block of the created choreography.
     * @return This choreographer.
     */
    fun and(block: Choreography.() -> Unit): Choreographer {
        return andAnimate(morphViews = *tailChoreography.morphViews, block = block)
    }

    /**
     * Creates a [Choreography] for the given view which will start directly after the animation of
     * its parent choreography is over. If no views have been specified the views of the previous
     * choreography will be used. See: [View]
     *
     * @param views The views which will be animated by this choreography.
     * @param block The encapsulation block of the created choreography.
     * @return This choreographer.
     */
    fun andAnimate(vararg views: View? = emptyArray(), block: Choreography.() -> Unit): Choreographer {
        return andAnimate(morphViews = *getViews(views, tailChoreography.morphViews), block = block)
    }

    /**
     * Creates a [Choreography] for the given view which will start directly after the animation of
     * its parent choreography is over. If no views have been specified the views of the previous
     * choreography will be used. See: [View]
     *
     * @param views The views which will be animated by this choreography.
     * @param block The encapsulation block of the created choreography.
     * @return This choreographer.
     */
    fun andAnimate(vararg morphViews: MorphLayout = emptyArray(), block: Choreography.() -> Unit): Choreographer {
        applyAdders(tailChoreography)
        applyMultipliers(tailChoreography)
        animateFor(tailChoreography, MIN_OFFSET, allowInheritance, *morphViews)
        block(tailChoreography)
        return this
    }

    /**
     * Creates a [Choreography] for the given views which will reverse the last choreography which was
     * assign to the same views if any is present. If the views have not been part of a previous choreography this
     * will do nothing. The animation will play upon the end of the animation of its parent.
     * If no views have been specified the views of the previous choreography will be used.
     *
     * @param views The views which will be animated by this choreography.
     * @param block The encapsulation block of the created choreography.
     * @return This choreographer.
     */
    fun thenReverse(block: (Choreography.() -> Unit)? = null): Choreographer {
        return thenReverseAnimate(morphViews = *tailChoreography.morphViews, block = block)
    }

    /**
     * Creates a [Choreography] for the given views which will reverse the last choreography which was
     * assign to the same views if any is present. If the views have not been part of a previous choreography this
     * will do nothing. The animation will play upon the end of the animation of its parent.
     * If no views have been specified the views of the previous choreography will be used.
     *
     * @param views The views which will be animated by this choreography.
     * @param block The encapsulation block of the created choreography.
     * @return This choreographer.
     */
    fun thenReverseAnimate(vararg views: View? = emptyArray(), block: (Choreography.() -> Unit)? = null): Choreographer {
        val morphViews = getViews(views, tailChoreography.morphViews)
        return thenReverseAnimate(morphViews = *morphViews, block = block)
    }

    /**
     * Creates a [Choreography] for the given views which will reverse the last choreography which was
     * assign to the same views if any is present. If the views have not been part of a previous choreography this
     * will do nothing. The animation will play upon the end of the animation of its parent.
     * If no views have been specified the views of the previous choreography will be used.
     *
     * @param views The views which will be animated by this choreography.
     * @param block The encapsulation block of the created choreography.
     * @return This choreographer.
     */
    fun thenReverseAnimate(vararg morphViews: MorphLayout = tailChoreography.morphViews, block: (Choreography.() -> Unit)? = null): Choreographer {
        applyAdders(tailChoreography)
        applyMultipliers(tailChoreography)
        reverseAnimateFor(tailChoreography, MAX_OFFSET, *morphViews)
        block?.invoke(tailChoreography)
        return this
    }

    /**
     * Creates a [Choreography] for the given views which will reverse the last choreography which was
     * assign to the same views if any is present. If the views have not been part of a previous choreography this
     * will do nothing. The animation will play at the same time as its parent and will clone its parents properties.
     *
     * @param block The encapsulation block of the created choreography.
     * @param block The encapsulation block of the created choreography.
     * @return This choreographer.
     */
    fun andReverse(block: (Choreography.() -> Unit)? = null): Choreographer {
        return andReverseAnimate(morphViews = *tailChoreography.morphViews, block = block)
    }

    /**
     * Creates a [Choreography] for the given views which will reverse the last choreography which was
     * assign to the same views if any is present. If the views have not been part of a previous choreography this
     * will do nothing. The animation will play at the same time as its parent and will clone its parents properties.
     * If no views have been specified the morphViews of the previous choreography will be used.
     *
     * @param views The views which will be animated by this choreography.
     * @param block The encapsulation block of the created choreography,
     * @return This choreographer.
     */
    fun andReverseAnimate(vararg views: View? = emptyArray(), block: (Choreography.() -> Unit)? = null): Choreographer {
        val morphViews = getViews(views, tailChoreography.morphViews)
        return andReverseAnimate(morphViews = *morphViews, block = block)
    }

    /**
     * Creates a [Choreography] for the given morphViews which will reverse the last choreography which was
     * assign to the same morphViews if any. If the morphViews have not been part of a previous choreography this
     * will do nothing. The animation will play at the same time as its parent and will clone its parents properties.
     * If no morphViews have been specified the morphViews of the previous choreography will be used.
     *
     * @param morphViews The morph layouts, see: [MorphLayout] which will be animated by this choreography.
     * @param block The encapsulation block of the created choreography,
     * @return This choreographer.
     */
    fun andReverseAnimate(vararg morphViews: MorphLayout = tailChoreography.morphViews, block: (Choreography.() -> Unit)? = null): Choreographer {
        applyAdders(tailChoreography)
        applyMultipliers(tailChoreography)
        reverseAnimateFor(tailChoreography, MIN_OFFSET, *morphViews)
        block?.invoke(tailChoreography)
        return this
    }

    /**
     * Creates and animation [Choreography] for the children of the specified [MorphLayout]. The
     * the animation can optionally play with a specified animation stagger. This
     * function will do nothing if the specified view has no children. When this is the
     * case the current head choreograpy will be returned
     *
     * @param view the [MorphLayout] to which the children belong to.
     * @param block The encapsulation block of the created choreography
     * @return This choreographer.
     */
    fun animateChildrenOf(view: MorphLayout, block: Choreography.() -> Unit): Choreographer {
        if (!view.hasChildren())
            return this

        val children = getViews(view.getChildren().toArrayList().toTypedArray())
        return animate(morphViews = *children, block = block)
    }

    /**
     * Creates and animation [Choreography] for the children of the specified [ViewGroup]. The
     * the animation can optionally play with a specified animation stagger. This
     * function will do nothing if the specified view has no children. When this is the
     * case the current head choreograpy will be returned
     *
     * @param view the [ViewGroup] to which the children belong to.
     * @param block The encapsulation block of the created choreography
     * @return This choreographer.
     */
    fun animateChildrenOf(view: ViewGroup, block: Choreography.() -> Unit): Choreographer {
        if(view.childCount <= 0)
            return this

        val morphViews = getViews(view.children.toArrayList().toTypedArray())
        animate(morphViews = *morphViews, block = block)

        return this
    }

    /**
     * Creates a [Choreography] for the given children of the specified view which will start at the duration
     * offset of its parent. A value of 0.5f indicates that this choreography will play when
     * the animation of its parent is half way through. If a stagger is specified the morphViews will be animated
     * with the specified stagger.
     *
     * @param offset The offset at which this choreography will start animating.
     * @param view The morph layouts, see: [MorphLayout] which children will be animated by this choreography.
     * @param block The encapsulation block of the created choreography.
     * @return This choreographer.
     */
    fun animateChildrenOfAfter(offset: Float, view: MorphLayout, block: Choreography.() -> Unit): Choreographer {
        val children = getViews(view.getChildren().toArrayList().toTypedArray())
        return animateAfter(offset = offset, morphViews = *children, block = block)
    }

    /**
     * Creates a [Choreography] for the given children of the specified view which will start at the duration
     * offset of its parent. A value of 0.5f indicates that this choreography will play when
     * the animation of its parent is half way through. If a stagger is specified the morphViews will be animated
     * with the specified stagger.
     *
     * @param offset The offset at which this choreography will start animating.
     * @param view The morph layouts, see: [MorphLayout] which children will be animated by this choreography.
     * @param block The encapsulation block of the created choreography.
     * @return This choreographer.
     */
    fun animateChildrenOfAfter(view: ViewGroup, offset: Float, block: Choreography.() -> Unit): Choreographer {
        val children = getViews(view.children.toArrayList().toTypedArray())
        return animateAfter(offset = offset, morphViews = *children, block = block)
    }

    /**
     * Creates a [Choreography] for the given children of the specified view which will start when the animation
     * of the parent choreography is over. If a stagger is specified the morphViews will be animated
     * with the specified stagger.
     *
     * @param view The morph layouts, see: [MorphLayout] which children will be animated by this choreography.
     * @param block The encapsulation block of the created choreography.
     * @return This choreographer.
     */
    fun thenAnimateChildrenOf(view: MorphLayout, block: Choreography.() -> Unit): Choreographer {
        val children = getViews(view.getChildren().toArrayList().toTypedArray())
        return thenAnimate(morphViews = *children, block = block)
    }

    /**
     * Creates a [Choreography] for the given children of the specified view which will start when the animation
     * of the parent choreography is over. If a stagger is specified the morphViews will be animated
     * with the specified stagger.
     * @param view The morph layouts, see: [MorphLayout] which children will be animated by this choreography.
     * @param stagger The stagger to use when animating the children. See [AnimationStagger]
     * @return This choreographer.
     */
    fun thenAnimateChildrenOf(view: ViewGroup, block: Choreography.() -> Unit): Choreographer {
        val children = getViews(view.children.toArrayList().toTypedArray())
        return thenAnimate(morphViews = *children, block = block)
    }

    /**
     * Creates a [Choreography] for the given children of the specified view which will start when the animation
     * of the parent choreography starts. If a stagger is specified the morphViews will be animated
     * with the specified stagger.
     * @param view The morph layouts, see: [MorphLayout] which children will be animated by this choreography.
     * @param stagger The stagger to use when animating the children. See [AnimationStagger]
     * @return This choreographer.
     */
    fun alsoAnimateChildrenOf(view: MorphLayout, block: Choreography.() -> Unit): Choreographer {
        val children = getViews(view.getChildren().toArrayList().toTypedArray())
        return andAnimate(morphViews = *children, block = block)
    }

    /**
     * Creates a [Choreography] for the given children of the specified view which will start when the animation
     * of the parent choreography starts. If a stagger is specified the morphViews will be animated
     * with the specified stagger.
     *
     * @param view The morph layouts, see: [MorphLayout] which children will be animated by this choreography.
     * @param block The encapsulation block of the created choreography.
     * @return This choreographer.
     */
    fun alsoAnimateChildrenOf(view: ViewGroup, block: Choreography.() -> Unit): Choreographer {
        val children = getViews(view.children.toArrayList().toTypedArray())
        return andAnimate(morphViews = *children, block = block)
    }

    /**
     * Creates a [Choreography] for the given children of the specified view which will start when the animation
     * of the parent choreography starts. The properties of the parent choreography will be used by this choreography.
     * If a stagger is specified the morphViews will be animated with the specified stagger.
     *
     * @param view The morph layouts, see: [MorphLayout] which children will be animated by this choreography.
     * @param block The encapsulation block of the created choreography.
     * @return This choreographer.
     */
    fun andAnimateChildrenOf(view: MorphLayout, block: Choreography.() -> Unit): Choreographer {
        val children = getViews(view.getChildren().toArrayList().toTypedArray())
        return andAnimate(morphViews = *children, block = block)
    }

    /**
     * Fetches a map of the properties for the given morphViews. The passed [Choreography] is traversed from
     * bottom to top in order to find the choreography which belongs to specified [MorphLayout]. When the choreography
     * is found a map created containing all the animation properties.
     *
     * @param choreography The choreography to traverse from.
     * @param views The morphViews to which the choreography requested belongs to.
     * @return A map of the animation properties with their respective property name.
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
     * Helper method used for traversing the predecessors of the given [Choreography].
     *
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
            if (traverseControl.skipCurrent) {
                traverseControl.skipCurrent = false
                continue
            }
            temp = temp.parent
        }
    }

    /**
     * Helper method used for traversing the successors of the given [Choreography].
     *
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
            if (traverseControl.skipCurrent) {
                traverseControl.skipCurrent = false
                continue
            }
            temp = temp.child
        }
    }

    /**
     * Traverses the successors of the specified [Choreography] in order to
     * find the head. If the choreography has no child the specified
     * choreography is returned.
     *
     * @param choreography The choreography to traverse.
     * @return The tail of the specified choreography
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
     *
     * @param choreography The choreography to traverse.
     * @return The head of the specified choreography
     */
    private tailrec fun getHead(choreography: Choreography): Choreography {
        if (choreography.parent != null) {
            return getHead(choreography.parent!!)
        }
        return choreography
    }

    /**
     * If the [views] array is not empty, a collection of morphview for each view in the
     * array will be returned. Otherwise the [morphViews] will be returned.
     *
     * @param views The views whose morphviews are to be returned
     * @param morphViews the default morphviews to return otherwise.
     * @return The morphlayout representation of the specified views or the default ones.
     */
    private fun getViews(views: Array<out View?>, morphViews: Array<out MorphLayout>): Array<out MorphLayout> {
        return if (views.isNotEmpty()) {
            getViews(views)
        } else {
            morphViews
        }
    }

    /**
     * Retrieves the corresponding [MorphLayout] for the specified views from the morph layout pool.
     * If the the views do not exist in the pool they are converted to Morph layouts and
     * which are then added to the pool and returned.
     *
     * @param views The views to be converted or returned as morph layouts.
     * @return The morphlayout representation of the specified views.
     */
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
     * Appends the head [Choreography] of the specified [Choreographer] to the
     * tail choreography of this Choreographer. The appended choreography will become part
     * of this choreographer and will play based on the properties which
     * were given upon its creation.
     *
     * - **Note:** When a choreography chain is appended the choreographer must be rebuilt, meaning you must call the
     * [Choreographer.buildAll] function even when it has already been called before.
     *
     * @param choreographer The choreographer whose head choreography is to be appended.
     * @param offset The new appended head will choereography be added with a default offset of 1f, meaning
     * that the appended sequence will play after the end of the previous. This parameter specifies
     * at what offset to play the animation.
     * @return This choreographer.
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
     * were given upon its creation .
     *
     * - **Note:** When a choreography chain is prepended the choreographer must be rebuilt, meaning you must call the
     * [Choreographer.build] function even when it has already been called.
     *
     * @param choreographer the choreographer whose tail choreography is to be prepended.
     * @param offset the offset at which the old head should start animating. Usually
     * the offset for the head is 0f. The default value for this parameter is 1f which means
     * that the old head choereography will start animating after the animation from the tail of the prepended
     * choreography chain is done animating.
     * @return This choreographer.
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
     * Plays all the choreographies within this [Choreographer].
     * A start delay may be specified. The current start value for the [startDelay]
     * is 0L. The play function uses a single animator in order to animate the
     * entire choreography sequence. This animator is started and returned.
     *
     * @param startDelay The time to wait before playing the choreography sequence.
     */
    fun play(duration: Long = totalDuration, startDelay: Long = MIN_DURATION): ValueAnimator{
        return play(headChoreography, duration = duration, startDelay = startDelay, traverse = true)
    }

    /**
     * Plays the animation for the specified [Choreography]. This function
     * creates the value animator which will be use for animating through the
     * choreography sequence with the compute total duration.
     *
     * @param choreography The choreography which is to be started/play.
     * @param startDelay The time to wait before playing the choreography sequence
     * @param traverse Determines whether the successors of the given choreography
     * should and be played
     * @return The create value animator.
     */
    private fun play(
        choreography: Choreography,
        duration: Long = totalDuration,
        startDelay: Long = MIN_DURATION,
        repeatMode: Int = 0,
        repeatCount: Int = 0,
        playReversed: Boolean = false,
        startFraction: Float = MIN_OFFSET,
        endFraction: Float = MAX_OFFSET,
        traverse: Boolean = true
    ): ValueAnimator{

        if (!built) {
            tailChoreography.build()
        }

        animator.cancel()
        animator.setCurrentFraction(MIN_OFFSET)

        successors(headChoreography) { _, it ->
            it.control.reset()
        }

        animator = ValueAnimator.ofFloat(startFraction, endFraction)

        animator.interpolator = null
        animator.startDelay = startDelay
        animator.duration = duration
        animator.repeatMode = repeatMode
        animator.repeatCount = repeatCount
        animator.addUpdateListener {
            val fraction = (it.animatedValue as Float)
            transitionTo(choreography, fraction, traverse)
        }
        animator.addListener(
            onStart = { transitionTo(choreography, MIN_OFFSET, traverse) },
            onEnd = { transitionTo(choreography, MAX_OFFSET, traverse) }
        )
        if (playReversed) {
            animator.reverse()
        } else {
            animator.start()
        }

        return animator
    }

    /**
     * Clears the choreographies for this [Choreographer].
     * A call to this function will reset this choreographer to
     * its initial state by destroying all of its choreographies
     * and by canceling any currently ongoing choreographies.
     * @return This choreographer.
     */
    fun clear(): Choreographer {
        if (!::tailChoreography.isInitialized)
            return this

        animator.cancel()

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
     * Resets properties for all the choreographies contained within this [Choreographer].
     *
     * @return This choreographer.
     */
    fun reset(): Choreographer {
        predecessors(tailChoreography) { _, choreography ->
            choreography.resetProperties()
        }
        return this
    }

    /**
     * Resets all the [Choreography] for the specified [MorphLayout].
     * If the specified view is not present in this choreographer a call
     * to this function will do nothing.
     *
     * @param view The view which will have its choreography reset.
     * @return This choreographer.
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
     * Resets all the [Choreography] for the specified [View].
     * If the specified view is not present in this choreographer a call
     * to this function will do nothing.
     *
     * @param view The view which will have its choreography reset.
     * @return This choreographer.
     */
    fun reset(view: View): Choreographer {
        val target = morphViewPool[view.id]

        if (target == null)
            return this

        predecessors(tailChoreography) { _, choreography ->
            choreography.morphViews.forEach { v ->
                if (target == v) {
                    choreography.resetProperties()
                }
            }
        }
        return this
    }

    /**
     * Builds the [Choreographer] by applying the defined values of each [Choreography]
     * and prepares the choreographies to be played. The build process is and must be called prior
     * to the start of the choreographer and each of its choreographies. This process allows
     * for the heavy process of building a choreography to be done prior to the point at
     * which it will be played. The build process traverses the head choreography all the way to
     * its tail and it calculates the durations and start times for each of the choreographies and
     * it defines an animation control for each one of them. See: [ChoreographyControl].
     *
     * @return The choreographer being used.
     */
    fun build(): Choreographer {
        if (!built) {
            tailChoreography.build()
            built = false
        }
        return this
    }

    /**
     * Builds the [Choreographer] by applying the defined values of each [Choreography]
     * and prepares the choreographies to be played. The build process is and must be called prior
     * to the start of the choreographer and each of its choreographies. This process allows
     * for the heavy process of building a choreography to be done prior to the point at
     * which it will be played. The build process traverses the head choreography all the way to
     * its tail and it calculates the durations and start times for each of the choreographies and
     * it defines an animation control for each one of them. See: [ChoreographyControl].
     *
     * @return The choreographer being used.
     */
    internal fun buildAll(): Choreographer {

        var totalDelay: Long = MIN_DURATION
        var totalDuration: Long = MIN_DURATION
        var lastDuration: Long = MIN_DURATION

        successors(headChoreography) { _, current ->

            applyReveal(current)
            applyConceal(current)
            applyArcType(current)
            createStagger(current)
            buildImageMorph(current)
            applyInterpolators(current)

            val trim: Long = current.offsetDelayDelta
            val delay: Long = abs(lastDuration - trim)

            totalDuration += (current.duration.toFloat() * (current.child?.offset ?: MAX_OFFSET)).toLong() + current.delay
            totalDelay += ((current.parent?.duration ?: MIN_DURATION).toFloat() * current.offset).toLong()
            lastDuration = abs(current.duration - trim)
            totalDelay += current.delay

            val start: Float = if (current.reverseToStartState) MAX_OFFSET else MIN_OFFSET
            val end: Float = if (current.reverseToStartState) MIN_OFFSET else MAX_OFFSET

            current.control = ChoreographyControl(defaultInterpolator, current, start, end)

            if (current.reverse) {
                current.control.repeatMode = ChoreographyControl.REVERSE
                current.control.repeatCount = 1
            }

            val updateListener = AnimationProgressListener { fraction, playTime ->
                animate(current, fraction, current.duration, playTime)

                current.progressListener?.invoke(fraction)
                current.offsetTrigger?.listenTo(fraction)
            }

            val startListener: Action = {
                current.startAction?.invoke(current)
                current.isRunning = true

                current.reveal?.let {
                    RevealUtility.circularReveal(it)
                }

                current.conceal?.let {
                    RevealUtility.circularConceal(it)
                }

                current.ripple?.let {
                    var view = current.views.last().getView()
                    view.overlay.add(particleEffect.setupWith(RippleEffect(it)))
                }
            }

            val endListener: Action = {
                current.endAction?.invoke(current)
                current.isRunning = false
                current.done = true
            }

            current.control.offsetDelay = (delay + current.delay)
            current.control.duration = current.duration
            current.control.startDelay = totalDelay
            current.control.updateListener = updateListener
            current.control.startListener = startListener
            current.control.endListener = endListener
        }

        totalDuration += lastDuration

        successors(headChoreography) { _, choreography ->
            val startOffset: Float = (choreography.control.startDelay.toFloat() / totalDuration.toFloat())
            val endOffset: Float = ((choreography.control.duration).toFloat() / totalDuration.toFloat()) + startOffset

            choreography.control.offsetStart = startOffset
            choreography.control.offsetEnd = endOffset
        }

        this.totalDuration = totalDuration
        this.built = true

        return this
    }

    /**
     * Applies the arc translation control point for the specified [Choreography]
     *
     * @param choreography The choreography to which its control point is applied to.
     */
    private fun applyArcType(choreography: Choreography) {
        choreography.arcType?.let {
            choreography.createControlPoint(
                choreography.positionX,
                choreography.positionY,
                it
            )
        }
    }

    /**
     * Creates the stagger info objects used for creating the stagger
     * effect used for animating the views within a [Choreography].
     *
     * See: [StaggerInfo]
     * @param choreography The choreography to which its control point is applied to.
     */
    private fun createStagger(choreography: Choreography) {
        choreography.stagger?.let {
            choreography.createStagger(
                it
            )
        }
    }

    /**
     * Builds the [BitmapMorph] for this [Choreography] when a bitmap morph
     * is available.
     *
     * See: [BitmapMorph]
     * @param choreography The choreography to which its control point is applied to.
     */
    private fun buildImageMorph(choreography: Choreography) {
        choreography.bitmapMorph?.build()
    }

    /**
     * Applies the reveal for the specified [Choreography]. The reveal
     * is applied to the choreography if it contains a specified [Reveal].
     *
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
     *
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
     * is applied the choreography if it contains a specified Interpolator. If the Choreography
     * When a property within a choreography has a defined interpolator, the property interpolator
     * takes precdence.
     *
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
     * Applies the multipliers for [AnimatedValue] toValue property for each of the animation properties
     * of the specified [Choreography].
     *
     * @param choreography The choreography to which its adders are applied to.
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
     *
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

    private var lastValue: Float = MIN_OFFSET
    /**
     * Animates the choreographies to the specified animation percentage/fraction recursively
     * by navigating through the successors of the head choreography using offset based prunning.
     * A value of 1.0f signifies the end of the animation while a value of 0.0f signifies
     * the start state of the animation.
     *
     * @param percentage The amount to animation to.
     * @throws IllegalArgumentException if the percentage is outside of the 0f to 1f range.
     */
    fun transitionTo(percentage: Float) {
        require(percentage >= MIN_OFFSET && percentage <= MAX_OFFSET) {
            "A percentage offset must be in the range of 0f and 1f: $percentage"
        }
        transitionTo(headChoreography, percentage, true)
    }

    /**
     * Animates this [Choreography] to the specified animation percentage/fraction recursively
     * by navigating through the successors of the head choreography using offset based prunning.
     * A value of 1.0f signifies the end of the animation while a value of 0.0f signifies
     * the start state of the animation.
     *
     * @param choreography the choreography currently being animated
     * @param percentage The amount to animation to.
     */
    private fun transitionTo(choreography: Choreography, percentage: Float, traverse: Boolean) {

        val startOffset = choreography.control.offsetStart

        if (percentage < startOffset) {
            return
        }

        choreography.control.animateFraction(percentage)

        if (!traverse)
            return

        choreography.child?.let {
            transitionTo(it, percentage, traverse)
        }
    }

    /**
     * Animates the specified [Choreography] to specified animation fraction. The total duration
     * and the current playtime must be known.
     *
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

        val staggerData = choreography.stagger?.staggerData

        if (staggerData == null) {
            for (view in views) {
                animate(view, choreography, fraction, duration, currentPlayTime)
            }
        } else {
            for (info in staggerData) {
                val view = info.view

                if (fraction < info.startOffset || fraction > info.endOffset)
                    continue

                val mappedRation = mapRange(fraction, info.startOffset, info.endOffset, MIN_OFFSET, MAX_OFFSET)

                animate(view, choreography, mappedRation, duration, currentPlayTime)
            }
        }
    }

    /**
     * Animates the specified [MorphLayout] with the specified [Choreography] to specified animation fraction.
     * The total duration and the current playtime must be known. Each property is animated using its respective from and to
     * values and the specified [TimeInterpolator] if any has been specified. This is the function where the animation
     * happens and must be kept as lightweight as possible.
     *
     * @param view The morph layout to be animated
     * @param choreography The choreography to be animated.
     * @param fraction The fraction to animate to.
     * @param duration The duration of the animation.
     * @param currentPlayTime The current playtime of the animation.
     */
    private fun animate(
        view: MorphLayout,
        choreography: Choreography,
        fraction: Float,
        duration: Long,
        currentPlayTime: Long
    ) {

        val alphaFraction = choreography.alpha.interpolator?.getInterpolation(fraction) ?: fraction

        view.morphPivotX = choreography.pivotPoint.x
        view.morphPivotY = choreography.pivotPoint.y

        view.morphAlpha = choreography.alpha.lerp(alphaFraction)

        if (choreography.scaleX.canInterpolate) {
            val scaleXFraction = choreography.scaleX.interpolator?.getInterpolation(fraction) ?: fraction

            view.morphScaleX = choreography.scaleX.lerp(scaleXFraction)
        } else {
            if(choreography.scaleXValues.canInterpolate) {
                animateThroughPoints(choreography.scaleXValues, view, currentPlayTime, duration, scaleXListener)
            }
        }

        if (choreography.scaleY.canInterpolate) {
            val scaleYFraction = choreography.scaleY.interpolator?.getInterpolation(fraction) ?: fraction

            view.morphScaleY = choreography.scaleY.lerp(scaleYFraction)
        } else {
            if(choreography.scaleYValues.canInterpolate) {
                animateThroughPoints(choreography.scaleYValues, view, currentPlayTime, duration, scaleYListener)
            }
        }

        if (choreography.rotation.canInterpolate) {
            val rotateFraction = choreography.rotation.interpolator?.getInterpolation(fraction) ?: fraction

            view.morphRotation = choreography.rotation.lerp(rotateFraction)
        } else {
            if(choreography.rotationValues.canInterpolate) {
                animateThroughPoints(choreography.rotationValues, view, currentPlayTime, duration, rotationListener)
            }
        }

        if (choreography.rotationX.canInterpolate) {
            val rotateXFraction = choreography.rotationX.interpolator?.getInterpolation(fraction) ?: fraction

            view.morphRotationX = choreography.rotationX.lerp(rotateXFraction)
        } else {
            if(choreography.rotationXValues.canInterpolate) {
                animateThroughPoints(choreography.rotationXValues, view, currentPlayTime, duration, rotationXListener)
            }
        }

        if (choreography.rotationY.canInterpolate) {
            val rotateYFraction = choreography.rotationY.interpolator?.getInterpolation(fraction) ?: fraction

            view.morphRotationY = choreography.rotationY.lerp(rotateYFraction)
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
                view.morphTranslationX = choreography.positionX.lerp(positionXFraction)
                view.morphTranslationY = choreography.positionY.lerp(positionYFraction)

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
                view.morphTranslationX = choreography.translateX.lerp(translateXFraction)
                view.morphTranslationY = choreography.translateY.lerp(translateYFraction)
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
            val translateZFraction = choreography.translateZ.interpolator?.getInterpolation(fraction) ?: fraction

            view.morphTranslationZ = choreography.translateZ.lerp(translateZFraction)
        } else {
            if (choreography.translateZValues.canInterpolate) {
                animateThroughPoints(choreography.translateZValues, view, currentPlayTime, duration, translationZListener)
            }
        }

        if (view.mutateCorners && choreography.cornerRadii.canInterpolate) {
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

            view.morphWidth = choreography.width.lerp(widthFraction)
            view.morphHeight = choreography.height.lerp(heightFraction)

            boundsChanged = true
        }

        choreography.bitmapMorph?.morph(fraction)

        if (boundsChanged) {
            view.updateLayout()
        }

        boundsChanged = false
    }

    /**
     * Animates the specified [MorphLayout] with the values specified on the [AnimatedFloatValueArray] to the specified animation playtime.
     * The total duration must be known and a [ViewPropertyValueListener] is used in order to notify the progression of the values.
     *
     * @param valueHolder The value holder containing the values to animate between.
     * @param view The morph layout to animate
     * @param playTime The current playtime of the animation.
     * @param duration The duration of the animation.
     * @param listener The value progression listener
     */
    private fun animateThroughPoints(
        valueHolder: AnimatedFloatValueArray,
        view: MorphLayout,
        playTime: Long,
        duration: Long,
        listener: ViewPropertyValueListener
    ) {
        val values = valueHolder.values
        val pointDuration = duration / values.size

        for(index in 0 until values.size - 1) {

            val timeStart: Long = index * pointDuration
            val timeEnd: Long = (index + 1) * pointDuration

            if (playTime < timeStart)
                continue

            val start: Float = values[index]
            val end: Float = values[index + 1]

            val mapFraction = mapRange(playTime.toFloat(), timeStart.toFloat(), timeEnd.toFloat(), MIN_OFFSET, MAX_OFFSET)

            val valueFraction = valueHolder.interpolator?.getInterpolation(mapFraction) ?: mapFraction

            listener(view,start + (end - start) * valueFraction)
        }
    }

    /**
     * Class which holds all the data on how to animate a [MorphLayout] in order
     * successfully create the desired animation sequence. The choreography is gives
     * its [Choreographer] instructions on how to animated and holds values for animateable
     * properties a the specified morphViews.
     *
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
        internal var interval: Long = MIN_DURATION
        internal var duration: Long = MIN_DURATION
        internal var offsetDelayAlpha: Long = MIN_DURATION
        internal var offsetDelayDelta: Long = MIN_DURATION

        internal var offset: Float = MIN_OFFSET

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

        internal var endAction: ChoreographerAction = null
        internal var startAction: ChoreographerAction = null

        internal var interpolator: TimeInterpolator? = null

        internal var stagger: AnimationStagger? = null

        internal var ripple: Ripple? = null
        internal var reveal: Reveal? = null
        internal var conceal: Conceal? = null
        internal var arcType: ArcType? = null
        internal var stretch: Stretch? = null

        internal var textMorph: TextMorph? = null
        internal var bitmapMorph: BitmapMorph? = null

        lateinit var control: ChoreographyControl
        internal var parent: Choreography? = null
        internal var child: Choreography? = null

        val views: Array<out MorphLayout>
            get() = morphViews

        init {
            applyDefaultValues()
        }

        /**
         * Creates a the stagger data for [AnimationStagger] assigned
         * to this [Choreography].
         *
         * @param stagger The nonnull version of the stagger of this
         * choreography.
         */
        internal fun createStagger(stagger: AnimationStagger) {
            val epicenter = morphViews[0]
            val parentBounds = epicenter.getParentBounds()
            StaggerAnimationHelper.computeStaggerData(morphViews.toList(), epicenter.centerLocation, parentBounds, duration, stagger) {
                it.morphWidth
            }
        }

        /**
         * Creates a control point for arc animations.
         * The control point is created using an [ArcType] which determines
         * how the translation happens between the start and end coordinates.
         * @param animatedX The animated x value used for translation.
         * @param animatedY The animated y value used for translation.
         * @param arcType The fadeType of arc translation to be perform.
         */
        internal fun createControlPoint(animatedX: AnimatedFloatValue, animatedY: AnimatedFloatValue, arcType: ArcType) {
            val coordinateFrom = Coordinates(animatedX.fromValue, animatedY.fromValue)
            val coordinateTo = Coordinates(animatedX.toValue, animatedY.toValue)

            controlPoint = createControlPoint(coordinateFrom, coordinateTo, arcType)
        }

        /**
         * Creates a control point for arc animations.
         * The control point is created using an [ArcType] which determines
         * how the translation happens between the start and end coordinates.
         * @param coordinatesFrom The coordinates to translate from.
         * @param coordinatesTo The coordinates to translate to.
         * @param arcType The fadeType of arc translation to be perform.
         */
        internal fun createControlPoint(coordinatesFrom: Coordinates, coordinatesTo: Coordinates, arcType: ArcType): Coordinates{
            val controlX: Float
            val controlY: Float

            when (arcType) {
                ArcType.INNER -> {
                    controlX = coordinatesFrom.x
                    controlY = coordinatesTo.y
                }
                ArcType.OUTER -> {
                    controlX = coordinatesTo.x
                    controlY = coordinatesFrom.y
                }
            }
            return Coordinates(controlX, controlY)
        }

        /**
         * A map of the properties or [AnimatedValue] of this [Choreography]
         *
         * @return A map containing of the properties accessed by their respective names.
         */
        internal fun properties(): Map<String, AnimatedValue<*>> {
            val properties: HashMap<String, AnimatedValue<*>> = HashMap()
            properties[width.propertyName] = width

            properties[height.propertyName] = height
            properties[alpha.propertyName] = alpha

            properties[scaleX.propertyName] = scaleX
            properties[scaleY.propertyName] = scaleY

            properties[rotation.propertyName] = rotation
            properties[rotationX.propertyName] = rotationX
            properties[rotationY.propertyName] = rotationY

            properties[positionX.propertyName] = positionX
            properties[positionY.propertyName] = positionY

            properties[translateX.propertyName] = translateX
            properties[translateY.propertyName] = translateY
            properties[translateZ.propertyName] = translateZ

            properties[color.propertyName] = color
            properties[paddings.propertyName] = paddings
            properties[margings.propertyName] = margings
            properties[cornerRadii.propertyName] = cornerRadii
            return properties
        }

        /**
         * A map containing of property sets or [AnimatedValueArray]
         *
         * @return A map containing of the properties sets accessed by their respective names.
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
         *
         * @param properties Map of all the properties to be set.
         */
        @Suppress("UNCHECKED_CAST")
        internal fun setStartProperties(properties: Map<String, AnimatedValue<*>>?) {
            if (properties == null)
                return

            width.set(properties[width.propertyName] as AnimatedFloatValue)
            height.set(properties[height.propertyName] as AnimatedFloatValue)

            alpha.set(properties[alpha.propertyName] as AnimatedFloatValue)

            scaleX.set(properties[scaleX.propertyName] as AnimatedFloatValue)
            scaleY.set(properties[scaleY.propertyName] as AnimatedFloatValue)

            rotation.set(properties[rotation.propertyName] as AnimatedFloatValue)
            rotationX.set(properties[rotationX.propertyName] as AnimatedFloatValue)
            rotationY.set(properties[rotationY.propertyName] as AnimatedFloatValue)

            positionX.set(properties[positionX.propertyName] as AnimatedFloatValue)
            positionY.set(properties[positionY.propertyName] as AnimatedFloatValue)

            translateX.set(properties[translateX.propertyName] as AnimatedFloatValue)
            translateY.set(properties[translateY.propertyName] as AnimatedFloatValue)
            translateZ.set(properties[translateZ.propertyName] as AnimatedFloatValue)

            color.set(properties.getValue(color.propertyName) as AnimatedValue<Color>)
            paddings.set(properties.getValue(paddings.propertyName) as AnimatedValue<Padding>)
            margings.set(properties.getValue(margings.propertyName) as AnimatedValue<Margin>)
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

            this.width.flip()
            this.height.flip()

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
         * Resets each of the views held by this [Choreography] to their initial
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

        /**
         * Animates the morphViews of this [Choreography] to the center of the specified [MorphLayout]
         * Optionally uses the specified [TimeInterpolator] if any is present.
         * @param otherView The layout to which the choreography will animate its morphViews to the center of
         * @param interpolator the interpolator to use for this animation.
         * @return this choreography.
         */
        fun centerIn(otherView: MorphLayout, interpolator: TimeInterpolator? = null): Choreography {
            val view = morphViews[0]
            val startX: Float = view.windowLocationX.toFloat() + (view.morphWidth / 2)
            val startY: Float = view.windowLocationY.toFloat() + (view.morphHeight / 2)

            val endX: Float = otherView.windowLocationX.toFloat() + (otherView.morphWidth / 2)
            val endY: Float = otherView.windowLocationY.toFloat() + (otherView.morphHeight / 2)

            val differenceX: Float = endX - startX
            val differenceY: Float = endY - startY

            this.translateX.toValue = differenceX
            this.translateY.toValue = differenceY

            this.translateX.interpolator = interpolator
            this.translateY.interpolator = interpolator

            return this
        }

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
                    anchorTo(Anchor.LEFT, view, margin, interpolator)
                }
            }
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

        fun withRipple(ripple: Ripple): Choreography {
            this.ripple = ripple
            return this
        }

        fun withImageChange(bitmapMorph: BitmapMorph): Choreography {
            this.bitmapMorph = bitmapMorph
            return this
        }

        fun withTextChange(textMorph: TextMorph): Choreography {
            TODO("Implement the logic necessary")
            return this
        }

        fun withTextChange(fromView: TextView, toView: TextView): Choreography {
            TODO("Implement the logic necessary")
            return this
        }

        fun withTextChange(view: TextView, toText: String, toSize: Float = -1f, toColor: Int = -1): Choreography {
            TODO("Implement the logic necessary")
            return this
        }

        fun visibilityTo(): Choreography {
            TODO("Implement the logic necessary")
            return this
        }

        fun backgroundTo(background: Background, interpolator: TimeInterpolator? = null): Choreography {
            TODO("Implement the logic necessary")
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
            require(duration >= MIN_DURATION) { "Choreographies cannot have negative durations: $duration" }
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
         * @throws IllegalArgumentException Thrown when the offset is negative.
         * @return this choreography.
         */
        fun withStartDelay(delay: Long): Choreography {
            require(delay >= MIN_DURATION) { "Choreographies cannot have negative delays: $delay" }
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
         * @param type The [Stagger] fadeType to use.
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
         * Specifies the way the arc translation control point should be computer. If arc
         * translation is used the control point will be calculated based on the specified
         * fadeType. The available types are:
         * * [ArcType.INNER] : Arc translates across the inner path of its destination.
         * * [ArcType.OUTER] : Arc translates across the outer path of its destination.
         * @param arcType the arc path to use for the arc translation.
         * @return this choreography.
         */
        fun withArcType(arcType: ArcType): Choreography {
            this.arcType = arcType
            return this
        }

        /**
         * Specifies whether or not arc translation should be used for translating
         * the views. When arc translation is used the views will arc translate using
         * the default or a specified control point.
         * @param useArcTranslation specifies if arc translation should be used. Default: `True`
         * @return this choreography.
         */
        fun witArchTranslation(useArcTranslation: Boolean = true): Choreography{
            this.useArcTranslator = useArcTranslation
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
         * Specifies the action that should be executed upon the start of the animation of this [Choreography]
         * @param action The start action to execute.
         * @return this choreography.
         */
        fun onStart(action: ChoreographerAction): Choreography {
            this.startAction = action
            return this
        }

        /**
         * Specifies the action that should be executed upon the end of the animation of this [Choreography]
         * @param action The end action to execute.
         * @return this choreography.
         */
        fun onFinished(action: ChoreographerAction): Choreography {
            this.endAction = action
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
            this.reveal = Reveal(morphViews[0].getView(), centerX, centerY, radius)
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
            this.reveal = Reveal(morphViews[0].getView(), coordinates.x, coordinates.y, radius)
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
            this.reveal = Reveal(morphView.getView(), centerX, centerY, radius)
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
            this.reveal = Reveal(morphView.getView(), radius)
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
            this.conceal = Conceal(morphViews[0].getView(), centerX, centerY, radius)
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
            this.conceal = Conceal(morphViews[0].getView(), coordinates.x, coordinates.y, radius)
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
            this.conceal = Conceal(morphView.getView(), centerX, centerY, radius)
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
            this.conceal = Conceal(morphView.getView(), radius)
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
         * A call to this function will buildAll the current and all the previously appended choreographies.
         * This function must be called prior to starting the [Choreography] animation. Note that a
         * call to this function will not only built the current choreography but and all its predecessors.
         * A built choreography can be saved to played at a later time. The ability to buildAll a
         * choreography helps to get rid of overhead.
         * @return the [Choreographer] which will animate this choreography.
         */
        internal fun build(): Choreographer {
            choreographer.applyAdders(this)
            choreographer.applyMultipliers(this)
            return choreographer.buildAll()
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
         * @param views the view to buildAll a choreography with.
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

    /**
     * Class containing information about the properties used for animating a [Choreography]
     * Each choreography uses an animation control for its core animation.
     * @param choreography The choreography to which the control belongs to.
     * @param fromValue The offset to which control will start the animation of the choreography
     * @param toValue The offset to which control will end the animation of the choreography
     */
    class ChoreographyControl(
        internal val interpolator: TimeInterpolator,
        internal val choreography: Choreography,
        internal var fromValue: Float,
        internal var toValue: Float
    ) {

        internal var repeatCount: Int = 0

        internal var repeatMode: Int = RESTART

        internal var duration: Long = MIN_DURATION
        internal var startDelay: Long = MIN_DURATION
        internal var offsetDelay: Long = MIN_DURATION

        internal var seekFraction: Float = -MAX_OFFSET

        internal var offsetStart: Float = MIN_OFFSET
        internal var offsetEnd: Float = MAX_OFFSET

        internal var startListener: Action = null
        internal var endListener: Action = null

        internal var startTime: Long = MIN_DURATION
        internal var endTime: Long = MIN_DURATION

        internal lateinit var updateListener: AnimationProgressListener

        internal var started: Boolean = false
            set(value) {
                field = value
                startTime = System.currentTimeMillis()
            }

        internal var ended: Boolean = false
            set(value) {
                field = value
                endTime = System.currentTimeMillis()
            }

        internal val currentPlayTime: Long
            get() {
                if (!started) {
                    return MIN_DURATION
                }
                if (seekFraction >= MIN_OFFSET) {
                    return (duration * seekFraction).toLong()
                }
                return System.currentTimeMillis() - startTime
            }

        internal val totalDuration: Long
            get() = if (repeatCount == INFINITE) {
                DURATION_INFINITE
            } else {
                startDelay + duration * (repeatCount + 1L)
            }

        fun reset() {
            seekFraction = MIN_OFFSET
            startTime = -1L
            started = false
            ended = false
        }

        fun animateFraction(fraction: Float) {
            if (!started) {
                startListener?.invoke()
                started = true
            }

            seekFraction = mapRange(fraction, offsetStart, offsetEnd, MIN_OFFSET, MAX_OFFSET)

            if (((fraction >= offsetEnd) || (seekFraction >= MAX_OFFSET)) && !ended) {
                endListener?.invoke()
                ended = true
            }

            updateListener.onProgress(interpolator.getInterpolation(seekFraction), currentPlayTime)
        }

        companion object {
            const val RESTART = 1
            const val REVERSE = 2
            const val INFINITE = -1
            const val DURATION_INFINITE = -1L
        }
    }

    /**
     * Class which controls whether a traversal should break traversion.
     * Allows for stopping a traversal to continue from outside of the traversal
     * function.
     */
    class TraverseControl {
        internal var breakTraverse: Boolean = false
        internal var skipCurrent: Boolean = false

        /**
         * Breaks the traversal this control is bound to
         */
        fun breakTraversal() {
            breakTraverse = true
        }

        fun skipCurrent() {
            skipCurrent = true
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

    companion object {
        /**
         * Resolves the pivot for a given fadeType and value
         * @param type specifies in relation to what the pivot should be computed
         * @param value the value to map the size of the view to
         * @param parentSize the size of the parent of the view whose pivot
         * is being resolved
         */
        private fun resolvePivot(type: Pivot, value: Float, size: Float, parentSize: Float): Float {
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
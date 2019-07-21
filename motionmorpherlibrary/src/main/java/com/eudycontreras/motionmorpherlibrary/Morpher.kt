package com.eudycontreras.motionmorpherlibrary

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.Interpolator
import com.eudycontreras.motionmorpherlibrary.drawables.MorphTransitionDrawable
import com.eudycontreras.motionmorpherlibrary.extensions.*
import com.eudycontreras.motionmorpherlibrary.helpers.CurvedTranslationHelper
import com.eudycontreras.motionmorpherlibrary.interfaces.Cloneable
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.layouts.MorphWrapper
import com.eudycontreras.motionmorpherlibrary.listeners.MorphAnimationListener
import com.eudycontreras.motionmorpherlibrary.properties.Coordintates
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii
import com.eudycontreras.motionmorpherlibrary.properties.Dimension
import com.eudycontreras.motionmorpherlibrary.properties.Point
import com.eudycontreras.motionmorpherlibrary.utilities.ColorUtility
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since June 10 2019
 */

class Morpher(private val context: Context) {

    private var lastOffset = MIN_OFFSET

    private var remainingDuration: Long = 0L

    private var mappingsCreated: Boolean = false
    private var initialValuesApplied: Boolean = false

    private var children: List<View>? = null

    private val curveTranslator = CurvedTranslationHelper()

    private lateinit var startingView: MorphLayout
    private lateinit var endingView: MorphLayout

    private lateinit var startingState: Properties
    private lateinit var endingState: Properties

    private lateinit var endViewEndState: MorphState
    private lateinit var endViewStartState: MorphState

    private lateinit var mappings: List<MorphMap>

    var backgroundDimListener: BackgroundDimListener = null
    var computedStatesListener: ComputedStatesListener = null
    var transitionOffsetListener: TransitionOffsetListener = null

    var morphIntoInterpolator: Interpolator? = null
    var morphFromInterpolator: Interpolator? = null

    var useDeepChildSearch: Boolean = true
    var useArcTranslator: Boolean = true
    var animateChildren: Boolean = true
    var morphChildren: Boolean = true

    var morphIntoDuration: Long = DEFAULT_DURATION
    var morphFromDuration: Long = DEFAULT_DURATION

    var overlayCrossfadeDurationIn: Long = 0L
    var overlayCrossfadeDurationOut: Long = 100L

    var childrenEndRevealed: Boolean = false
        private set

    var childrenStartRevealed: Boolean = false
        private set

    var isMorphing: Boolean = false
        private set

    var isMorphed: Boolean = false
        private set

    var startView: MorphLayout
        get() = startingView
        set(value) {
            startingView = value
            mappingsCreated = false
        }

    var endView: MorphWrapper
        get() = endingView as MorphWrapper
        set(value) {
            endingView = value
            mappingsCreated = false
        }

    var endStateMorphIntoDescriptor: AnimationDescriptor = AnimationDescriptor(AnimationType.REVEAL)
    var endStateMorphFromDescriptor: AnimationDescriptor = AnimationDescriptor(AnimationType.CONCEAL)

    var startStateMorphIntoDescriptor: AnimationDescriptor = AnimationDescriptor(AnimationType.REVEAL)
    var startStateMorphFromDescriptor: AnimationDescriptor = AnimationDescriptor(AnimationType.CONCEAL)

    var endStateChildMorphIntoDescriptor: ChildAnimationDescriptor = ChildAnimationDescriptor(AnimationType.REVEAL).apply { default = true }
        set(value) {
            field = value
            field.default = true
        }

    var endStateChildMorphFromDescriptor: ChildAnimationDescriptor = ChildAnimationDescriptor(AnimationType.CONCEAL).apply { default = true }
        set(value) {
            field = value
            field.default = true
        }

    var startStateChildMorphIntoDescriptor: ChildAnimationDescriptor = ChildAnimationDescriptor(AnimationType.REVEAL).apply { default = true }
        set(value) {
            field = value
            field.default = true
        }

    var startStateChildMorphFromDescriptor: ChildAnimationDescriptor = ChildAnimationDescriptor(AnimationType.CONCEAL).apply { default = true }
        set(value) {
            field = value
            field.default = true
        }

    var dimPropertyInto: PropertyDescriptor<Float> = PropertyDescriptor(
        propertyType = PropertyDescriptor.COLOR,
        fromValue = MIN_OFFSET,
        toValue = MAX_OFFSET,
        startOffset = 0f,
        endOffset = 0.6f
    )

    var dimPropertyFrom: PropertyDescriptor<Float> = PropertyDescriptor(
        propertyType = PropertyDescriptor.COLOR,
        fromValue = MAX_OFFSET,
        toValue = MIN_OFFSET,
        startOffset = 0.4f,
        endOffset = 1f
    )

    private fun createMappings() {
        endingState = endingView.getProperties()
        startingState = startingView.getProperties()

        mappings = if (morphChildren) {
            getChildMappings(startingView, endingView, useDeepChildSearch)
        } else emptyList()
    }

    fun cancelMorph() {
        //TODO( Implement logic that could cancel a currently going morph animation)
    }

    private fun performSetup() {
        if (!mappingsCreated) {
            createMappings()
            mappingsCreated = true
        }

        val endingViewStart = endView.getStartState()
        val endingViewEnd = endView.getEndState()

        val pivotPoint: Point<Float> = calculatePivot(endingViewStart, endingViewEnd)

        endViewEndState = MorphState(endingViewEnd, endingViewEnd.getProperties())
        endViewStartState = MorphState(endingViewStart, endingViewStart.getProperties())

        endingViewStart.morphPivotX = pivotPoint.x
        endingViewStart.morphPivotY = pivotPoint.y

        endingViewStart.morphWidth = endingViewStart.morphWidth
        endingViewStart.morphHeight = endingViewStart.morphHeight

        endingViewEnd.morphPivotX = 0f
        endingViewEnd.morphPivotY = 0f

        endingViewEnd.morphWidth = endingViewEnd.morphWidth
        endingViewEnd.morphHeight = endingViewEnd.morphHeight

        val endChildren = getAllChildren(endingViewEnd, useDeepChildSearch) { it.tag != null }

        endChildren.forEach {
            val endView = it as MorphLayout
            if (endView.hasMorphTransitionDrawable()) {
                val transition = endView.getMorphTransitionDrawable()
                transition.resetTransition()
            }
        }

        if (endStateChildMorphIntoDescriptor.default) {
            endStateChildMorphIntoDescriptor.let {
                val propertyChange = AnimationProperties(
                    translationX = endingViewEnd.morphWidth * it.defaultTranslateMultiplier,
                    translationY = endingViewEnd.morphHeight * it.defaultTranslateMultiplier
                )
                it.startStateProps = AnimationProperties.from(this, it, propertyChange, endingViewStart, endingViewEnd, ValueType.START)
                it.endStateProps = AnimationProperties.from(this, it, propertyChange, endingViewStart, endingViewEnd, ValueType.END)
                it.default = true
            }
        }

        if (endStateChildMorphFromDescriptor.default) {
            val clone = endStateChildMorphIntoDescriptor.startStateProps.clone(AnimationProperties::class.java)

            clone.translationX = clone.translationX * 2
            clone.translationY = clone.translationY * 2

            endStateChildMorphFromDescriptor.let {
                it.startStateProps = endStateChildMorphIntoDescriptor.endStateProps
                it.endStateProps = clone
                it.default = true
            }
        }

        if (startStateChildMorphIntoDescriptor.default) {
            startStateChildMorphIntoDescriptor.let {
                val propertyChangeEnd = AnimationProperties(
                    translationX = endingViewStart.morphWidth * it.defaultTranslateMultiplier,
                    translationY = endingViewStart.morphHeight * it.defaultTranslateMultiplier
                )
                val propertyChangeStart = AnimationProperties()
                it.startStateProps = AnimationProperties.from(this, it, propertyChangeStart, endingViewStart, endingViewEnd, ValueType.START)
                it.endStateProps = AnimationProperties.from(this, it, propertyChangeEnd, endingViewStart, endingViewEnd, ValueType.END)
                it.default = true
            }
        }

        if (startStateChildMorphFromDescriptor.default) {
            startStateChildMorphFromDescriptor.let {
                val propertyChangeEnd = AnimationProperties()

                val propertyChangeStart = AnimationProperties(
                    translationX = startStateChildMorphIntoDescriptor.endStateProps.translationX / 3,
                    translationY = startStateChildMorphIntoDescriptor.endStateProps.translationX / 3,
                    alpha = 1f
                )
                startStateChildMorphFromDescriptor.let {
                    it.startStateProps = propertyChangeStart
                    it.endStateProps = propertyChangeEnd
                    it.default = true
                }
            }
        }

        for (child in endingViewEnd.getChildren()) {

            if ((morphChildren && child.tag != null) || child.visibility == View.GONE)
                continue

            if (child is MorphLayout) {
                if (!child.animate) {
                    continue
                }
            }

            child.alpha = MIN_OFFSET
            child.pivotX = child.width / 2f
            child.pivotY = child.height / 2f
            child.layoutParams.width = child.width
            child.layoutParams.height = child.height
        }

        applyProps(endingView, startingState)

        endingView.show(overlayCrossfadeDurationIn) { VISIBLE }
        startingView.hide(overlayCrossfadeDurationOut)

        val startX: Float = startingState.windowLocationX.toFloat()
        val startY: Float = startingState.windowLocationY.toFloat()

        val endX: Float = endingState.windowLocationX.toFloat()
        val endY: Float = endingState.windowLocationY.toFloat()

        val translationX: Float = abs(endX - startX)
        val translationY: Float = abs(endY - startY)

        startingState.translationX =  translationX
        startingState.translationY =  translationY

        endingView.morphTranslationX = startingState.translationX
        endingView.morphTranslationY = startingState.translationY

        curveTranslator.setControlPoint(Coordintates(endingState.translationX, startingState.translationY))

        initialValuesApplied = true
    }

    fun transitionInto(
        duration: Long = DEFAULT_DURATION,
        onStart: Action = null,
        onEnd: Action = null,
        offsetTrigger: OffsetTrigger? = null
    ) {
        //TODO ( Implement transitioner: Separete morphing from transition)
    }

    fun transitionFrom(
        duration: Long = DEFAULT_DURATION,
        onStart: Action = null,
        onEnd: Action = null,
        offsetTrigger: OffsetTrigger? = null
    ) {
        //TODO ( Implement transitioner: Separete morphing from transition)
    }

    fun morphInto(
        duration: Long = morphIntoDuration,
        onStart: Action = null,
        onEnd: Action = null,
        offsetTrigger: OffsetTrigger? = null
    ) {

        if (isMorphing)
            return

        isMorphed = false
        isMorphing = true

        val doOnEnd = {
            onEnd?.invoke()

            applyProps(endingView, endingState)

            mappings.forEach {
                applyProps(it.endView, it.endProps)
            }

            isMorphing = false
            isMorphed = true
        }

        performSetup()

        endStateMorphIntoDescriptor.morphState = endViewEndState
        startStateMorphIntoDescriptor.morphState = endViewStartState

        computedStatesListener?.invoke(startingState, endingState)

        curveTranslator.setStartPoint(startingState.getDeltaCoordinates())
        curveTranslator.setEndPoint(endingState.getDeltaCoordinates())

        applyIntoTransitionDrawable(context, mappings)

        morph(
            endingView,
            startingState,
            endingState,
            mappings,
            morphIntoInterpolator,
            curveTranslator,
            duration,
            onStart,
            doOnEnd,
            offsetTrigger,
            AnimationType.REVEAL,
            MorphType.INTO
        )
    }

    private fun applyIntoTransitionDrawable(context: Context, mappings: List<MorphMap>) {
        mappings.forEach {

            when {
                all(it.endView, it.startView) { all -> all.hasBitmapDrawable() } -> {

                    val transitionDrawable = MorphTransitionDrawable(it.startProps.background, it.endProps.background)

                    it.endView.morphBackground = transitionDrawable

                    transitionDrawable.startDrawableType = it.startView.getBackgroundType()
                    transitionDrawable.endDrawableType = it.endView.getBackgroundType()

                    transitionDrawable.isCrossFadeEnabled = true
                }
                all(it.startView, it.endView) { all -> all.hasVectorDrawable() } -> {

                    val fromBitmap = BitmapDrawable(context.resources, it.startProps.background?.toBitmap())
                    val toBitmap = BitmapDrawable(context.resources, it.endProps.background?.toBitmap())

                    val transitionDrawable = MorphTransitionDrawable(fromBitmap, toBitmap)

                    it.endView.morphBackground = transitionDrawable

                    transitionDrawable.startDrawableType = it.startView.getBackgroundType()
                    transitionDrawable.endDrawableType = it.endView.getBackgroundType()

                    transitionDrawable.isSequentialFadeEnabled = true
                }
                it.endView.hasMorphTransitionDrawable() -> {

                    val fromBitmap = BitmapDrawable(context.resources, it.startProps.background?.toBitmap())

                    val transitionDrawable = it.endView.getMorphTransitionDrawable()

                    if (transitionDrawable.startDrawableType == MorphTransitionDrawable.DrawableType.VECTOR ) {
                        transitionDrawable.setStartDrawable(fromBitmap)
                    }

                    transitionDrawable.setUpTransition(false)
                }
            }
        }
    }

    fun morphFrom(
        duration: Long = morphFromDuration,
        onStart: Action = null,
        onEnd: Action = null,
        offsetTrigger: OffsetTrigger? = null
    ) {

        if (isMorphing || !isMorphed)
            return

        if (!mappingsCreated) {
            createMappings()
            mappingsCreated = true
        }

        isMorphed = false
        isMorphing = true

        val doOnEnd = {
            onEnd?.invoke()

            startingView.show(overlayCrossfadeDurationIn) { VISIBLE }
            endingView.hide(overlayCrossfadeDurationOut) { INVISIBLE }

            applyProps(endingView, endingState)

            mappings.forEach {
                applyProps(it.endView, it.endProps)
            }

            isMorphing = false
            isMorphed = true
        }

        endStateMorphFromDescriptor.morphState = endViewEndState
        startStateMorphFromDescriptor.morphState = endViewStartState

        curveTranslator.setStartPoint(endingState.getDeltaCoordinates())
        curveTranslator.setEndPoint(startingState.getDeltaCoordinates())

        applyFromTransitionDrawable(context, mappings)

        morph(
            endingView,
            endingState,
            startingState,
            mappings,
            morphFromInterpolator,
            curveTranslator,
            duration,
            onStart,
            doOnEnd,
            offsetTrigger,
            AnimationType.CONCEAL,
            MorphType.FROM
        )
    }

    private fun applyFromTransitionDrawable(context: Context, mappings: List<MorphMap>) {
        mappings.forEach {
            when {
                all(it.endView, it.startView) { all -> all.hasBitmapDrawable() } -> {

                    val transitionDrawable = MorphTransitionDrawable(it.endProps.background, it.startProps.background)

                    it.endView.morphBackground = transitionDrawable

                    transitionDrawable.startDrawableType = it.startView.getBackgroundType()
                    transitionDrawable.endDrawableType = it.endView.getBackgroundType()

                    transitionDrawable.isCrossFadeEnabled = true
                }
                all(it.startView, it.endView) { all -> all.hasVectorDrawable() } -> {

                    val fromBitmap = BitmapDrawable(context.resources, it.startProps.background?.toBitmap())
                    val toBitmap = BitmapDrawable(context.resources, it.endProps.background?.toBitmap())

                    val transitionDrawable = MorphTransitionDrawable(toBitmap, fromBitmap)

                    it.endView.morphBackground = transitionDrawable

                    transitionDrawable.startDrawableType = it.startView.getBackgroundType()
                    transitionDrawable.endDrawableType = it.endView.getBackgroundType()

                    transitionDrawable.isSequentialFadeEnabled = true
                }
                it.endView.hasMorphTransitionDrawable() -> {
                    val fromBitmap = BitmapDrawable(context.resources, it.startProps.background?.toBitmap())

                    val transitionDrawable = it.endView.getMorphTransitionDrawable()

                    if (transitionDrawable.startDrawableType == MorphTransitionDrawable.DrawableType.VECTOR ) {
                        transitionDrawable.setStartDrawable(fromBitmap)
                    }

                    transitionDrawable.setUpTransition(true)
                }
            }
        }
    }

    fun transitionBy(percent: Int) {
        if (percent in 0..100) {
            transitionBy(percent.toFloat() / 100.0f)
        }
    }

    private fun transitionBy(fraction: Float) {

        if (!initialValuesApplied) {
            performSetup()
        }

        val startingProps = startingState
        val endingProps = endingState

        animateProperties(endingView, startingProps, endingProps, fraction)

        endingView.animator().translationX(startingProps.translationX + (endingProps.translationX - startingProps.translationX) * fraction).setDuration(0).start()
        endingView.animator().translationY(startingProps.translationY + (endingProps.translationY - startingProps.translationY) * fraction).setDuration(0).start()

        if (morphChildren && mappings.isNotEmpty()) {
            for (mapping in mappings) {
                animateProperties(mapping.endView, mapping.startProps, mapping.endProps, fraction)

                mapping.endView.animator().x(mapping.startProps.x + (mapping.endProps.x - mapping.startProps.x) * fraction).setDuration(0).start()
                mapping.endView.animator().y(mapping.startProps.y + (mapping.endProps.y - mapping.startProps.y) * fraction).setDuration(0).start()
            }
        }

        if (children == null) {
            children = getAllChildren(endingView, false) { it.tag == null}
        }

        children?.let { list ->
            for (it in list) {
                it.animate()
                    .alpha(MIN_OFFSET + (MAX_OFFSET - MIN_OFFSET) * fraction )
                    .setDuration(0)
                    .start()
            }
        }

        lastOffset = fraction
    }

    private fun morph(
        endView: MorphLayout,
        startingProps: Properties,
        endingProps: Properties,
        mappings: List<MorphMap>,
        interpolator: Interpolator?,
        curveTranslationHelper: CurvedTranslationHelper?,
        duration: Long,
        onStart: Action,
        onEnd: Action,
        trigger: OffsetTrigger?,
        animationType: AnimationType,
        morphType: MorphType
    ) {

        this.remainingDuration = duration

        val animator: ValueAnimator = ValueAnimator.ofFloat(MIN_OFFSET, MAX_OFFSET)

        animator.addListener(MorphAnimationListener(onStart, onEnd))
        animator.addUpdateListener {

            val fraction = (it.animatedValue as Float).clamp(0f, 1f)

            val interpolatedFraction = interpolator?.getInterpolation(fraction) ?: fraction

            remainingDuration = duration - (duration * interpolatedFraction).toLong()

            animateProperties(endView, startingProps, endingProps, interpolatedFraction)

            moveWithOffset(endView, startingProps, endingProps, interpolatedFraction, if (useArcTranslator) curveTranslationHelper else null)

            if (morphChildren && mappings.isNotEmpty()) {
                for (mapping in mappings) {
                    when (morphType) {
                        MorphType.INTO -> {
                            animateProperties(mapping.endView, mapping.startProps, mapping.endProps, interpolatedFraction)

                            mapping.endView.morphX = mapping.startProps.x + (mapping.endProps.x - mapping.startProps.x) * interpolatedFraction
                            mapping.endView.morphY = mapping.startProps.y + (mapping.endProps.y - mapping.startProps.y) * interpolatedFraction
                        }
                        MorphType.FROM -> {
                            animateProperties(mapping.endView, mapping.endProps, mapping.startProps, interpolatedFraction)

                            mapping.endView.morphX = mapping.endProps.x + (mapping.startProps.x - mapping.endProps.x) * interpolatedFraction
                            mapping.endView.morphY = mapping.endProps.y + (mapping.startProps.y - mapping.endProps.y) * interpolatedFraction
                        }
                    }
                }
            }

            when (animationType) {
                AnimationType.REVEAL -> {

                    val dimFraction = mapRange(interpolatedFraction, dimPropertyInto.startOffset, dimPropertyInto.endOffset, 0f, 1f, 0f, 1f)

                    backgroundDimListener?.invoke(dimPropertyInto.fromValue + (dimPropertyInto.toValue - dimPropertyInto.fromValue) * dimFraction)

                    animateContainers(endStateMorphIntoDescriptor, MIN_OFFSET, MAX_OFFSET, fraction)
                    animateContainers(startStateMorphIntoDescriptor, MIN_OFFSET, MAX_OFFSET, fraction)

                    if (animateChildren && !childrenEndRevealed && interpolatedFraction >= endStateChildMorphIntoDescriptor.animateOnOffset) {
                        childrenEndRevealed = true

                        endStateMorphIntoDescriptor.morphState?.morphView?.let { parent ->
                            val children = getAllChildren(parent, !morphChildren) { child -> child.tag == null }

                            animateChildren(endStateChildMorphIntoDescriptor, children, remainingDuration)
                        }
                    }

                    if (animateChildren && !childrenStartRevealed && interpolatedFraction >= startStateChildMorphIntoDescriptor.animateOnOffset) {
                        childrenStartRevealed = true

                        startStateMorphIntoDescriptor.morphState?.morphView?.let { parent ->
                            val children = getAllChildren(parent, !morphChildren) { child -> child.tag == null }

                            animateChildren(startStateChildMorphIntoDescriptor, children, remainingDuration)
                        }
                    }
                }
                AnimationType.CONCEAL -> {

                    val dimFraction = mapRange(interpolatedFraction, dimPropertyFrom.startOffset, dimPropertyFrom.endOffset, 0f, 1f, 0f, 1f)

                    backgroundDimListener?.invoke(dimPropertyFrom.fromValue + (dimPropertyFrom.toValue - dimPropertyFrom.fromValue) * dimFraction)

                    animateContainers(endStateMorphFromDescriptor, MIN_OFFSET, MAX_OFFSET, fraction)
                    animateContainers(startStateMorphFromDescriptor, MIN_OFFSET, MAX_OFFSET, fraction)

                    if (animateChildren && childrenEndRevealed && interpolatedFraction >= endStateChildMorphFromDescriptor.animateOnOffset) {
                        childrenEndRevealed = false

                        endStateMorphFromDescriptor.morphState?.morphView?.let { parent ->
                            val children = getAllChildren(parent, !morphChildren) { child -> child.tag == null }

                            animateChildren(endStateChildMorphFromDescriptor, children, remainingDuration)
                        }
                    }
                    if (animateChildren && childrenStartRevealed && interpolatedFraction >= startStateChildMorphFromDescriptor.animateOnOffset) {
                        childrenStartRevealed = false

                        startStateMorphFromDescriptor.morphState?.morphView?.let { parent ->
                            val children = getAllChildren(parent, !morphChildren) { child -> child.tag == null }

                            animateChildren(startStateChildMorphFromDescriptor, children, remainingDuration)
                        }
                    }
                }
            }

            if (trigger != null && !trigger.hasTriggered) {
                if (interpolatedFraction >= trigger.percentage) {
                    trigger.triggerAction?.invoke()
                    trigger.hasTriggered = true
                }
            }
            transitionOffsetListener?.invoke(interpolatedFraction)
        }

        animator.interpolator = null
        animator.duration = duration
        animator.start()
    }

    private fun moveWithOffset(
        endView: MorphLayout,
        startingProps: Properties,
        endingProps: Properties,
        fraction: Float,
        curveTranslationHelper: CurvedTranslationHelper?
    ) {
        if (curveTranslationHelper != null) {
            endView.morphTranslationX = curveTranslationHelper.getCurvedTranslationX(fraction).toFloat()
            endView.morphTranslationY = curveTranslationHelper.getCurvedTranslationY(fraction).toFloat()
        } else {
            endView.morphTranslationX = startingProps.translationX + (endingProps.translationX - startingProps.translationX) * fraction
            endView.morphTranslationY = startingProps.translationY + (endingProps.translationY - startingProps.translationY) * fraction
        }
    }

    private fun getChildMappings(
        startView: MorphLayout,
        endView: MorphLayout,
        deepSearch: Boolean
    ): List<MorphMap> {

        val startChildren = getAllChildren(startView, deepSearch) { it.tag != null }
        val endChildren = getAllChildren(endView, deepSearch) { it.tag != null }

        val mappings: ArrayList<MorphMap> = ArrayList()

        startChildren.forEach { startChild ->
            endChildren.forEach { endChild ->
                if (startChild.tag == endChild.tag) {

                    val start: MorphLayout = startChild as MorphLayout
                    val end: MorphLayout = endChild as MorphLayout

                    val startProps = start.getProperties()
                    val endProps = end.getProperties()

                    mappings.add(MorphMap(start, end, startProps, endProps))
                }
            }
        }
        return mappings
    }

    private fun animateChildren(
        animDescriptor: ChildAnimationDescriptor,
        children: List<View>,
        inDuration: Long
    ) {
        if (children.isEmpty())
            return

        val duration = animDescriptor.duration ?: inDuration

        animateChildren(children, animDescriptor, duration)
    }

    private fun applyProps(view: MorphLayout, props: Properties) {
        view.morphX = props.x
        view.morphY = props.y
        view.morphAlpha = props.alpha
        view.morphElevation = props.elevation
        view.morphTranslationX = props.translationX
        view.morphTranslationY = props.translationY
        view.morphTranslationZ = props.translationZ
        view.morphPivotX = view.morphWidth
        view.morphPivotY = view.morphHeight
        view.morphRotation = props.rotation
        view.morphRotationX = props.rotationX
        view.morphRotationY = props.rotationY
        view.morphScaleX = props.scaleX
        view.morphScaleY = props.scaleY
        view.morphStateList = props.stateList
        view.morphWidth = props.width
        view.morphHeight = props.height

        if (view.hasGradientDrawable() && view.mutateCorners) {
            view.updateCorners(0, props.cornerRadii[0])
            view.updateCorners(1, props.cornerRadii[1])
            view.updateCorners(2, props.cornerRadii[2])
            view.updateCorners(3, props.cornerRadii[3])
            view.updateCorners(4, props.cornerRadii[4])
            view.updateCorners(5, props.cornerRadii[5])
            view.updateCorners(6, props.cornerRadii[6])
            view.updateCorners(7, props.cornerRadii[7])
        }

        view.updateLayout()
    }

    private fun animateProperties(
        morphView: MorphLayout,
        startingProps: Properties,
        endingProps: Properties,
        fraction: Float
    ) {
        morphView.morphAlpha = startingProps.alpha + (endingProps.alpha - startingProps.alpha) * fraction

        morphView.morphScaleX = startingProps.scaleX + (endingProps.scaleX - startingProps.scaleX) * fraction
        morphView.morphScaleY = startingProps.scaleY + (endingProps.scaleY - startingProps.scaleY) * fraction

        morphView.morphRotation = startingProps.rotation + (endingProps.rotation - startingProps.rotation) * fraction

        morphView.morphRotationX = startingProps.rotationX + (endingProps.rotationX - startingProps.rotationX) * fraction
        morphView.morphRotationY = startingProps.rotationY + (endingProps.rotationY - startingProps.rotationY) * fraction

        morphView.morphTranslationZ = startingProps.translationZ + (endingProps.translationZ - startingProps.translationZ) * fraction

        morphView.morphElevation = startingProps.elevation + (endingProps.elevation - startingProps.elevation) * fraction

        if (morphView.mutateCorners && morphView.hasGradientDrawable() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            morphView.updateCorners(0, startingProps.cornerRadii[0] + (endingProps.cornerRadii[0] - startingProps.cornerRadii[0]) * fraction)
            morphView.updateCorners(1, startingProps.cornerRadii[1] + (endingProps.cornerRadii[1] - startingProps.cornerRadii[1]) * fraction)
            morphView.updateCorners(2, startingProps.cornerRadii[2] + (endingProps.cornerRadii[2] - startingProps.cornerRadii[2]) * fraction)
            morphView.updateCorners(3, startingProps.cornerRadii[3] + (endingProps.cornerRadii[3] - startingProps.cornerRadii[3]) * fraction)
            morphView.updateCorners(4, startingProps.cornerRadii[4] + (endingProps.cornerRadii[4] - startingProps.cornerRadii[4]) * fraction)
            morphView.updateCorners(5, startingProps.cornerRadii[5] + (endingProps.cornerRadii[5] - startingProps.cornerRadii[5]) * fraction)
            morphView.updateCorners(6, startingProps.cornerRadii[6] + (endingProps.cornerRadii[6] - startingProps.cornerRadii[6]) * fraction)
            morphView.updateCorners(7, startingProps.cornerRadii[7] + (endingProps.cornerRadii[7] - startingProps.cornerRadii[7]) * fraction)
        }

        if (morphView.hasMorphTransitionDrawable()) {
            morphView.getMorphTransitionDrawable().updateTransition(fraction)
        }

        if (startingProps.color != endingProps.color) {
            morphView.morphStateList = ColorUtility.interpolateColor(fraction, startingProps.color, endingProps.color).toStateList()
        }

        if (endingProps.width != startingProps.width || endingProps.height != startingProps.height) {
            morphView.morphWidth = startingProps.width + (endingProps.width - startingProps.width) * fraction
            morphView.morphHeight = startingProps.height + (endingProps.height - startingProps.height) * fraction

            morphView.updateLayout()
        }
    }

    private fun getAllChildren(
        view: MorphLayout,
        deepSearch: Boolean,
        predicate: ((View) -> Boolean)? = null
    ): List<View> {

        if (!deepSearch) {
            return if (predicate != null) {
                view.getChildren().filter(predicate).toArrayList()
            } else {
                view.getChildren().toArrayList()
            }
        }

        val visited = ArrayList<View>()
        val unvisited = ArrayList<View>()

        var initialCondition = true

        while (unvisited.isNotEmpty() || initialCondition) {

            if (!initialCondition) {
                val child = unvisited.removeAt(0)

                if (predicate != null) {
                    if (predicate(child)) {
                        visited.add(child)
                    }
                } else {
                    visited.add(child)
                }

                if (child !is ViewGroup)
                    continue

                (0 until child.childCount).mapTo(unvisited) { child.getChildAt(it) }
            } else {
                (0 until view.morphChildCount).mapTo(unvisited) { view.getChildViewAt(it) }

                initialCondition = false
            }
        }

        return visited
    }

    private fun animateContainers(descriptor: AnimationDescriptor, startValue: Float, endValue: Float, fraction: Float ){
        val scaleX = descriptor.propertyScaleX
        val scaleY = descriptor.propertyScaleY
        val alpha = descriptor.propertyAlpha

        val scaleXFraction = mapRange(fraction, scaleX.startOffset, scaleX.endOffset, startValue, endValue, startValue, endValue)
        val scaleYFraction = mapRange(fraction, scaleY.startOffset, scaleY.endOffset, startValue, endValue, startValue, endValue)
        val alphaFraction = mapRange(fraction, alpha.startOffset, alpha.endOffset, startValue, endValue, startValue, endValue)

        val scaleXInterpolation = descriptor.propertyScaleX.interpolator?.getInterpolation(scaleXFraction) ?: scaleXFraction
        val scaleYInterpolation = descriptor.propertyScaleY.interpolator?.getInterpolation(scaleYFraction) ?: scaleYFraction
        val alphaInterpolation = descriptor.propertyAlpha.interpolator?.getInterpolation(alphaFraction) ?: alphaFraction

        val scaleXDelta = scaleX.fromValue + (scaleX.toValue - scaleX.fromValue) * scaleXInterpolation
        val scaleYDelta = scaleY.fromValue + (scaleY.toValue - scaleY.fromValue) * scaleYInterpolation
        val alphaDelta =  alpha.fromValue  + (alpha.toValue  - alpha.fromValue)  * alphaInterpolation

        val stateLayout = descriptor.morphState?.morphView

        if (stateLayout != null) {
            stateLayout.morphScaleX = scaleXDelta
            stateLayout.morphScaleY = scaleYDelta
            stateLayout.morphAlpha = alphaDelta
        }
    }

    private fun <T: View> animateChildren(
        inChildren: List<T>,
        descriptor: ChildAnimationDescriptor,
        totalDuration: Long
    ) {
        if (!inChildren.any())
            return

        val startDelay = descriptor.delay ?: 0L

        val delta = totalDuration * descriptor.durationMultiplier

        val startStateProps = descriptor.startStateProps
        val endStateProps = descriptor.endStateProps

        if (totalDuration == 0L) {
            inChildren.forEach { child ->
                child.visibility = endStateProps.visibility
                child.translationX = endStateProps.translationX
                child.translationY = endStateProps.translationY
                child.translationZ = startStateProps.translationZ
                child.rotationX = endStateProps.rotationX
                child.rotationY = endStateProps.rotationY
                child.rotation = endStateProps.rotation
                child.scaleX = endStateProps.scaleX
                child.scaleY = endStateProps.scaleY
                child.alpha = endStateProps.alpha
            }
            return
        }

        val durationDelta = when (descriptor.type) {
            AnimationType.REVEAL -> totalDuration + (delta).roundToLong()
            AnimationType.CONCEAL -> totalDuration + (delta).roundToLong()
        }

        val children = if (descriptor.reversed) inChildren.reversed() else inChildren

        if (descriptor.stagger != null) {
            descriptor.stagger?.let { stagger ->
                val delayAdd = (durationDelta * stagger.multiplier).toLong()
                val duration = durationDelta - (delayAdd / children.count())
                var delay = startDelay
                for (child in children) {
                    if (child is MorphLayout) {
                        if (!child.animate) {
                            continue
                        }
                    }
                    if (startStateProps.visibility == INVISIBLE) {
                        continue
                    }
                    child.visibility = startStateProps.visibility
                    child.translationX = startStateProps.translationX
                    child.translationY = startStateProps.translationY
                    child.translationZ = startStateProps.translationZ
                    child.rotationX = startStateProps.rotationX
                    child.rotationY = startStateProps.rotationY
                    child.rotation = startStateProps.rotation
                    child.scaleX = startStateProps.scaleX
                    child.scaleY = startStateProps.scaleY
                    child.alpha = startStateProps.alpha
                    child.animate()
                        .setListener(null)
                        .setDuration(duration)
                        .setStartDelay(delay)
                        .alpha(endStateProps.alpha)
                        .scaleX(endStateProps.scaleX)
                        .scaleY(endStateProps.scaleY)
                        .rotation(endStateProps.rotation)
                        .rotationX(endStateProps.rotationX)
                        .rotationY(endStateProps.rotationY)
                        .translationX(endStateProps.translationX)
                        .translationY(endStateProps.translationY)
                        .translationZ(endStateProps.translationZ)
                        .setInterpolator(descriptor.interpolator)
                        .start()
                    delay += delayAdd
                }
            }
        } else {
            for (child in children) {
                if (child is MorphLayout) {
                    if (!child.animate) {
                        continue
                    }
                }
                child.visibility = startStateProps.visibility
                child.translationX = startStateProps.translationX
                child.translationY = startStateProps.translationY
                child.translationZ = startStateProps.translationZ
                child.rotationX = startStateProps.rotationX
                child.rotationY = startStateProps.rotationY
                child.rotation = startStateProps.rotation
                child.scaleX = startStateProps.scaleX
                child.scaleY = startStateProps.scaleY
                child.alpha = startStateProps.alpha
                child.animate()
                    .setListener(null)
                    .setDuration(durationDelta)
                    .setStartDelay(startDelay)
                    .alpha(endStateProps.alpha)
                    .scaleX(endStateProps.scaleX)
                    .scaleY(endStateProps.scaleY)
                    .rotation(endStateProps.rotation)
                    .rotationX(endStateProps.rotationX)
                    .rotationY(endStateProps.rotationY)
                    .translationX(endStateProps.translationX)
                    .translationY(endStateProps.translationY)
                    .translationZ(endStateProps.translationZ)
                    .setInterpolator(descriptor.interpolator)
                    .start()
            }
        }
    }

    private fun calculatePivot(forView: MorphLayout, inRelationToView: MorphLayout): Point<Float> {
        val dimensionStart = Dimension(forView.morphWidth, forView.morphHeight)
        val dimensionRelation = Dimension(inRelationToView.morphWidth, inRelationToView.morphHeight)

        val scaleX = dimensionStart.width / dimensionRelation.width
        val scaleY = dimensionStart.height / dimensionRelation.height

        val startTop = forView.windowLocationY
        val startLeft = forView.windowLocationX

        val relativeTop = inRelationToView.windowLocationY
        val relativeLeft = inRelationToView.windowLocationX

        val topDistance = abs(startTop - relativeTop)
        val leftDistance = abs(startLeft - relativeLeft)

        val centerPointX = leftDistance + (dimensionStart.width / 2)
        val centerPointY = topDistance + (dimensionStart.height / 2)

        return Point(centerPointX * scaleX, centerPointY * scaleY)
    }

    private fun computeAnimationDirection(startView: MorphLayout, endView: MorphLayout): TranslationPositions {
        val centerStartX = startView.windowLocationX + (startView.morphWidth / 2)
        val centerStartY = startView.windowLocationY + (startView.morphHeight / 2)

        val centerEndX = endView.windowLocationX + (endView.morphWidth / 2)
        val centerEndY = endView.windowLocationY + (endView.morphHeight / 2)

        val distanceX = abs(centerStartX - centerEndX)
        val distanceY = abs(centerStartY - centerEndY)

        if (distanceX < DISTANCE_THRESHOLD && distanceY < DISTANCE_THRESHOLD) {
            return TranslationPositions.of(TranslationPosition.CENTER)
        }

        return when {
            centerStartY < centerEndY && centerStartX < centerEndX -> TranslationPosition.top(distanceY) and TranslationPosition.left(distanceX)
            centerStartY < centerEndY && centerStartX > centerEndX -> TranslationPosition.top(distanceY) and TranslationPosition.right(distanceX)
            centerStartY > centerEndY && centerStartX < centerEndX -> TranslationPosition.bottom(distanceY) and TranslationPosition.left(distanceX)
            centerStartY > centerEndY && centerStartX > centerEndX -> TranslationPosition.bottom(distanceY) and TranslationPosition.right(distanceX)

            centerStartY < centerEndY && centerStartX == centerEndX -> TranslationPositions.of(TranslationPosition.TOP)
            centerStartY > centerEndY && centerStartX == centerEndX -> TranslationPositions.of(TranslationPosition.BOTTOM)
            centerStartY == centerEndY && centerStartX < centerEndX -> TranslationPositions.of(TranslationPosition.LEFT)
            centerStartY == centerEndY && centerStartX > centerEndX -> TranslationPositions.of(TranslationPosition.RIGHT)

            else -> TranslationPositions.of(TranslationPosition.CENTER)
        }
    }

    companion object {

        const val MAX_OFFSET: Float = 1f
        const val MIN_OFFSET: Float = 0f

        const val DEFAULT_DURATION: Long = 350L

        const val DEFAULT_CHILDREN_REVEAL_OFFSET: Float = 0.0f
        const val DEFAULT_CHILDREN_CONCEAL_OFFSET: Float = 0.0f

        const val DEFAULT_REVEAL_DURATION_MULTIPLIER: Float = 0.2f
        const val DEFAULT_CONCEAL_DURATION_MULTIPLIER: Float = -0.2f

        const val DEFAULT_CHILDREN_STAGGER_MULTIPLIER: Float = 0.15f

        const val DEFAULT_TRANSLATION_MULTIPLIER: Float = 0.20f

        val DISTANCE_THRESHOLD = 20.dp
    }

    data class MorphState(
        val morphView: MorphLayout,
        val stateProps: Properties
    )

    data class MorphMap(
        var startView: MorphLayout,
        var endView: MorphLayout,
        var startProps: Properties,
        var endProps: Properties
    )

    data class OffsetTrigger(
        val percentage: Float,
        val triggerAction: Action,
        var hasTriggered: Boolean = false
    )

    data class Properties(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val alpha: Float,
        val elevation: Float,
        var translationX: Float,
        var translationY: Float,
        val translationZ: Float,
        val pivotX: Float,
        val pivotY: Float,
        val rotation: Float,
        val rotationX: Float,
        val rotationY: Float,
        var scaleX: Float,
        var scaleY: Float,
        val color: Int,
        val stateList: ColorStateList?,
        var cornerRadii: CornerRadii,
        val windowLocationX: Int,
        val windowLocationY: Int,
        val background: Drawable?,
        val hasVectorBackground: Boolean,
        val hasBitmapBackground: Boolean,
        val hasGradientBackground: Boolean,
        val tag: String
    ) {
        fun getDeltaCoordinates() = Coordintates(translationX, translationY)

        override fun toString() = tag
    }

    data class AnimationStagger(
        val multiplier: Float = DEFAULT_CHILDREN_STAGGER_MULTIPLIER
    )

    data class PropertyDescriptor<T>(
        val propertyType: String,
        var fromValue: T,
        var toValue: T,
        var startOffset: Float = MIN_OFFSET,
        var endOffset: Float = MAX_OFFSET,
        var interpolator: TimeInterpolator? = null
    ) {
        companion object {
            const val X = "x"
            const val Y = "y"
            const val COLOR = "color"
            const val ALPHA = "alpha"
            const val SCALE_X = "scale_x"
            const val SCALE_Y = "scale_y"
            const val ROTATION = "rotation"
            const val ROTATION_X = "rotation_x"
            const val ROTATION_Y = "rotation_y"
            const val TRANSLATION_X = "translation_x"
            const val TRANSLATION_Y = "translation_Y"
        }
    }

    open class AnimationProperties (
        alpha: Float = MAX_OFFSET,
        elevation: Float = MIN_OFFSET,
        translationX: Float = MIN_OFFSET,
        translationY: Float = MIN_OFFSET,
        translationZ: Float = MIN_OFFSET,
        rotation: Float = MIN_OFFSET,
        rotationX: Float = MIN_OFFSET,
        rotationY: Float = MIN_OFFSET,
        scaleX: Float = MAX_OFFSET,
        scaleY: Float = MAX_OFFSET,
        visibility: Int = VISIBLE
    ): Cloneable {

        var alpha: Float = alpha
            internal set
        var elevation: Float = elevation
            internal set
        var translationX: Float = translationX
            internal set
        var translationY: Float = translationY
            internal set
        var translationZ: Float = translationZ
            internal set
        var rotation: Float = rotation
            internal set
        var rotationX: Float = rotationX
            internal set
        var rotationY: Float = rotationY
            internal set
        var scaleX: Float = scaleX
            internal set
        var scaleY: Float = scaleY
            internal set
        var visibility: Int = visibility
            internal set

        companion object {
            fun from(morpher: Morpher, animatorDescriptor: ChildAnimationDescriptor, propertyChange: AnimationProperties, endingViewStart: MorphLayout, endingViewEnd: MorphLayout, valueType: ValueType): AnimationProperties {

                val allowDiagonal = animatorDescriptor.allowDiagonalTranslation

                return when (valueType) {
                    ValueType.START -> {
                        val direction = morpher.computeAnimationDirection(endingViewStart, endingViewEnd)

                        when  {
                            direction has TranslationPosition.TOP.and(TranslationPosition.LEFT)-> {
                                val xAmountQualified = direction.get(TranslationPosition.LEFT).amount > DISTANCE_THRESHOLD
                                val yAmountQualified = direction.get(TranslationPosition.TOP).amount > DISTANCE_THRESHOLD

                                AnimationProperties(
                                    alpha = propertyChange.alpha,
                                    scaleX = 1f,
                                    scaleY = 1f,
                                    translationX = if (xAmountQualified && allowDiagonal) -propertyChange.translationX else 0f,
                                    translationY = if (yAmountQualified && allowDiagonal) -propertyChange.translationY else 0f
                                )
                            }
                            direction has TranslationPosition.TOP.and(TranslationPosition.RIGHT)-> {
                                val xAmountQualified = direction.get(TranslationPosition.RIGHT).amount > DISTANCE_THRESHOLD
                                val yAmountQualified = direction.get(TranslationPosition.TOP).amount > DISTANCE_THRESHOLD

                                AnimationProperties(
                                    alpha = propertyChange.alpha,
                                    scaleX = 1f,
                                    scaleY = 1f,
                                    translationX = if (xAmountQualified && allowDiagonal) propertyChange.translationX else 0f,
                                    translationY = if (yAmountQualified && allowDiagonal) -propertyChange.translationY else 0f
                                )
                            }
                            direction has TranslationPosition.BOTTOM.and(TranslationPosition.LEFT)-> {
                                val xAmountQualified = direction.get(TranslationPosition.LEFT).amount > DISTANCE_THRESHOLD
                                val yAmountQualified = direction.get(TranslationPosition.BOTTOM).amount > DISTANCE_THRESHOLD

                                AnimationProperties(
                                    alpha = propertyChange.alpha,
                                    scaleX = 1f,
                                    scaleY = 1f,
                                    translationX = if (xAmountQualified && allowDiagonal) -propertyChange.translationX else 0f,
                                    translationY = if (yAmountQualified && allowDiagonal) propertyChange.translationY else 0f
                                )
                            }
                            direction has TranslationPosition.BOTTOM.and(TranslationPosition.RIGHT)-> {
                                val xAmountQualified = direction.get(TranslationPosition.RIGHT).amount > DISTANCE_THRESHOLD
                                val yAmountQualified = direction.get(TranslationPosition.BOTTOM).amount > DISTANCE_THRESHOLD

                                AnimationProperties(
                                    alpha = propertyChange.alpha,
                                    scaleX = 1f,
                                    scaleY = 1f,
                                    translationX = if (xAmountQualified && allowDiagonal) propertyChange.translationX else 0f,
                                    translationY = if (yAmountQualified && allowDiagonal) propertyChange.translationY else 0f
                                )
                            }
                            direction has TranslationPosition.TOP -> {
                                val amountQualified = direction.get(TranslationPosition.TOP).amount > DISTANCE_THRESHOLD

                                AnimationProperties(
                                    alpha = propertyChange.alpha,
                                    scaleX = 1f,
                                    scaleY = 1f,
                                    translationY = if (amountQualified) -propertyChange.translationY else 0f
                                )
                            }
                            direction has TranslationPosition.BOTTOM -> {
                                val amountQualified = direction.get(TranslationPosition.BOTTOM).amount > DISTANCE_THRESHOLD

                                AnimationProperties(
                                    alpha = propertyChange.alpha,
                                    scaleX = 1f,
                                    scaleY = 1f,
                                    translationY = if (amountQualified) propertyChange.translationY else 0f
                                )
                            }
                            direction has TranslationPosition.LEFT -> {
                                val amountQualified = direction.get(TranslationPosition.LEFT).amount > DISTANCE_THRESHOLD

                                AnimationProperties(
                                    alpha = propertyChange.alpha,
                                    scaleX = 1f,
                                    scaleY = 1f,
                                    translationX = if (amountQualified) -propertyChange.translationX else 0f
                                )
                            }
                            direction has TranslationPosition.RIGHT -> {
                                val amountQualified = direction.get(TranslationPosition.RIGHT).amount > DISTANCE_THRESHOLD

                                AnimationProperties(
                                    alpha = propertyChange.alpha,
                                    scaleX = 1f,
                                    scaleY = 1f,
                                    translationX = if (amountQualified) propertyChange.translationX else 0f
                                )
                            }
                            else -> {
                                AnimationProperties(
                                    alpha = propertyChange.alpha,
                                    scaleX = 1f,
                                    scaleY = 1f,
                                    translationY = 0f
                                )
                            }
                        }
                    }
                    else -> AnimationProperties()
                }
            }
        }
    }

    class ChildAnimationDescriptor (
        var type: AnimationType,
        var allowDiagonalTranslation: Boolean = false,
        var animateOnOffset: Float = DEFAULT_CHILDREN_REVEAL_OFFSET,
        var durationMultiplier: Float = DEFAULT_REVEAL_DURATION_MULTIPLIER,
        var defaultTranslateMultiplier: Float = DEFAULT_TRANSLATION_MULTIPLIER,
        var interpolator: TimeInterpolator? = null,
        var stagger: AnimationStagger? = null,
        var reversed: Boolean = false,
        var duration: Long? = null,
        var delay: Long? = null,
        startStateProps: AnimationProperties = AnimationProperties(),
        endStateProps: AnimationProperties = AnimationProperties()
    ) {
        internal var default: Boolean = true

        private var _startStateProps: AnimationProperties = startStateProps

        private var _endStateProps: AnimationProperties = endStateProps

        var startStateProps: AnimationProperties
            get() = _startStateProps
            set(value) {
                _startStateProps = value
                default = false
            }

        var endStateProps: AnimationProperties
            get() = _endStateProps
            set(value) {
                _endStateProps = value
                default = false
            }

        init {
            default = false
        }
    }

    data class AnimationDescriptor(
        var type: AnimationType,
        var propertyTranslateX: PropertyDescriptor<Float> = PropertyDescriptor(PropertyDescriptor.TRANSLATION_X, MIN_OFFSET, MIN_OFFSET),
        var propertyTranslateY: PropertyDescriptor<Float> = PropertyDescriptor(PropertyDescriptor.TRANSLATION_Y, MIN_OFFSET, MIN_OFFSET),
        var propertyRotationX: PropertyDescriptor<Float> = PropertyDescriptor(PropertyDescriptor.ROTATION_X, MIN_OFFSET, MIN_OFFSET),
        var propertyRotationY: PropertyDescriptor<Float> = PropertyDescriptor(PropertyDescriptor.ROTATION_Y, MIN_OFFSET, MIN_OFFSET),
        var propertyRotation: PropertyDescriptor<Float> = PropertyDescriptor(PropertyDescriptor.ROTATION, MIN_OFFSET, MIN_OFFSET),
        var propertyScaleX: PropertyDescriptor<Float> = PropertyDescriptor(PropertyDescriptor.SCALE_X, MIN_OFFSET, MAX_OFFSET),
        var propertyScaleY: PropertyDescriptor<Float> = PropertyDescriptor(PropertyDescriptor.SCALE_Y, MIN_OFFSET, MAX_OFFSET),
        var propertyAlpha: PropertyDescriptor<Float> = PropertyDescriptor(PropertyDescriptor.ALPHA, MIN_OFFSET, MAX_OFFSET),
        var propertyColor: PropertyDescriptor<Int> = PropertyDescriptor(PropertyDescriptor.COLOR, 0, 255),
        var propertyX: PropertyDescriptor<Float> = PropertyDescriptor(PropertyDescriptor.X, MIN_OFFSET, MIN_OFFSET),
        var propertyY: PropertyDescriptor<Float> = PropertyDescriptor(PropertyDescriptor.Y, MIN_OFFSET, MIN_OFFSET),
        var morphState: MorphState? = null
    )

    enum class ValueType { START , END }

    enum class MorphType { INTO, FROM }

    enum class AnimationType { REVEAL, CONCEAL }

   // enum class MorphFlag { DISOLVE, CROSS_DISSOLVE, FADE_THROUGH, TRANSFORM }

    enum class TranslationPosition(var amount: Float) {
        TOP(0f),
        LEFT(0f),
        RIGHT(0f),
        BOTTOM(0f),
        CENTER(0f);

        fun withAmount(amount: Float): TranslationPosition {
            this.amount = amount
            return this
        }

        infix fun and(other: TranslationPosition): TranslationPositions = TranslationPositions.of(this, other)

        companion object {
            fun top(amount: Float): TranslationPosition {
                return TOP.withAmount(amount)
            }
            fun left(amount: Float): TranslationPosition {
                return LEFT.withAmount(amount)
            }
            fun right(amount: Float): TranslationPosition {
                return RIGHT.withAmount(amount)
            }
            fun bottom(amount: Float): TranslationPosition {
                return BOTTOM.withAmount(amount)
            }
        }
    }
}

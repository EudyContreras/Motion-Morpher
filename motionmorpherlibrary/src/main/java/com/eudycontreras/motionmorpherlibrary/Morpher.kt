package com.eudycontreras.motionmorpherlibrary

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.core.view.children
import com.eudycontreras.motionmorpherlibrary.drawables.MorphTransitionDrawable
import com.eudycontreras.motionmorpherlibrary.enumerations.AnimationType
import com.eudycontreras.motionmorpherlibrary.extensions.*
import com.eudycontreras.motionmorpherlibrary.helpers.ArcTranslationHelper
import com.eudycontreras.motionmorpherlibrary.interactions.Interaction
import com.eudycontreras.motionmorpherlibrary.interfaces.Cloneable
import com.eudycontreras.motionmorpherlibrary.interpolators.Easing
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.layouts.MorphView
import com.eudycontreras.motionmorpherlibrary.listeners.MorphAnimationListener
import com.eudycontreras.motionmorpherlibrary.properties.*
import com.eudycontreras.motionmorpherlibrary.properties.AnimatedValues.AnimatedFloatValue
import com.eudycontreras.motionmorpherlibrary.properties.AnimatedValues.AnimatedIntValue
import com.eudycontreras.motionmorpherlibrary.properties.AnimatedValue
import com.eudycontreras.motionmorpherlibrary.properties.AnimatedProperties
import com.eudycontreras.motionmorpherlibrary.utilities.ColorUtility
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since June 10 2019
 */

class Morpher(private val context: Context) {

    private var lastOffset = MIN_OFFSET

    private var mappingsCreated: Boolean = false
    private var initialValuesApplied: Boolean = false

   // private var children: List<MorphLayout> = LinkedList()

    private val curveTranslator = ArcTranslationHelper()

    private lateinit var startingView: MorphLayout
    private lateinit var endingView: MorphLayout

    private lateinit var startingState: AnimatedProperties
    private lateinit var endingState: AnimatedProperties

    private lateinit var mappings: List<MorphMap>

    var dimPropertyInto: AnimatedFloatValue =
        AnimatedFloatValue(
            propertyName = AnimatedValue.COLOR,
            fromValue = MIN_OFFSET,
            toValue = MAX_OFFSET,
            startOffset = MIN_OFFSET,
            endOffset = MAX_OFFSET
        )

    var dimPropertyFrom: AnimatedFloatValue =
        AnimatedFloatValue(
            propertyName = AnimatedValue.COLOR,
            fromValue = MAX_OFFSET,
            toValue = MIN_OFFSET,
            startOffset = MIN_OFFSET,
            endOffset = MAX_OFFSET
        )

    var containerStateIn: AnimationDescriptor = AnimationDescriptor(AnimationType.REVEAL)
    var containerStateOut: AnimationDescriptor = AnimationDescriptor(AnimationType.CONCEAL)

    var placeholderStateIn: AnimationDescriptor = AnimationDescriptor(AnimationType.REVEAL)
    var placeholderStateOut: AnimationDescriptor = AnimationDescriptor(AnimationType.CONCEAL)

    var otherStateIn: AnimationDescriptor = AnimationDescriptor(AnimationType.REVEAL)
    var otherStateOut: AnimationDescriptor = AnimationDescriptor(AnimationType.CONCEAL)

    var containerChildStateIn: ChildAnimationDescriptor = ChildAnimationDescriptor.getDefault(AnimationType.REVEAL)
    var containerChildStateOut: ChildAnimationDescriptor = ChildAnimationDescriptor.getDefault(AnimationType.CONCEAL)

    var placeholderChildStateIn: ChildAnimationDescriptor = ChildAnimationDescriptor(AnimationType.REVEAL)
    var placeholderChildStateOut: ChildAnimationDescriptor = ChildAnimationDescriptor(AnimationType.CONCEAL)

    var backgroundDimListener: BackgroundDimListener = null
    var computedStatesListener: ComputedStatesListener = null
    var transitionProgressListener: TransitionProgressListener = null
    var containerBoundsListener: ContainerBoundsListener = null
    var morphValuesListener: MorphValuesListener = null
    var siblingInteraction: Interaction? = null

    var arcTranslationControlPoint: Coordinates? = null

    var morphIntoInterpolator: TimeInterpolator = Easing.STANDARD
    var morphFromInterpolator: TimeInterpolator = Easing.STANDARD

    var outgoingInterpolator: TimeInterpolator = Easing.OUTGOING
    var incomingInterpolator: TimeInterpolator = Easing.INCOMING

    var useDeepChildSearch: Boolean = true
    var useArcTranslator: Boolean = false
    var animateChildren: Boolean = true
    var morphChildren: Boolean = true

    var morphIntoDuration: Long = DEFAULT_DURATION
    var morphFromDuration: Long = DEFAULT_DURATION

    var overlayCrossfadeDurationIn: Long = MIN_DURATION
    var overlayCrossfadeDurationOut: Long = 150L

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

    var endView: MorphLayout
        get() = endingView
        set(value) {
            endingView = value
            mappingsCreated = false
        }

    fun applyMinimumDimensions(view: View) {
        view.layoutParams.width = view.width
        view.layoutParams.height = view.height
        if (view is ViewGroup) {
            for (child in view.children) {
                applyMinimumDimensions(child)
            }
        }
    }

    private fun performSetup() {
        endingState = endingView.getProperties()
        startingState = startingView.getProperties()

        mappings = if (morphChildren) {
            getChildMappings(startingView, endingView, useDeepChildSearch)
        } else emptyList()

        val containers = getContainers(endView)
        val placeholders = getPlaceholders(endView)
        val children = getUntaggedChildren(endView) { (!it.placeholder && !it.animatedContainer && it.morphVisibility != GONE) }
        val endChildren = getAllChildren(endView, useDeepChildSearch) { it.tag != null }

        endChildren.forEach {
            val view = if (it is MorphLayout) {
                it
            } else {
                MorphView.makeMorphable(it)
            }
            if (endView.hasMorphTransitionDrawable()) {
                val transition = view.getMorphTransitionDrawable()
                transition.resetTransition()
            }
        }

        applyProps(endView, startingState)

        applyEndStateDescriptors(containers, containerStateIn, containerStateOut, startingState, endingState, true)

        applyEndStateDescriptors(children, otherStateIn, otherStateOut, startingState, endingState, false)

        applyStartStateDescriptors(placeholders, placeholderStateIn, placeholderStateOut, endingState, startingState)

        adjustChildAnimations(endView, startView)

        endingView.getChildren().forEach {
            applyMinimumDimensions(it)
        }

        mappings.forEach {
            applyProps(it.endView, it.startProps)
        }

        children.forEach {
            applyChildProperties(it)
            it.morphPivotY = it.morphHeight * MIN_OFFSET
        } // TODO("Find out how to calculate this dynamically")

        containers.forEach {
            applyChildProperties(it)
            it.morphPivotY = it.morphHeight * MIN_OFFSET
        } // TODO("Find out how to calculate this dynamically")

        placeholders.forEach {
            // it.morphPivotY = it.morphHeight * MAX_OFFSET
            //it.morphPivotX = it.morphWidth * 0.45f
        } // TODO("Find out how to calculate this dynamically")

        startingState.translationX =  startingState.windowLocationX.toFloat() - endingState.windowLocationX.toFloat()
        startingState.translationY =  startingState.windowLocationY.toFloat() - endingState.windowLocationY.toFloat()

        endingView.morphTranslationX = startingState.translationX
        endingView.morphTranslationY = startingState.translationY

        initialValuesApplied = true
    }

    private fun getContainers(view: MorphLayout): List<MorphLayout> {
        return view.getChildren()
            .map { if (it is MorphLayout) it else MorphView.makeMorphable(it)}
            .filter { it.animatedContainer }
            .toList()
    }

    private fun getPlaceholders(view: MorphLayout): List<MorphLayout> {
        return view.getChildren()
            .map { if (it is MorphLayout) it else MorphView.makeMorphable(it)}
            .filter { it.placeholder }
            .toList()
    }

    private fun getUntaggedChildren(view: MorphLayout, predicate: ((MorphLayout) -> Boolean)? = null): List<MorphLayout> {
        return view.getChildren()
            .map { if (it is MorphLayout) it else MorphView.makeMorphable(it)}
            .filter { it.morphTag == null && predicate?.invoke(it) ?: true }
            .toList()
    }

    private fun getTaggedChildren(view: MorphLayout, predicate: ((MorphLayout) -> Boolean)? = null): List<MorphLayout> {
        return view.getChildren()
            .map { if (it is MorphLayout) it else MorphView.makeMorphable(it)}
            .filter { it.morphTag != null && predicate?.invoke(it) ?: true  }
            .toList()
    }

    private fun applyEndStateDescriptors(
        containers: List<MorphLayout>,
        morphIntoDescriptor: AnimationDescriptor,
        morphFromDescriptor: AnimationDescriptor,
        startingState: AnimatedProperties,
        endingState: AnimatedProperties,
        container: Boolean
    ) {
        morphIntoDescriptor.childrenRevealed = false
        morphIntoDescriptor.animationContainer = container
        morphIntoDescriptor.propertyScaleX.fromValue = (startingState.width) / (endingState.width)
        morphIntoDescriptor.propertyScaleY.fromValue = (startingState.width) / (endingState.width)

        morphIntoDescriptor.propertyScaleX.toValue = MAX_OFFSET
        morphIntoDescriptor.propertyScaleY.toValue = MAX_OFFSET

        morphIntoDescriptor.propertyAlpha.fromValue = MIN_OFFSET
        morphIntoDescriptor.propertyAlpha.toValue = MAX_OFFSET

/*        morphIntoDescriptor.propertyAlpha.interpolateOffsetStart = 0.4f
        morphIntoDescriptor.propertyAlpha.interpolateOffsetEnd = MAX_OFFSET*/

        morphIntoDescriptor.propertyScaleX.interpolator = incomingInterpolator
        morphIntoDescriptor.propertyScaleY.interpolator = incomingInterpolator
       /* morphIntoDescriptor.propertyAlpha.interpolator = morphIntoInterpolator*/

        morphIntoDescriptor.morphStates = containers.map { MorphState(it).apply {
            children = getAllChildren(morphView, false) { child -> child.tag == null }
        }}

        morphFromDescriptor.childrenRevealed = false
        morphFromDescriptor.animationContainer = container
        morphFromDescriptor.propertyScaleX.fromValue = morphIntoDescriptor.propertyScaleX.toValue
        morphFromDescriptor.propertyScaleY.fromValue = morphIntoDescriptor.propertyScaleY.toValue

        morphFromDescriptor.propertyScaleX.toValue = morphIntoDescriptor.propertyScaleX.fromValue
        morphFromDescriptor.propertyScaleY.toValue = morphIntoDescriptor.propertyScaleY.fromValue

        morphFromDescriptor.propertyAlpha.fromValue = morphIntoDescriptor.propertyAlpha.toValue
        morphFromDescriptor.propertyAlpha.toValue = morphIntoDescriptor.propertyAlpha.fromValue

        /*morphFromDescriptor.propertyAlpha.interpolateOffsetStart = MIN_OFFSET
        morphFromDescriptor.propertyAlpha.interpolateOffsetEnd = 0.3f*/

        morphFromDescriptor.propertyScaleX.interpolator = outgoingInterpolator
        morphFromDescriptor.propertyScaleY.interpolator = outgoingInterpolator
        //morphFromDescriptor.propertyAlpha.interpolator = morphFromInterpolator

        morphFromDescriptor.morphStates = containers.map { MorphState(it).apply {
            children =  getAllChildren(morphView, false) { child -> child.tag == null }
        }}
    }

    private fun applyStartStateDescriptors(
        placeholders: List<MorphLayout>,
        morphIntoDescriptor: AnimationDescriptor,
        morphFromDescriptor: AnimationDescriptor,
        startingState: AnimatedProperties,
        endingState: AnimatedProperties
    ) {

        morphIntoDescriptor.propertyScaleX.toValue = (startingState.width) / (endingState.width)
        morphIntoDescriptor.propertyScaleY.toValue = (startingState.width) / (endingState.width)

        morphIntoDescriptor.propertyScaleX.fromValue = MAX_OFFSET
        morphIntoDescriptor.propertyScaleY.fromValue = MAX_OFFSET

        morphIntoDescriptor.propertyAlpha.fromValue = MAX_OFFSET
        morphIntoDescriptor.propertyAlpha.toValue = MIN_OFFSET

        morphIntoDescriptor.propertyAlpha.interpolateOffsetStart = MIN_OFFSET
        morphIntoDescriptor.propertyAlpha.interpolateOffsetEnd = 0.4f

        morphIntoDescriptor.propertyScaleX.interpolator = incomingInterpolator
        morphIntoDescriptor.propertyScaleY.interpolator = incomingInterpolator
        morphIntoDescriptor.propertyAlpha.interpolator = incomingInterpolator

        morphIntoDescriptor.morphStates = placeholders.map { MorphState(it).apply {
            children =  getAllChildren(morphView, false) { child -> child.tag == null }
        }}

        morphFromDescriptor.propertyScaleX.fromValue = morphIntoDescriptor.propertyScaleX.toValue
        morphFromDescriptor.propertyScaleY.fromValue = morphIntoDescriptor.propertyScaleY.toValue

        morphFromDescriptor.propertyScaleX.toValue = morphIntoDescriptor.propertyScaleX.fromValue
        morphFromDescriptor.propertyScaleY.toValue = morphIntoDescriptor.propertyScaleY.fromValue

        morphFromDescriptor.propertyAlpha.fromValue = morphIntoDescriptor.propertyAlpha.toValue
        morphFromDescriptor.propertyAlpha.toValue = morphIntoDescriptor.propertyAlpha.fromValue

        morphFromDescriptor.propertyAlpha.interpolateOffsetStart = 0.3f
        morphFromDescriptor.propertyAlpha.interpolateOffsetEnd = MAX_OFFSET

        morphFromDescriptor.propertyScaleX.interpolator = outgoingInterpolator
        morphFromDescriptor.propertyScaleY.interpolator = outgoingInterpolator
        morphFromDescriptor.propertyAlpha.interpolator = outgoingInterpolator

        morphFromDescriptor.morphStates = placeholders.map { MorphState(it).apply {
            children =  getAllChildren(morphView, false) { child -> child.tag == null }
        }}
    }

    private fun applyChildProperties(view: MorphLayout) {
        for (child in view.getChildren()) {

            if (child is MorphLayout && !child.animate)
                continue

            child.layoutParams.width = child.width
            child.layoutParams.height = child.height

            if (animateChildren)
            child.alpha = MIN_OFFSET
        }
    }

    /*private fun applyPivots(
        start: MorphLayout,
        end: MorphLayout
    ) {
        val pivotPoint: Point<Float> = calculatePivot(start, end)

        start.morphPivotX = pivotPoint.x
        start.morphPivotY = pivotPoint.y

        start.morphWidth = start.morphWidth
        start.morphHeight = start.morphHeight

        end.morphPivotX = MIN_OFFSET
        end.morphPivotY = MIN_OFFSET

        end.morphWidth = end.morphWidth
        end.morphHeight = end.morphHeight
    }*/

    private fun adjustChildAnimations(
        endView: MorphLayout,
        startView: MorphLayout
    ) {
        containerChildStateIn.let {
            val propertyChange = it.startStateProps

            propertyChange.translationX = endView.morphWidth * it.defaultTranslateMultiplierX
            propertyChange.translationY = endView.morphHeight * it.defaultTranslateMultiplierY

            it.startStateProps.computeTranslation(this, propertyChange, startView, endView)
        }

        containerChildStateOut.let {
            val clone = containerChildStateIn.startStateProps.clone()

            clone.translationX = clone.translationX * 2
            clone.translationY = clone.translationY * 2

            it.startStateProps = containerChildStateIn.endStateProps
            it.endStateProps = clone
        }

        placeholderChildStateIn.let {
            val propertyChange = it.endStateProps

            propertyChange.translationX = startView.morphWidth * it.defaultTranslateMultiplierX
            propertyChange.translationY = startView.morphHeight * it.defaultTranslateMultiplierY

            it.startStateProps.reset()
            it.endStateProps.computeTranslation(this, propertyChange, startView, endView)
        }

        placeholderChildStateOut.let {
            it.startStateProps.translationX = startView.morphWidth * it.defaultTranslateMultiplierX
            it.startStateProps.translationY = startView.morphHeight * it.defaultTranslateMultiplierY
            it.endStateProps.reset()
        }
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

    /*
    fun createBinding(from: View, to: MorphView) {

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
    }*/

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

       /* if (children.isEmpty()) {
            children = getAllChildren(endingView, false) { it.tag == null}.map { if (it is MorphLayout) it else MorphView.makeMorphable(it)}
        }

        for (child in children) {
            child.animator()
                .alpha(MIN_OFFSET + (MAX_OFFSET - MIN_OFFSET) * fraction )
                .setDuration(0)
                .start()
        }*/
        lastOffset = fraction
    }

    fun cancelMorph() {
        //TODO( Implement logic that could cancel a currently going morph animation)
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

            isMorphing = false
            isMorphed = true
        }

        siblingInteraction?.buildInteraction(startView, endView)

        performSetup()

        endingState.alpha = 1f
        endView.morphAlpha = 1f
       /* endingView.show(overlayCrossfadeDurationIn) {
            startingView.hide(overlayCrossfadeDurationOut)
        }*/

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
            AnimationType.REVEAL
        )
    }

    fun morphFrom(
        duration: Long = morphFromDuration,
        onStart: Action = null,
        onEnd: Action = null,
        offsetTrigger: OffsetTrigger? = null
    ) {

        if (isMorphing || !isMorphed)
            return

        isMorphed = false
        isMorphing = true

        val doOnEnd = {
            onEnd?.invoke()

            startingView.show(overlayCrossfadeDurationIn)
            endingView.hide(overlayCrossfadeDurationOut)

            applyProps(endView, endingState)

            isMorphing = false
            isMorphed = true
        }

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
            AnimationType.CONCEAL
        )
    }

    private fun morph(
        endView: MorphLayout,
        startingProps: AnimatedProperties,
        endingProps: AnimatedProperties,
        mappings: List<MorphMap>,
        interpolator: TimeInterpolator?,
        curveTranslationHelper: ArcTranslationHelper,
        duration: Long,
        onStart: Action,
        onEnd: Action,
        trigger: OffsetTrigger?,
        animationType: AnimationType
    ) {

        var remainingDuration: Long

        val animateMappings = mappings.isNotEmpty() && morphChildren

        val updater: (Float) -> Unit = {

            val fraction = it.clamp(MIN_OFFSET, MAX_OFFSET)

            val interpolatedFraction = interpolator?.getInterpolation(fraction) ?: fraction

            remainingDuration = duration - (duration * interpolatedFraction).toLong()

            animateProperties(endView, startingProps, endingProps, interpolatedFraction)

            moveWithOffset(endView, startingProps, endingProps, interpolatedFraction, curveTranslationHelper, useArcTranslator)

            if (animateMappings) {
                for (mapping in mappings) {
                    when (animationType) {
                        AnimationType.REVEAL -> {
                            animateProperties(mapping.endView, mapping.startProps, mapping.endProps, interpolatedFraction)

                            mapping.endView.morphX = mapping.startProps.x + (mapping.endProps.x - mapping.startProps.x) * interpolatedFraction
                            mapping.endView.morphY = mapping.startProps.y + (mapping.endProps.y - mapping.startProps.y) * interpolatedFraction
                        }
                        AnimationType.CONCEAL -> {
                            animateProperties(mapping.endView, mapping.endProps, mapping.startProps, interpolatedFraction)

                            mapping.endView.morphX = mapping.endProps.x + (mapping.startProps.x - mapping.endProps.x) * interpolatedFraction
                            mapping.endView.morphY = mapping.endProps.y + (mapping.startProps.y - mapping.endProps.y) * interpolatedFraction
                        }
                    }
                }
            }

            when (animationType) {
                AnimationType.REVEAL -> {
                    if (containerStateIn.morphStates.isNotEmpty())
                        animateContainers(containerStateIn, containerChildStateIn, MIN_OFFSET, MAX_OFFSET, fraction, interpolatedFraction, remainingDuration)

                    if (placeholderStateIn.morphStates.isNotEmpty())
                        animateContainers(placeholderStateIn, placeholderChildStateIn, MIN_OFFSET, MAX_OFFSET, fraction, interpolatedFraction, remainingDuration)

                    if (otherStateIn.morphStates.isNotEmpty())
                        animateContainers(otherStateIn, null, MIN_OFFSET, MAX_OFFSET, fraction, interpolatedFraction, remainingDuration)
                }
                AnimationType.CONCEAL -> {
                    if (containerStateOut.morphStates.isNotEmpty())
                        animateContainers(containerStateOut, containerChildStateOut, MIN_OFFSET, MAX_OFFSET, fraction, interpolatedFraction, remainingDuration)

                    if (placeholderStateOut.morphStates.isNotEmpty())
                        animateContainers(placeholderStateOut, placeholderChildStateOut, MIN_OFFSET, MAX_OFFSET, fraction, interpolatedFraction, remainingDuration)

                    if (otherStateOut.morphStates.isNotEmpty())
                        animateContainers(otherStateOut, null, MIN_OFFSET, MAX_OFFSET, fraction, interpolatedFraction, remainingDuration)
                }
            }

            if (trigger != null && !trigger.hasTriggered && interpolatedFraction >= trigger.percentage) {
                trigger.triggerAction.invoke()
                trigger.hasTriggered = true
            }

            transitionProgressListener?.invoke(interpolatedFraction)
        }

        if (siblingInteraction != null) {
            siblingInteraction?.playWith(animationType, duration, dimPropertyInto, dimPropertyFrom, backgroundDimListener, updater, onStart, onEnd)
        } else {
            val animator: ValueAnimator = ValueAnimator.ofFloat(MIN_OFFSET, MAX_OFFSET)
            animator.interpolator = null
            animator.duration = duration
            animator.addUpdateListener {
                val fraction = it.animatedValue as Float

                updater(fraction)

                when (animationType) {
                    AnimationType.REVEAL -> {
                        val dimFraction = mapRange(fraction, dimPropertyInto.interpolateOffsetStart, dimPropertyInto.interpolateOffsetEnd, MIN_OFFSET, MAX_OFFSET)

                        backgroundDimListener?.invoke(dimPropertyInto.fromValue + (dimPropertyInto.toValue - dimPropertyInto.fromValue) * dimFraction)
                    }
                    AnimationType.CONCEAL -> {
                        val dimFraction = mapRange(fraction, dimPropertyFrom.interpolateOffsetStart, dimPropertyFrom.interpolateOffsetEnd, MIN_OFFSET, MAX_OFFSET)

                        backgroundDimListener?.invoke(dimPropertyFrom.fromValue + (dimPropertyFrom.toValue - dimPropertyFrom.fromValue) * dimFraction)
                    }
                }
            }
            animator.addListener(MorphAnimationListener(onStart, onEnd))
            animator.start()
        }
    }

    private fun moveWithOffset(
        endView: MorphLayout,
        startingProps: AnimatedProperties,
        endingProps: AnimatedProperties,
        fraction: Float,
        curveTranslationHelper: ArcTranslationHelper,
        useArcTranslator: Boolean
    ) {
        if (useArcTranslator) {
            val arcTranslationX = curveTranslationHelper.getCurvedTranslationX(fraction, startingProps.translationX, endingProps.translationX, endingProps.translationX)
            val arcTranslationY = curveTranslationHelper.getCurvedTranslationY(fraction, startingProps.translationY, endingProps.translationY, startingProps.translationY)

            endView.morphTranslationX = arcTranslationX.toFloat()
            endView.morphTranslationY = arcTranslationY.toFloat()
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

        val mappings: LinkedList<MorphMap> = LinkedList()

        startChildren.forEach { startChild ->
            endChildren.forEach { endChild ->
                if (startChild.tag == endChild.tag) {

                    val start: MorphLayout = if (startChild is MorphLayout) {
                        startChild
                    } else {
                        MorphView.makeMorphable(startChild)
                    }

                    val end: MorphLayout = if (endChild is MorphLayout) {
                        endChild
                    } else {
                        MorphView.makeMorphable(endChild)
                    }

                    if (!end.placeholder) {

                        val startProps = start.getProperties()
                        val endProps = end.getProperties()

                        mappings.add(MorphMap(start, end, startProps, endProps))
                    }
                }
            }
        }
        return mappings
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun applyProps(view: MorphLayout, props: AnimatedProperties) {
        view.morphX = props.x
        view.morphY = props.y
        view.morphAlpha = props.alpha
        view.morphElevation = props.elevation
        view.morphTranslationX = props.translationX
        view.morphTranslationY = props.translationY
        view.morphTranslationZ = props.translationZ
        view.morphPivotX = props.pivotX
        view.morphPivotY = props.pivotY
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
        startingProps: AnimatedProperties,
        endingProps: AnimatedProperties,
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

    private fun animateContainers(
        descriptor: AnimationDescriptor,
        childDescriptor: ChildAnimationDescriptor?,
        startValue: Float,
        endValue: Float,
        fraction: Float,
        interpolatedFraction: Float,
        remainingDuration: Long
    ){
        for(state in descriptor.morphStates){

            val layout = state.morphView

            val scaleX = descriptor.propertyScaleX
            val scaleY = descriptor.propertyScaleY
            val alpha = descriptor.propertyAlpha

            val scaleXFraction = mapRange(fraction, scaleX.interpolateOffsetStart, scaleX.interpolateOffsetEnd, startValue, endValue, startValue, endValue)
            val scaleYFraction = mapRange(fraction, scaleY.interpolateOffsetStart, scaleY.interpolateOffsetEnd, startValue, endValue, startValue, endValue)
            val alphaFraction = mapRange(fraction, alpha.interpolateOffsetStart, alpha.interpolateOffsetEnd, startValue, endValue, startValue, endValue)

            val scaleXInterpolation = descriptor.propertyScaleX.interpolator?.getInterpolation(scaleXFraction) ?: scaleXFraction
            val scaleYInterpolation = descriptor.propertyScaleY.interpolator?.getInterpolation(scaleYFraction) ?: scaleYFraction
            val alphaInterpolation = descriptor.propertyAlpha.interpolator?.getInterpolation(alphaFraction) ?: alphaFraction

            val scaleXDelta = scaleX.fromValue + (scaleX.toValue - scaleX.fromValue) * scaleXInterpolation
            val scaleYDelta = scaleY.fromValue + (scaleY.toValue - scaleY.fromValue) * scaleYInterpolation
            val alphaDelta =  alpha.fromValue  + (alpha.toValue  - alpha.fromValue)  * alphaInterpolation

            layout.morphScaleX = scaleXDelta
            layout.morphScaleY = scaleYDelta
            layout.morphAlpha = alphaDelta

            if (childDescriptor == null)
                return

            if (animateChildren && !descriptor.childrenRevealed  && interpolatedFraction >= childDescriptor.animateOnOffset) {
                descriptor.childrenRevealed = true
                animateChildren(state.children,  childDescriptor, remainingDuration)
            }
       }
    }

    private fun getAllChildren(
        view: MorphLayout,
        deepSearch: Boolean,
        predicate: ((View) -> Boolean)? = null
    ): List<View> {

        if (!view.hasChildren())
            return emptyList()

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

    private fun <T: View> animateChildren(
        inChildren: List<T>,
        descriptor: ChildAnimationDescriptor,
        inDuration: Long
    ) {
        if (inChildren.isEmpty())
            return

        val totalDuration = descriptor.duration ?: inDuration

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

        val delayAdd = (durationDelta * (descriptor.stagger?.staggerOffset ?: MIN_OFFSET)).toLong()
        val duration = durationDelta - (delayAdd / children.count())
        var delay = 0L

        for (child in children) {
            if (child is MorphLayout && !child.animate) {
                continue
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
                .setDuration(if (delayAdd == 0L) durationDelta else duration)
                .setStartDelay(if (delayAdd == 0L) startDelay else delay)
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

    private fun calculatePivot(
        forView: MorphLayout,
        inRelationToView: MorphLayout
    ): Point<Float> {
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

    private fun computeAnimationDirection(
        startView: MorphLayout,
        endView: MorphLayout
    ): TranslationPositions {
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

    class MorphState(
        val morphView: MorphLayout,
        val stateProps: AnimatedProperties
    ){
        var children: List<View> = emptyList()
        constructor(morphView: MorphLayout):this(morphView, morphView.getProperties())
    }

    data class MorphMap(
        val startView: MorphLayout,
        val endView: MorphLayout,

        val startProps: AnimatedProperties,
        val endProps: AnimatedProperties
    )

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
    ): Cloneable<AnimationProperties> {

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

        fun reset() {
            alpha = MAX_OFFSET
            elevation = MIN_OFFSET
            translationX = MIN_OFFSET
            translationY = MIN_OFFSET
            translationZ = MIN_OFFSET
            rotation = MIN_OFFSET
            rotationX = MIN_OFFSET
            rotationY = MIN_OFFSET
            scaleX = MAX_OFFSET
            scaleY = MAX_OFFSET
            visibility = VISIBLE
        }

        override fun clone(): AnimationProperties {
            return AnimationProperties(
                alpha = this.alpha,
                elevation = this.elevation,
                translationX = this.translationX,
                translationY = this.translationY,
                translationZ = this.translationZ,
                rotation = this.rotation,
                rotationX = this.rotationX,
                rotationY = this.rotationY,
                scaleX = this.scaleX,
                scaleY = this.scaleY,
                visibility = this.visibility
            )
        }

        fun computeTranslation(morpher: Morpher, propertyChange: AnimationProperties, endingViewStart: MorphLayout, endingViewEnd: MorphLayout) {

            val direction = morpher.computeAnimationDirection(endingViewStart, endingViewEnd)

            when  {
                direction has TranslationPosition.TOP.and(TranslationPosition.LEFT)-> {
                    val xAmountQualified = direction.get(TranslationPosition.LEFT).amount > DISTANCE_THRESHOLD
                    val yAmountQualified = direction.get(TranslationPosition.TOP).amount > DISTANCE_THRESHOLD

                    translationX = if (xAmountQualified) -propertyChange.translationX else translationX
                    translationY = if (yAmountQualified) -propertyChange.translationY else translationY
                }
                direction has TranslationPosition.TOP.and(TranslationPosition.RIGHT)-> {
                    val xAmountQualified = direction.get(TranslationPosition.RIGHT).amount > DISTANCE_THRESHOLD
                    val yAmountQualified = direction.get(TranslationPosition.TOP).amount > DISTANCE_THRESHOLD

                    translationX = if (xAmountQualified) propertyChange.translationX else translationX
                    translationY = if (yAmountQualified) -propertyChange.translationY else translationY
                }
                direction has TranslationPosition.BOTTOM.and(TranslationPosition.LEFT)-> {
                    val xAmountQualified = direction.get(TranslationPosition.LEFT).amount > DISTANCE_THRESHOLD
                    val yAmountQualified = direction.get(TranslationPosition.BOTTOM).amount > DISTANCE_THRESHOLD

                    translationX = if (xAmountQualified) -propertyChange.translationX else translationX
                    translationY = if (yAmountQualified) propertyChange.translationY else translationY
                }
                direction has TranslationPosition.BOTTOM.and(TranslationPosition.RIGHT)-> {
                    val xAmountQualified = direction.get(TranslationPosition.RIGHT).amount > DISTANCE_THRESHOLD
                    val yAmountQualified = direction.get(TranslationPosition.BOTTOM).amount > DISTANCE_THRESHOLD

                    translationX = if (xAmountQualified) propertyChange.translationX else translationX
                    translationY = if (yAmountQualified) propertyChange.translationY else translationY
                }
                direction has TranslationPosition.TOP -> {
                    val amountQualified = direction.get(TranslationPosition.TOP).amount > DISTANCE_THRESHOLD

                    translationY = if (amountQualified) -propertyChange.translationY else translationY
                }
                direction has TranslationPosition.BOTTOM -> {
                    val amountQualified = direction.get(TranslationPosition.BOTTOM).amount > DISTANCE_THRESHOLD

                    translationY = if (amountQualified) propertyChange.translationY else translationY
                }
                direction has TranslationPosition.LEFT -> {
                    val amountQualified = direction.get(TranslationPosition.LEFT).amount > DISTANCE_THRESHOLD

                    translationX = if (amountQualified) -propertyChange.translationX else translationX
                }
                direction has TranslationPosition.RIGHT -> {
                    val amountQualified = direction.get(TranslationPosition.RIGHT).amount > DISTANCE_THRESHOLD

                    translationX = if (amountQualified) propertyChange.translationX else translationX
                }
                else -> { }
            }
        }

        companion object {
            fun from(morpher: Morpher, propertyChange: AnimationProperties, endingViewStart: MorphLayout, endingViewEnd: MorphLayout): AnimationProperties {

                val direction = morpher.computeAnimationDirection(endingViewStart, endingViewEnd)

                return when  {
                    direction has TranslationPosition.TOP.and(TranslationPosition.LEFT)-> {
                        val xAmountQualified = direction.get(TranslationPosition.LEFT).amount > DISTANCE_THRESHOLD
                        val yAmountQualified = direction.get(TranslationPosition.TOP).amount > DISTANCE_THRESHOLD

                        AnimationProperties(
                            alpha = propertyChange.alpha,
                            scaleX = MAX_OFFSET,
                            scaleY = MAX_OFFSET,
                            translationX = if (xAmountQualified) -propertyChange.translationX else MIN_OFFSET,
                            translationY = if (yAmountQualified) -propertyChange.translationY else MIN_OFFSET
                        )
                    }
                    direction has TranslationPosition.TOP.and(TranslationPosition.RIGHT)-> {
                        val xAmountQualified = direction.get(TranslationPosition.RIGHT).amount > DISTANCE_THRESHOLD
                        val yAmountQualified = direction.get(TranslationPosition.TOP).amount > DISTANCE_THRESHOLD

                        AnimationProperties(
                            alpha = propertyChange.alpha,
                            scaleX = MAX_OFFSET,
                            scaleY = MAX_OFFSET,
                            translationX = if (xAmountQualified) propertyChange.translationX else MIN_OFFSET,
                            translationY = if (yAmountQualified) -propertyChange.translationY else MIN_OFFSET
                        )
                    }
                    direction has TranslationPosition.BOTTOM.and(TranslationPosition.LEFT)-> {
                        val xAmountQualified = direction.get(TranslationPosition.LEFT).amount > DISTANCE_THRESHOLD
                        val yAmountQualified = direction.get(TranslationPosition.BOTTOM).amount > DISTANCE_THRESHOLD

                        AnimationProperties(
                            alpha = propertyChange.alpha,
                            scaleX = MAX_OFFSET,
                            scaleY = MAX_OFFSET,
                            translationX = if (xAmountQualified) -propertyChange.translationX else MIN_OFFSET,
                            translationY = if (yAmountQualified) propertyChange.translationY else MIN_OFFSET
                        )
                    }
                    direction has TranslationPosition.BOTTOM.and(TranslationPosition.RIGHT)-> {
                        val xAmountQualified = direction.get(TranslationPosition.RIGHT).amount > DISTANCE_THRESHOLD
                        val yAmountQualified = direction.get(TranslationPosition.BOTTOM).amount > DISTANCE_THRESHOLD

                        AnimationProperties(
                            alpha = propertyChange.alpha,
                            scaleX = MAX_OFFSET,
                            scaleY = MAX_OFFSET,
                            translationX = if (xAmountQualified) propertyChange.translationX else MIN_OFFSET,
                            translationY = if (yAmountQualified) propertyChange.translationY else MIN_OFFSET
                        )
                    }
                    direction has TranslationPosition.TOP -> {
                        val amountQualified = direction.get(TranslationPosition.TOP).amount > DISTANCE_THRESHOLD

                        AnimationProperties(
                            alpha = propertyChange.alpha,
                            scaleX = MAX_OFFSET,
                            scaleY = MAX_OFFSET,
                            translationY = if (amountQualified) -propertyChange.translationY else MIN_OFFSET
                        )
                    }
                    direction has TranslationPosition.BOTTOM -> {
                        val amountQualified = direction.get(TranslationPosition.BOTTOM).amount > DISTANCE_THRESHOLD

                        AnimationProperties(
                            alpha = propertyChange.alpha,
                            scaleX = MAX_OFFSET,
                            scaleY = MAX_OFFSET,
                            translationY = if (amountQualified) propertyChange.translationY else MIN_OFFSET
                        )
                    }
                    direction has TranslationPosition.LEFT -> {
                        val amountQualified = direction.get(TranslationPosition.LEFT).amount > DISTANCE_THRESHOLD

                        AnimationProperties(
                            alpha = propertyChange.alpha,
                            scaleX = MAX_OFFSET,
                            scaleY = MAX_OFFSET,
                            translationX = if (amountQualified) -propertyChange.translationX else MIN_OFFSET
                        )
                    }
                    direction has TranslationPosition.RIGHT -> {
                        val amountQualified = direction.get(TranslationPosition.RIGHT).amount > DISTANCE_THRESHOLD

                        AnimationProperties(
                            alpha = propertyChange.alpha,
                            scaleX = MAX_OFFSET,
                            scaleY = MAX_OFFSET,
                            translationX = if (amountQualified) propertyChange.translationX else MIN_OFFSET
                        )
                    }
                    else -> {
                        AnimationProperties(
                            alpha = propertyChange.alpha,
                            scaleX = MAX_OFFSET,
                            scaleY = MAX_OFFSET,
                            translationY = MIN_OFFSET
                        )
                    }
                }
            }
        }
    }

    class ChildAnimationDescriptor (
        val type: AnimationType,
        var animateOnOffset: Float = DEFAULT_CHILDREN_REVEAL_OFFSET,
        var durationMultiplier: Float = DEFAULT_REVEAL_DURATION_MULTIPLIER,
        var defaultTranslateMultiplierX: Float = DEFAULT_TRANSLATION_MULTIPLIER,
        var defaultTranslateMultiplierY: Float = DEFAULT_TRANSLATION_MULTIPLIER,
        var interpolator: TimeInterpolator? = null,
        var stagger: AnimationStagger? = null,
        var reversed: Boolean = false,
        var duration: Long? = null,
        var delay: Long? = null,
        var startStateProps: AnimationProperties = AnimationProperties(),
        var endStateProps: AnimationProperties = AnimationProperties()
    ) {
        companion object {
            fun getDefault(type: AnimationType): ChildAnimationDescriptor {
                return when(type){
                    AnimationType.REVEAL -> {
                         ChildAnimationDescriptor(
                             type = type,
                             animateOnOffset = MIN_OFFSET,
                             durationMultiplier = -0.2f,
                             defaultTranslateMultiplierX = 0.02f,
                             defaultTranslateMultiplierY = 0.02f,
                             interpolator = Easing.INCOMING,
                             stagger = AnimationStagger(0.12f),
                             startStateProps = AnimationProperties(alpha = MIN_OFFSET)
                        )
                    }
                    AnimationType.CONCEAL -> {
                         ChildAnimationDescriptor(
                             type = type,
                             animateOnOffset = MIN_OFFSET,
                             durationMultiplier = -0.8f,
                             defaultTranslateMultiplierX = 0.1f,
                             defaultTranslateMultiplierY = 0.1f,
                             interpolator = Easing.OUTGOING,
                             stagger = AnimationStagger(0.11f),
                             reversed = true
                        )
                    }
                }
            }
        }
    }

    data class AnimationDescriptor(
        var type: AnimationType,
        var propertyTranslateX: AnimatedFloatValue = AnimatedFloatValue(
            AnimatedValue.TRANSLATION_X,
            MIN_OFFSET,
            MIN_OFFSET
        ),
        var propertyTranslateY: AnimatedFloatValue = AnimatedFloatValue(
            AnimatedValue.TRANSLATION_Y,
            MIN_OFFSET,
            MIN_OFFSET
        ),
        var propertyRotationX: AnimatedFloatValue = AnimatedFloatValue(
            AnimatedValue.ROTATION_X,
            MIN_OFFSET,
            MIN_OFFSET
        ),
        var propertyRotationY: AnimatedFloatValue = AnimatedFloatValue(
            AnimatedValue.ROTATION_Y,
            MIN_OFFSET,
            MIN_OFFSET
        ),
        var propertyRotation: AnimatedFloatValue = AnimatedFloatValue(
            AnimatedValue.ROTATION,
            MIN_OFFSET,
            MIN_OFFSET
        ),
        var propertyScaleX: AnimatedFloatValue = AnimatedFloatValue(
            AnimatedValue.SCALE_X,
            MIN_OFFSET,
            MAX_OFFSET
        ),
        var propertyScaleY: AnimatedFloatValue = AnimatedFloatValue(
            AnimatedValue.SCALE_Y,
            MIN_OFFSET,
            MAX_OFFSET
        ),
        var propertyAlpha: AnimatedFloatValue = AnimatedFloatValue(
            AnimatedValue.ALPHA,
            MIN_OFFSET,
            MAX_OFFSET
        ),
        var propertyColor: AnimatedIntValue = AnimatedIntValue(
            AnimatedValue.COLOR,
            0,
            255
        ),
        var propertyX: AnimatedFloatValue = AnimatedFloatValue(
            AnimatedValue.X,
            MIN_OFFSET,
            MIN_OFFSET
        ),
        var propertyY: AnimatedFloatValue = AnimatedFloatValue(
            AnimatedValue.Y,
            MIN_OFFSET,
            MIN_OFFSET
        )
    ) {
        var animationContainer: Boolean = false
        var childrenRevealed: Boolean = false
        var morphStates: List<MorphState> = emptyList()
    }

    class MorphValues

    enum class MorphMethod { AS_DIALOG, AS_SOURCE }

    enum class MorphFlag { DISSOLVE, CROSS_DISSOLVE, FADE_THROUGH, TRANSFORM }

    enum class TranslationPosition(var amount: Float) {
        TOP(MIN_OFFSET),
        LEFT(MIN_OFFSET),
        RIGHT(MIN_OFFSET),
        BOTTOM(MIN_OFFSET),
        CENTER(MIN_OFFSET);

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

    companion object {
        const val DEFAULT_DURATION: Long = 350L

        const val DEFAULT_CHILDREN_REVEAL_OFFSET: Float = 0.0f
        const val DEFAULT_CHILDREN_CONCEAL_OFFSET: Float = 0.0f

        const val DEFAULT_REVEAL_DURATION_MULTIPLIER: Float = 0.2f
        const val DEFAULT_CONCEAL_DURATION_MULTIPLIER: Float = -0.2f

        const val DEFAULT_CHILDREN_STAGGER_MULTIPLIER: Float = 0.15f

        const val DEFAULT_TRANSLATION_MULTIPLIER: Float = 0.20f

        val DISTANCE_THRESHOLD = 20.dp
    }
}

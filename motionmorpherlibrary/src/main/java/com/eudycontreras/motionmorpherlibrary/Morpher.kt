package com.eudycontreras.motionmorpherlibrary

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.eudycontreras.motionmorpherlibrary.drawables.MorphTransitionDrawable
import com.eudycontreras.motionmorpherlibrary.extensions.*
import com.eudycontreras.motionmorpherlibrary.helpers.CurvedTranslationHelper
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.layouts.MorphWrapper
import com.eudycontreras.motionmorpherlibrary.listeners.MorphAnimationListener
import com.eudycontreras.motionmorpherlibrary.properties.Coordintates
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii
import com.eudycontreras.motionmorpherlibrary.utilities.ColorUtility
import kotlin.math.abs
import kotlin.math.roundToLong


class Morpher(private val context: Context) {

    private var remainingDuration: Long = 0L

    private var mappingsCreated: Boolean = false
    private var initialValuesApplied: Boolean = false

    private val curveTranslator = CurvedTranslationHelper()

    private lateinit var startingView: MorphLayout
    private lateinit var endingView: MorphLayout

    private lateinit var startingState: Properties
    private lateinit var endingState: Properties

    private lateinit var endViewEndState: MorphState
    private lateinit var endViewStartState: MorphState

    private lateinit var mappings: List<MorphMap>

    private var children: List<View>? = null

    var backgroundDimListener: BackgroundDimListener = null
    var transitionOffsetListener: TransitionOffsetListener = null

    var morphIntoInterpolator: Interpolator? = null
    var morphFromInterpolator: Interpolator? = null

    var useDeepChildSearch: Boolean = true
    var useArcTranslator: Boolean = true
    var animateChildren: Boolean = true
    var morphChildren: Boolean = true

    var morphIntoDuration: Long = DEFAULT_DURATION
    var morphFromDuration: Long = DEFAULT_DURATION

    var childrenRevealed: Boolean = false
        private set
    var isMorphing: Boolean = false
        private set
    var isMorphed: Boolean = false
        private set

    var startView: MorphWrapper
        get() = startingView as MorphWrapper
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


    var endStateMorphIntoDescriptor: AnimationDescriptor = AnimationDescriptor(
        type =  AnimationType.REVEAL,
        animateOnOffset = DEFAULT_CHILDREN_REVEAL_OFFSET,
        durationMultiplier = DEFAULT_REVEAL_DURATION_MULTIPLIER
       // stagger = AnimationStagger()
    )

    var endStateMorphFromDescriptor: AnimationDescriptor = AnimationDescriptor(
        type =  AnimationType.CONCEAL,
        animateOnOffset = DEFAULT_CHILDREN_CONCEAL_OFFSET,
        durationMultiplier = DEFAULT_CONCEAL_DURATION_MULTIPLIER
    )

    var startStateMorphIntoDescriptor: AnimationDescriptor = AnimationDescriptor(
        type =  AnimationType.REVEAL,
        animateOnOffset = DEFAULT_CHILDREN_REVEAL_OFFSET,
        durationMultiplier = DEFAULT_REVEAL_DURATION_MULTIPLIER
    )

    var startStateMorphFromDescriptor: AnimationDescriptor = AnimationDescriptor(
        type =  AnimationType.CONCEAL,
        animateOnOffset = DEFAULT_CHILDREN_CONCEAL_OFFSET,
        durationMultiplier = DEFAULT_CONCEAL_DURATION_MULTIPLIER
    )

    var dimPropertyInto: PropertyDescriptor<Float> = PropertyDescriptor(
        propertyType = PropertyDescriptor.COLOR,
        fromValue = 0f,
        toValue = 1f,
        startOffset = 0f,
        endOffset = 0.6f
    )

    var dimPropertyFrom: PropertyDescriptor<Float> = PropertyDescriptor(
        propertyType = PropertyDescriptor.COLOR,
        fromValue = 1f,
        toValue = 0f,
        startOffset = 0.4f,
        endOffset = 1f
    )

    private fun createMappings() {
        endingState = endingView.getProperties()
        startingState = startingView.getProperties()

        mappings = if (morphChildren) {
            getChildMappings(startingView, endingView)
        } else emptyList()
    }

    fun cancelMorph() {

    }

    private fun performSetup() {
        if (!mappingsCreated) {
            createMappings()
            mappingsCreated = true
        }

        val endingViewStart = endView.getStartState()
        val endingViewEnd = endView.getEndState()

        endViewEndState = MorphState(endingViewEnd, endingViewEnd.getProperties())
        endViewStartState = MorphState(endingViewStart, endingViewStart.getProperties())

        endingViewStart.morphPivotX = endingViewStart.morphWidth
        endingViewStart.morphPivotY = endingViewStart.morphHeight
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


        for (child in endingViewEnd.getChildren()) {
            if (child.visibility == View.GONE)
                continue

            if (morphChildren && child.tag != null)
                continue

            child.pivotX = 0f
            child.pivotY = 0f
            child.layoutParams.width = child.width
            child.layoutParams.height = child.height
        }

        applyProps(endingView, startingState)
        //applyProps(endingViewStart, startingState)

        endingView.show(0) { View.VISIBLE }
        startingView.hide(250)

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

        initialValuesApplied = true
    }

    fun transitionInto(
        duration: Long = DEFAULT_DURATION,
        onStart: Action = null,
        onEnd: Action = null,
        offsetTrigger: OffsetTrigger? = null
    ) {

    }

    fun transitionFrom(
        duration: Long = DEFAULT_DURATION,
        onStart: Action = null,
        onEnd: Action = null,
        offsetTrigger: OffsetTrigger? = null
    ) {

    }

    fun morphInto(
        duration: Long = morphIntoDuration,
        onStart: Action = null,
        onEnd: Action = null,
        offsetTrigger: OffsetTrigger? = null
    ) {

        if (isMorphing)
            return

        endStateMorphIntoDescriptor.duration = duration
        endStateMorphIntoDescriptor.delay = 0L
        endStateMorphIntoDescriptor.durationMultiplier = 0f
        endStateMorphFromDescriptor.animateOnOffset = 0f

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

        endStateMorphIntoDescriptor.propertyScaleX.fromValue = (startingState.width) / (endingState.width)
        endStateMorphIntoDescriptor.propertyScaleY.fromValue = (startingState.width) / (endingState.width)

        endStateMorphIntoDescriptor.propertyScaleX.toValue = 1f
        endStateMorphIntoDescriptor.propertyScaleY.toValue = 1f

        endStateMorphIntoDescriptor.propertyAlpha.startOffset = 0.33f
        endStateMorphIntoDescriptor.propertyAlpha.endOffset = 1f

        endStateMorphIntoDescriptor.propertyScaleX.interpolator = FastOutSlowInInterpolator()
        endStateMorphIntoDescriptor.propertyScaleY.interpolator = FastOutSlowInInterpolator()
        endStateMorphIntoDescriptor.propertyAlpha.interpolator = DecelerateInterpolator()

        endStateMorphIntoDescriptor.morphState = endViewEndState


        startStateMorphIntoDescriptor.propertyScaleX.toValue = (endingState.width) / (startingState.width)
        startStateMorphIntoDescriptor.propertyScaleY.toValue = (endingState.width) / (startingState.width)

        startStateMorphIntoDescriptor.propertyScaleX.fromValue = 1f
        startStateMorphIntoDescriptor.propertyScaleY.fromValue = 1f

        startStateMorphIntoDescriptor.propertyAlpha.fromValue = 1f
        startStateMorphIntoDescriptor.propertyAlpha.toValue = 0f

        startStateMorphIntoDescriptor.propertyAlpha.startOffset = 0f
        startStateMorphIntoDescriptor.propertyAlpha.endOffset = 0.33f

        startStateMorphIntoDescriptor.propertyScaleX.interpolator = FastOutSlowInInterpolator()
        startStateMorphIntoDescriptor.propertyScaleY.interpolator = FastOutSlowInInterpolator()
        startStateMorphIntoDescriptor.propertyAlpha.interpolator = AccelerateInterpolator()

        startStateMorphIntoDescriptor.morphState = endViewStartState

        curveTranslator.setStartPoint(startingState.getDeltaCoordinates())
        curveTranslator.setEndPoint(endingState.getDeltaCoordinates())

        curveTranslator.setControlPoint(Coordintates(endingState.translationX, startingState.translationY))

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

    fun morphFrom(
        duration: Long = morphFromDuration,
        onStart: Action = null,
        onEnd: Action = null,
        offsetTrigger: OffsetTrigger? = null
    ) {

        if (isMorphing)
            return

        endStateMorphFromDescriptor.duration = duration
        endStateMorphFromDescriptor.delay = 0L
        endStateMorphFromDescriptor.durationMultiplier = 0f
        endStateMorphFromDescriptor.animateOnOffset = 0f

        if (!mappingsCreated) {
            createMappings()
            mappingsCreated = true
        }

        isMorphed = false
        isMorphing = true

        val doOnEnd = {
            onEnd?.invoke()

            startingView.show(0) { View.VISIBLE }
            endingView.hide(100) { View.INVISIBLE }

            applyProps(endingView, endingState)

            mappings.forEach {
                applyProps(it.endView, it.endProps)
            }

            isMorphing = false
            isMorphed = true
        }

        endStateMorphFromDescriptor.propertyScaleX.fromValue = 1f
        endStateMorphFromDescriptor.propertyScaleY.fromValue = 1f

        endStateMorphFromDescriptor.propertyScaleX.toValue = (startingState.width) / (endingState.width)
        endStateMorphFromDescriptor.propertyScaleY.toValue = (startingState.width) / (endingState.width)

        endStateMorphFromDescriptor.propertyAlpha.fromValue = 1f
        endStateMorphFromDescriptor.propertyAlpha.toValue = 0f

        endStateMorphFromDescriptor.propertyAlpha.startOffset = 0f
        endStateMorphFromDescriptor.propertyAlpha.endOffset = 0.3f

        endStateMorphFromDescriptor.propertyScaleX.interpolator = FastOutSlowInInterpolator()
        endStateMorphFromDescriptor.propertyScaleY.interpolator = FastOutSlowInInterpolator()
        endStateMorphFromDescriptor.propertyAlpha.interpolator = AccelerateInterpolator()

        endStateMorphFromDescriptor.morphState = endViewEndState



        startStateMorphFromDescriptor.propertyScaleX.fromValue = (endingState.width) / (startingState.width)
        startStateMorphFromDescriptor.propertyScaleY.fromValue = (endingState.width) / (startingState.width)

        startStateMorphFromDescriptor.propertyScaleX.toValue = 1f
        startStateMorphFromDescriptor.propertyScaleY.toValue = 1f

        startStateMorphFromDescriptor.propertyAlpha.fromValue = 0f
        startStateMorphFromDescriptor.propertyAlpha.toValue = 1f

        startStateMorphFromDescriptor.propertyAlpha.startOffset = 0.3f
        startStateMorphFromDescriptor.propertyAlpha.endOffset = 1f

        startStateMorphFromDescriptor.propertyScaleX.interpolator = FastOutSlowInInterpolator()
        startStateMorphFromDescriptor.propertyScaleY.interpolator = FastOutSlowInInterpolator()
        startStateMorphFromDescriptor.propertyAlpha.interpolator = DecelerateInterpolator()

        startStateMorphFromDescriptor.morphState = endViewStartState


        curveTranslator.setStartPoint(endingState.getDeltaCoordinates())
        curveTranslator.setEndPoint(startingState.getDeltaCoordinates())

        curveTranslator.setControlPoint(Coordintates(endingState.translationX, startingState.translationY))

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

    private var lastOffset = MIN_OFFSET

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

            remainingDuration = duration - (duration * fraction).toLong()

            animateProperties(endView, startingProps, endingProps, fraction)

            moveWithOffset(endView, startingProps, endingProps, fraction, curveTranslationHelper)

            if (morphChildren && mappings.isNotEmpty()) {
                for (mapping in mappings) {
                    when (morphType) {
                        MorphType.INTO -> {
                            animateProperties(mapping.endView, mapping.startProps, mapping.endProps, fraction)

                            mapping.endView.morphX = mapping.startProps.x + (mapping.endProps.x - mapping.startProps.x) * fraction
                            mapping.endView.morphY = mapping.startProps.y + (mapping.endProps.y - mapping.startProps.y) * fraction
                        }
                        MorphType.FROM -> {
                            animateProperties(mapping.endView, mapping.endProps, mapping.startProps, fraction)

                            mapping.endView.morphX = mapping.endProps.x + (mapping.startProps.x - mapping.endProps.x) * fraction
                            mapping.endView.morphY = mapping.endProps.y + (mapping.startProps.y - mapping.endProps.y) * fraction
                        }
                    }
                }
            }

            when (animationType) {
                AnimationType.REVEAL -> {
                    if (animateChildren && !childrenRevealed && fraction >= endStateMorphIntoDescriptor.animateOnOffset) {
                        childrenRevealed = true
                        animateChildren(endStateMorphIntoDescriptor, morphChildren, remainingDuration)
                        animateChildren(startStateMorphIntoDescriptor, morphChildren, remainingDuration)
                    }
                    val dimFraction = mapRange(fraction, dimPropertyInto.startOffset, dimPropertyInto.endOffset, 0f, 1f, 0f, 1f)

                    backgroundDimListener?.invoke(dimPropertyInto.fromValue + (dimPropertyInto.toValue - dimPropertyInto.fromValue) * dimFraction)
                }
                AnimationType.CONCEAL -> {
                    if (animateChildren && childrenRevealed && fraction >= endStateMorphFromDescriptor.animateOnOffset) {
                        childrenRevealed = false
                        animateChildren(endStateMorphFromDescriptor, morphChildren, remainingDuration)
                        animateChildren(startStateMorphFromDescriptor, morphChildren, remainingDuration)
                    }
                    val dimFraction = mapRange(fraction, dimPropertyFrom.startOffset, dimPropertyFrom.endOffset, 0f, 1f, 0f, 1f)

                    backgroundDimListener?.invoke(dimPropertyFrom.fromValue + (dimPropertyFrom.toValue - dimPropertyFrom.fromValue) * dimFraction)
                }
            }

            if (trigger != null && !trigger.hasTriggered) {
                if (fraction >= trigger.percentage) {
                    trigger.triggerAction?.invoke()
                    trigger.hasTriggered = true
                }
            }
            transitionOffsetListener?.invoke(fraction)
        }

        animator.interpolator = interpolator
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
        if (useArcTranslator && curveTranslationHelper != null) {
            endView.morphTranslationX = curveTranslationHelper.getCurvedTranslationX(fraction).toFloat()
            endView.morphTranslationY = curveTranslationHelper.getCurvedTranslationY(fraction).toFloat()
        } else {
            endView.morphTranslationX = startingProps.translationX + (endingProps.translationX - startingProps.translationX) * fraction
            endView.morphTranslationY = startingProps.translationY + (endingProps.translationY - startingProps.translationY) * fraction
        }
    }

    private fun getChildMappings(
        startView: MorphLayout,
        endView: MorphLayout
    ): List<MorphMap> {

        val startChildren = getAllChildren(startView, useDeepChildSearch) { it.tag != null }
        val endChildren = getAllChildren(endView, useDeepChildSearch) { it.tag != null }

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
        animDescriptor: AnimationDescriptor,
        skipTagged: Boolean,
        inDuration: Long
    ) {

        val duration = animDescriptor.duration ?: inDuration

        animateChildren(animDescriptor, duration)
    }

    private fun applyProps(view: MorphLayout, props: Properties) {
        view.morphX = props.x
        view.morphY = props.y
        view.morphAlpha = props.alpha
        view.morphElevation = props.elevation
        view.morphTranslationX = props.translationX
        view.morphTranslationY = props.translationY
        view.morphTranslationZ = props.translationZ
        view.morphPivotX = (view.morphWidth / 2)
        view.morphPivotY = (view.morphHeight / 2)
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
        morphView.morphAlpha = 1f

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

    private fun animateChildren(
        descriptor: AnimationDescriptor,
        totalDuration: Long
    ) {
        val offsetMultiplier = descriptor.durationMultiplier
        val startDelay = descriptor.delay ?: 0L

        val durationDelta = when (descriptor.type) {
            AnimationType.REVEAL -> (totalDuration + (totalDuration * offsetMultiplier)).roundToLong()
            AnimationType.CONCEAL -> (totalDuration + (totalDuration * offsetMultiplier)).roundToLong()
        }

        if (descriptor.stagger != null) {
            /*descriptor.stagger?.let { stagger ->
                val delayAdd = (durationDelta * stagger.multiplier).toLong()
                val duration = durationDelta - (delayAdd / children.count())
                var delay = startDelay
                children.forEach {
                    it.visibility = startStateProps.visibility
                    it.translationY = startStateProps.translationY
                    it.scaleX = startStateProps.scaleX
                    it.scaleY = startStateProps.scaleY
                    it.alpha = startStateProps.alpha
                    it.animate()
                        .setListener(null)
                        .setDuration((duration + (duration * offsetMultiplier)).roundToLong())
                        .setStartDelay(delay)
                        .alpha(endStateProps.alpha)
                        .scaleX(endStateProps.scaleX)
                        .scaleY(endStateProps.scaleY)
                        .translationY(endStateProps.translationY)
                        .setInterpolator(descriptor.interpolator)
                        .start()
                    delay += delayAdd
                }
            }*/
        } else {

            val scaleX = descriptor.propertyScaleX
            val scaleY = descriptor.propertyScaleY
            val alpha = descriptor.propertyAlpha

            val startValue = 0f
            val endValue = 1f

            val animator = ValueAnimator.ofFloat(startValue, endValue)

            animator.addUpdateListener {
                val fraction = it.animatedFraction

                val scaleXFraction = mapRange(fraction, scaleX.startOffset, scaleX.endOffset, startValue, endValue, startValue, endValue)
                val scaleYFraction = mapRange(fraction, scaleY.startOffset, scaleY.endOffset, startValue, endValue, startValue, endValue)
                val alphaFraction = mapRange(fraction, alpha.startOffset, alpha.endOffset, startValue, endValue, startValue, endValue)


                val scaleXInterpolation = descriptor.propertyScaleX.interpolator?.getInterpolation(scaleXFraction) ?: scaleXFraction
                val scaleYInterpolation = descriptor.propertyScaleY.interpolator?.getInterpolation(scaleYFraction) ?: scaleYFraction
                val alphaInterpolation = descriptor.propertyAlpha.interpolator?.getInterpolation(alphaFraction) ?: alphaFraction

                val scaleXDelta = scaleX.fromValue + (scaleX.toValue - scaleX.fromValue) * scaleXInterpolation
                val scaleYDelta = scaleY.fromValue + (scaleY.toValue - scaleY.fromValue) * scaleYInterpolation
                val alphaDelta =  alpha.fromValue  + (alpha.toValue  - alpha.fromValue)  * alphaInterpolation

                Log.d("ALPHA::", alphaDelta.toString())

                val stateLayout = descriptor.morphState?.morphView

                if (stateLayout != null) {
                    stateLayout.morphScaleX = scaleXDelta
                    stateLayout.morphScaleY = scaleYDelta
                    stateLayout.morphAlpha = alphaDelta
                }
            }

            animator.interpolator = null
            animator.duration = durationDelta
            animator.start()
           /* val scaleX = descriptor.propertyScaleX
            val scaleY = descriptor.propertyScaleY
            val alpha = descriptor.propertyAlpha
            children.forEachIndexed { index, it ->

                it.visibility = startStateProps.visibility
                //it.alpha = alpha.fromValue
                if(index == 0) {
                    it.translationY = startStateProps.translationY
                    it.scaleX = scaleX.fromValue
                    it.scaleY = scaleY.fromValue
                    it.animate()
                        .setListener(null)
                        .setDuration(durationDelta)
                        .setStartDelay(startDelay)
                        .alpha(alpha.toValue)
                        .scaleX(scaleX.toValue)
                        .scaleY(scaleY.toValue)
                        .translationY(endStateProps.translationY)
                        .setInterpolator(descriptor.interpolator)
                        .start()
                }
            }*/
        }
    }

    companion object {

        const val MAX_OFFSET: Float = 1f
        const val MIN_OFFSET: Float = 0f

        const val DEFAULT_DURATION: Long = 350L

        const val DEFAULT_CHILDREN_REVEAL_OFFSET: Float = 0.0f
        const val DEFAULT_CHILDREN_CONCEAL_OFFSET: Float = 0.0f

        const val DEFAULT_REVEAL_DURATION_MULTIPLIER: Float = 0f
        const val DEFAULT_CONCEAL_DURATION_MULTIPLIER: Float = 0f
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
        val multiplier: Float = 0.2f
    )

    data class PropertyDescriptor<T>(
        val propertyType: String,
        var fromValue: T,
        var toValue: T,
        var startOffset: Float = 0f,
        var endOffset: Float = 1f,
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

    data class AnimationDescriptor(
        var type: AnimationType,
        var animateOnOffset: Float,
        var durationMultiplier: Float,
        var propertyScaleX: PropertyDescriptor<Float> = PropertyDescriptor(PropertyDescriptor.SCALE_X, 0f, 1f),
        var propertyScaleY: PropertyDescriptor<Float> = PropertyDescriptor(PropertyDescriptor.SCALE_Y, 0f, 1f),
        var propertyAlpha: PropertyDescriptor<Float> = PropertyDescriptor(PropertyDescriptor.ALPHA, 0f, 1f),
        var propertyColor: PropertyDescriptor<Int> = PropertyDescriptor(PropertyDescriptor.COLOR, 0, 255),
        var morphState: MorphState? = null,
        var stagger: AnimationStagger? = null,
        var reversed: Boolean = false,
        var duration: Long? = null,
        var delay: Long? = null
    )

    enum class MorphType { INTO, FROM }

    enum class AnimationType { REVEAL, CONCEAL }

}

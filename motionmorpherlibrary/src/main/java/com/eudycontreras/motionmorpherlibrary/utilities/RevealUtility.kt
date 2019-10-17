package com.eudycontreras.motionmorpherlibrary.utilities

import android.animation.TimeInterpolator
import android.view.View
import android.view.ViewAnimationUtils
import com.eudycontreras.motionmorpherlibrary.Action
import com.eudycontreras.motionmorpherlibrary.MIN_DURATION
import com.eudycontreras.motionmorpherlibrary.listeners.MorphAnimationListener
import com.eudycontreras.motionmorpherlibrary.properties.Conceal
import com.eudycontreras.motionmorpherlibrary.properties.Reveal
import kotlin.math.max

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since September 19 2018
 */

object RevealUtility {

    private const val CORNER_RADIUS_PROPERTY = "cornerRadius"

    private const val SCALE_X_PROPERTY = "scaleX"
    private const val SCALE_Y_PROPERTY = "scaleY"

    fun circularReveal(
        reveal: Reveal
    ) {
        circularReveal(
            reveal.radiusStart,
            reveal.centerX.toInt(),
            reveal.centerY.toInt(),
            reveal.view,
            reveal.interpolator,
            reveal.duration ?: MIN_DURATION,
            MIN_DURATION,
            reveal.onStart,
            reveal.onEnd)
    }

    fun circularReveal(
        sourceView: View,
        resultView: View,
        interpolator: TimeInterpolator? = null,
        divider: Int = 2,
        duration: Long,
        startDelay: Long = MIN_DURATION,
        onStart: Action = null,
        onEnd: Action = null
    ) {
        val startRadius = max(sourceView.width.toDouble(), sourceView.height.toDouble()).toFloat()

        val location = IntArray(2)

        sourceView.getLocationOnScreen(location)

        val cx = location[0] + sourceView.width / 2
        val cy = location[1] + sourceView.height / 2

        circularReveal(startRadius, cx, cy, resultView, interpolator, duration, startDelay, onStart, onEnd)
    }

    fun circularReveal(
        startRadius: Float,
        centerX: Int,
        centerY: Int,
        resultView: View,
        interpolator: TimeInterpolator? = null,
        duration: Long,
        startDelay: Long = MIN_DURATION,
        onStart: Action = null,
        onEnd: Action = null
    ) {
        val endRadius = max(resultView.width.toDouble(), resultView.height.toDouble()).toFloat()

        val revealAnimator = ViewAnimationUtils.createCircularReveal(
            resultView,
            centerX,
            centerY,
            startRadius,
            endRadius
        )

        val listener = MorphAnimationListener(
            onStart = {
                resultView.visibility = View.VISIBLE
                onStart?.invoke()
            },
            onEnd = onEnd
        )

        revealAnimator.addListener(listener)
        revealAnimator.interpolator = interpolator
        revealAnimator.duration = duration
        revealAnimator.startDelay = startDelay
        revealAnimator.start()
    }

    fun circularConceal(
        conceal: Conceal
    ) {
        circularConceal(
            conceal.radiusEnd,
            conceal.centerX.toInt(),
            conceal.centerY.toInt(),
            conceal.view,
            conceal.interpolator,
            conceal.duration ?: MIN_DURATION,
            MIN_DURATION,
            conceal.onStart,
            conceal.onEnd
        )
    }

    fun circularConceal(
        sourceView: View,
        resultView: View,
        interpolator: TimeInterpolator? = null,
        duration: Long,
        startDelay: Long = MIN_DURATION,
        onStart: Action = null,
        onEnd: Action = null
    ) {

        val endRadius: Float = max(sourceView.width.toDouble(), sourceView.height.toDouble()).toFloat() / 2f

        val location = IntArray(2)

        sourceView.getLocationInWindow(location)

        val cx = location[0] + resultView.width / 2
        val cy = location[1] + resultView.height / 2

        circularConceal(endRadius, cx, cy, resultView, interpolator, duration, startDelay, onStart, onEnd)
    }

    fun circularConceal(
        endRadius: Float,
        centerX: Int,
        centerY: Int,
        resultView: View,
        interpolator: TimeInterpolator? = null,
        duration: Long,
        startDelay: Long = MIN_DURATION,
        onStart: Action = null,
        onEnd: Action = null
    ) {

        val startRadius: Float = max(resultView.width.toDouble(), resultView.height.toDouble()).toFloat()

        val revealAnimator = ViewAnimationUtils.createCircularReveal(
            resultView,
            centerX,
            centerY,
            startRadius,
            endRadius
        )

        val listener = MorphAnimationListener(
            onStart = onStart,
            onEnd = {
                resultView.visibility = View.INVISIBLE
                onEnd?.invoke()
            }
        )

        revealAnimator.addListener(listener)
        revealAnimator.interpolator = interpolator
        revealAnimator.duration = duration
        revealAnimator.startDelay = startDelay
        revealAnimator.start()
    }

/*    fun getLayoutTransition(
        viewGroup: ViewGroup,
        duration: Long,
        interpolator: Interpolator? = null
    ): LayoutTransition {
        return getLayoutTransition(viewGroup, duration, interpolator, interpolator)
    }

    fun getLayoutTransition(
        viewGroup: ViewGroup,
        duration: Long,
        scaleUpInterpolator: Interpolator?,
        scaleDownInterpolator: Interpolator?
    ): LayoutTransition {

        val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
            viewGroup,
            PropertyValuesHolder.ofFloat(SCALE_X_PROPERTY, 1f, 0f),
            PropertyValuesHolder.ofFloat(SCALE_Y_PROPERTY, 1f, 0f)
        )
        scaleDown.duration = duration

        if (scaleDownInterpolator != null)
            scaleDown.interpolator = scaleDownInterpolator

        val scaleUp = ObjectAnimator.ofPropertyValuesHolder(
            viewGroup,
            PropertyValuesHolder.ofFloat(SCALE_X_PROPERTY, 0f, 1f),
            PropertyValuesHolder.ofFloat(SCALE_Y_PROPERTY, 0f, 1f)
        )

        scaleUp.duration = duration

        if (scaleUpInterpolator != null)
            scaleUp.interpolator = scaleUpInterpolator

        viewGroup.layoutTransition = LayoutTransition()

        val itemLayoutTransition = viewGroup.layoutTransition

        itemLayoutTransition.setAnimator(LayoutTransition.APPEARING, scaleUp)
        itemLayoutTransition.setAnimator(LayoutTransition.DISAPPEARING, scaleDown)
        itemLayoutTransition.setAnimator(LayoutTransition.CHANGE_APPEARING, scaleUp)
        itemLayoutTransition.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, scaleDown)
        itemLayoutTransition.setAnimator(LayoutTransition.CHANGING, scaleUp)

        itemLayoutTransition.enableTransitionType(LayoutTransition.APPEARING)
        itemLayoutTransition.enableTransitionType(LayoutTransition.DISAPPEARING)
        itemLayoutTransition.enableTransitionType(LayoutTransition.CHANGE_APPEARING)
        itemLayoutTransition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)
        itemLayoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        itemLayoutTransition.setAnimateParentHierarchy(true)

        return itemLayoutTransition
    }
*/

}

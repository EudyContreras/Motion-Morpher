package com.eudycontreras.motionmorpherlibrary.utilities

import android.animation.TimeInterpolator
import android.view.View
import android.view.ViewAnimationUtils
import com.eudycontreras.motionmorpherlibrary.Action
import com.eudycontreras.motionmorpherlibrary.MIN_DURATION
import com.eudycontreras.motionmorpherlibrary.listeners.MorphAnimationListener
import kotlin.math.hypot



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
        sourceView: View,
        resultView: View,
        interpolator: TimeInterpolator? = null,
        divider: Int = 2,
        duration: Long,
        startDelay: Long = MIN_DURATION,
        onStart: Action = null,
        onEnd: Action = null
    ) {
        val startRadius = hypot(sourceView.width.toDouble(), sourceView.height.toDouble()).toFloat() / (divider * 2f)

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
        val endRadius = hypot(resultView.width.toDouble(), resultView.height.toDouble()).toFloat()

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
        sourceView: View,
        resultView: View,
        interpolator: TimeInterpolator? = null,
        duration: Long,
        startDelay: Long = MIN_DURATION,
        onStart: Action = null,
        onEnd: Action = null
    ) {

        val endRadius: Float = hypot(resultView.width.toDouble(), resultView.height.toDouble()).toFloat() / 2f
        val startRadius: Float = hypot(sourceView.width.toDouble(), sourceView.height.toDouble()).toFloat()

        val location = IntArray(2)

        resultView.getLocationInWindow(location)

        val cx = location[0] + resultView.width / 2
        val cy = location[1] + resultView.height / 2

        val revealAnimator = ViewAnimationUtils.createCircularReveal(sourceView, cx, cy, startRadius, endRadius)

        revealAnimator.addListener(MorphAnimationListener(onStart, onEnd))
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

    fun getBitmapFromDrawable(context: Context, @DrawableRes drawableId: Int): Bitmap {
        val drawable = AppCompatResources.getDrawable(context, drawableId)
        return getBitmapFromDrawable(drawable)
    }

    fun getBitmapFromDrawable(drawable: Drawable?): Bitmap {
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else if (drawable is VectorDrawableCompat || drawable is VectorDrawable) {
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            bitmap
        } else if (drawable is TransitionDrawable){
            val bitmap = Bitmap.createBitmap(drawable.getDrawable(0).intrinsicWidth, drawable.getDrawable(0).intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            bitmap
        } else {
            throw IllegalArgumentException("unsupported drawable propertyName")
        }
    }*/

}

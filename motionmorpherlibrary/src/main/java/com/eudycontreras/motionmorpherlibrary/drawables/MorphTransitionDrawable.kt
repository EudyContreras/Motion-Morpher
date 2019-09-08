package com.eudycontreras.motionmorpherlibrary.drawables

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import androidx.core.animation.doOnEnd
import com.eudycontreras.motionmorpherlibrary.*
import com.eudycontreras.motionmorpherlibrary.extensions.dp

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 18 2019
 */

class MorphTransitionDrawable(vararg drawables: Drawable?) : TransitionDrawable(drawables) {

    //TODO("Fix so that the animation goes 30% to 70% Outgoing and Incoming")

    private var mFromAlpha: Int = 0
    private var mToAlpha: Int = MAX_COLOR

    private var mFromAngle: Int = 0
    private var mToAngle: Int = 180

    private var mFromScaleX: Float = MAX_OFFSET
    private var mToScaleX: Float = MIN_OFFSET

    private var mFromScaleY: Float = MAX_OFFSET
    private var mToScaleY: Float = MIN_OFFSET

    private var alphaValue: Int = 0

    private var scaleValueX: Float = MIN_OFFSET
    private var scaleValueY: Float = MIN_OFFSET

    private var rotationValue: Float = MIN_OFFSET

    private var lastFraction: Float = MIN_OFFSET
    private var fraction: Float = MIN_OFFSET

    private var mCrossfadePadding: Float = MIN_OFFSET

    private var mStartDrawableType: DrawableType = DrawableType.BITMAP
    private var mEndDrawableType: DrawableType = DrawableType.BITMAP

    private var mTransitionType: TransitionType = TransitionType.SEQUENTIAL

    private var mDecorators: ArrayList<Decorator> = ArrayList()

    private lateinit var mCurrentDrawable: Drawable

    private var reverseTransition: Boolean = false

    private var resetValues: Boolean = false

    private val onDoneInternal: Action = {
        resetTransition()
    }

    var isSequentialFadeEnabled: Boolean
        get() = transitionType == TransitionType.SEQUENTIAL
        set(value) {
            transitionType = if (value) TransitionType.SEQUENTIAL else transitionType
        }

    var transitionType: TransitionType
        get() = mTransitionType
        set(value) {
            mTransitionType = value
        }

    var crossfadePadding: Float
        get() = mCrossfadePadding
        set(value) {
            if (value in MIN_OFFSET..MAX_OFFSET) {
                mCrossfadePadding = value
            }
        }

    var fromAlpha: Float
        get() = mFromAlpha / 255f
        set(value) {
            if (value in MIN_OFFSET..MAX_OFFSET) {
                mFromAlpha = (MAX_COLOR * value).toInt()
            }
        }

    var toAlpha: Float
        get() = mToAlpha / 255f
        set(value) {
            if (value in MIN_OFFSET..MAX_OFFSET) {
                mToAlpha = (MAX_COLOR * value).toInt()
            }
        }

    var fromAngle: Int
        get() = mFromAngle
        set(value) {
            if (value in 0..360) {
                mFromAngle = value
            }
        }

    var toAngle: Int
        get() = mToAngle
        set(value) {
            if (value in 0..360) {
                mToAngle = value
            }
        }

    var fromScaleX: Float
        get() = mFromScaleX
        set(value) {
            if (value in MIN_OFFSET..MAX_OFFSET) {
                mFromScaleX = value
            }
        }

    var toScaleX: Float
        get() = mToScaleX
        set(value) {
            if (value in MIN_OFFSET..MAX_OFFSET) {
                mToScaleX = value
            }
        }

    var fromScaleY: Float
        get() = mFromScaleY
        set(value) {
            if (value in MIN_OFFSET..MAX_OFFSET) {
                mFromScaleY = value
            }
        }

    var toScaleY: Float
        get() = mToScaleY
        set(value) {
            if (value in MIN_OFFSET..MAX_OFFSET) {
                mToScaleY = value
            }
        }

    var startDrawableType: DrawableType
        get() = mStartDrawableType
        set(value) {
            mStartDrawableType = value
        }

    var endDrawableType: DrawableType
        get() = mEndDrawableType
        set(value) {
            mEndDrawableType = value
        }

    init {
        setId(0, 0)
        setId(1, 1)
    }

    fun setStartDrawable(drawable: Drawable?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setDrawable(0, drawable)
        } else {
            setDrawableByLayerId(0, drawable)
        }
    }

    fun setEndDrawable(drawable: Drawable?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setDrawable(1, drawable)
        } else {
            setDrawableByLayerId(1, drawable)
        }
    }

    override fun startTransition(durationMillis: Int) {
        startTransition(durationMillis.toLong())
    }

    fun startTransition(durationMillis: Long, interpolator: TimeInterpolator? = null, onEnd: Action = null): ValueAnimator {
        setUpTransition(false)

        val animator = ValueAnimator.ofFloat(MIN_OFFSET, MAX_OFFSET)
        animator.interpolator = interpolator
        animator.duration = durationMillis
        animator.doOnEnd { onEnd?.invoke() }
        animator.addUpdateListener {
            val value = it.animatedValue as Float
            updateTransition(value)
        }
        animator.start()
        return animator
    }

    override fun reverseTransition(durationMillis: Int) {
        reverseTransition(durationMillis.toLong())
    }

    fun reverseTransition(durationMillis: Long, interpolator: TimeInterpolator? = null, onEnd: Action = null): ValueAnimator {
        setUpTransition(true)

        val animator = ValueAnimator.ofFloat(MIN_OFFSET, MAX_OFFSET)
        animator.interpolator = interpolator
        animator.duration = durationMillis
        animator.doOnEnd { onEnd?.invoke() }
        animator.addUpdateListener {
            val value = it.animatedValue as Float
            updateTransition(value)
        }
        animator.start()
        return animator
    }

    fun showFirstdLayer() {
        alphaValue = 0
        invalidateSelf()
    }

    fun showSecondLayer() {
        alphaValue = MAX_ALPHA
        reverseTransition = false
        invalidateSelf()
    }

    override fun resetTransition() {
        lastFraction = MIN_OFFSET
        fraction = MIN_OFFSET

        alphaValue = 0
        scaleValueX = MAX_OFFSET
        scaleValueY = MAX_OFFSET
        rotationValue = MIN_OFFSET

        reverseTransition = false

        resetValues = true
        invalidateSelf()
    }

    fun setUpTransition(useReverse: Boolean = false) {
        if (useReverse) {
            if (transitionType == TransitionType.CROSSFADE) {
                lastFraction = MAX_OFFSET
                fraction = MAX_OFFSET

                mFromAlpha = MAX_ALPHA
                mToAlpha = 0

                mFromAngle = 180
                mToAngle = 0

                mFromScaleX = MIN_OFFSET
                mFromScaleY = MIN_OFFSET

                mToScaleX = MAX_OFFSET
                mToScaleY = MAX_OFFSET

                rotationValue = 180f
                scaleValueX = MIN_OFFSET
                scaleValueY = MIN_OFFSET
                alphaValue = MAX_COLOR

                reverseTransition = true
            } else {
                lastFraction = MIN_OFFSET
                fraction = MIN_OFFSET

                mFromAlpha = 0
                mToAlpha = MAX_COLOR

                mFromAngle = 180
                mToAngle = 0

                mFromScaleX = MAX_OFFSET
                mFromScaleY = MAX_OFFSET

                mToScaleX = MIN_OFFSET
                mToScaleY = MIN_OFFSET

                rotationValue = 180f
                scaleValueX = MAX_OFFSET
                scaleValueY = MAX_OFFSET
                alphaValue = 0

                reverseTransition = true
            }
        } else {
            lastFraction = MIN_OFFSET
            fraction = MIN_OFFSET

            mFromAlpha = 0
            mToAlpha = MAX_COLOR

            mFromAngle = 0
            mToAngle = 180

            mFromScaleX = MAX_OFFSET
            mFromScaleY = MAX_OFFSET

            mToScaleX = MIN_OFFSET
            mToScaleY = MIN_OFFSET

            rotationValue = MIN_OFFSET
            scaleValueX = MAX_OFFSET
            scaleValueY = MAX_OFFSET
            alphaValue = 0

            reverseTransition = false
        }
        inFirstHalf = true
    }

    fun updateTransition(inFraction: Float) {
        fraction = mapRange(inFraction, mCrossfadePadding, MAX_OFFSET - mCrossfadePadding, MIN_OFFSET, MAX_OFFSET, MIN_OFFSET, MAX_OFFSET)

        if (lastFraction > MIN_OFFSET && lastFraction < MAX_OFFSET) {
            invalidateSelf()
        }

        lastFraction = fraction
    }

    private fun animateCrossfade(canvas: Canvas, fraction: Float) {
        fun animateFade() {
            alphaValue = (mFromAlpha + (mToAlpha - mFromAlpha) * fraction).toInt()

            val alpha = alphaValue

            var drawable: Drawable = getDrawable(0)

            drawable.alpha = MAX_ALPHA - alpha

            drawable.draw(canvas)

            drawable.alpha = MAX_ALPHA

            if (alpha > 0) {
                drawable = getDrawable(1)
                drawable.alpha = alpha
                drawable.draw(canvas)
                drawable.alpha = MAX_ALPHA
            }
        }

        animateFade()
    }

    private var inFirstHalf: Boolean = true

    private fun animateSequential(canvas: Canvas, fraction: Float) {

        fun animateStartScale(value: Float) {
            scaleValueX = (mFromScaleX + (mToScaleX - mFromScaleX) * value)
            scaleValueY = (mFromScaleY + (mToScaleY - mFromScaleY) * value)

            mCurrentDrawable = getDrawable(if (reverseTransition) 1 else 0)

            canvas.scale(scaleValueX, scaleValueY,mCurrentDrawable.intrinsicWidth / 2f, mCurrentDrawable.intrinsicHeight / 2f)
        }

        fun animateEndScale(value: Float) {
            scaleValueX = (mFromScaleX + (mToScaleX - mFromScaleX) * value)
            scaleValueY = (mFromScaleY + (mToScaleY - mFromScaleY) * value)

            mCurrentDrawable = getDrawable(if (reverseTransition) 0 else 1)

            canvas.scale(scaleValueX, scaleValueY,mCurrentDrawable.intrinsicWidth / 2f, mCurrentDrawable.intrinsicHeight / 2f)
        }

        fun animateRotate(value: Float) {
            rotationValue = (mFromAngle + (mToAngle - mFromAngle) * value)

            mCurrentDrawable = getDrawable(if (reverseTransition) 1 else 0)

            canvas.rotate(rotationValue, mCurrentDrawable.intrinsicWidth / 2f, mCurrentDrawable.intrinsicHeight / 2f)
        }

        fun animateStartFade(value: Float) {

            alphaValue = (mFromAlpha + (mToAlpha - mFromAlpha) * value).toInt()

            val alpha = alphaValue

            mCurrentDrawable = getDrawable(if (reverseTransition) 1 else 0)

            mCurrentDrawable.alpha = MAX_ALPHA - alpha

            mCurrentDrawable.draw(canvas)

            mCurrentDrawable.alpha = MAX_ALPHA
        }

        fun animateEndFade(value: Float) {

            alphaValue = (mFromAlpha + (mToAlpha - mFromAlpha) * value).toInt()

            val alpha = alphaValue

            mCurrentDrawable = getDrawable(if (reverseTransition) 0 else 1)

            if (alpha > 0) {
                mCurrentDrawable.alpha = alpha
                mCurrentDrawable.draw(canvas)
                mCurrentDrawable.alpha = MAX_ALPHA
            }
        }

        animateRotate(fraction)

        if (inFirstHalf) {
            val value = mapRange(
                value = fraction,
                fromMin = 0f,
                fromMax = 0.5f,
                toMin = 0f,
                toMax = 1f,
                clampMin = 0f,
                clampMax = 1.0f)

            animateStartScale(value)
            animateStartFade(value)

            if (value >= 1f) {
                inFirstHalf = false
                lastFraction = MAX_OFFSET

                mFromAlpha = 0
                mToAlpha = MAX_COLOR

                mFromScaleX = MIN_OFFSET
                mFromScaleY = MIN_OFFSET

                mToScaleX = MAX_OFFSET
                mToScaleY = MAX_OFFSET

                scaleValueX = MIN_OFFSET
                scaleValueY = MIN_OFFSET

                alphaValue = 0
            }
        } else {
            val value = mapRange(
                value = fraction,
                fromMin = 0.5f,
                fromMax = 1f,
                toMin = 0f,
                toMax = 1f,
                clampMin = 0f,
                clampMax = 1.0f)

            animateEndScale(value)
            animateEndFade(value)
        }
        if (mDecorators.size > 0) {
            mDecorators.forEach {
                it.draw(canvas, mapRange(
                    value = fraction,
                    fromMin = it.fractionStart,
                    fromMax = it.fractionEnd,
                    toMin = 0f,
                    toMax = 1f,
                    clampMin = 0f,
                    clampMax = 1.0f)
                )
            }
        }
    }

    private fun resetDrawables(canvas: Canvas) {
        val drawableStart = getDrawable(0)
        val drawableEnd = getDrawable(1)

        canvas.scale(MAX_OFFSET, MAX_OFFSET,drawableEnd.intrinsicWidth / 2f, drawableEnd.intrinsicHeight / 2f)
        canvas.rotate(MIN_OFFSET, drawableEnd.intrinsicWidth / 2f, drawableEnd.intrinsicHeight / 2f)

        drawableEnd.alpha = 0
        drawableEnd.draw(canvas)

        canvas.scale(MAX_OFFSET, MAX_OFFSET,drawableStart.intrinsicWidth / 2f, drawableStart.intrinsicHeight / 2f)
        canvas.rotate(MIN_OFFSET, drawableStart.intrinsicWidth / 2f, drawableStart.intrinsicHeight / 2f)

        drawableStart.alpha = MAX_ALPHA
        drawableStart.draw(canvas)

        resetValues = false
    }

    override fun draw(canvas: Canvas) {
        if (resetValues) {
            resetDrawables(canvas)
        }
        when (transitionType) {
            TransitionType.CROSSFADE -> animateCrossfade(canvas, fraction)
            TransitionType.SEQUENTIAL -> animateSequential(canvas, fraction)
        }
    }

    override fun setCrossFadeEnabled(enabled: Boolean) {
        transitionType = TransitionType.CROSSFADE
    }

    override fun isCrossFadeEnabled(): Boolean {
        return transitionType == TransitionType.CROSSFADE
    }

    enum class TransitionType {
        CROSSFADE, SEQUENTIAL
    }

    enum class DrawableType {
        VECTOR, BITMAP, OTHER
    }

    companion object {
        private const val MAX_ALPHA = MAX_COLOR
        private const val MIN_ALPHA = 0
    }

    data class ValueMap<T: Number>(
        var value: T,
        var fromValue: T,
        var toValue: T
    )

    interface Decorator {
        var fractionStart: Float
        var fractionEnd: Float
        fun draw(canvas: Canvas, fraction: Float)
    }
}

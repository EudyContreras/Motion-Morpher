package com.eudycontreras.motionmorpherlibrary

import android.animation.TimeInterpolator
import androidx.core.math.MathUtils.clamp
import androidx.core.view.animation.PathInterpolatorCompat
import com.eudycontreras.motionmorpherlibrary.properties.AnimatedFloatValue
import com.eudycontreras.motionmorpherlibrary.properties.Coordinates
import java.lang.IllegalStateException
import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicInteger
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.hypot
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.full.isSupertypeOf

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

/**
 * A global atomic int id which is incremented upon access
 */
private var id = AtomicInteger(Int.MIN_VALUE)

const val MID_OFFSET: Float = 0.5f
/**
 * The global maximun offset for lerping interpolations. Value: `1F`
 */
const val MAX_OFFSET: Float = 1f

/**
 * The global minimun offset for lerping interpolations. Value: `0F`
 */
const val MIN_OFFSET: Float = 0f

/**
 * The global maximun angle. Value: `360F`
 */
const val MAX_ANGLE: Float = 360f

/**
 * The global minumun angle. Value: `0F`
 */
const val MIN_ANGLE: Float = 0f

/**
 * The global maximun color range. Value: `255`
 */
const val MAX_COLOR: Int = 255

/**
 * The global minimun color range. Value: `0`
 */
const val MIN_COLOR: Int = 0

/**
 * The global minimun default duration. Value: `0L`
 */
const val MIN_DURATION: Long = 0L

/**
 * The global default color in hexadecimal. Value: `0x000000`
 */
const val DEFAULT_COLOR: Int = 0x000000


/**
 * Standard easing.
 *
 * Elements that begin and end at rest use standard easing. They speed up quickly and slow down
 * gradually, in order to emphasize the end of the transition.
 *
 * Copyright 2019 The Android Open Source Project.
 */
val STANDARD: TimeInterpolator by lazy(LazyThreadSafetyMode.NONE) {
    PathInterpolatorCompat.create(0.4f, 0f, 0.2f, 1f)
}

/**
 * Decelerate easing.
 *
 * Incoming elements are animated using deceleration easing, which starts a transition at peak
 * velocity (the fastest point of an element’s movement) and ends at rest.
 *
 * Copyright 2019 The Android Open Source Project.
 */
val INCOMING: TimeInterpolator by lazy(LazyThreadSafetyMode.NONE) {
    PathInterpolatorCompat.create(0f, 0f, 0.2f, 1f)
}

/**
 * Accelerate easing.
 *
 * Elements exiting a screen use acceleration easing, where they start at rest and end at peak
 * velocity.
 *
 * Copyright 2019 The Android Open Source Project.
 */
val OUTGOING: TimeInterpolator by lazy(LazyThreadSafetyMode.NONE) {
    PathInterpolatorCompat.create(0.4f, 0f, 1f, 1f)
}

/**
 * Generates and returns an unique globa id by incrementing
 * an atomic integer. See: [AtomicInteger]
 */
fun getUniqueId(): Int {
    return id.incrementAndGet()
}

/**
 * Returns the linear interpolation of a start value
 * to an end value given the specified fraction of progress.
 * @param from the start value
 * @param to the end value
 * @param fraction the amount to lerp to given the range
 */
fun lerp(from: Int, to: Int, fraction: Float): Int {
    return (from + (to - from) * fraction).toInt()
}

/**
 * Returns the linear interpolation of a start value
 * to an end value given the specified fraction of progress.
 * @param from the start value
 * @param to the end value
 * @param fraction the amount to lerp to given the range
 */
fun lerp(from: Float, to: Float, fraction: Float): Float {
    return from + (to - from) * fraction
}

/**
 * Returns the linear interpolation of a start value
 * to an end value given the specified fraction of progress.
 * @param from the start value
 * @param to the end value
 * @param fraction the amount to lerp to given the range
 */
fun lerp(from: Double, to: Double, fraction: Double): Double {
    return from + (to - from) * fraction
}

/**
 * Returns the linear interpolation of a start value
 * to an end value given the specified fraction of progress.
 * @param floatValue the [AnimatedFloatValue] to interpolate
 * @param fraction the amount to lerp to given the range
 */
fun lerp(floatValue: AnimatedFloatValue, fraction: Double): Double {
    return floatValue.fromValue + (floatValue.toValue - floatValue.fromValue) * fraction
}

/**
 * Fuzzy approximation function which returns true if the [value] is
 * less or equals to the [target] plus the [margin] or greater or equal
 * to to the [target] minus the [margin].
 * @param value the input value to compare
 * @param target the value to compare against.
 * @param margin the margin of error to use in the comparison
 */
fun approximate(value: Float, target: Float, margin: Float): Boolean {
    return value <= (target + margin) && value > (target - margin)
}

/**
 * Fuzzy approximation function which returns true if the [value] is
 * less or equals to the [target] plus the [margin] or greater or equal
 * to to the [target] minus the [margin].
 * @param value the input value to compare
 * @param target the value to compare against.
 * @param margin the margin of error to use in the comparison
 */
fun approximate(value: Int, target: Int, margin: Int): Boolean {
    return value <= (target + margin) && value > (target - margin)
}

/**
 * Maps the given value from the specified minimum to the specified
 * minimun and from the specified maximun to the specified maximun
 * value. Ex:
 * ```
 *  var value = 40f
 *
 *  var fromMin = 0f
 *  var fromMax = 100f
 *
 *  var toMin = 0f
 *  var toMax = 1f
 *
 *  var result = 0.4f
 * ```
 * @param value the value to be transformed
 * @param fromMin the minimun value to map from
 * @param fromMax the maximun value to map from
 * @param toMin the minimun value to map to
 * @param toMax the maximun value to map to
 */
fun mapRange(value: Float, fromMin: Float, fromMax: Float, toMin: Float, toMax: Float): Float {
    return mapRange(value, fromMin, fromMax, toMin, toMax, toMin, toMax)
}

/**
 * Maps the given value from the specified minimum to the specified
 * minimun and from the specified maximun to the specified maximun using
 * clamping.
 * value. Ex:
 * ```
 *  var value = 40f
 *
 *  var fromMin = 0f
 *  var fromMax = 100f
 *
 *  var toMin = 0f
 *  var toMax = 1f
 *
 *  var result = 0.4f
 * ```
 * @param value the value to be transformed
 * @param fromMin the minimun value to map from
 * @param fromMax the maximun value to map from
 * @param toMin the minimun value to map to
 * @param toMax the maximun value to map to
 * @param clampMin the minimun value that the function can return
 * @param clampMax the maximun value that the function can return
 */
fun mapRange(value: Float, fromMin: Float, fromMax: Float, toMin: Float, toMax: Float, clampMin: Float, clampMax: Float): Float {
    return clamp((value - fromMin) * (toMax - toMin) / (fromMax - fromMin) + toMin, clampMin, clampMax)
}

/**
 * Returns the distance between two coordinates.
 * @param x1 The x axis coordinates of the first point
 * @param y1 The y axis coordinates of the first point
 * @param x2 The x axis coordinates of the second point
 * @param y2 The y axis coordinates of the second point
 */
fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Double {
    val x = (x2 - x1)
    val y = (y2 - y1)
    return hypot(x.toDouble(), y.toDouble())
}

/**
 * Returns the distance between two coordinates.
 * @param locationOne The [Coordinates] of the first point
 * @param locationTwo The [Coordinates] of the second point
 */
fun distance(locationOne: Coordinates, locationTwo: Coordinates): Double {
    return distance(locationOne.x, locationOne.y, locationTwo.x, locationTwo.y)
}

/**
 * Returns the true if any of the elements inside [args] meets the
 * specified [predicate].
 */
inline fun <reified T> any(vararg args: T, predicate: (any: T) -> Boolean): Boolean {
    return args.any(predicate)
}

/**
 * Returns the true if all of the elements inside [args] meets the
 * specified [predicate].
 */
inline fun <reified T> all(vararg args: T, predicate: (all: T) -> Boolean): Boolean {
    return args.all(predicate)
}

/**
 * Returns the true if none of the elements inside [args] meets the
 * specified [predicate].
 */
inline fun <reified T> none(vararg args: T, predicate: (none: T) -> Boolean): Boolean {
    return args.none(predicate)
}

/**
 * Runs the code inside the [capsule] for the given
 * [param]
 */
inline fun <reified T> doWith(param: T, capsule: (T) -> Unit) {
    return capsule.invoke(param)
}

/**
 * Runs the code inside the [capsule] for the given parameters
 * given that neither the first nor the second parameters are null.
 */
inline fun <reified X,reified Y> doWith(first: X?, second: Y?, capsule: (X, Y) -> Unit) {
    if (first != null && second != null) {
        return capsule.invoke(first, second)
    }
}

/**
 * Runs the code inside the [capsule] for the given parameters
 * given that neither the first, the second, nor the third parameters are null.
 */
inline fun <reified X,reified Y,reified Z> doWith(first: X?, second: Y?, third: Z?, capsule: (X, Y, Z) -> Unit) {
    if (first != null && second != null && third != null) {
        return capsule.invoke(first, second, third)
    }
    throw KotlinNullPointerException("")
}

/**
 * Casts the target to the precedding fadeType of its assignment.
 */
inline fun <reified T> Any.cast(): T{
    return this as T
}



/**
 * Throws an [IllegalStateException] with the result of calling [lazyMessage] if the [value] is false.
 *
 * @sample samples.misc.Preconditions.failRequireWithLazyMessage
 */
public inline fun requireThat(value: Boolean, lazyMessage: () -> Any): Unit {
    if (!value) {
        val message = lazyMessage()
        throw IllegalStateException(message.toString())
    }
}
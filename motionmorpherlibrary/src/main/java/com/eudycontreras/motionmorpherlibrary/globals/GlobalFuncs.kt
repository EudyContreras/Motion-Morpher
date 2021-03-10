package com.eudycontreras.motionmorpherlibrary.globals

import androidx.core.math.MathUtils.clamp
import com.eudycontreras.motionmorpherlibrary.extensions.dp
import com.eudycontreras.motionmorpherlibrary.properties.AnimatedValues.AnimatedFloatValue
import com.eudycontreras.motionmorpherlibrary.properties.Coordinates
import java.lang.IllegalStateException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs
import kotlin.math.hypot

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

/**
 * A global atomic int id which is incremented upon access
 */
private var id = AtomicInteger(Int.MIN_VALUE)

/**
 * Transforms a collections of values into dp values
 * and returns it in the form of a FloatArray
 */
fun dpValues(vararg values: Float): FloatArray{
    return values.map { it.dp }.toFloatArray()
}

/**
 * Transforms a collections of values into dp values
 * and returns it in the form of a FloatArray
 */
fun dpValues(vararg values: Int): FloatArray {
    return values.map { it.dp }.toFloatArray()
}

/**
 * Transforms a collections of values into dp values
 * and returns it in the form of a FloatArray
 */
fun <T: Number> dpValues(vararg values: T): FloatArray {
    return values.map { it.toFloat() }.toFloatArray()
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
 * Determines if a two values are within a specific range of
 * one-another. If the values are within the [range] this function
 * will return true.
 * @param arg1 First value.
 * @param arg2 Second value.
 * @param range The range to compare against.
 */
fun inRange(arg1: Float, arg2: Float, range: Float): Boolean {
    return abs(arg2 - arg1) <= range
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
fun mapRange(value: Float, fromMin: Float, fromMax: Float, toMin: Float = MIN_OFFSET, toMax: Float = MAX_OFFSET): Float {
    return mapRange(
        value,
        fromMin,
        fromMax,
        toMin,
        toMax,
        toMin,
        toMax
    )
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
    return distance(
        locationOne.x,
        locationOne.y,
        locationTwo.x,
        locationTwo.y
    )
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
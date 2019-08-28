package com.eudycontreras.motionmorpherlibrary

import androidx.core.math.MathUtils.clamp
import com.eudycontreras.motionmorpherlibrary.observable.ObservableProperty
import com.eudycontreras.motionmorpherlibrary.observable.ObservableValue
import com.eudycontreras.motionmorpherlibrary.observable.PropertyChangeObservable
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.hypot
import kotlin.reflect.KProperty

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

private var id = AtomicInteger(Int.MIN_VALUE)

const val MAX_OFFSET: Float = 1f
const val MIN_OFFSET: Float = 0f

const val MAX_ANGLE: Float = 360f
const val MIN_ANGLE: Float = 0f

const val MIN_COLOR: Int = 0
const val MAX_COLOR: Int = 255

const val MIN_DURATION: Long = 0L

const val DEFAULT_COLOR: Int = 0x000000

fun getUniqueId(): Int {
    return id.incrementAndGet()
}

fun interpolate(from: Int, to: Int, fraction: Float): Float {
    return from + (to - from) * fraction
}

fun interpolate(from: Float, to: Float, fraction: Float): Float {
    return from + (to - from) * fraction
}

fun interpolate(from: Double, to: Double, fraction: Double): Double {
    return from + (to - from) * fraction
}

fun mapRange(value: Float, fromMin: Float, fromMax: Float, toMin: Float, toMax: Float): Float {
    return mapRange(value, fromMin, fromMax, toMin, toMax, toMin, toMax)
}

fun mapRange(value: Float, fromMin: Float, fromMax: Float, toMin: Float, toMax: Float, clampMin: Float, clampMax: Float): Float {
    return clamp((value - fromMin) * (toMax - toMin) / (fromMax - fromMin) + toMin, clampMin, clampMax)
}

fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Double {
    val x = (x2 - x1)
    val y = (y2 - y1)
    return hypot(x.toDouble(), y.toDouble())
}

inline fun <reified T> any(vararg args: T, predicate: (any: T) -> Boolean): Boolean {
    return args.any(predicate)
}

inline fun <reified T> all(vararg args: T, predicate: (all: T) -> Boolean): Boolean {
    return args.all(predicate)
}

inline fun <reified T> none(vararg args: T, predicate: (none: T) -> Boolean): Boolean {
    return args.none(predicate)
}

inline fun <reified T> doWith(param: T, capsule: (T) -> Unit) {
    return capsule.invoke(param)
}

inline fun <reified X,reified Y> doWith(first: X?, second: Y?, capsule: (X,Y) -> Unit) {
    if (first != null && second != null) {
        return capsule.invoke(first, second)
    }
}

inline fun <reified X,reified Y,reified Z> doWith(first: X?, second: Y?, third: Z?, capsule: (X,Y,Z) -> Unit) {
    if (first != null && second != null && third != null) {
        return capsule.invoke(first, second, third)
    }
    throw KotlinNullPointerException("")
}

fun <T> T.toObservable(property: KProperty<Any>, observable: PropertyChangeObservable): ObservableProperty<T>{
    return ObservableProperty(this, property.name, observable)
}

fun <T> from(value: T, property: KProperty<Any>, observable: PropertyChangeObservable): ObservableProperty<T>{
    return ObservableProperty(value, property.name, observable)
}

fun <T> from(value: T): ObservableValue<T> {
    return ObservableValue(value)
}

inline fun <reified T> Any.cast(): T{
    return this as T
}

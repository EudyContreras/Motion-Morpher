package com.eudycontreras.motionmorpherlibrary

import androidx.core.math.MathUtils


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
    return (value - fromMin) * (toMax - toMin) / (fromMax - fromMin) + toMin
}

fun mapRange(value: Float, fromMin: Float, fromMax: Float, toMin: Float, toMax: Float, clampMin: Float, clampMax: Float): Float {
    return MathUtils.clamp(((value - fromMin) * (toMax - toMin) / (fromMax - fromMin) + toMin), clampMin, clampMax)
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
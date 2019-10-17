package com.eudycontreras.motionmorpherlibrary.interfaces

import kotlin.reflect.KMutableProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since September 08 2019
 */

@FunctionalInterface interface Cloneable<T> {
    fun clone(): T
}
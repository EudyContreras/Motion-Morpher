package com.eudycontreras.motionmorpherlibrary.interfaces

import kotlin.reflect.KMutableProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

@FunctionalInterface interface Cloneable<T> {
    fun clone(): T
}
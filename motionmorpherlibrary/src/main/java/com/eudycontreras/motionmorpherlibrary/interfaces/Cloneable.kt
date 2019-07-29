package com.eudycontreras.motionmorpherlibrary.interfaces

import kotlin.reflect.KMutableProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

@FunctionalInterface interface Cloneable<T> {

    fun clone(): T

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> clone(entityClass: Class<T>): T {
        val clone: T = entityClass.newInstance()

        clone::class.memberProperties
            .filter{ it.visibility == KVisibility.PUBLIC }
            .filterIsInstance<KMutableProperty<*>>()
            .forEach { prop ->
                prop.setter.call(clone, prop.getter.call(this))
            }

        return clone
    }
}
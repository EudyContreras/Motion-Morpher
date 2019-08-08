package com.eudycontreras.motionmorpherlibrary.observable

import com.eudycontreras.motionmorpherlibrary.ValueChangeListener
import kotlin.properties.ObservableProperty


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */


data class ObservableValue<T>(private val _value: T): ObservableProperty<T>(_value) {

    var value = _value
        set(value) {
            if(value != field) {
                val old = value
                field = value
                changeListeners.forEach { it.invoke(old, field) }
            }
        }

    var changeListeners = ArrayList<ValueChangeListener<T>>()

    fun set(value: T) {
        this.value = value
    }

    fun get(): T = value

    fun addChangeListener(changeListener: ValueChangeListener<T>) {
        changeListeners.add(changeListener)
    }

    fun removeChangeListener(changeListener: ValueChangeListener<T>) {
        changeListeners.remove(changeListener)
    }
}
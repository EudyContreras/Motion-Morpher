package com.eudycontreras.motionmorpherlibrary.utilities.binding

import com.eudycontreras.motionmorpherlibrary.BindingChangeListener


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 02 2019
 */


abstract class Bindable<T> {

    val bindings: ArrayList<Binding<T>> = ArrayList()

    var changeListener: BindingChangeListener<T> = null

    fun bindTo(bind: Bind, other: Bindable<T>) {
        when (bind) {
            Bind.UNIDIRECTIONAL -> {
                bindings.add(Binding(this, other))
            }
            Bind.BIDIRECTIONAL -> {
                bindings.add(Binding(this, other))
                other.bindings.add(Binding(other, this))
            }
        }
    }

    fun notifyChange(newValue: T) {
        bindings.forEach { it.other.onBindingChanged(newValue) }

        changeListener?.invoke(newValue)
    }

    abstract fun onBindingChanged(newValue: T)
}
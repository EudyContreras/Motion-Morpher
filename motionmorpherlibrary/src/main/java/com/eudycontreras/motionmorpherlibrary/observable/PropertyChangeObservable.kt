package com.eudycontreras.motionmorpherlibrary.observable

import com.eudycontreras.motionmorpherlibrary.PropertyChangeListener


/**
 * Created by eudycontreras.
 */

abstract class PropertyChangeObservable {
    protected var listeners = ArrayList<PropertyChangeListener<Any>>()

    internal var processChanges = false

    fun addPropertyChangeListener(changeListener: PropertyChangeListener<Any>){
        listeners.add(changeListener)
    }

    fun removePropertyChangeListener(changeListener: PropertyChangeListener<Any>){
        listeners.add(changeListener)
    }

    fun onPropertyChange(oldValue: Any, newValue: Any, name: String) {
        listeners.forEach { it.invoke(oldValue, newValue, name) }
    }
}
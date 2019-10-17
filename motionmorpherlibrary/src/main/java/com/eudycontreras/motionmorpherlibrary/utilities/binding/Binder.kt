package com.eudycontreras.motionmorpherlibrary.utilities.binding


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 02 2019
 */

class Binder {
    companion object {
        fun <T> createBinding(bind: Bind, argOne: Bindable<T>, argTwo: Bindable<T>) {
            argOne.bindTo(bind, argTwo)
        }
    }
}
package com.eudycontreras.motionmorpherlibrary.utilities.binding


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 02 2019
 */

data class Binding<T>(
    var bindable: Bindable<T>,
    var other: Bindable<T>
)
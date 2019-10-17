package com.eudycontreras.motionmorpherlibrary.interfaces

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since September 08 2019
 */

interface Morphable {
    fun build()

    fun reset()

    fun morph(fraction: Float)

    fun onEnd()
}
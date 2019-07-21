package com.eudycontreras.motionmorpherlibrary.properties

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

data class Dimension(
    var width: Float = 0f,
    var height: Float = 0f
) {
    var padding = Paddings()

    fun copy(): Dimension {
        return Dimension(width, height)
    }
}
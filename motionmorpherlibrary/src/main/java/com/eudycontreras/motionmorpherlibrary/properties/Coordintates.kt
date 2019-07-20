package com.eudycontreras.motionmorpherlibrary.properties

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

data class Coordintates(
    var x: Float = 0f,
    var y: Float = 0f
) {
    fun copy(): Coordintates {
        return Coordintates(x, y)
    }

    fun midPoint(other: Coordintates): Coordintates {
        return Companion.midPoint(this, other)
    }

    companion object {
        fun midPoint(start: Coordintates, end: Coordintates): Coordintates {
            return Coordintates((start.x + end.x) / 2 , (start.y + end.y) / 2)
        }
    }
}
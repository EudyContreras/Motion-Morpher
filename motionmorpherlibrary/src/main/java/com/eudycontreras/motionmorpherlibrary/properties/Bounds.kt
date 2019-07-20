package com.eudycontreras.motionmorpherlibrary.properties

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

data class Bounds(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 0f,
    var height: Float = 0f
) {
    fun inRange(sourceX: Float, sourceY: Float): Boolean {
        return sourceX >= x && sourceX < width && sourceY >= y && sourceY < height
    }

    fun inRange(sourceX: Float, sourceY: Float, radius: Float): Boolean {
        return sourceX >= x - radius && sourceX < width + radius && sourceY >= y - radius && sourceY < height + radius
    }
}
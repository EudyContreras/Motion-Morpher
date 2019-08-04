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
    fun copy(): Dimension {
        return Dimension(width, height)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Dimension) return false

        if (width != other.width) return false
        if (height != other.height) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width.hashCode()
        result = 31 * result + height.hashCode()
        return result
    }

}
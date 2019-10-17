package com.eudycontreras.motionmorpherlibrary.properties

import com.eudycontreras.motionmorpherlibrary.globals.MIN_OFFSET

/**
 * Class which holds information about a dimension.
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

data class Dimension(
    var width: Float = MIN_OFFSET,
    var height: Float = MIN_OFFSET
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
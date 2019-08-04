package com.eudycontreras.motionmorpherlibrary.properties

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

data class Coordinates(
    var x: Float = 0f,
    var y: Float = 0f
) {
    fun copy(): Coordinates {
        return Coordinates(x, y)
    }

    fun midPoint(other: Coordinates): Coordinates {
        return Companion.midPoint(this, other)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Coordinates) return false

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }


    companion object {
        fun midPoint(start: Coordinates, end: Coordinates): Coordinates {
            return Coordinates((start.x + end.x) / 2 , (start.y + end.y) / 2)
        }
    }
}
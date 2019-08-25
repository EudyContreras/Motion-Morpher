package com.eudycontreras.motionmorpherlibrary.properties

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

data class Gradient(
    var colors: Array<Color> = emptyArray(),
    val type: Type = Type.LINEAR
) {
    enum class Type {
        RADIAL,
        LINEAR,
        SWEEP
    }

    companion object {
        const val TOP_TO_BOTTOM = 0
        const val BOTTOM_TO_TOP = 1
        const val LEFT_TO_RIGHT = 2
        const val RIGHT_TO_LEFT = 3
    }

    override fun equals(other: Any?): Boolean {

        if (this === other) return true
        if (other !is Gradient) return false

        if (!colors.contentEquals(other.colors)) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = colors.contentHashCode()
        result = 31 * result + type.ordinal
        return result
    }
}
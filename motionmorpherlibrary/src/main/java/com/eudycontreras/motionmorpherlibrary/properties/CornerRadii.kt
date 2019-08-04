package com.eudycontreras.motionmorpherlibrary.properties

import com.eudycontreras.motionmorpherlibrary.utilities.binding.Bindable

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

class CornerRadii(
    topLeft: Float = 0f,
    topRight: Float = 0f,
    bottomRight: Float = 0f,
    bottomLeft: Float = 0f
): Bindable<FloatArray>() {
    val topLeft: Float
        get() = corners[0]

    val topRight: Float
        get() = corners[2]

    val bottomRight: Float
        get() = corners[4]

    val bottomLeft: Float
        get() = corners[6]

    val corners = FloatArray(8)

    val size: Int
        get() = corners.size

    init {
        apply(topLeft, topRight, bottomRight, bottomLeft)
    }

    constructor(radii: FloatArray): this(radii[0], radii[2], radii[4], radii[6])

    constructor(radii: Float): this(radii, radii, radii, radii)

    constructor(): this(0f, 0f, 0f, 0f)

    fun apply(cornerRadii: CornerRadii) {
        for (index in 0 until cornerRadii.size) {
            corners[index] = cornerRadii[index]
        }
        notifyChange(corners)
    }

    fun apply(cornerRadii: FloatArray) {
        for (index in 0 until cornerRadii.size) {
            corners[index] = cornerRadii[index]
        }
        notifyChange(corners)
    }

    fun apply(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float) {
        corners[0] = topLeft
        corners[1] = topLeft

        corners[2] = topRight
        corners[3] = topRight

        corners[4] = bottomRight
        corners[5] = bottomRight

        corners[6] = bottomLeft
        corners[7] = bottomLeft

        notifyChange(corners)
    }

    fun getCopy(): CornerRadii {
        return CornerRadii(topLeft, topRight, bottomRight, bottomLeft)
    }

    operator fun get(index: Int): Float {
        return corners[index]
    }

    operator fun set(index: Int, value: Float) {
        corners[index] = value
        notifyChange(corners)
    }

    override fun onBindingChanged(newValue: FloatArray) {
        for (index in 0 until newValue.size) {
            corners[index] = newValue[index]
        }
    }

    override fun toString(): String {
        return "TL: ${corners[0]} TR: ${corners[2]} BR: ${corners[4]} BL: ${corners[6]}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CornerRadii) return false

        if (!corners.contentEquals(other.corners)) return false

        return true
    }

    override fun hashCode(): Int {
        return corners.contentHashCode()
    }
}
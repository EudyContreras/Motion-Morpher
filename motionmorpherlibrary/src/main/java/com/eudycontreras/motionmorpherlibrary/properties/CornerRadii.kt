package com.eudycontreras.motionmorpherlibrary.properties

import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.interfaces.Cloneable
import com.eudycontreras.motionmorpherlibrary.utilities.binding.Bindable

/**
 * Class which holds information about the corner radii of
 * a rectangular shape. This internally holds a float array
 * containing the x and y value of all 4 corners.
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

class CornerRadii(
    topLeft: Float = MIN_OFFSET,
    topRight: Float = MIN_OFFSET,
    bottomRight: Float = MIN_OFFSET,
    bottomLeft: Float = MIN_OFFSET
): Bindable<FloatArray>(), Cloneable<CornerRadii>{

    val topLeft: Float
        get() = corners[0]

    val topRight: Float
        get() = corners[2]

    val bottomRight: Float
        get() = corners[4]

    val bottomLeft: Float
        get() = corners[6]

    val corners = FloatArray(SIZE)

    val size: Int
        get() = corners.size

    init {
        apply(topLeft, topRight, bottomRight, bottomLeft)
    }

    constructor(radii: FloatArray): this(radii[0], radii[2], radii[4], radii[6])

    constructor(radii: Float): this(radii, radii, radii, radii)

    constructor(): this(MIN_OFFSET, MIN_OFFSET, MIN_OFFSET, MIN_OFFSET)

    fun apply(cornerRadii: CornerRadii) {
        for (index in 0 until cornerRadii.size) {
            corners[index] = cornerRadii[index]
        }
        notifyChange(corners)
    }

    fun apply(cornerRadii: FloatArray) {
        for (index in cornerRadii.indices) {
            corners[index] = cornerRadii[index]
        }
        notifyChange(corners)
    }

    fun apply(
        topLeft: Float = MIN_OFFSET,
        topRight: Float = MIN_OFFSET,
        bottomRight: Float = MIN_OFFSET,
        bottomLeft: Float = MIN_OFFSET
    ) {
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

    fun setTopLeft(radii: Float) {
        corners[0] = radii
        corners[1] = radii
    }

    fun setTopRight(radii: Float) {
        corners[2] = radii
        corners[3] = radii
    }

    fun setBottomRight(radii: Float) {
        corners[4] = radii
        corners[5] = radii
    }

    fun setBottomLeft(radii: Float) {
        corners[6] = radii
        corners[7] = radii
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

    override fun clone(): CornerRadii {
        return getCopy()
    }

    override fun onBindingChanged(newValue: FloatArray) {
        for (index in newValue.indices) {
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

    companion object {
        const val SIZE = 8
    }
}
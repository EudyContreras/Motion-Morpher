package com.eudycontreras.motionmorpherlibrary.properties

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

open class Bounds(
    x: Int = 0,
    y: Int = 0,
    width: Float = 0f,
    height: Float = 0f
) {
    var x: Int = x
        set(value) {
            field = value
            mCoordinates.x = value.toFloat()
        }

    var y: Int = y
        set(value) {
            field = value
            mCoordinates.y = value.toFloat()
        }

    var width: Float = width
        set(value) {
            field = value
            mDimension.width = value
        }

    var height: Float = height
        set(value) {
            field = value
            mDimension.height = value
        }

    var mCoordinates: Coordinates = Coordinates(x.toFloat(), y.toFloat())

    var mDimension: Dimension = Dimension(width, height)

    fun dimension(): Dimension = mDimension

    fun coordinates(): Coordinates = mCoordinates

    fun inRange(sourceX: Float, sourceY: Float): Boolean {
        return sourceX >= x && sourceX < width && sourceY >= y && sourceY < height
    }

    fun inRange(sourceX: Float, sourceY: Float, radius: Float): Boolean {
        return sourceX >= x - radius && sourceX < width + radius && sourceY >= y - radius && sourceY < height + radius
    }

    fun getCopy(): Bounds {
       return Bounds(x, y, width, height)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Bounds) return false

        if (x != other.x) return false
        if (y != other.y) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (mCoordinates != other.mCoordinates) return false
        if (mDimension != other.mDimension) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        result = 31 * result + width.hashCode()
        result = 31 * result + height.hashCode()
        result = 31 * result + mCoordinates.hashCode()
        result = 31 * result + mDimension.hashCode()
        return result
    }

    operator fun times(value: Float): Bounds {
        val bounds = this.getCopy()
        bounds.width = bounds.width * value
        bounds.height = bounds.height * value
        return bounds
    }

    operator fun plus(value: Float): Bounds {
        val bounds = this.getCopy()
        bounds.width = bounds.width + value
        bounds.height = bounds.height + value
        return bounds
    }

    operator fun minus(value: Float): Bounds {
        val bounds = this.getCopy()
        bounds.width = bounds.width - value
        bounds.height = bounds.height - value
        return bounds
    }

    operator fun div(value: Float): Bounds {
        val bounds = this.getCopy()
        bounds.width = bounds.width / value
        bounds.height = bounds.height / value
        return bounds
    }
}
package com.eudycontreras.motionmorpherlibrary.properties

import com.eudycontreras.motionmorpherlibrary.globals.MIN_OFFSET

/**
 * Class which holds information about the bounds
 * of a rectangle. It holds the dimensions and coordinates
 * of said rectangle.
 *
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

open class Bounds(
    x: Int = 0,
    y: Int = 0,
    width: Float = MIN_OFFSET,
    height: Float = MIN_OFFSET
) {
    /**
     * The `x` axis coordinates of this
     * bounds
     */
    var x: Int = x
        set(value) {
            field = value
            mCoordinates.x = value.toFloat()
        }

    /**
     * The `y` axis coordinates of this
     * bounds
     */
    var y: Int = y
        set(value) {
            field = value
            mCoordinates.y = value.toFloat()
        }

    /**
     * The `width` dimension of this
     * bounds
     */
    var width: Float = width
        set(value) {
            field = value
            mDimension.width = value
        }

    /**
     * The `height` dimension of this
     * bounds
     */
    var height: Float = height
        set(value) {
            field = value
            mDimension.height = value
        }

    /**
     * Returns the max x coordinates
     * of this bounds
     */
    val maxX: Float
        get() = x + width

    /**
     * Returns the max y coordinates
     * of this bounds
     */
    val maxY: Float
        get() = y + height

    private var mCoordinates: Coordinates = Coordinates(x.toFloat(), y.toFloat())

    private var mDimension: Dimension = Dimension(width, height)

    /**
     * The dimension of this Bounds. See: [Dimension]
     */
    fun dimension(): Dimension = mDimension

    /**
     * The coordinates of this Bounds. See: [Coordinates]
     */
    fun coordinates(): Coordinates = mCoordinates

    /**
     * Returns true if the given point and its radius are inside this bounds.
     */
    fun inRange(sourceX: Float, sourceY: Float, radius: Float): Boolean {
        return sourceX >= x - radius && sourceX < width + radius && sourceY >= y - radius && sourceY < height + radius
    }

    /**
     * Returns true if the given coordinates are inside this bounds.
     */
    fun inside(coordinates: Coordinates): Boolean {
        return (coordinates.x >= this.x && coordinates.x <= (this.x + this.width))  && (coordinates.y >= this.y && coordinates.y <= (this.y + this.height))
    }

    /**
     * Returns true if the given [Bounds] are inside this bounds.
     */
    fun inside(other: Bounds): Boolean {
        val minX = x >= other.x
        val maxX = maxX <= other.maxX
        val minY = y >= other.y
        val maxY = maxY <= other.maxY
        return minX && maxX && minY && maxY
    }

    /**
     * Returns true if the given [Bounds] overlap with this bounds.
     */
    fun overlaps(other: Bounds, margin: Float = 0f): Boolean {
        val minX = x < (other.maxX + margin)
        val maxX = maxX > (other.x - margin)
        val minY = y < (other.maxY + margin)
        val maxY = maxY > (other.y - margin)
        return minX && maxX && minY && maxY
    }

    /**
     * Returns true if the given [Bounds] overlaps vertically with this bounds.
     */
    fun overlapsVertically(other: Bounds, margin: Float = 0f): Boolean {
        val minY = y < (other.maxY + margin)
        val maxY = maxY > (other.y - margin)
        return minY && maxY
    }

    /**
     * Returns true if the given [Bounds] overlap with this bounds.
     */
    fun overlapsHorizontally(other: Bounds, margin: Float = 0f): Boolean {
        val minX = x < (other.maxX + margin)
        val maxX = maxX > (other.x - margin)
        return minX && maxX
    }

    /**
     * Returns a copy of this bounds.
     */
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
package com.eudycontreras.motionmorpherlibrary.helpers

import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.properties.Coordinates
import java.lang.StrictMath.pow
import kotlin.math.round

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 19 2019
 *
 */

class ArcTranslationHelper(
    fromX: Float = MIN_OFFSET,
    toX: Float = MIN_OFFSET,
    fromY: Float = MIN_OFFSET,
    toY: Float = MIN_OFFSET
) {
    private var startPoint: Coordinates = Coordinates(fromX, fromY)
    private var endPoint: Coordinates = Coordinates(toX, toY)

    private var controlPoint: Coordinates = Coordinates.midPoint(startPoint, endPoint)

    fun setStartPoint(start: Coordinates) {
        startPoint = start
    }

    fun setStartPoint(x: Float, y: Float) {
        setStartPoint(Coordinates(x, y))
    }

    fun setEndPoint(end: Coordinates) {
        endPoint = end
    }

    fun setEndPoint(x: Float, y: Float) {
        setEndPoint(Coordinates(x, y))
    }

    fun setControlPoint(control: Coordinates) {
        controlPoint = control
    }

    fun setControlPoint(x: Float, y: Float) {
        setControlPoint(Coordinates(x, y))
    }

    fun getCurvedTranslationX(fraction: Float): Double {
        return calcBezier(fraction.toDouble(), startPoint.x, controlPoint.x, endPoint.x)
    }

    fun getCurvedTranslationX(fraction: Float, startX: Float, endX: Float, controlX: Float): Double {
        return calcBezier(fraction.toDouble(), startX, controlX, endX)
    }

    fun getCurvedTranslationY(fraction: Float): Double {
        return calcBezier(fraction.toDouble(), startPoint.y, controlPoint.y, endPoint.y)
    }

    fun getCurvedTranslationY(fraction: Float, startY: Float, endY: Float, controlY: Float): Double {
        return calcBezier(fraction.toDouble(), startY, controlY, endY)
    }

    /**
     * algorithm from: http://en.wikipedia.org/wiki/B%C3%A9zier_curve
     */
    private fun calcBezier(fraction: Double, start: Float, control: Float, end: Float): Double {
        return round(pow(((1.0 - fraction)), 2.0) * start) + (2.0 * (1.0 - fraction) * fraction * control) + (pow(fraction, 2.0) * end)
    }
}
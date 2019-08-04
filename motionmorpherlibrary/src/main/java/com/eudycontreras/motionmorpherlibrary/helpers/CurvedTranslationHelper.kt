package com.eudycontreras.motionmorpherlibrary.helpers

import com.eudycontreras.motionmorpherlibrary.properties.Coordinates
import java.lang.StrictMath.pow
import kotlin.math.round

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 19 2019
 *
 * Uses algorithm from: http://en.wikipedia.org/wiki/B%C3%A9zier_curve
 */

class CurvedTranslationHelper(
    fromX: Float = 0f,
    toX: Float = 0f,
    fromY: Float = 0f,
    toY: Float = 0f
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

    fun getCurvedTranslationX(scale: Float): Double {
        return calcBezier(scale.toDouble(), startPoint.x, controlPoint.x, endPoint.x)
    }

    fun getCurvedTranslationY(scale: Float): Double {
        return calcBezier(scale.toDouble(), startPoint.y, controlPoint.y, endPoint.y)
    }

    private fun calcBezier(scale: Double, start: Float, control: Float, end: Float): Double {
        return round(pow(((1.0 - scale)), 2.0) * start) + (2.0 * (1.0 - scale) * scale * control) + (pow(scale, 2.0) * end)
    }
}
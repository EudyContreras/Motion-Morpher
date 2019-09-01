package com.eudycontreras.motionmorpherlibrary.properties


/**
 * Class which holds information about a point
 * of floats.
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 20 2019
 */
 
data class FloatPoint(
    override var x: Float,
    override var y: Float
): Point<Float>(x, y)
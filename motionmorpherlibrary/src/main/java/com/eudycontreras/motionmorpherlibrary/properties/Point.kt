package com.eudycontreras.motionmorpherlibrary.properties


/**
 * Class which represents a point of generic fadeType [T].
 * The point holds an arbitrary `x` and `y` value
 *
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 20 2019
 */

open class Point<T>(
   open var x: T,
   open var y: T
)
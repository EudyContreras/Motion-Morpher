package com.eudycontreras.motionmorpherlibrary.properties


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 01 2019
 */
 
 
class Background {

    var corners: CornerRadii = CornerRadii()
    var gradient: Gradient = Gradient()
    var color: Color = MutableColor()
    var shape: Shape = Shape.RECTANGULAR

    enum class Shape {
        RECTANGULAR, CIRCULAR
    }
}
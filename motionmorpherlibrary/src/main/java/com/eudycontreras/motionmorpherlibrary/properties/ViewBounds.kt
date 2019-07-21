package com.eudycontreras.motionmorpherlibrary.properties

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

data class ViewBounds(
    var top: Int = 0,
    var left: Int = 0,
    var right: Int = 0,
    var bottom: Int = 0
) {
    val paddings: Paddings by lazy {
        Paddings()
    }

    val margins: Margins by lazy {
        Margins()
    }
}

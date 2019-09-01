package com.eudycontreras.motionmorpherlibrary.enumerations

import com.eudycontreras.motionmorpherlibrary.CornersSet

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 03 2019
 */

enum class Corner {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    ALL;

    infix fun and(other: Corner): CornersSet = CornersSet.of(this, other)
}

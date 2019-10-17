package com.eudycontreras.motionmorpherlibrary.extensions

import com.eudycontreras.motionmorpherlibrary.globals.CornersSet
import com.eudycontreras.motionmorpherlibrary.enumerations.Corner


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 03 2019
 */

infix fun CornersSet.and(other: Corner): CornersSet = CornersSet.of(other, *this.toTypedArray())

infix fun CornersSet.has(other: CornersSet) = this.containsAll(other)

infix fun CornersSet.has(other: Corner) = this.contains(other)

fun CornersSet.get(item: Corner): Corner {
    return this.first { it == item}
}
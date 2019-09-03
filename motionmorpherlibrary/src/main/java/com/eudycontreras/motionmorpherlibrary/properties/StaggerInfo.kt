package com.eudycontreras.motionmorpherlibrary.properties

import com.eudycontreras.motionmorpherlibrary.MAX_OFFSET
import com.eudycontreras.motionmorpherlibrary.MIN_DURATION
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since September 02 2019
 */

data class StaggerInfo(
    val view: MorphLayout,
    val distance: Float = MIN_OFFSET
) {
    var stagger: Long = MIN_DURATION

    var startOffset: Float = MIN_OFFSET
    var endOffset: Float = MAX_OFFSET
}
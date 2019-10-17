package com.eudycontreras.motionmorpherlibrary.properties

import com.eudycontreras.motionmorpherlibrary.globals.MAX_OFFSET
import com.eudycontreras.motionmorpherlibrary.globals.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.enumerations.Stagger

/**
 * Class which holds information about the stagger effect
 * to be applied upon animating a collection of view elements.
 *
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

data class AnimationStagger(
    var staggerOffset: Float = MIN_OFFSET,
    var staggerMultiplier: Float = MAX_OFFSET,
    var type: Stagger = Stagger.LINEAR
) {
    var epicenterNode: AnimationNode? = null

    var staggerData: List<StaggerInfo>? = null
}
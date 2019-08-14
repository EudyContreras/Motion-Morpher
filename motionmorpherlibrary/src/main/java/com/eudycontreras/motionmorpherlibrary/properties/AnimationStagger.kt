package com.eudycontreras.motionmorpherlibrary.properties

import com.eudycontreras.motionmorpherlibrary.MAX_OFFSET
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET

data class AnimationStagger(
    var staggerOffset: Float = MIN_OFFSET,
    var staggerMultiplier: Float = MAX_OFFSET
)
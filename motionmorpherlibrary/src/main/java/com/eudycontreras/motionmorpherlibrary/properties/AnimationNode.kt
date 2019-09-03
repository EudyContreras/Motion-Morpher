package com.eudycontreras.motionmorpherlibrary.properties

import com.eudycontreras.motionmorpherlibrary.MAX_OFFSET
import com.eudycontreras.motionmorpherlibrary.MIN_DURATION
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout

data class AnimationNode(
    val view: MorphLayout,
    val distance: Float,
    val translationX: AnimatedFloatValue,
    val translationY: AnimatedFloatValue,
    val epicenter: Boolean = false
    ) {
        var stagger: Long = MIN_DURATION
        var startOffset: Float = MIN_OFFSET
        var endOffset: Float = MAX_OFFSET

        fun copyOffsets(other: AnimationNode) {
            this.startOffset = other.startOffset
            this.endOffset = other.endOffset
        }

        override fun toString(): String {
            return "Distance: $distance, Stagger: $stagger Start: $startOffset End: $endOffset"
        }
    }
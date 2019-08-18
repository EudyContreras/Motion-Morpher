package com.eudycontreras.motionmorpherlibrary.helpers

import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.distance
import com.eudycontreras.motionmorpherlibrary.enumerations.AnimationType
import com.eudycontreras.motionmorpherlibrary.interactions.Explode
import com.eudycontreras.motionmorpherlibrary.properties.AnimationStagger
import com.eudycontreras.motionmorpherlibrary.properties.Bounds
import com.eudycontreras.motionmorpherlibrary.properties.Coordinates
import java.lang.StrictMath.round


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 15 2019
 */

class StaggerAnimationHelper {

/*    fun applyStagger(animationType: AnimationType, nodesGroups: LinkedHashMap<Float, List<Explode.Node>>, animationStagger: AnimationStagger, duration: Long) {
        val stagger = (duration * animationStagger.staggerOffset)
        val durationDelta = (duration - stagger)
        val delayAddition = stagger / nodesGroups.size
        var delay = 0f

        if (animationType == AnimationType.CONCEAL) {
            for (nodeEntry in nodesGroups.entries.reversed()) {
                val startOffset = delay / duration
                val endOffset = (delay + durationDelta) / duration

                for(node in nodeEntry.value.reversed()) {
                    node.startOffset = startOffset
                    node.endOffset = endOffset
                }

                delay += delayAddition
            }
        } else {
            for (nodeEntry in nodesGroups.entries) {
                val startOffset = delay / duration
                val endOffset = (delay + durationDelta) / duration

                for(node in nodeEntry.value) {
                    node.startOffset = startOffset
                    node.endOffset = endOffset
                }

                delay += delayAddition
            }
        }
    }*/

    companion object {

        fun applyStagger(
            bounds: Bounds,
            epicenter: Coordinates,
            animationType: AnimationType,
            nodeEntries: List<Explode.Node>,
            animationStagger: AnimationStagger,
            duration: Long
        ) {

            when (animationType) {

                AnimationType.REVEAL -> {
                    for (node in nodeEntries) {
                        val stagger: Float = getStagger(bounds, epicenter, node.centerLocation, animationStagger.speed, duration).toFloat()
                        val durationDelta: Float = (duration - stagger)

                        val startOffset: Float = stagger / duration
                        val endOffset: Float = (stagger + durationDelta) / duration

                        node.startOffset = startOffset
                        node.endOffset = endOffset
                    }
                }
                AnimationType.CONCEAL -> {
                    for (index in nodeEntries.size - 1 downTo 0) {
                        val node = nodeEntries[index]

                        val stagger = getStagger(bounds, epicenter, node.centerLocation, animationStagger.speed, duration)
                        val durationDelta = (duration - stagger)

                        val startOffset = stagger / duration
                        val endOffset = (stagger + durationDelta) / duration

                        node.startOffset = startOffset.toFloat()
                        node.endOffset = endOffset.toFloat()
                    }
                }
            }
        }

        fun getStagger(bounds: Bounds, epicenter: Coordinates, center: Coordinates, speed: Float, duration: Long): Long {
            val directionMultiplier = -1f

            val distanceFraction = distance(
                center.x,
                center.y,
                epicenter.x,
                epicenter.y
            ) / distance(MIN_OFFSET, MIN_OFFSET, bounds.width, bounds.height)

            return round(duration * directionMultiplier / speed * distanceFraction)
        }
    }

}

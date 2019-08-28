package com.eudycontreras.motionmorpherlibrary.helpers

import com.eudycontreras.motionmorpherlibrary.MAX_OFFSET
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.distance
import com.eudycontreras.motionmorpherlibrary.properties.Bounds
import com.eudycontreras.motionmorpherlibrary.properties.Coordinates
import java.lang.StrictMath.round


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 15 2019
 */

class StaggerAnimationHelper {

/*    fun applyStagger(animationType: AnimationType, nodesGroups: LinkedHashMap<Float, List<Explode.Node>>, stagger: AnimationStagger, duration: Long) {
        val stagger = (duration * stagger.staggerOffset)
        val durationDelta = (duration - stagger)
        val delayAddition = stagger / nodesGroups.size
        var delay = 0f

        if (animationType == AnimationType.CONCEAL) {
            for (nodeEntry in nodesGroups.entries.reversed()) {
                val fromValue = delay / duration
                val toValue = (delay + durationDelta) / duration

                for(node in nodeEntry.value.reversed()) {
                    node.fromValue = fromValue
                    node.toValue = toValue
                }

                delay += delayAddition
            }
        } else {
            for (nodeEntry in nodesGroups.entries) {
                val fromValue = delay / duration
                val toValue = (delay + durationDelta) / duration

                for(node in nodeEntry.value) {
                    node.fromValue = fromValue
                    node.toValue = toValue
                }

                delay += delayAddition
            }
        }
    }*/

    companion object {

     /*   fun applyStagger(
            bounds: Bounds,
            epicenter: Coordinates,
            animationType: AnimationType,
            nodeEntries: List<Explode.Node>,
            stagger: AnimationStagger,
            duration: Long
        ) {

            when (animationType) {

                AnimationType.REVEAL -> {
                    for (node in nodeEntries) {
                        val stagger: Float = getStagger(bounds, epicenter, node.centerLocation, stagger.speed, duration).toFloat()
                        val durationDelta: Float = (duration - stagger)

                        val fromValue: Float = stagger / duration
                        val toValue: Float = (stagger + durationDelta) / duration

                        node.fromValue = fromValue
                        node.toValue = toValue
                    }
                }
                AnimationType.CONCEAL -> {
                    for (index in nodeEntries.size - 1 downTo 0) {
                        val node = nodeEntries[index]

                        val stagger = getStagger(bounds, epicenter, node.centerLocation, stagger.speed, duration)
                        val durationDelta = (duration - stagger)

                        val fromValue = stagger / duration
                        val toValue = (stagger + durationDelta) / duration

                        node.fromValue = fromValue.toFloat()
                        node.toValue = toValue.toFloat()
                    }
                }
            }
        }*/

        fun getStagger(bounds: Bounds, epicenter: Coordinates, center: Coordinates, speed: Float, duration: Long): Long {
            val directionMultiplier = -MAX_OFFSET

            val distanceA = distance(
                center.x,
                center.y,
                epicenter.x,
                epicenter.y
            )

            val distanceB = distance(MIN_OFFSET, MIN_OFFSET, bounds.width, bounds.height)

            val distanceFraction = distanceA / distanceB

            return round(duration * directionMultiplier / speed * distanceFraction)
        }
    }

}

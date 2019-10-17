package com.eudycontreras.motionmorpherlibrary.helpers

import com.eudycontreras.motionmorpherlibrary.extensions.groupByAnd
import com.eudycontreras.motionmorpherlibrary.globals.MAX_OFFSET
import com.eudycontreras.motionmorpherlibrary.globals.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.globals.approximate
import com.eudycontreras.motionmorpherlibrary.globals.distance
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.properties.*
import java.lang.StrictMath.round
import java.util.*
import kotlin.math.hypot
import kotlin.math.roundToLong


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 15 2019
 */

class StaggerAnimationHelper {
    companion object {

        fun getStagger(bounds: Bounds, epicenter: Coordinates, center: Coordinates, speed: Float, duration: Long): Long {
            val directionMultiplier = -MAX_OFFSET

            val distanceA = distance(
                center.x,
                center.y,
                epicenter.x,
                epicenter.y
            )

            val distanceB = distance(
                MIN_OFFSET,
                MIN_OFFSET,
                bounds.width,
                bounds.height
            )

            val distanceFraction = distanceA / distanceB

            return round(duration * directionMultiplier / speed * distanceFraction)
        }

        private fun computeDistances(views: List<MorphLayout>, epicenter: Coordinates, bounds: Bounds?): LinkedList<StaggerInfo> {
            var nodes = LinkedList<StaggerInfo>()

            for (view in views) {

                if (bounds != null) {
                    if (!view.viewBounds.overlaps(bounds)) {
                        continue
                    }
                }

                val centerPoint = view.centerLocation

                val distance = hypot(centerPoint.x - epicenter.x, centerPoint.y - epicenter.y)

                val staggerInfo = StaggerInfo(
                    view = view,
                    distance = distance
                )

                nodes.add(staggerInfo)
            }

            return nodes
        }

        fun computeStaggerData(
            views: List<MorphLayout>,
            epicenter: Coordinates,
            bounds: Bounds?,
            duration: Long,
            animationStagger: AnimationStagger,
            margin: Float
        ) {
            var selector: (MorphLayout) -> Float = { margin }
            return computeStaggerData(views, epicenter, bounds, duration, animationStagger, selector)
        }

        fun computeStaggerData(
            views: List<MorphLayout>,
            epicenter: Coordinates?,
            bounds: Bounds?,
            duration: Long,
            animationStagger: AnimationStagger,
            marginSelector: (MorphLayout) -> Float
        ) {

            var nodes = computeDistances(views, epicenter ?: views[0].centerLocation, bounds)

            val nodesGroups = with(nodes) {
                nodes = LinkedList(nodes.sortedBy { it.distance })
                nodes.groupByAnd( { it.distance }, { it, other ->
                    approximate(
                        it.distance,
                        other.distance,
                        marginSelector(other.view)
                    )
                })
            }

            val stagger = (duration * animationStagger.staggerOffset)

            val durationDelta = (duration - stagger).toFloat()

            val delayAddition = (stagger / (nodesGroups.size)).toFloat()

            var delay = MIN_OFFSET

            for (nodeEntry in nodesGroups.entries) {

                val startOffset = delay.toFloat() / duration.toFloat()

                delay +=  delayAddition

                val endOffset = (delay.toFloat() + durationDelta).toFloat() / duration.toFloat()

                for(node in nodeEntry.value) {
                    node.startOffset = startOffset
                    node.endOffset = endOffset
                    node.stagger = delay.roundToLong()
                }
            }
            animationStagger.staggerData = nodes
        }
    }
}

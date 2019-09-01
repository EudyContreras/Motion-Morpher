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
    companion object {

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

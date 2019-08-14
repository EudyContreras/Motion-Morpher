/*
package com.eudycontreras.motionmorpherlibrary.interactions

import android.animation.TimeInterpolator
import com.eudycontreras.motionmorpherlibrary.MAX_OFFSET
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.enumerations.AnimationType
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.properties.AnimatedFloatValue
import com.eudycontreras.motionmorpherlibrary.properties.AnimatedValue
import com.eudycontreras.motionmorpherlibrary.properties.Coordinates
import com.eudycontreras.motionmorpherlibrary.properties.ViewBounds


*/
/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 12 2019
 *//*



class Implode(
    override var amountMultiplier: Float = MAX_OFFSET,
    override var interpolator: TimeInterpolator? = null
): Interaction() {

    private lateinit var mappingsX: Array<AnimatedFloatValue>
    private lateinit var mappingsY: Array<AnimatedFloatValue>

    private var siblings: List<MorphLayout>? = null
        set(value) {
            field = value
            mappingsX = Array(siblings?.size ?: 0) { AnimatedFloatValue(AnimatedValue.TRANSLATION_X, MIN_OFFSET, MIN_OFFSET) }
            mappingsY = Array(siblings?.size ?: 0) { AnimatedFloatValue(AnimatedValue.TRANSLATION_Y, MIN_OFFSET, MIN_OFFSET) }
        }
    */
/*
     The max distances should be the distance between the epicenter and the edge minus the item initials size divided by half
      *//*

    override fun createInteraction(
        startView: MorphLayout,
        endView: MorphLayout
    ) {
        val epicenter = Coordinates(
            startView.windowLocationX.toFloat() + startView.morphWidth / 2,
            startView.windowLocationY.toFloat() + startView.morphHeight / 2
        )

        val boundsStart: ViewBounds = startView.viewBounds
        val boundsEnd: ViewBounds = endView.viewBounds

        val maxX = (boundsEnd.right - boundsStart.right).toFloat()
        val maxY = (boundsEnd.bottom - boundsStart.bottom).toFloat()
        val minX = (boundsEnd.left - boundsStart.left).toFloat()
        val minY = (boundsEnd.top - boundsStart.top).toFloat()

        siblings = startView.siblings

        siblings?.let { siblingCollection ->
            for ((index, sibling) in siblingCollection.withIndex()) {

                val centerPoint = sibling.centerLocation

                val xDifference = epicenter.x - centerPoint.x
                val yDifference = epicenter.y - centerPoint.y

                if (xDifference > 0) mappingsX[index].toValue = maxX + xDifference
                if (yDifference > 0) mappingsY[index].toValue = maxY + yDifference
                if (xDifference < 0) mappingsX[index].toValue = minX + xDifference
                if (yDifference < 0) mappingsY[index].toValue = minY + yDifference
            }
        }
    }

    override fun applyInteraction(fraction: Float, animationType: AnimationType) {
        val siblings = this.siblings ?: return

        for ((index, sibling) in siblings.withIndex()) {
            val animatedX = mappingsX[index]
            val animatedY = mappingsY[index]

            val interpolatedFraction = interpolator?.getInterpolation(fraction) ?: fraction

            when(animationType) {
                AnimationType.REVEAL -> {
                    sibling.morphTranslationX = (animatedX.fromValue + (animatedX.toValue - animatedX.fromValue) * interpolatedFraction) * amountMultiplier
                    sibling.morphTranslationY = (animatedY.fromValue + (animatedY.toValue - animatedY.fromValue) * interpolatedFraction) * amountMultiplier
                }
                AnimationType.CONCEAL -> {
                    sibling.morphTranslationX = (animatedX.toValue + (animatedX.fromValue - animatedX.toValue) * interpolatedFraction) * amountMultiplier
                    sibling.morphTranslationY = (animatedY.toValue + (animatedY.fromValue - animatedY.toValue) * interpolatedFraction) * amountMultiplier
                }
            }
        }
    }
}*/

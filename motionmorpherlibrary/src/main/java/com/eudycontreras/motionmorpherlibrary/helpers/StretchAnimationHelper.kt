package com.eudycontreras.motionmorpherlibrary.helpers

import com.eudycontreras.motionmorpherlibrary.MAX_OFFSET
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.enumerations.Direction
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.mapRange
import com.eudycontreras.motionmorpherlibrary.properties.AnimatedValues.AnimatedFloatValue
import com.eudycontreras.motionmorpherlibrary.properties.Stretch
import kotlin.math.abs


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 15 2019
 */

class StretchAnimationHelper {

    companion object {
        fun applyStretch(view: MorphLayout, translationValue: AnimatedFloatValue, stretch: Stretch, translation: Float) {
            if (!stretch.hasDirection) {
                stretch.computeDirection(translationValue)
            }
            applyStretch(view, stretch.direction, translationValue, stretch.stretchOffset, stretch.amountMultiplier, stretch.squashAmount, stretch.startOffset, stretch.endOffset, translation)
        }

        fun applyStretch(
            view: MorphLayout,
            direction: Direction,
            translationValue: AnimatedFloatValue,
            stretchOffset: Float,
            stretchAmount: Float,
            squashAmount: Float,
            offsetStart: Float,
            offsetEnd: Float,
            fraction: Float
        ) {

            if (stretchOffset == MIN_OFFSET)
                return

            when (direction) {
                Direction.NONE -> {
                    return
                }
                Direction.UP, Direction.DOWN  -> {
                    view.morphPivotY = view.morphHeight * (MAX_OFFSET - stretchOffset)
                }
                Direction.LEFT, Direction.RIGHT  -> {
                    view.morphPivotX = view.morphWidth * (MAX_OFFSET - stretchOffset)
                }
            }

            val scaleMap: Float = mapRange(fraction, translationValue.fromValue, translationValue.toValue, MIN_OFFSET, MAX_OFFSET)

            val stretchMap: Float = mapRange(scaleMap, offsetStart, offsetEnd, MIN_OFFSET, MAX_OFFSET)

            val scaleStart = MAX_OFFSET
            val squashEnd = MAX_OFFSET - squashAmount

            if (stretchMap < stretchOffset) {

                val stretchFraction: Float = mapRange(stretchMap, MIN_OFFSET, stretchOffset, MIN_OFFSET, MAX_OFFSET)

                if (direction == Direction.DOWN || direction == Direction.UP) {

                    val difference: Float = abs(translationValue.difference - view.morphHeight)

                    val scaleEnd: Float = (((difference * stretchAmount) + view.morphHeight) / view.morphHeight)

                    view.morphScaleY =  (scaleStart + (scaleEnd - scaleStart) * stretchFraction)
                    view.morphScaleX =  (scaleStart + (squashEnd - scaleStart) * stretchFraction)
                }
                else {

                    val difference: Float = abs(translationValue.difference - view.morphWidth)

                    val scaleEnd: Float = ((difference * stretchAmount) + view.morphWidth / view.morphHeight)

                    view.morphScaleX =  (scaleStart + (scaleEnd - scaleStart) * stretchFraction)
                    view.morphScaleY =  (scaleStart + (squashEnd - scaleStart) * stretchFraction)
                }
            } else {

                if (stretchOffset == MAX_OFFSET)
                    return

                val unStretchFraction: Float = mapRange(stretchMap, stretchOffset, MAX_OFFSET, MIN_OFFSET, MAX_OFFSET)

                if (direction == Direction.DOWN || direction == Direction.UP) {

                    val difference: Float = abs(translationValue.difference - view.morphHeight)

                    val scaleEnd: Float = (((difference * stretchAmount) + view.morphHeight) / view.morphHeight)

                    view.morphScaleY =  (scaleEnd + (scaleStart - scaleEnd) * unStretchFraction)
                    view.morphScaleX =  (squashEnd + (scaleStart - squashEnd) * unStretchFraction)
                }
                else {

                    val difference: Float = abs(translationValue.difference - view.morphWidth)

                    val scaleEnd: Float = ((difference * stretchAmount) + view.morphWidth / view.morphHeight)

                    view.morphScaleX =  (scaleEnd + (scaleStart - scaleEnd) * unStretchFraction)
                    view.morphScaleY =  (squashEnd + (scaleStart - squashEnd) * unStretchFraction)
                }
            }
        }
    }
}
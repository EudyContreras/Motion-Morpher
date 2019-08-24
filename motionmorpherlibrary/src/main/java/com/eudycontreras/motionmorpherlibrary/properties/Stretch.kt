package com.eudycontreras.motionmorpherlibrary.properties

import com.eudycontreras.motionmorpherlibrary.MAX_OFFSET
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.enumerations.Direction


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 15 2019
 */
 
data class Stretch(
    var stretchOffset: Float,
    var amountMultiplier: Float,
    var squashAmount: Float = MIN_OFFSET
) {

    var startOffset: Float = MIN_OFFSET
    var endOffset: Float = MAX_OFFSET

    lateinit var direction: Direction

    val hasDirection: Boolean
        get() = ::direction.isInitialized

    constructor(
        stretchOffset: Float,
        amountMultiplier: Float,
        squashAmount: Float,
        startOffset: Float = MIN_OFFSET,
        endOffset: Float = MAX_OFFSET,
        direction: Direction? = null
    ): this(stretchOffset, amountMultiplier, squashAmount) {
        this.startOffset = startOffset
        this.endOffset = endOffset
        if (direction != null) {
            this.direction = direction
        }
    }

    constructor(
        stretchOffset: Float,
        amountMultiplier: Float,
        squashAmount: Float,
        direction: Direction
    ): this(stretchOffset, amountMultiplier, squashAmount) {
        this.direction = direction
    }

    internal fun computeDirection(translation: AnimatedFloatValue) {
        if (!::direction.isInitialized) {
            when (translation.propertyName) {
                AnimatedValue.TRANSLATION_Y, AnimatedValue.POSITION_Y -> {
                    direction = when {
                        translation.toValue > translation.fromValue -> Direction.DOWN
                        translation.toValue < translation.fromValue -> Direction.UP
                        else -> Direction.NONE
                    }
                }
                else -> {
                    direction = when {
                        translation.toValue > translation.fromValue -> Direction.RIGHT
                        translation.toValue < translation.fromValue -> Direction.LEFT
                        else -> Direction.NONE
                    }
                }
            }
        }
    }
}
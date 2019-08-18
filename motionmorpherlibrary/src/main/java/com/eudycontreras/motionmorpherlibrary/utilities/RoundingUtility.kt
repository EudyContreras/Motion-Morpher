package com.eudycontreras.motionmorpherlibrary.utilities


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 18 2019
 */


class RoundingUtility {

    private var positiveScale = intArrayOf(10, 100, 1_000, 10_000, 100_000, 1_000_000, 10_000_000, 100_000_000, 1_000_000_000)
    private var negativeScale = intArrayOf(-10, -100, -1_000, -10_000, -100_000, -1_000_000, -10_000_000, -100_000_000, -1_000_000_000)

    fun Int.magnitude(): Pair<Int, Float> {
        return when{
            this <= 4 -> Pair(2, 0.25f)
            this <= 9 -> Pair(2, 0.5f)
            this <= 19 -> Pair(2, 0.5f)
            this <= 49 -> Pair(2, 0.5f)
            this <= 74 -> Pair(2, 0.5f)
            this <= 99 -> Pair(1, 0.5f)
            else -> Pair(1, 1f)
        }
    }

    fun Int.roundToNearest(ratio: Float = 1.0f, shift: Int = 2, multiple: Int? = null, method: RoundMethod = RoundMethod.UP): Int {
        if(this == 0) {
            return this
        }
        val scaleDivider = 2f
        val min = positiveScale[0] /  2
        val multiplier = 0.75f

        if (this > 0 && this <= positiveScale[0]) {
            return  getRounding(this, min, method)
        } else if (this < 0 && this >= negativeScale[0]){
            return  getRounding(this, min, method)
        } else {
            for(i in shift until positiveScale.size) {
                var actual = (positiveScale[i - shift] * ratio).toInt()

                if(this >= 0) {
                    if(this <= multiple?:positiveScale[i]) {
                        val value = getRounding(this, actual, method)
                        if(this < value * multiplier) {
                            actual = (positiveScale[i - shift] * ratio / scaleDivider).toInt()
                            return getRounding(this, actual, method)
                        }
                        return  value
                    }
                } else {
                    if(this >= if (multiple != null) -multiple else negativeScale[i]) {
                        val value = getRounding(this, actual, method)
                        if(this > value * multiplier) {
                            actual = (positiveScale[i - shift] * ratio / scaleDivider).toInt()
                            return getRounding(this, actual, method)
                        }
                        return value
                    }
                }
            }
        }
        return this
    }

    private fun getRounding(value: Int, scale: Int, method: RoundMethod): Int {
        return when (method) {
            RoundMethod.UP ->  (value + if (value < 0) -scale else scale) / scale * scale
            RoundMethod.DOWN -> (value - if (value < 0) -scale else scale) / scale * scale
        }
    }

    enum class RoundMethod {
        UP,
        DOWN
    }
}
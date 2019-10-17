package com.eudycontreras.motionmorpherlibrary.properties

import com.eudycontreras.motionmorpherlibrary.globals.AndroidColor
import com.eudycontreras.motionmorpherlibrary.globals.MAX_COLOR
import com.eudycontreras.motionmorpherlibrary.globals.MIN_COLOR


/**
 * Class which represent a color built alpha, red, green and blue
 * chanels.
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

abstract class Color(
    var alpha: Int = MAX_COLOR,
    var red: Int = MIN_COLOR,
    var green: Int = MIN_COLOR,
    var blue: Int = MIN_COLOR
) {
    protected var mTempColor: Int = -1

    protected var colorChanged: Boolean = false

    abstract fun getOpacity(): Float

    abstract fun toColor(): Int

    abstract fun reset()

    protected fun clamp(color: Int): Int {
        return when {
            color > MAX_COLOR -> MAX_COLOR
            color < MIN_COLOR -> MIN_COLOR
            else -> color
        }
    }

    companion object {

        fun mutate(): MutableColor {
            return MutableColor()
        }

        fun colorDecToHex(r: Int, g: Int, b: Int): Int {
            return AndroidColor.parseColor(colorDecToHexString(r, g, b))
        }

        fun colorDecToHex(a: Int, r: Int, g: Int, b: Int): Int {
            return AndroidColor.parseColor(colorDecToHexString(a, r, g, b))
        }

        fun colorDecToHexString(r: Int, g: Int, b: Int): String {
            return colorDecToHexString(MAX_COLOR, r, g, b)
        }

        fun colorDecToHexString(a: Int, r: Int, g: Int, b: Int): String {
            var red = Integer.toHexString(r)
            var green = Integer.toHexString(g)
            var blue = Integer.toHexString(b)
            var alpha = Integer.toHexString(a)

            if (red.length == 1) {
                red = "0$red"
            }
            if (green.length == 1) {
                green = "0$green"
            }
            if (blue.length == 1) {
                blue = "0$blue"
            }
            if (alpha.length == 1) {
                alpha = "0$alpha"
            }

            return "#$alpha$red$green$blue"
        }
    }
}
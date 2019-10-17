package com.eudycontreras.motionmorpherlibrary.properties

import android.view.View
import android.view.ViewGroup
import com.eudycontreras.motionmorpherlibrary.globals.MIN_OFFSET

/**
 * Class that holds information about the margins
 * on all four sides of a view. These are defined as
 * start, end, top and bottom.
 *
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

class Margings(
    private var view: View? = null,
    start: Float = MIN_OFFSET,
    end: Float = MIN_OFFSET,
    top: Float = MIN_OFFSET,
    bottom: Float = MIN_OFFSET
) {

    val marginParams: ViewGroup.MarginLayoutParams? by lazy {
        view?.let {
            it.layoutParams as ViewGroup.MarginLayoutParams
        }
    }

    var start: Float = start
        set(value) {
            field = value
            marginParams?.marginStart = value.toInt()
        }
    var end: Float = end
        set(value) {
            field = value
            marginParams?.marginEnd = value.toInt()
        }
    var top: Float = top
        set(value) {
            field = value
            marginParams?.topMargin = value.toInt()
        }
    var bottom: Float = bottom
        set(value) {
            field = value
            marginParams?.bottomMargin = value.toInt()
        }

    fun getCopy(): Margings {
        return Margings(
            null,
            start,
            end,
            top,
            bottom
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Margings) return false

        if (start != other.start) return false
        if (end != other.end) return false
        if (top != other.top) return false
        if (bottom != other.bottom) return false

        return true
    }

    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + end.hashCode()
        result = 31 * result + top.hashCode()
        result = 31 * result + bottom.hashCode()
        return result
    }


}



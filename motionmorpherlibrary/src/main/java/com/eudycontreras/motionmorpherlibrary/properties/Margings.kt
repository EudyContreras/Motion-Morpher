package com.eudycontreras.motionmorpherlibrary.properties

import android.view.View
import android.view.ViewGroup

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

class Margings(
    private var view: View? = null,
    start: Float = 0f,
    end: Float = 0f,
    top: Float = 0f,
    bottom: Float = 0f
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



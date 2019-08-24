package com.eudycontreras.motionmorpherlibrary.properties

import android.view.View
import androidx.core.view.updatePadding
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

class Paddings(
    private var view: View? = null,
    start: Float = MIN_OFFSET,
    end: Float = MIN_OFFSET,
    top: Float = MIN_OFFSET,
    bottom: Float = MIN_OFFSET
) {

    var start: Float = start
        set(value) {
            field = value
            view?.updatePadding(
                left = value.toInt()
            )
        }
    var end: Float = end
        set(value) {
            field = value
            view?.updatePadding(
                right = value.toInt()
            )
        }
    var top: Float = top
        set(value) {
            field = value
            view?.updatePadding(
                top = value.toInt()
            )
        }
    var bottom: Float = bottom
        set(value) {
            field = value
            view?.updatePadding(
                bottom = value.toInt()
            )
        }

    fun getCopy(): Paddings {
        return Paddings(
            null,
            start,
            end,
            top,
            bottom
        )
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Paddings) return false

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


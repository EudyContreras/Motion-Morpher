package com.eudycontreras.motionmorpherlibrary.interfaces

import android.graphics.Canvas
import android.graphics.Path
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET

@FunctionalInterface interface Clipable {

    fun clipChildren(clipPath: Path, canvas: Canvas, corners: FloatArray, width: Float, height: Float) {
        val count = canvas.saveCount

        val top = MIN_OFFSET
        val left = MIN_OFFSET
        val bottom = top + height
        val right = left + width

        clipPath.rewind()
        clipPath.addRoundRect(left, top, right, bottom, corners, Path.Direction.CCW)
        clipPath.close()

        canvas.clipPath(clipPath)

        canvas.restoreToCount(count)
    }
}
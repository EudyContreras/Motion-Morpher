package com.eudycontreras.motionmorpherlibrary.interfaces

import android.graphics.Canvas
import android.graphics.Path

@FunctionalInterface interface Clipable {

    fun clipChildren(clipPath: Path, canvas: Canvas, corners: FloatArray, width: Float, height: Float) {
        val count = canvas.saveCount

        val top = 0f
        val left = 0f
        val bottom = top + height
        val right = left + width

        clipPath.rewind()
        clipPath.addRoundRect(left, top, right, bottom, corners, Path.Direction.CCW)
        clipPath.close()

        canvas.clipPath(clipPath)

        canvas.restoreToCount(count)
    }
}
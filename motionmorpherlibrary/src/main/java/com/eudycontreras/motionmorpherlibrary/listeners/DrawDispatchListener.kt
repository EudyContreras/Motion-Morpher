package com.eudycontreras.motionmorpherlibrary.listeners

import android.graphics.Canvas

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

interface DrawDispatchListener {
    fun onDrawDispatched(canvas: Canvas)
    fun onDraw(canvas: Canvas)
}

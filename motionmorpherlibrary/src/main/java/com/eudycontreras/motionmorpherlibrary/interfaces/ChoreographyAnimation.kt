package com.eudycontreras.motionmorpherlibrary.interfaces

import android.graphics.Bitmap
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 28 2019
 */
 
interface ChoreographyAnimation {
    fun withRipple()
    fun toVisibility(visibility: Int)
    fun fromVisibility(visibilityFrom: Int, visibilityTo: Int)
    fun animateFramesFor(layout: MorphLayout, frames: Array<Bitmap>)
}
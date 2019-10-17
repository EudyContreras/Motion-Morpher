package com.eudycontreras.motionmorpherlibrary.interpolators

import android.animation.TimeInterpolator
import com.eudycontreras.motionmorpherlibrary.globals.MAX_OFFSET
import com.eudycontreras.motionmorpherlibrary.globals.MIN_OFFSET

/**
 * Copyright 2019 The Android Open Source Project.
 */
class SharedInterpolator(
    val base: TimeInterpolator,
    val start: Float = MIN_OFFSET,
    val end: Float = MAX_OFFSET
) : TimeInterpolator {

    private val offset = base.getInterpolation(start)
    private val xRatio = (end - start) / MAX_OFFSET
    private val yRatio = (base.getInterpolation(end) - offset) / MAX_OFFSET

    override fun getInterpolation(input: Float): Float {
        return (base.getInterpolation(start + (input * xRatio)) - offset) / yRatio
    }
}
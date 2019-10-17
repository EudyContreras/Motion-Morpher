package com.eudycontreras.motionmorpherlibrary.interpolators

import android.animation.TimeInterpolator
import androidx.core.view.animation.PathInterpolatorCompat

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since September 08 2019
 */

object Easing {

    /**
     * Standard easing.
     *
     * Elements that begin and end at rest use standard easing. They speed up quickly and slow down
     * gradually, in order to emphasize the end of the transition.
     *
     * Copyright 2019 The Android Open Source Project.
     */
    val STANDARD: TimeInterpolator by lazy(LazyThreadSafetyMode.NONE) {
        PathInterpolatorCompat.create(0.4f, 0f, 0.2f, 1f)
    }

    /**
     * Decelerate easing.
     *
     * Incoming elements are animated using deceleration easing, which starts a transition at peak
     * velocity (the fastest point of an element’s movement) and ends at rest.
     *
     * Copyright 2019 The Android Open Source Project.
     */
    val INCOMING: TimeInterpolator by lazy(LazyThreadSafetyMode.NONE) {
        PathInterpolatorCompat.create(0f, 0f, 0.2f, 1f)
    }

    /**
     * Accelerate easing.
     *
     * Elements exiting a screen use acceleration easing, where they start at rest and end at peak
     * velocity.
     *
     * Copyright 2019 The Android Open Source Project.
     */
    val OUTGOING: TimeInterpolator by lazy(LazyThreadSafetyMode.NONE) {
        PathInterpolatorCompat.create(0.4f, 0f, 1f, 1f)
    }
}
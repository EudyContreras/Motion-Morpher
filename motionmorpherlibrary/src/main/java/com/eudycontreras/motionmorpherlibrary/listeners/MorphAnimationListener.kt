package com.eudycontreras.motionmorpherlibrary.listeners

import android.animation.Animator
import android.animation.AnimatorListenerAdapter

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

class MorphAnimationListener(
    var onStart: (() -> Unit)? = null,
    var onEnd: (() -> Unit)? = null
) : AnimatorListenerAdapter() {

    override fun onAnimationStart(animation: Animator?) {
        super.onAnimationStart(animation)
        onStart?.invoke()
    }

    override fun onAnimationEnd(animation: Animator?) {
        super.onAnimationEnd(animation)
        onEnd?.invoke()
    }
}

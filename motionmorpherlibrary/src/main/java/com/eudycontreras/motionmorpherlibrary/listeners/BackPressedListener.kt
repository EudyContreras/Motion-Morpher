package com.eudycontreras.motionmorpherlibrary.listeners

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

interface BackPressedListener {
    fun onBackPressed()
    fun disallowExit(): Boolean
}
package com.eudycontreras.motionmorpherlibrary.layouts

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 19 2019
 */

interface MorphContainer {

    fun getStartState(): MorphLayout
    fun getEndState(): MorphLayout
}
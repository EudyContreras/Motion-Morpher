package com.eudycontreras.motionmorpherlibrary.listeners

interface BackPressedListener {
    fun onBackPressed()
    fun disallowExit(): Boolean
}
package com.eudycontreras.motionmorpherlibrary.activities

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.eudycontreras.motionmorpherlibrary.listeners.BackPressedListener


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 18 2019
 */
 
 
abstract class MorphActivity: AppCompatActivity(){

    private val backNavigationListeners = ArrayList<BackPressedListener>()

    abstract fun getRoot(): ViewGroup

    override fun onBackPressed() {
        backNavigationListeners.forEach {
            it.onBackPressed()
        }

        if (!backNavigationListeners.any { it.disallowExit() }) {
            super.onBackPressed()
        }
    }

    fun addBackPressListeners(listener: BackPressedListener) {
        backNavigationListeners.add(listener)
    }

    fun removeBackPressListeners(listener: BackPressedListener) {
        backNavigationListeners.remove(listener)
    }

}
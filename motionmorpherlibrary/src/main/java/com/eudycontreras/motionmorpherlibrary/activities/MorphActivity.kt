package com.eudycontreras.motionmorpherlibrary.activities

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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

    fun openFragment(fragment: Fragment) {
        val prev = supportFragmentManager.findFragmentByTag(fragment::class.java.simpleName)

        val fragmentTransaction = supportFragmentManager.beginTransaction()

        if (supportFragmentManager.fragments.contains(fragment) || prev != null) {
            fragmentTransaction.remove(prev!!)
        }

        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.show(fragment)
    }
}
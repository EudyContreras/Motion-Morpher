package com.eudycontreras.motionmorpherlibrary.activities

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 18 2019
 */
 
 
abstract class MorphActivity: AppCompatActivity(){
    abstract fun getRoot(): ViewGroup
}
package com.eudycontreras.motionmorpher.examples.demo1

import android.view.View
import androidx.appcompat.widget.Toolbar
import com.eudycontreras.motionmorpher.R
import com.eudycontreras.motionmorpherlibrary.activities.MorphDialog


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 19 2019
 */
 
 
class DetailsDemo1(
    private val activityDemo1: ActivityDemo1,
    private val dialog: MorphDialog,
    private val layout: View) {

    private lateinit var toolbar: Toolbar

    init {
        initialize(layout)
    }

    private fun initialize(layout: View) {
        toolbar = layout.findViewById(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
            activityDemo1.morpher.morphFrom(onEnd = { dialog.dismiss() })
        }

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.more -> {

                    true
                }
                else -> {
                    false
                }
            }
        }
    }
}
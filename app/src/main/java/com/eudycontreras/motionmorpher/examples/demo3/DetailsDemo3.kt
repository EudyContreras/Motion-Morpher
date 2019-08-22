package com.eudycontreras.motionmorpher.examples.demo3

import android.view.View
import androidx.appcompat.widget.Toolbar
import com.eudycontreras.motionmorpher.R
import com.eudycontreras.motionmorpher.examples.demo3.ActivityDemo3
import com.eudycontreras.motionmorpherlibrary.activities.MorphDialog


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 19 2019
 */
 
 
class DetailsDemo3(
    private val activityDemo: ActivityDemo3,
    private val dialog: MorphDialog) {

    private lateinit var toolbar: Toolbar

    init {
        initialize(dialog.morphView)
    }

    private fun initialize(layout: View) {
        toolbar = layout.findViewById(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
            dialog.requestDismiss()
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
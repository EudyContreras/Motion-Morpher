package com.eudycontreras.motionmorpher.examples.demo2

import android.view.View
import androidx.appcompat.widget.Toolbar
import com.eudycontreras.motionmorpher.R
import com.eudycontreras.motionmorpherlibrary.activities.MorphDialog


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 19 2019
 */
 
 
class DetailsDemo2(
    private val activityDemo: ActivityDemo2,
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
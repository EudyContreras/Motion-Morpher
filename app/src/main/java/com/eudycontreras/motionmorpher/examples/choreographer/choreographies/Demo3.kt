package com.eudycontreras.motionmorpher.examples.choreographer.choreographies

import android.animation.TimeInterpolator
import android.app.Activity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.eudycontreras.motionmorpher.R
import com.eudycontreras.motionmorpherlibrary.Choreographer
import com.eudycontreras.motionmorpherlibrary.activities.MorphActivity
import com.eudycontreras.motionmorpherlibrary.enumerations.*
import com.eudycontreras.motionmorpherlibrary.extensions.dp
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.layouts.MorphView
import com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts.ConstraintLayout
import com.eudycontreras.motionmorpherlibrary.properties.*
import kotlinx.android.synthetic.main.activity_demo0.*
import kotlinx.android.synthetic.main.activity_demo0_card.view.*


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since September 11 2019
 */

class Demo3(var activity: MorphActivity) {

    lateinit var actions: MorphLayout
    lateinit var icon1: MorphLayout
    lateinit var icon2: MorphLayout
    lateinit var icon3: MorphLayout
    lateinit var image: View
    lateinit var text: View

    init {
        val card = activity.cardLayout as ConstraintLayout

        if (!::actions.isInitialized) {
            actions = MorphView.makeMorphable(card.demo_0_actions)
        }
        if (!::image.isInitialized) {
            image = card.demo_0_image
        }
        if (!::text.isInitialized) {
            text = card.demo_0_header
        }
        if (!::icon1.isInitialized) {
            icon1 = MorphView.makeMorphable(card.demo_0_favorite)
        }
        if (!::icon2.isInitialized) {
            icon2 = MorphView.makeMorphable(card.demo_0_book)
        }
        if (!::icon3.isInitialized) {
            icon3 = MorphView.makeMorphable(card.demo_0_share)
        }
    }

    fun create(card: View, root: MorphLayout, interpolator: TimeInterpolator): Choreographer {
        val choreographer = Choreographer(activity)

        choreographer
            .allowChildInheritance(false)
            .withDefaultPivot(0.5f, 0f)
            .withDefaultDuration(800)
            .withDefaultInterpolator(interpolator)

            .animate(card){

            }

            .build()

        return choreographer
    }
}
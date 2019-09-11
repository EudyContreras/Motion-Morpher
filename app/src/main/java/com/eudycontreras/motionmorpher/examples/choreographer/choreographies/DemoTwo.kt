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
 
 
class DemoTwo(var activity: MorphActivity) {

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
                val textMorph = TextMorph(card.demo_0_header,"Deadpool 2", textSizeTo = 16f)
                val imageMorph = BitmapMorph(card.demo_0_image, R.drawable.background13, R.drawable.background2, FadeType.FADETHROUGH)

                withDuration(550)
                withTextChange(textMorph)
                withImageChange(imageMorph)
            }
            .then(card) {
                xRotateAdd(30f)
            }
            .then {
                xRotateAdd(-60f)
            }
            .then {
                xRotateAdd(30f)
            }
            .thenChildrenOf(card.demo_0_actions) {
                withDuration(1000)
                withPivot(0.5f, 0.5f)
                withInterpolator(DecelerateInterpolator())
                withStagger(AnimationStagger(0.3f, type = Stagger.LINEAR))
                rotateFrom(0f, 360f)
            }
            .then(card) {
                withDuration(2000)
                withInterpolator(AccelerateDecelerateInterpolator())
                xRotateBetween(0f, -40f, 40f, -40f, 40f, 0f)
                rotateBetween(0f, -20f, 20f, -30f, 30f, -40f, 40f, -30f, 30f, -20f, 20f, 0f)
            }
            .then(0.9f) {
                withPivot(0.5f, 0.5f)
                withDuration(1000)
                xScaleBetween(1f,  0.4f, 1f)
                yScaleBetween(1f, 1.2f, 1f)
                yRotateAdd(360f * 3)
            }
            .then(0.9f) {
                anchorTo(Anchor.LEFT, root)
            }
            .then {
                yTranslateBetween(0f, -(10.dp), (10.dp), -(15.dp), (15.dp), -(25.dp), (25.dp), -(20.dp), (20.dp), -(10.dp), (10.dp), 0f)
                withDuration(500)
            }
            .then(0.3f) {
                withDuration(600)
                withReveal(Reveal(card.demo_0_image_2, 0.5f, 0.5f, 0f))
                anchorTo(Anchor.RIGHT, root, 30.dp)
                rotateTo(35f)
            }
            .then(0.6f) {
                withDuration(2000)
                withInterpolator(AccelerateDecelerateInterpolator())
                rotateBetween(35f * 0.6f, -30f, 25f, -20f, 15f, -10f, 8f, -6f, 4f, -2f, 0f)
            }
            .then {
                yTranslateBetween(0f, -(10.dp), (10.dp), -(15.dp), (15.dp), -(25.dp), (25.dp), -(20.dp), (20.dp), -(10.dp), (10.dp), 0f)
                withDuration(500)
            }
            .then(0.3f) {
                val concel = Conceal(card.demo_0_image_2, 0.5f, 0.5f, 0f)

                withDuration(400)
                withInterpolator(AnticipateOvershootInterpolator())
                withConceal(concel)
                anchorTo(Anchor.LEFT, root, 30.dp)
                rotateFrom(0f, -35f)
            }
            .then(0.6f) {
                withDuration(3000)
                withInterpolator(AccelerateDecelerateInterpolator())
                rotateBetween( -35f * 0.6f, 25f, -20f, 15f, -10f, 8f, -6f, 4f, -2f, 0f)
            }
            .then(0.6f) {
                val textMorph = TextMorph(card.demo_0_header,"Deadpool", textSizeTo = 16f)
                val imageMorph = BitmapMorph(card.demo_0_image, R.drawable.background2, R.drawable.background13, FadeType.CROSSFADE)

                withDuration(500)
                withTextChange(textMorph)
                withImageChange(imageMorph)
                withInterpolator(OvershootInterpolator())
                anchorTo(Anchor.CENTER, root)
            }
            .build()

        return choreographer
    }
}
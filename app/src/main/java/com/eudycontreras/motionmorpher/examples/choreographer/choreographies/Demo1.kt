package com.eudycontreras.motionmorpher.examples.choreographer.choreographies

import android.animation.TimeInterpolator
import android.app.Activity
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.eudycontreras.motionmorpherlibrary.Choreographer
import com.eudycontreras.motionmorpherlibrary.activities.MorphActivity
import com.eudycontreras.motionmorpherlibrary.enumerations.Anchor
import com.eudycontreras.motionmorpherlibrary.enumerations.Corner
import com.eudycontreras.motionmorpherlibrary.enumerations.Measurement
import com.eudycontreras.motionmorpherlibrary.extensions.dp
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.layouts.MorphView
import com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts.ConstraintLayout
import com.eudycontreras.motionmorpherlibrary.properties.Stretch
import kotlinx.android.synthetic.main.activity_demo0.*
import kotlinx.android.synthetic.main.activity_demo0_card.view.*


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since September 11 2019
 */
 
 
class Demo1(var activity: MorphActivity) {

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
            .withDefaultInterpolator(interpolator)
            .withDefaultDuration(300)

            .animate(card) {
                withPivot(0.5f, 0.5f)
                rotateTo(360f)
                withDuration(1000)
            }
            .then {
                xTranslateBetween(0f, -(30.dp), (30.dp), -(70.dp), (70.dp), -(100.dp), (100.dp), -(70.dp), (70.dp), -(30.dp), (30.dp), 0f)
                withDuration(500)
            }
            .then {
                withStartDelay(100)
                yTranslateBetween(0f, -(30.dp), (30.dp), -(70.dp), (70.dp), -(100.dp), (100.dp), -(70.dp), (70.dp), -(30.dp), (30.dp), 0f)
                withDuration(500)
            }
            .then {
                withInterpolator(DecelerateInterpolator())
                cornerRadiusTo(Corner.ALL, 30.dp)
                zTranslateTo(16.dp)
                withDuration(1000)
            }
            .and(image){
                withInterpolator(DecelerateInterpolator())
                cornerRadiusTo(Corner.TOP_LEFT and Corner.TOP_RIGHT, 30.dp)
                withDuration(1000)
            }
            .then(card) {
                resizeTo(root.viewBounds)
                zTranslateTo(8.dp)
                cornerRadiusTo(root.morphCornerRadii)
                withDuration(1000)
            }
            .and(image) {
                withEvenRatio()
                cornerRadiusTo(Corner.TOP_LEFT and Corner.TOP_RIGHT, 0.dp)
                resizeTo(Measurement.WIDTH, root.viewBounds)
                withDuration(1000)
            }
            .and(text) {
                resizeTo(Measurement.WIDTH, root.viewBounds)
                withDuration(1000)
            }
            .and(icon1, icon2, icon3) {
                scaleTo(0f)
                withDuration(1000)
            }
            .thenReversedWith(card, text, image, icon1, icon2, icon3) {
                withDuration(1000)
            }
            .then(card) {
                withDuration(2000      )
                withInterpolator(FastOutSlowInInterpolator())
                withStretch(Stretch(0.5f, 0.5f,0.2f))
                anchorTo(Anchor.BOTTOM, root)
            }
            .then {
                withDuration(2000)
                withInterpolator(FastOutSlowInInterpolator())
                withStretch(Stretch(0.5f, 0.15f, 0.2f, 0f, 1f))
                anchorTo(Anchor.TOP, root, 20.dp)
            }
            .then {
                withDuration(500)
                anchorTo(Anchor.CENTER, root)
            }
            .then {
                withDuration(500)
                anchorTo(Anchor.TOP_LEFT, root, 20.dp)
            }
            .then {
                withDuration(500)
                anchorTo(Anchor.RIGHT, root, 20.dp)
            }
            .then {
                withDuration(500)
                anchorTo(Anchor.BOTTOM, root, 20.dp)
            }
            .then {
                withDuration(500)
                anchorTo(Anchor.LEFT, root, 20.dp)
            }
            .then {
                withDuration(500)
                anchorArcTo(Anchor.TOP_RIGHT, root, 20.dp)
            }
            .then {
                withDuration(500)
                anchorTo(Anchor.CENTER, root)
            }
            .then {
                rotateTo(0f)
                withDuration(1000)
            }
            .then {
                withPivot(0.5f, 0f)
                yScaleTo(0.1f)
                withDuration(2000)
            }
            .then {
                withPivot(0.5f, 0f)
                withInterpolator(AnticipateOvershootInterpolator())
                zTranslateTo(30.dp)
                scaleTo(1.2f)
                withDuration(2000)
            }
            .then {
                withInterpolator(AnticipateOvershootInterpolator())
                withPivot(0.5f, 0f)
                zTranslateTo(0f)
                alphaTo(0f)
                scaleTo(1f)
                withDuration(1000)
            }
            .then {
                zTranslateTo(10.dp)
                alphaTo(1f)
                withDuration(1000)
            }
            .thenWith(card, image) {
                withInterpolator(DecelerateInterpolator())
                withDuration(1000)
                cornerRadiusTo(Corner.ALL, 6.dp)
            }
            .build()
        return choreographer
    }
}
package com.eudycontreras.motionmorpher.examples.choreographer.choreographies

import android.animation.TimeInterpolator
import android.app.Activity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.eudycontreras.motionmorpherlibrary.Choreographer
import com.eudycontreras.motionmorpherlibrary.activities.MorphActivity
import com.eudycontreras.motionmorpherlibrary.dpValues
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
            .withDefaultDuration(500)

            .animate(card) {
                withDuration(1000)
                withPivot(0.5f, 0.5f)
                rotateTo(360f)
            }
            .then {
                xTranslateBetween(*dpValues(0, -30, 30, -70, 70, -100, 100, -70, 70, -30, 30, 0))
            }
            .then {
                withStartDelay(100)
                yTranslateBetween(*dpValues(0, -30, 30, -70, 70, -100, 100, -70, 70, -30, 30, 0))
            }
            .thenTogether {
                with(image) {
                    withDuration(1500)
                    withInterpolator(AccelerateDecelerateInterpolator())
                    xScaleBetween(*dpValues(1, 0.3, 1))
                }
                with(actions) {
                    withDuration(1500)
                    withInterpolator(AccelerateDecelerateInterpolator())
                    xScaleBetween(*dpValues(1, 0.3, 1))
                }
                with(text) {
                    withDuration(1500)
                    withPivot(0.5f, 1f)
                    withInterpolator(AccelerateDecelerateInterpolator())
                    yScaleBetween(*dpValues(1, 1.5, 1))
                }
            }
            .then {
                withInterpolator(DecelerateInterpolator())
                withDuration(1000)
                cornerRadiusTo(Corner.ALL, 30.dp)
                zTranslateTo(16.dp)
            }
            .and(image){
                withInterpolator(DecelerateInterpolator())
                withDuration(1000)
                cornerRadiusTo(Corner.TOP_LEFT and Corner.TOP_RIGHT, 30.dp)
            }
            .then(card) {
                withDuration(1000)
                resizeTo(root.viewBounds)
                zTranslateTo(8.dp)
                cornerRadiusTo(root.morphCornerRadii)
            }
            .and(image) {
                withDuration(1000)
                withEvenRatio()
                cornerRadiusTo(Corner.TOP_LEFT and Corner.TOP_RIGHT, 0.dp)
                resizeTo(Measurement.WIDTH, root.viewBounds)
            }
            .and(text) {
                withDuration(1000)
                resizeTo(Measurement.WIDTH, root.viewBounds)
            }
            .and(icon1, icon2, icon3) {
                withDuration(1000)
                scaleTo(0f)
            }
            .thenReversedWith(card, text, image, icon1, icon2, icon3) {
                withDuration(1000)
            }
            .then(0.1f, card) {
                withDuration(1500)
                withStretch(Stretch(0.5f, 0.5f,0.2f))
                withInterpolator(FastOutSlowInInterpolator())
                anchorTo(Anchor.BOTTOM, root)
            }
            .then {
                withDuration(1500)
                withStretch(Stretch(0.5f, 0.15f, 0.2f, 0f, 1f))
                withInterpolator(FastOutSlowInInterpolator())
                anchorTo(Anchor.TOP, root)
            }
            .then {
                anchorTo(Anchor.CENTER, root)
            }
            .then {
                anchorTo(Anchor.TOP_LEFT, root, 20.dp)
            }
            .then {
                anchorTo(Anchor.RIGHT, root, 20.dp)
            }
            .then {
                anchorTo(Anchor.BOTTOM, root, 20.dp)
            }
            .then {
                anchorTo(Anchor.LEFT, root, 20.dp)
            }
            .then {
                anchorArcTo(Anchor.TOP_RIGHT, root, 20.dp)
            }
            .then {
                addRotation(-360f)
                anchorTo(Anchor.CENTER, root)
            }
            .then {
                withDuration(1000)
                addRotation(360f)
            }
            .then {
                withDuration(2000)
                withPivot(0.5f, 0f)
                yScaleTo(0.1f)
            }
            .then {
                withDuration(2000)
                withPivot(0.5f, 0f)
                withInterpolator(AnticipateOvershootInterpolator())
                zTranslateTo(30.dp)
                scaleTo(1.2f)
            }
            .then {
                withDuration(1000)
                withPivot(0.5f, 0f)
                withInterpolator(AnticipateOvershootInterpolator())
                zTranslateTo(0f)
                alphaTo(0f)
                scaleTo(1f)
            }
            .then {
                withDuration(7000)
                zTranslateTo(10.dp)
                alphaTo(1f)
            }
            .andWith(card, image) {
                withDeepInheritance(true)
                withInterpolator(DecelerateInterpolator())
                cornerRadiusTo(Corner.ALL, 6.dp)
            }
            .build()
        return choreographer
    }
}
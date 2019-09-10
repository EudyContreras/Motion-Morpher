package com.eudycontreras.motionmorpher.examples.choreographer

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.widget.TextView
import androidx.core.animation.addListener
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.eudycontreras.motionmorpher.R
import com.eudycontreras.motionmorpherlibrary.*
import com.eudycontreras.motionmorpherlibrary.activities.MorphActivity
import com.eudycontreras.motionmorpherlibrary.enumerations.*
import com.eudycontreras.motionmorpherlibrary.extensions.clamp
import com.eudycontreras.motionmorpherlibrary.extensions.dp
import com.eudycontreras.motionmorpherlibrary.interpolators.Easing
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.layouts.MorphView
import com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts.ConstraintLayout
import com.eudycontreras.motionmorpherlibrary.properties.*
import kotlinx.android.synthetic.main.activity_demo0.*
import kotlinx.android.synthetic.main.activity_demo0_card.view.*

class ActivityDemo0 : MorphActivity() {

    lateinit var actions: MorphLayout
    lateinit var image: View
    lateinit var image2: View
    lateinit var text: View
    lateinit var icon1: MorphLayout
    lateinit var icon2: MorphLayout
    lateinit var icon3: MorphLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo0)

        val card = cardLayout as ConstraintLayout


        val root = MorphView.makeMorphable(getRoot())
        val interpolator = FastOutSlowInInterpolator()

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

        image2 = card.demo_0_image_2

        var chor1: Choreographer? = null

        cardLayout.post {
            chor1 = createChoreographyOne(cardLayout as ConstraintLayout, root, FastOutSlowInInterpolator())
        }
        cardLayout.setOnClickListener {
           /* val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.setDuration(10000)
            animator.setInterpolator(LinearInterpolator())
            animator.addUpdateListener {
                val fraction = (it.animatedValue as Float).clamp(MIN_OFFSET, MAX_OFFSET)
                chor1?.transitionTo(fraction)
            }
            animator.addListener(
                onStart = { chor1?.transitionTo(MIN_OFFSET) },
                onEnd = { chor1?.transitionTo(MAX_OFFSET) }
            )
            animator.start()*/
            chor1?.play()
        }
    }

    override fun getRoot(): ViewGroup {
        return this.findViewById(R.id.root)
    }

    fun createChoreographyOne(card: View, root: MorphLayout, interpolator: TimeInterpolator): Choreographer {

        val choreographer = Choreographer(this)

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
                resizeTo(root.viewBounds)
                cornerRadiusTo(root.morphCornerRadii)
                withDuration(1000)
            }
            .and(image) {
                cornerRadiusTo(Corner.TOP_LEFT and Corner.TOP_RIGHT, 0.dp)
                resizeTo(root.viewBounds)
                withDuration(1000)
            }
            .and(icon1, icon2, icon3) {
                scaleTo(0f)
                withDuration(1000)
            }
            .thenReversedWith(card, image, icon1, icon2, icon3) {
                withDuration(8000)
            }
            .then(card) {
                withDuration(2000      )
                withInterpolator(FastOutSlowInInterpolator())
                withStretch(Stretch(0.5f, 0.5f,0.2f))
                anchorTo(Anchor.BOTTOM, root)
            }
            .then {
                withDuration(2600)
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
            .build()

        return choreographer
    }

    fun createChoreographyTwo(card: View, root: MorphLayout, interpolator: TimeInterpolator): Choreographer {
        val choreographer = Choreographer(this)

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
                //resizeBy(Measurement.BOTH, 1.6f, Easing.STANDARD)
            }
            /*.andAnimate(card.demo_0_image) {
                withDuration(550)
                resizeBy(Measurement.BOTH, 1.6f, Easing.STANDARD)
            }
            .andAnimate(card.demo_0_header){
                withDuration(550)
                resizeBy(Measurement.BOTH, 1.6f, Easing.STANDARD)
            }*/
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

    fun createChoreographyThree(card: View, root: MorphLayout, interpolator: TimeInterpolator): Choreographer {
        val choreographer = Choreographer(this)

        choreographer
            .withDefaultPivot(0.5f, 0f)
            .allowChildInheritance(false)
            .withDefaultInterpolator(interpolator)
            .withDefaultDuration(800)

            .animate(card) {
                xRotateAdd(30f)
            }
            .then {
                xRotateAdd(-60f)
            }
            .then {
                xRotateAdd(30f)
            }
            .then {
                withDuration(2400)
                xRotateBetween(0f, -40f, 40f, -40f, 40f, 0f)
                rotateBetween(0f, -20f, 20f, -30f, 30f, -40f, 40f, -30f, 30f, -20f, 20f, 0f)
            }
            .then{
                withDuration(1000)
                xScaleBetween(1f,  0.6f, 0.7f, 0.8f, 0.9f, 1f)
                yRotateAdd(360f * 2)
            }
            .then {
                anchorTo(Anchor.LEFT, root)
            }
            .then {
                withDuration(800)
                withInterpolator(AnticipateOvershootInterpolator())
                withReveal(Reveal(image2, 1f, 0.5f, 0f))
                anchorTo(Anchor.RIGHT, root)
                rotateTo(35f)
            }
            .then(0.60f){
                withDuration(2000)
                withInterpolator(AccelerateDecelerateInterpolator())
                rotateBetween(35f * 0.6f, -30f, 25f, -20f, 15f, -10f, 8f, -6f, 4f, -2f, 0f)
            }

            .then {
                withDuration(500)
                withInterpolator(AnticipateOvershootInterpolator())
                withConceal(Conceal(image2, 1f, 0.5f, 0f))
                anchorTo(Anchor.LEFT, root)
                rotateFrom(0f, -35f)
            }
            .then(0.60f) {
                withDuration(3000)
                withInterpolator(AccelerateDecelerateInterpolator())
                rotateBetween( -35f * 0.6f, 25f, -20f, 15f, -10f, 8f, -6f, 4f, -2f, 0f)
            }
            .then{
                val textMorph = TextMorph(card.demo_0_header,"Deadpool", textSizeTo = 16f)
                val imageMorph = BitmapMorph(card.demo_0_image, R.drawable.background13, FadeType.CROSSFADE)

                withDuration(500)
                withTextChange(textMorph)
                withImageChange(imageMorph)
                anchorTo(Anchor.CENTER, root)
            }

            .build()

        return choreographer
    }
}

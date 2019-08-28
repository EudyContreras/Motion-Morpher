package com.eudycontreras.motionmorpher.examples.demo2

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.children
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.eudycontreras.motionmorpher.R
import com.eudycontreras.motionmorpherlibrary.Choreographer
import com.eudycontreras.motionmorpherlibrary.activities.MorphActivity
import com.eudycontreras.motionmorpherlibrary.activities.MorphDialog
import com.eudycontreras.motionmorpherlibrary.enumerations.Anchor
import com.eudycontreras.motionmorpherlibrary.enumerations.Interpolation
import com.eudycontreras.motionmorpherlibrary.interactions.Explode
import com.eudycontreras.motionmorpherlibrary.interpolators.MaterialInterpolator
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.layouts.MorphView
import com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts.ConstraintLayout
import com.eudycontreras.motionmorpherlibrary.properties.Stretch
import com.eudycontreras.motionmorpherlibrary.utilities.binding.Bind
import com.eudycontreras.motionmorpherlibrary.utilities.binding.Binder
import kotlinx.android.synthetic.main.activity_demo2.*
import kotlinx.android.synthetic.main.activity_demo2_card.view.*

class ActivityDemo2 : MorphActivity() {

    lateinit var choreographer: Choreographer

    lateinit var actions: MorphLayout
    lateinit var image: MorphLayout
    lateinit var text: MorphLayout
    lateinit var icon1: MorphLayout
    lateinit var icon2: MorphLayout
    lateinit var icon3: MorphLayout

    var value: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo2)

        choreographer = Choreographer(this)

        /*cardLayout.setOnClickListener {
            createChoreographyOne()
        }*/

        testMorphing()
    }

    override fun getRoot(): ViewGroup {
        return this.findViewById(R.id.root)
    }

    fun testMorphing() {

        val interpolator = MaterialInterpolator(Interpolation.FAST_OUT_SLOW_IN)

        choreographer = Choreographer(this)

        choreographer.morpher.morphIntoDuration = 1450
        choreographer.morpher.morphFromDuration = 1350

        choreographer.morpher.animateChildren = true
        choreographer.morpher.useArcTranslator = false

        choreographer.morpher.morphIntoInterpolator = interpolator
        choreographer.morpher.morphFromInterpolator = interpolator

        choreographer.morpher.dimPropertyInto.interpolateOffsetStart = 0f
        choreographer.morpher.dimPropertyInto.interpolateOffsetEnd = 0.7f

        choreographer.morpher.dimPropertyFrom.interpolateOffsetStart = 0f
        choreographer.morpher.dimPropertyFrom.interpolateOffsetEnd = 0.6f

        choreographer.morpher.containerChildStateIn.duration = 1300
        choreographer.morpher.containerChildStateIn.animateOnOffset = 0f
        choreographer.morpher.containerChildStateIn.defaultTranslateMultiplierX = 0.02f
        choreographer.morpher.containerChildStateIn.defaultTranslateMultiplierY = 0.02f
        choreographer.morpher.containerChildStateIn.stagger?.staggerOffset = 0.12f
        choreographer.morpher.containerChildStateIn.interpolator = FastOutSlowInInterpolator()

        choreographer.morpher.siblingInteraction = Explode(Explode.Type.TIGHT, 0.7f).apply {
            outInterpolator = interpolator
            inInterpolator = interpolator
            //animationStaggerOut = AnimationStagger(0f, type = Stagger.LINEAR)
            //animationStaggerIn = AnimationStagger(0f, type = Stagger.LINEAR)
            //stretch = Stretch(1f, 0.1f)
        }

        val dialog = MorphDialog.instance(this, choreographer.morpher, R.layout.activity_demo2_details, R.style.AppTheme_Dialog)
        dialog.addCreateListener {
            DetailsDemo2(this, dialog)
        }

        dialog.addDismissRequestListener {
            choreographer.morpher.morphFrom(
                onEnd = { super.onBackPressed() },
                onStart = {}
            )
        }

        grid.children.forEach { child ->
            child.setOnClickListener { start ->
                choreographer.morpher.startView = child as MorphLayout

                dialog.show {
                    choreographer.morpher.morphInto()
                }
            }
        }
    }

    fun testChoreographer() {
        val card: MorphLayout = cardLayout as ConstraintLayout

        if (!::actions.isInitialized) {
            actions = MorphView.makeMorphable(cardLayout.demo_2_actions)
        }
        if (!::image.isInitialized) {
            image = MorphView.makeMorphable(cardLayout.demo_2_image)
        }
        if (!::text.isInitialized) {
            text = MorphView.makeMorphable(cardLayout.demo_2_header)
        }
        if (!::icon1.isInitialized) {
            icon1 = MorphView.makeMorphable(cardLayout.demo_2_favorite)
        }
        if (!::icon2.isInitialized) {
            icon2 = MorphView.makeMorphable(cardLayout.demo_2_book)
        }
        if (!::icon3.isInitialized) {
            icon3 = MorphView.makeMorphable(cardLayout.demo_2_share)
        }

        val interpolator = FastOutSlowInInterpolator()

        val root = MorphView.makeMorphable(getRoot())

        Binder.createBinding(Bind.UNIDIRECTIONAL, card.morphCornerRadii, image.morphCornerRadii)

        choreographer
         //   .withDefaultInterpolator(interpolator)
            .withDefaultDuration(300)

           /* .animate(card)
            .withPivot(0.5f, 0.5f)
            .rotateTo(360f)
            .withDuration(1000)

            .thenAnimate()
            .xTranslateBetween(0f, -(30.dp), (30.dp), -(70.dp), (70.dp), -(100.dp), (100.dp), -(70.dp), (70.dp), -(30.dp), (30.dp), 0f)
            .withDuration(500)

            .thenAnimate()
            .withStartDelay(100)
            .yTranslateBetween(0f, -(30.dp), (30.dp), -(70.dp), (70.dp), -(100.dp), (100.dp), -(70.dp), (70.dp), -(30.dp), (30.dp), 0f)
            .withDuration(500)

            .thenAnimate()
            .resizeTo(root.viewBounds)
            *//*.positionAt(root.viewBounds)*//*
            .cornerRadiusTo(root.morphCornerRadii)
            .withDuration(1000)

            .alsoAnimate(image)
            .resizeTo(root.viewBounds)
            .withDuration(1000)

            .alsoAnimate(icon1, icon2, icon3)
            .scaleTo(0f)
            .withDuration(1000)

            .reverseAnimate(card)
            .withDuration(2000)

            .andReverseAnimate(image)
            .withDuration(2000)

            .andReverseAnimate(icon1, icon2, icon3)
            .withDuration(1000)
*/
            .animate(card)
            .withDuration(500)
            .withInterpolator(FastOutSlowInInterpolator())
            .withStretch(Stretch(0.5f, 0.5f,0.2f))
            .anchorTo(Anchor.BOTTOM, root)

            .thenAnimate()
            .withDuration(3600)
            .withInterpolator(FastOutSlowInInterpolator())
            .withStretch(Stretch(0.5f, 0.15f, 0.2f, 0f, 1f))
            .anchorTo(Anchor.TOP, root)

            .thenAnimate()
            .withStartDelay(500)
            .withDuration(500)
            .anchorTo(Anchor.CENTER, root)

            .thenAnimate()

           /* .thenAnimate(card)
            .withStartDelay(500)
            .withDuration(500)
            .anchorTo(Anchor.TOP_LEFT, root)

            .thenAnimate()
            .withDuration(500)
            .anchorTo(Anchor.RIGHT, root)

            .thenAnimate()
            .withDuration(500)
            .anchorTo(Anchor.BOTTOM, root)

            .thenAnimate()
            .withDuration(500)
            .anchorTo(Anchor.LEFT, root)

            .thenAnimate()
            .withDuration(500)
            .anchorArcTo(Anchor.TOP_RIGHT, root)

            .thenAnimate()
            .withDuration(500)
            .anchorTo(Anchor.CENTER, root)

            .thenAnimate()
            .rotateTo(0f)
            .withDuration(1000)

            .thenAnimate()
            .withPivot(0.5f, 0f)
            .yScaleTo(0.1f)
            .withDuration(2000)

            .thenAnimate()
            .withPivot(0.5f, 0f)
            .withInterpolator(AnticipateOvershootInterpolator())
            .zTranslateTo(30.dp)
            .scaleTo(1.2f)
            .withDuration(2000)

            .thenAnimate()
            .withPivot(0.5f, 0f)
            .zTranslateTo(0f)
            .alphaTo(0f)
            .scaleTo(1f)
            .withDuration(1000)

            .thenAnimate()
            .zTranslateTo(10.dp)
            .alphaTo(1f)
            .withDuration(1000)*/

            .build()
            .start()

        //TODO("Attempt to implement counter transform scaling")
        //TODO("Reuse already assigned animators. IDEA!! Use an animator pool where they can be recycled")
    }
}

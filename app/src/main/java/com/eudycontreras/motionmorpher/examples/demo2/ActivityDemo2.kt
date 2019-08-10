package com.eudycontreras.motionmorpher.examples.demo2

import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.children
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.eudycontreras.motionmorpher.R
import com.eudycontreras.motionmorpherlibrary.Choreographer
import com.eudycontreras.motionmorpherlibrary.Morpher
import com.eudycontreras.motionmorpherlibrary.activities.MorphActivity
import com.eudycontreras.motionmorpherlibrary.activities.MorphDialog
import com.eudycontreras.motionmorpherlibrary.enumerations.Anchor
import com.eudycontreras.motionmorpherlibrary.extensions.dp
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.layouts.MorphView
import com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts.ConstraintLayout
import com.eudycontreras.motionmorpherlibrary.utilities.binding.Bind
import com.eudycontreras.motionmorpherlibrary.utilities.binding.Binder
import kotlinx.android.synthetic.main.activity_demo2.*
import kotlinx.android.synthetic.main.activity_demo2.view.*
import kotlinx.android.synthetic.main.activity_demo2_card.view.*

class ActivityDemo2 : MorphActivity() {

    val choreographer: Choreographer = Choreographer()

    lateinit var morpher: Morpher

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

        morpher = Morpher(this)

        morpher.useArcTranslator = false
        morpher.animateChildren = true
        morpher.animateChildren = true

        morpher.morphIntoInterpolator = FastOutSlowInInterpolator()
        morpher.morphFromInterpolator = FastOutSlowInInterpolator()

        morpher.endStateChildMorphIntoDescriptor = Morpher.ChildAnimationDescriptor(
            type = Morpher.AnimationType.REVEAL,
            animateOnOffset = 0f,
            durationMultiplier = 0f,
            defaultTranslateMultiplierX = 0.04f,
            defaultTranslateMultiplierY = 0.04f,
            interpolator = DecelerateInterpolator(),
            stagger = Morpher.AnimationStagger(0.14f),
            startStateProps = Morpher.AnimationProperties(alpha = 0f)
        )

        morpher.endStateChildMorphFromDescriptor = Morpher.ChildAnimationDescriptor(
            type = Morpher.AnimationType.CONCEAL,
            animateOnOffset = 0f,
            durationMultiplier = -0.8f,
            defaultTranslateMultiplierX = 0.1f,
            defaultTranslateMultiplierY = 0.1f,
            interpolator = AccelerateInterpolator(),
            stagger = Morpher.AnimationStagger(0.15f),
            reversed = true
        )

        morpher.morphIntoDuration = 2450
        morpher.morphFromDuration = 2450

        val dialog = MorphDialog.instance(this, morpher, R.layout.activity_demo2_details, R.style.AppTheme_Dialog)

        dialog.addShowListener {
            DetailsDemo2(this, dialog)
        }

        grid.children.forEach { child ->
            child.setOnClickListener {
                morpher.startView = child as MorphLayout
                dialog.show {
                    morpher.morphInto()
                }
            }
        }
    }

    override fun getRoot(): ViewGroup {
        return this.findViewById(R.id.root)
    }

    fun testChoreographer() {
        val card = grid.cardLayout as ConstraintLayout

        if (!::actions.isInitialized) {
            actions = MorphView.makeMorphable(card.demo_2_actions)
        }
        if (!::image.isInitialized) {
            image = MorphView.makeMorphable(card.demo_2_image)
        }
        if (!::text.isInitialized) {
            text = MorphView.makeMorphable(card.demo_2_header)
        }
        if (!::icon1.isInitialized) {
            icon1 = MorphView.makeMorphable(card.demo_2_favorite)
        }
        if (!::icon2.isInitialized) {
            icon2 = MorphView.makeMorphable(card.demo_2_book)
        }
        if (!::icon3.isInitialized) {
            icon3 = MorphView.makeMorphable(card.demo_2_share)
        }

        val interpolator = FastOutSlowInInterpolator()

        val root = MorphView.makeMorphable(getRoot())

        Binder.createBinding(Bind.UNIDIRECTIONAL, card.morphCornerRadii, image.morphCornerRadii)

        choreographer
            .withDefaultInterpolator(interpolator)
            .withDefaultDuration(300)

            .animate(card)
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
            /*.positionAt(root.viewBounds)*/
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

            .thenAnimate(card)
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
            .withDuration(1000)

            .build()
            .start()

        //TODO("Attempt to implement counter transform scaling")
        //TODO("Reuse already assigned animators. IDEA!! Use an animator pool where they can be recycled")
    }
}

package com.eudycontreras.motionmorpher.examples.demo2

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.children
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.eudycontreras.motionmorpher.R
import com.eudycontreras.motionmorpherlibrary.Choreographer
import com.eudycontreras.motionmorpherlibrary.Morpher
import com.eudycontreras.motionmorpherlibrary.activities.MorphActivity
import com.eudycontreras.motionmorpherlibrary.activities.MorphDialog
import com.eudycontreras.motionmorpherlibrary.enumerations.Stagger
import com.eudycontreras.motionmorpherlibrary.interactions.Explode
import com.eudycontreras.motionmorpherlibrary.interpolators.Easing
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.layouts.MorphView
import com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts.ConstraintLayout
import com.eudycontreras.motionmorpherlibrary.properties.AnimationStagger
import com.eudycontreras.motionmorpherlibrary.utilities.binding.Bind
import com.eudycontreras.motionmorpherlibrary.utilities.binding.Binder
import kotlinx.android.synthetic.main.activity_demo2.*
import kotlinx.android.synthetic.main.activity_demo2_card.view.*

class ActivityDemo2 : MorphActivity() {

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

        testMorphing()
    }

    override fun getRoot(): ViewGroup {
        return this.findViewById(R.id.root)
    }

    fun testMorphing() {

        val morpher = Morpher(this).apply {
            this.morphIntoDuration = 800
            this.morphFromDuration = 800
        }

        val standardEasing = Easing.STANDARD
        val outgoingEasing = Easing.OUTGOING
        val incomingEasing = Easing.INCOMING

        morpher.animateChildren = true
        morpher.useArcTranslator = false

        morpher.morphIntoInterpolator = standardEasing
        morpher.morphFromInterpolator = standardEasing

        morpher.dimPropertyInto.interpolateOffsetStart = 0f
        morpher.dimPropertyInto.interpolateOffsetEnd = 0.7f

        morpher.dimPropertyFrom.interpolateOffsetStart = 0f
        morpher.dimPropertyFrom.interpolateOffsetEnd = 0.6f

        morpher.containerStateIn.propertyAlpha.interpolator = incomingEasing
        morpher.containerStateIn.propertyAlpha.interpolateOffsetStart = 0.25f
        morpher.containerStateIn.propertyAlpha.interpolateOffsetEnd = 1f

        morpher.containerStateOut.propertyAlpha.interpolator = outgoingEasing
        morpher.containerStateOut.propertyAlpha.interpolateOffsetStart = 0f
        morpher.containerStateOut.propertyAlpha.interpolateOffsetEnd = 0.3f

        morpher.placeholderStateIn.propertyAlpha.interpolator = outgoingEasing
        morpher.placeholderStateIn.propertyAlpha.interpolateOffsetStart = 0f
        morpher.placeholderStateIn.propertyAlpha.interpolateOffsetEnd = 0.3f

        morpher.placeholderStateOut.propertyAlpha.interpolator = incomingEasing
        morpher.placeholderStateOut.propertyAlpha.interpolateOffsetStart = 0.3f

        morpher.containerChildStateIn.animateOnOffset = 0f
        morpher.containerChildStateIn.durationMultiplier = -0.2f
        morpher.containerChildStateIn.defaultTranslateMultiplierX = 0.02f
        morpher.containerChildStateIn.defaultTranslateMultiplierY = 0.02f
        morpher.containerChildStateIn.stagger?.staggerOffset = 0.1f
        morpher.containerChildStateIn.interpolator = incomingEasing

        // We assign sibbling interaction. In this case we use explode which creates
        // an explode effect to interact with the children. The explode interaction is powerful
        // and can be customized to create a large variety of effects.
        morpher.siblingInteraction = Explode(Explode.Type.LOOSE, 1f).apply {
            this.outInterpolator = standardEasing
            this.inInterpolator = incomingEasing
            this.animationStaggerOut = AnimationStagger(0.1f, type = Stagger.INCREMENTAL)
            this.animationStaggerIn = AnimationStagger(0.2f, type = Stagger.DECREMENTAL)
        }

        // If we are morphing into a details dialog. A dialog can be created to make which
        // holds the result view. When using a MorphDialog the endview does not need to be explictly
        // specified.
        val dialog = MorphDialog.instance(this, morpher, R.layout.activity_demo2_details, R.style.AppTheme_Dialog)

        // We can create an object to wrappd the dialog in once it is created.
        dialog.addCreateListener {
            DetailsDemo2(this, dialog)
        }

        // When the dialog dismiss is requested we morph back  if we are morphed.
        dialog.addDismissRequestListener {
            morpher.morphFrom(
                onEnd = { super.onBackPressed() }
            )
        }

        // We can morph from each view in the grid or list by setting said view
        // as the startview of the morpher. The view must be a MorphLayout or you need
        // to call view.asMorphLayout() to convert the view to one. Once we set the view
        // we can morph from it.
        grid.children.forEach { child ->
            child.setOnClickListener { _ ->
                morpher.startView = child as MorphLayout
                dialog.show {
                    morpher.morphInto()
                }
            }
        }
    }
}

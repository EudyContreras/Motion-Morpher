package com.eudycontreras.motionmorpher.examples.demo1

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.eudycontreras.motionmorpher.R
import com.eudycontreras.motionmorpherlibrary.Morpher
import com.eudycontreras.motionmorpherlibrary.activities.MorphActivity
import com.eudycontreras.motionmorpherlibrary.activities.MorphDialog
import com.eudycontreras.motionmorpherlibrary.enumerations.AnimationType
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.properties.AnimationStagger
import kotlinx.android.synthetic.main.activity_demo1.*


class ActivityDemo1 : MorphActivity() {

    lateinit var morpher: Morpher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo1)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_menu)

        morpher = Morpher(this)

        /**
         * Decide whether arc translator should be use
         */
        morpher.useArcTranslator = false

        /**
         * If set to true the morpher will animate the untagged
         * children of the layouts using the child animation descriptors
         */
        morpher.animateChildren = false

        /**
         * Optionally assign the duration for both morphing into
         * and morphing from
         */
        morpher.morphIntoDuration = 1450
        morpher.morphFromDuration = 1450

        morpher.dimPropertyInto.toValue = 1f
        morpher.dimPropertyFrom.fromValue = 1f

        /**
         * Optionally assign the base interpolators to use when
         * morphing into and from a startView
         */
        morpher.morphIntoInterpolator = FastOutSlowInInterpolator()
        morpher.morphFromInterpolator = FastOutSlowInInterpolator()


        /**
         * Apply child animation descriptor values. The descriptor
         * tells the morpher what it should do with non shared/tagged children.
         * If no custom values are defined the default values will be used
         */

        morpher.containerChildStateIn = Morpher.ChildAnimationDescriptor(
            type = AnimationType.REVEAL,
            animateOnOffset = 0f,
            durationMultiplier = -0.2f,
            defaultTranslateMultiplierX = 0.12f,
            defaultTranslateMultiplierY = 0.12f,
            interpolator = DecelerateInterpolator(),
            stagger = AnimationStagger(0.14f)
        )

        morpher.containerChildStateOut = Morpher.ChildAnimationDescriptor(
            type = AnimationType.CONCEAL,
            animateOnOffset = 0f,
            durationMultiplier = -0.8f,
            defaultTranslateMultiplierX = 0.18f,
            defaultTranslateMultiplierY = 0.18f,
            interpolator = AccelerateInterpolator(),
            stagger =  AnimationStagger(0.15f),
            reversed = true
        )
 
        morpher.placeholderChildStateIn = Morpher.ChildAnimationDescriptor(
            type = AnimationType.REVEAL,
            animateOnOffset = 0f,
            durationMultiplier = 0.2f,
            defaultTranslateMultiplierX = 0f,
            defaultTranslateMultiplierY = -4f,
            interpolator = AccelerateInterpolator()
        )

        morpher.placeholderChildStateOut = Morpher.ChildAnimationDescriptor(
            type = AnimationType.CONCEAL,
            animateOnOffset = 0f,
            durationMultiplier = 0f,
            defaultTranslateMultiplierX = 0f,
            defaultTranslateMultiplierY = -1f,
            interpolator = DecelerateInterpolator()
        )

        morpher.computedStatesListener = { startState, endState ->
            /**
             * Assign the animation descriptor for the end state end layout
             * the descriptor tells the morpher how to animate the
             * resulting layout out from the source/starting layout
             *
             * The descriptor helps with building complex animation choreography
             * If no descriptor is specified the default values will be used
             *
             */
            morpher.containerStateIn.propertyScaleX.fromValue = (startState.width) / (endState.width)
            morpher.containerStateIn.propertyScaleY.fromValue = (startState.width) / (endState.width)

            morpher.containerStateIn.propertyScaleX.toValue = 1f
            morpher.containerStateIn.propertyScaleY.toValue = 1f

            morpher.containerStateIn.propertyAlpha.interpolateOffsetStart = 0.30f
            morpher.containerStateIn.propertyAlpha.interpolateOffsetEnd = 1f

            morpher.containerStateIn.propertyScaleX.interpolator = FastOutSlowInInterpolator()
            morpher.containerStateIn.propertyScaleY.interpolator = FastOutSlowInInterpolator()
            morpher.containerStateIn.propertyAlpha.interpolator = DecelerateInterpolator()


            /**
             * Assign the animation descriptor for the end state start layout
             * the descriptor tells the morpher how to animate the
             * start layout out from the result/ending layout
             *
             * The descriptor helps with building complex animation choreography
             * If no descriptor is specified the default values will be used
             *
             */
            morpher.placeholderStateIn.propertyScaleX.toValue = (endState.width) / (startState.width)
            morpher.placeholderStateIn.propertyScaleY.toValue = (endState.width) / (startState.width)

            morpher.placeholderStateIn.propertyScaleX.fromValue = 1f
            morpher.placeholderStateIn.propertyScaleY.fromValue = 1f

            morpher.placeholderStateIn.propertyAlpha.fromValue = 1f
            morpher.placeholderStateIn.propertyAlpha.toValue = 0f

            morpher.placeholderStateIn.propertyAlpha.interpolateOffsetStart = 0f
            morpher.placeholderStateIn.propertyAlpha.interpolateOffsetEnd = 0.3f

            morpher.placeholderStateIn.propertyScaleX.interpolator = FastOutSlowInInterpolator()
            morpher.placeholderStateIn.propertyScaleY.interpolator = FastOutSlowInInterpolator()
            morpher.placeholderStateIn.propertyAlpha.interpolator = AccelerateInterpolator()


            /**
             * Assign the animation descriptor for the start state end layout
             * the descriptor tells the morpher how to animate the
             * start layout out from the result/ending layout
             *
             * The descriptor helps with building complex animation choreography
             * If no descriptor is specified the default values will be used
             *
             */
            morpher.containerStateOut.propertyScaleX.fromValue = 1f
            morpher.containerStateOut.propertyScaleY.fromValue = 1f

            morpher.containerStateOut.propertyScaleX.toValue = (startState.width) / (endState.width)
            morpher. containerStateOut.propertyScaleY.toValue = (startState.width) / (endState.width)

            morpher.containerStateOut.propertyAlpha.fromValue = 1f
            morpher.containerStateOut.propertyAlpha.toValue = 0f

            morpher.containerStateOut.propertyAlpha.interpolateOffsetStart = 0f
            morpher.containerStateOut.propertyAlpha.interpolateOffsetEnd = 0.3f

            morpher.containerStateOut.propertyScaleX.interpolator = FastOutSlowInInterpolator()
            morpher.containerStateOut.propertyScaleY.interpolator = FastOutSlowInInterpolator()
            morpher.containerStateOut.propertyAlpha.interpolator = AccelerateInterpolator()

            /**
             * Assign the animation descriptor for the start state start layout
             * the descriptor tells the morpher how to animate the
             * start layout out from the result/ending layout
             *
             * The descriptor helps with building complex animation choreography
             * If no descriptor is specified the default values will be used
             *
             */
            morpher.placeholderStateOut.propertyScaleX.fromValue = (endState.width) / (startState.width)
            morpher.placeholderStateOut.propertyScaleY.fromValue = (endState.width) / (startState.width)

            morpher.placeholderStateOut.propertyScaleX.toValue = 1f
            morpher.placeholderStateOut.propertyScaleY.toValue = 1f

            morpher.placeholderStateOut.propertyAlpha.fromValue = 0f
            morpher.placeholderStateOut.propertyAlpha.toValue = 1f

            morpher.placeholderStateOut.propertyAlpha.interpolateOffsetStart = 0.3f
            morpher.placeholderStateOut.propertyAlpha.interpolateOffsetEnd = 1f

            morpher.placeholderStateOut.propertyScaleX.interpolator = FastOutSlowInInterpolator()
            morpher.placeholderStateOut.propertyScaleY.interpolator = FastOutSlowInInterpolator()
            morpher.placeholderStateOut.propertyAlpha.interpolator = DecelerateInterpolator()

        }
        /**
         *  Set the starting startView to morph from. The startView
         *  must be a morphable layout/startView
         */
        morpher.startView = fab as MorphLayout

        /**
         * When the resulting startView from a morph is a dialog
         * or a fragment a MorphDialog must be created using the layout id
         * of the resulting startView. The layout must be a MorphContainer containing both
         * the End startView and a mock of the start startView. THis is needed for when doing some sort
         * of overlapping between two different layouts or for shared animations where and element
         * from the starting layout will become part of the end layout.
         * MorphDialogs are useful when morphing between two completely
         * different morphViews that share no elements. Shared elements may
         * still be present.
         *
         * In order to construct a MorphDialog an instance a MorphActivity and an
         * instance of a Morpher is needed. As well a the layout and Theme containing
         * the desired look and palettes
         */
        val dialog = MorphDialog.instance(this, morpher, R.layout.activity_demo1_details, R.style.AppTheme_Dialog)

        dialog.addCreateListener {
            val details = DetailsDemo1(this, dialog)
        }

        fab.setOnClickListener {
            dialog.show {
                morpher.morphInto()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_demo_1, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun getRoot(): ViewGroup {
        return this.findViewById(R.id.root)
    }
}

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
import com.eudycontreras.motionmorpherlibrary.extensions.dp
import com.eudycontreras.motionmorpherlibrary.layouts.MorphWrapper
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
        morpher.animateChildren = true

        /**
         * Optionally assign the duration for both morphing into
         * and morphing from
         */
        morpher.morphIntoDuration = 1000
        morpher.morphFromDuration = 1000

        morpher.dimPropertyInto.toValue = 1f
        morpher.dimPropertyFrom.fromValue = 1f

        /**
         * Optionally assign the base interpolators to use when
         * morphing into and from a view
         */
        morpher.morphIntoInterpolator = FastOutSlowInInterpolator()
        morpher.morphFromInterpolator = FastOutSlowInInterpolator()


        /**
         * Apply child animation descriptor values. The descriptor
         * tells the morpher what it should do with non shared/tagged children.
         * If no custom values are defined the default values will be used
         */

        morpher.endStateChildMorphIntoDescriptor = Morpher.ChildAnimationDescriptor(
            type = Morpher.AnimationType.REVEAL,
            animateOnOffset = 0.1f,
            durationMultiplier = -0.15f,
            startStateProps = Morpher.AnimationProperties().apply {
                alpha = 0f
                scaleX = 1f
                scaleY = 1f
                translationY = 55.dp
            },
            endStateProps = Morpher.AnimationProperties().apply {
                alpha = 1f
                scaleX = 1f
                scaleY = 1f
                translationY = 0f
            },
            interpolator = DecelerateInterpolator(),
            stagger = Morpher.AnimationStagger(0.15f)
        )

        morpher.endStateChildMorphFromDescriptor = Morpher.ChildAnimationDescriptor(
            type = Morpher.AnimationType.CONCEAL,
            animateOnOffset = 0f,
            durationMultiplier = -0.8f,
            startStateProps = Morpher.AnimationProperties().apply {
                alpha = 1f
                scaleX = 1f
                scaleY = 1f
                translationY = 0f
            },
            endStateProps = Morpher.AnimationProperties().apply {
                alpha = 0f
                scaleX = 1f
                scaleY = 1f
                translationY = 100.dp
            },
            interpolator = AccelerateInterpolator(),
            stagger = Morpher.AnimationStagger(0.15f),
            reversed = true
        )

        morpher.startStateChildMorphIntoDescriptor = Morpher.ChildAnimationDescriptor(
            type = Morpher.AnimationType.REVEAL,
            animateOnOffset = 0f,
            durationMultiplier = 0.2f,
            startStateProps = Morpher.AnimationProperties().apply {
                alpha = 1f
                scaleX = 1f
                scaleY = 1f
                translationY = 0f
            },
            endStateProps = Morpher.AnimationProperties().apply {
                alpha = 0f
                scaleX = 1f
                scaleY = 1f
                translationY = -(80.dp)
            },
            interpolator = AccelerateInterpolator()
        )

        morpher.startStateChildMorphFromDescriptor = Morpher.ChildAnimationDescriptor(
            type = Morpher.AnimationType.CONCEAL,
            animateOnOffset = 0f,
            durationMultiplier = 0.1f,
            startStateProps = Morpher.AnimationProperties().apply {
                alpha = 1f
                scaleX = 1f
                scaleY = 1f
                translationY = -(30.dp)
            },
            endStateProps = Morpher.AnimationProperties().apply {
                alpha = 1f
                scaleX = 1f
                scaleY = 1f
                translationY = 0.dp
            },
            interpolator = DecelerateInterpolator(),
            reversed = false
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
            morpher.endStateMorphIntoDescriptor.propertyScaleX.fromValue = (startState.width) / (endState.width)
            morpher.endStateMorphIntoDescriptor.propertyScaleY.fromValue = (startState.width) / (endState.width)

            morpher.endStateMorphIntoDescriptor.propertyScaleX.toValue = 1f
            morpher.endStateMorphIntoDescriptor.propertyScaleY.toValue = 1f

            morpher.endStateMorphIntoDescriptor.propertyAlpha.startOffset = 0.30f
            morpher.endStateMorphIntoDescriptor.propertyAlpha.endOffset = 1f

            morpher.endStateMorphIntoDescriptor.propertyScaleX.interpolator = FastOutSlowInInterpolator()
            morpher.endStateMorphIntoDescriptor.propertyScaleY.interpolator = FastOutSlowInInterpolator()
            morpher.endStateMorphIntoDescriptor.propertyAlpha.interpolator = DecelerateInterpolator()


            /**
             * Assign the animation descriptor for the end state start layout
             * the descriptor tells the morpher how to animate the
             * start layout out from the result/ending layout
             *
             * The descriptor helps with building complex animation choreography
             * If no descriptor is specified the default values will be used
             *
             */
            morpher.startStateMorphIntoDescriptor.propertyScaleX.toValue = (endState.width) / (startState.width)
            morpher.startStateMorphIntoDescriptor.propertyScaleY.toValue = (endState.width) / (startState.width)

            morpher.startStateMorphIntoDescriptor.propertyScaleX.fromValue = 1f
            morpher.startStateMorphIntoDescriptor.propertyScaleY.fromValue = 1f

            morpher.startStateMorphIntoDescriptor.propertyAlpha.fromValue = 1f
            morpher.startStateMorphIntoDescriptor.propertyAlpha.toValue = 0f

            morpher.startStateMorphIntoDescriptor.propertyAlpha.startOffset = 0f
            morpher.startStateMorphIntoDescriptor.propertyAlpha.endOffset = 0.30f

            morpher.startStateMorphIntoDescriptor.propertyScaleX.interpolator = FastOutSlowInInterpolator()
            morpher.startStateMorphIntoDescriptor.propertyScaleY.interpolator = FastOutSlowInInterpolator()
            morpher.startStateMorphIntoDescriptor.propertyAlpha.interpolator = AccelerateInterpolator()


            /**
             * Assign the animation descriptor for the start state end layout
             * the descriptor tells the morpher how to animate the
             * start layout out from the result/ending layout
             *
             * The descriptor helps with building complex animation choreography
             * If no descriptor is specified the default values will be used
             *
             */
            morpher.endStateMorphFromDescriptor.propertyScaleX.fromValue = 1f
            morpher.endStateMorphFromDescriptor.propertyScaleY.fromValue = 1f

            morpher.endStateMorphFromDescriptor.propertyScaleX.toValue = (startState.width) / (endState.width)
            morpher. endStateMorphFromDescriptor.propertyScaleY.toValue = (startState.width) / (endState.width)

            morpher.endStateMorphFromDescriptor.propertyAlpha.fromValue = 1f
            morpher.endStateMorphFromDescriptor.propertyAlpha.toValue = 0f

            morpher.endStateMorphFromDescriptor.propertyAlpha.startOffset = 0f
            morpher.endStateMorphFromDescriptor.propertyAlpha.endOffset = 0.3f

            morpher.endStateMorphFromDescriptor.propertyScaleX.interpolator = FastOutSlowInInterpolator()
            morpher.endStateMorphFromDescriptor.propertyScaleY.interpolator = FastOutSlowInInterpolator()
            morpher.endStateMorphFromDescriptor.propertyAlpha.interpolator = AccelerateInterpolator()

            /**
             * Assign the animation descriptor for the start state start layout
             * the descriptor tells the morpher how to animate the
             * start layout out from the result/ending layout
             *
             * The descriptor helps with building complex animation choreography
             * If no descriptor is specified the default values will be used
             *
             */
            morpher.startStateMorphFromDescriptor.propertyScaleX.fromValue = (endState.width) / (startState.width)
            morpher.startStateMorphFromDescriptor.propertyScaleY.fromValue = (endState.width) / (startState.width)

            morpher.startStateMorphFromDescriptor.propertyScaleX.toValue = 1f
            morpher.startStateMorphFromDescriptor.propertyScaleY.toValue = 1f

            morpher.startStateMorphFromDescriptor.propertyAlpha.fromValue = 0f
            morpher.startStateMorphFromDescriptor.propertyAlpha.toValue = 1f

            morpher.startStateMorphFromDescriptor.propertyAlpha.startOffset = 0.3f
            morpher.startStateMorphFromDescriptor.propertyAlpha.endOffset = 1f

            morpher.startStateMorphFromDescriptor.propertyScaleX.interpolator = FastOutSlowInInterpolator()
            morpher.startStateMorphFromDescriptor.propertyScaleY.interpolator = FastOutSlowInInterpolator()
            morpher.startStateMorphFromDescriptor.propertyAlpha.interpolator = DecelerateInterpolator()

        }
        /**
         *  Set the starting view to morph from. The view
         *  must be a morphable layout/view
         */
        morpher.startView = toolbarMenuBor as MorphWrapper

        /**
         * When the resulting view from a morph is a dialog
         * or a fragment a MorphDialog must be created using the layout id
         * of the resulting view. The layout must be a MorphContainer containing both
         * the End view and a mock of the start view. THis is needed for when doing some sort
         * of overlapping between two different layouts or for shared animations where and element
         * from the starting layout will become part of the end layout.
         * MorphDialogs are useful when morphing between two completely
         * different views that share no elements. Shared elements may
         * still be present.
         *
         * In order to construct a MorphDialog an instance a MorphActivity and an
         * instance of a Morpher is needed. As well a the layout and Theme containing
         * the desired look and palettes
         */
        val dialog = MorphDialog.instance(this, morpher, R.layout.activity_demo1_details, R.style.AppTheme_Dialog)

        dialog.addShowListener {
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

package com.eudycontreras.motionmorpher.examples.demo2

import android.os.Bundle
import android.view.ViewGroup
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.eudycontreras.motionmorpher.R
import com.eudycontreras.motionmorpherlibrary.Choreographer
import com.eudycontreras.motionmorpherlibrary.Morpher
import com.eudycontreras.motionmorpherlibrary.activities.MorphActivity
import com.eudycontreras.motionmorpherlibrary.enumerations.Anchor
import com.eudycontreras.motionmorpherlibrary.extensions.dp
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.layouts.MorphView
import com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts.ConstraintLayout
import com.eudycontreras.motionmorpherlibrary.utilities.binding.Bind
import com.eudycontreras.motionmorpherlibrary.utilities.binding.Binder
import kotlinx.android.synthetic.main.activity_demo2.*
import kotlinx.android.synthetic.main.activity_demo2_card.view.*

class ActivityDemo2 : MorphActivity() {

    lateinit var morpher: Morpher
    val choreographer: Choreographer = Choreographer()

    lateinit var actions: MorphLayout
    lateinit var image: MorphLayout
    lateinit var text: MorphLayout
    lateinit var icon1: MorphLayout
    lateinit var icon2: MorphLayout
    lateinit var icon3: MorphLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo2)

        card.setOnClickListener {
            testChoreographer()
        }
    }

    override fun onResume() {
        super.onResume()
      /*  Handler().postDelayed({
            testChoreographer()
        }, 1000)*/
    }

    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_demo_1, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }*/

    override fun getRoot(): ViewGroup {
        return this.findViewById(R.id.root)
    }

    fun testChoreographer() {
        val card = card as ConstraintLayout

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

        choreographer.withDefaultDuration(300)
            .animate(card)
            .rotate(0f, 360f)
            .withDuration(1000)

            .thenAnimate(card)
            .resizeTo(root.viewBounds)
            .positionAt(root.viewBounds)
            .cornerRadiusTo(root.morphCornerRadii)
            .withDuration(1000)
            .interpolator(interpolator)

            .alsoAnimate(image)
            .resizeTo(root.viewBounds)
            .withDuration(1000)
            .interpolator(interpolator)

            .alsoAnimate(icon1, icon2, icon3)
            .scale(0f)
            .withDuration(1000)
            .interpolator(interpolator)

            .reverseAnimate(card)
            .withDuration(2000)
            .interpolator(interpolator)

            .andReverseAnimate(image)
            .withDuration(2000)
            .interpolator(interpolator)

            .andReverseAnimate(icon1, icon2, icon3)
            .withDuration(1000)
            .interpolator(interpolator)

            .thenAnimate(card)
            .withStartDelay(1000)
            .withDuration(500)
            .anchorTo(Anchor.TOP_LEFT, root)
            .interpolator(interpolator)

            .thenAnimate(card)
            .withDuration(500)
            .anchorTo(Anchor.RIGHT, root)
            .interpolator(interpolator)

            .thenAnimate(card)
            .withDuration(500)
            .anchorTo(Anchor.BOTTOM, root)
            .interpolator(interpolator)

            .thenAnimate(card)
            .withDuration(500)
            .anchorTo(Anchor.LEFT, root)
            .interpolator(interpolator)

            .thenAnimate(card)
            .withDuration(500)
            .anchorTo(Anchor.TOP_RIGHT, root)
            .interpolator(interpolator)

            .thenAnimate(card)
            .withDuration(500)
            .anchorTo(Anchor.CENTER, root)
            .interpolator(interpolator)

            .thenAnimate(card)
            .rotate(0f)
            .withDuration(1000)
            .interpolator(interpolator)

            .thenAnimate(card)
            .translateZ(30.dp)
            .scale(1.2f)
            .withDuration(2000)
            .interpolator(interpolator)

            .thenAnimate(card)
            .translateZ(0f)
            .opacity(0f)
            .scale(1f)
            .withDuration(1000)
            .interpolator(interpolator)

            .thenAnimate(card)
            .translateZ(10.dp)
            .opacity(1f)
            .withDuration(1000)
            .interpolator(interpolator)

            .whenDone {
                //it.choreographer.reset()
                /*it.choreographer.resetWithAnimation(card)
                it.choreographer.resetWithAnimation(image)
                it.choreographer.resetWithAnimation(text)*/
            }
            .build()
            .start()

        //TODO("Reuse already assigned animators")
        //TODO("allow repeat forever or for a duration")
    }
}

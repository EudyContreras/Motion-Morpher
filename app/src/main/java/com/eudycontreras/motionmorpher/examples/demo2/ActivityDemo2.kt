package com.eudycontreras.motionmorpher.examples.demo2

import android.os.Bundle
import android.os.Handler
import android.view.ViewGroup
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.eudycontreras.motionmorpher.R
import com.eudycontreras.motionmorpherlibrary.Choreographer
import com.eudycontreras.motionmorpherlibrary.Morpher
import com.eudycontreras.motionmorpherlibrary.activities.MorphActivity
import com.eudycontreras.motionmorpherlibrary.enumerations.Dimension
import com.eudycontreras.motionmorpherlibrary.extensions.dp
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.layouts.MorphView
import com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts.ConstraintLayout
import kotlinx.android.synthetic.main.activity_demo2.*
import kotlinx.android.synthetic.main.activity_demo2_card.view.*

class ActivityDemo2 : MorphActivity() {

    lateinit var morpher: Morpher
    val choreographer: Choreographer = Choreographer()

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
        Handler().postDelayed({
            testChoreographer()
        }, 1000)
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

        choreographer
            .animate(card)
            .resizeTo(root)// View, Bounds
            .positionAt(root)
            /*.positionAt() //Coordinates, Bounds, x, y, etc
            .toMatchBounds() //View, Bounds
            .toMatchShape()//View, Background
            .translateY(-(150.dp))
            .resizeBy(Dimension.BOTH,2f)*/
            .withDuration(1000)
            .interpolator(interpolator)
            .cornerRadius(radius = 0f)

            .alsoAnimate(image)
            //.resizeTo(MorphView.makeMorphable(getRoot()))// View, Bounds
            //.resizeBy(Dimension.BOTH,2f)
            .withDuration(1000)
            .interpolator(interpolator)

            .alsoAnimate(text)
            .resizeBy(Dimension.BOTH,2f)
            .withDuration(1000)
            .interpolator(interpolator)

            .animateAfter(1f, icon1)
            .rotate(360f)
            .scale(0.6f)
            .translateY(12.dp)
            .withDuration(1000)
            .interpolator(interpolator)
            .thenReverse()

            .andAnimateAfter(1f, icon2)
            .thenReverse()
            .andAnimateAfter(1f, icon3)
            .thenReverse()
            .whenDone {
                //it.choreographer.reset()
                it.choreographer.resetWithAnimation(card)
                it.choreographer.resetWithAnimation(image)
                it.choreographer.resetWithAnimation(text)
            }
            .build()
            .start()
    }
}

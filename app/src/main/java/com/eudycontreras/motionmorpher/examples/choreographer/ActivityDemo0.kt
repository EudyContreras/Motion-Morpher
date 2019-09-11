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
import com.eudycontreras.motionmorpher.examples.choreographer.choreographies.DemoOne
import com.eudycontreras.motionmorpher.examples.choreographer.choreographies.DemoTwo
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo0)

        val card = cardLayout as ConstraintLayout
        val root = MorphView.makeMorphable(getRoot())
        val interpolator = FastOutSlowInInterpolator()

        cardLayout.post {
            var choreographyOne: Choreographer = DemoOne(this).create(card, root, interpolator)
            var choreographyTwo: Choreographer = DemoTwo(this).create(card, root, interpolator)

            cardLayout.setOnClickListener {
                choreographyTwo.play()
            }
        }
    }

    override fun getRoot(): ViewGroup {
        return this.findViewById(R.id.root)
    }
}

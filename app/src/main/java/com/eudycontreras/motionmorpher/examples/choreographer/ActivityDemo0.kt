package com.eudycontreras.motionmorpher.examples.choreographer

import android.os.Bundle
import android.view.ViewGroup
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.eudycontreras.motionmorpher.R
import com.eudycontreras.motionmorpher.examples.choreographer.choreographies.Demo1
import com.eudycontreras.motionmorpher.examples.choreographer.choreographies.Demo2
import com.eudycontreras.motionmorpher.examples.choreographer.choreographies.Demo3
import com.eudycontreras.motionmorpherlibrary.*
import com.eudycontreras.motionmorpherlibrary.activities.MorphActivity
import com.eudycontreras.motionmorpherlibrary.layouts.MorphView
import com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts.ConstraintLayout
import kotlinx.android.synthetic.main.activity_demo0.*

class ActivityDemo0 : MorphActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo0)

        val card = cardLayout as ConstraintLayout
        val root = MorphView.makeMorphable(getRoot())
        val interpolator = FastOutSlowInInterpolator()

        cardLayout.post {
            var choreography1: Choreographer = Demo1(this).create(card, root, interpolator)
            var choreography2: Choreographer = Demo2(this).create(card, root, interpolator)
            var choreography3: Choreographer = Demo3(this).create(card, root, interpolator)

            cardLayout.setOnClickListener {
                choreography1.play()
            }
        }
    }

    override fun getRoot(): ViewGroup {
        return this.findViewById(R.id.root)
    }
}

package com.eudycontreras.motionmorpher.examples.demo3

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.eudycontreras.motionmorpher.R
import com.eudycontreras.motionmorpherlibrary.Choreographer
import com.eudycontreras.motionmorpherlibrary.activities.MorphActivity
import com.eudycontreras.motionmorpherlibrary.activities.MorphDialog
import com.eudycontreras.motionmorpherlibrary.interactions.Explode
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import kotlinx.android.synthetic.main.activity_demo3.*
import kotlin.random.Random

class ActivityDemo3 : MorphActivity() {

    lateinit var choreographer: Choreographer

    lateinit var actions: MorphLayout
    lateinit var image: MorphLayout
    lateinit var text: MorphLayout
    lateinit var icon1: MorphLayout
    lateinit var icon2: MorphLayout
    lateinit var icon3: MorphLayout

    var value: Float = 0f

    val movieNames = arrayListOf(
        "The Avengers: Civil War",
        "Batman vs Superman",
        "DeadPool",
        "Alien vs Predator",
        "The Dark Night",
        "Priest and Saints",
        "John Wick: Episode 3",
        "300: Rise of the Empire",
        "Captain America",
        "Avatar",
        "Man of Steel",
        "SpiderMan: Homecoming",
        "SpiderMan: Symbiosis"
    )

    val tags = arrayListOf("SD", "HD", "UHD", "4K", "8K")

    val description = "The hero eventually reaches \"the innermost cave\" or the central crisis of his adventure, where he must undergo \"the ordeal\" where he overcomes the main obstacle or enemy, undergoing \"apotheosis\" and gaining his reward"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo3)

        choreographer = Choreographer(this)

        testMorphing()
    }

    override fun getRoot(): ViewGroup {
        return this.findViewById(R.id.root)
    }

    fun createDummyData(count: Int): List<Movie> {
        val moviePosters = arrayListOf(
            ContextCompat.getDrawable(this, R.drawable.background0),
            ContextCompat.getDrawable(this, R.drawable.background1),
            ContextCompat.getDrawable(this, R.drawable.background2),
            ContextCompat.getDrawable(this, R.drawable.background3),
            ContextCompat.getDrawable(this, R.drawable.background4),
            ContextCompat.getDrawable(this, R.drawable.background5),
            ContextCompat.getDrawable(this, R.drawable.background6),
            ContextCompat.getDrawable(this, R.drawable.background7),
            ContextCompat.getDrawable(this, R.drawable.background8),
            ContextCompat.getDrawable(this, R.drawable.background9),
            ContextCompat.getDrawable(this, R.drawable.background10),
            ContextCompat.getDrawable(this, R.drawable.background11),
            ContextCompat.getDrawable(this, R.drawable.background12)
        )

        val movieList = ArrayList<Movie>()

        var innerIndex = 0

        for (index in 0 until count) {
            if (innerIndex < moviePosters.size) {
                val movie = Movie(
                    title = movieNames[innerIndex],
                    description = description,
                    image = moviePosters[innerIndex],
                    reviews = Random.nextInt(from = 64, until = 853),
                    rating = Random.nextInt(from = 5, until = 9),
                    tags = ArrayList(tags)
                )
                movieList.add(movie)
                innerIndex++
            } else {
                innerIndex = 0
            }
        }

        for(movie in movieList) {
            movie.related = movieList.shuffled().take(3)
        }

        return movieList
    }

    fun testMorphing() {

        val standardInterpolator = FastOutSlowInInterpolator()
        val incomingInterpolator = LinearOutSlowInInterpolator()
        val outgoingInterpolator = FastOutLinearInInterpolator()

        choreographer.morpher.morphIntoDuration = 450
        choreographer.morpher.morphFromDuration = 350

        choreographer.morpher.animateChildren = true
        choreographer.morpher.useArcTranslator = false

        choreographer.morpher.morphIntoInterpolator = standardInterpolator
        choreographer.morpher.morphFromInterpolator = standardInterpolator

        choreographer.morpher.dimPropertyInto.interpolateOffsetStart = 0f
        choreographer.morpher.dimPropertyInto.interpolateOffsetEnd = 0.7f
        choreographer.morpher.dimPropertyInto.toValue = 1f

        choreographer.morpher.dimPropertyFrom.interpolateOffsetStart = 0f
        choreographer.morpher.dimPropertyFrom.interpolateOffsetEnd = 1f
        choreographer.morpher.dimPropertyFrom.fromValue = 1f

        choreographer.morpher.placeholderStateIn.propertyAlpha.interpolator = outgoingInterpolator
        choreographer.morpher.placeholderStateIn.propertyAlpha.interpolateOffsetStart = 0f
        choreographer.morpher.placeholderStateIn.propertyAlpha.interpolateOffsetEnd = 0.3f

        choreographer.morpher.containerStateIn.propertyAlpha.interpolator = incomingInterpolator
        choreographer.morpher.containerStateIn.propertyAlpha.interpolateOffsetStart = 0.25f
        choreographer.morpher.containerStateIn.propertyAlpha.interpolateOffsetEnd = 1f

        choreographer.morpher.containerStateOut.propertyAlpha.interpolator = outgoingInterpolator
        choreographer.morpher.containerStateOut.propertyAlpha.interpolateOffsetStart = 0f
        choreographer.morpher.containerStateOut.propertyAlpha.interpolateOffsetEnd = 0.3f

        choreographer.morpher.placeholderStateOut.propertyAlpha.interpolator = incomingInterpolator
        choreographer.morpher.placeholderStateOut.propertyAlpha.interpolateOffsetStart = 0.3f
        choreographer.morpher.placeholderStateOut.propertyAlpha.interpolateOffsetEnd = 1f

        choreographer.morpher.containerChildStateIn.animateOnOffset = 0f
        choreographer.morpher.containerChildStateIn.durationMultiplier = -0.1f
        choreographer.morpher.containerChildStateIn.defaultTranslateMultiplierX = 0.04f
        choreographer.morpher.containerChildStateIn.defaultTranslateMultiplierY = 0.04f
        choreographer.morpher.containerChildStateIn.stagger?.staggerOffset = 0.09f
        choreographer.morpher.containerChildStateIn.interpolator = incomingInterpolator

        choreographer.morpher.siblingInteraction = Explode(Explode.Type.TIGHT, 0.1f).apply {
            outInterpolator = standardInterpolator
            inInterpolator = standardInterpolator
            //stretch = Stretch(1f, 0.1f)
        }

        val dialog = MorphDialog.instance(this, choreographer.morpher, R.layout.activity_demo3_details, R.style.AppTheme_Dialog)
        dialog.addCreateListener {
            DetailsDemo3(this, dialog)
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
}

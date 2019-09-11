package com.eudycontreras.motionmorpher

import android.content.Context
import androidx.core.content.ContextCompat
import com.eudycontreras.motionmorpher.testdata.Movie
import kotlin.random.Random


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since September 11 2019
 */
 
 
class DataFactory(val context: Context) {

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


    fun createDummyData(count: Int): List<Movie> {
        val moviePosters = arrayListOf(
            ContextCompat.getDrawable(context, R.drawable.background0),
            ContextCompat.getDrawable(context, R.drawable.background1),
            ContextCompat.getDrawable(context, R.drawable.background2),
            ContextCompat.getDrawable(context, R.drawable.background3),
            ContextCompat.getDrawable(context, R.drawable.background4),
            ContextCompat.getDrawable(context, R.drawable.background5),
            ContextCompat.getDrawable(context, R.drawable.background6),
            ContextCompat.getDrawable(context, R.drawable.background7),
            ContextCompat.getDrawable(context, R.drawable.background8),
            ContextCompat.getDrawable(context, R.drawable.background9),
            ContextCompat.getDrawable(context, R.drawable.background10),
            ContextCompat.getDrawable(context, R.drawable.background11),
            ContextCompat.getDrawable(context, R.drawable.background12)
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
}
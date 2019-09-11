package com.eudycontreras.motionmorpher.testdata

import android.graphics.drawable.Drawable


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 19 2019
 */
 
 
data class Movie(
    var title: String,
    var description: String,
    var image: Drawable?,
    var rating: Int,
    var reviews: Int,
    var tags: List<String> = emptyList()
) {
    var related: List<Movie> = emptyList()
}
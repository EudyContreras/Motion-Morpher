package com.eudycontreras.motionmorpherlibrary.properties

/**
 * <h1>Class description!</h1>
 *
 *
 *
 * **Note:** Unlicensed private property of the author and creator
 * unauthorized use of this class outside of the Soul Vibe project
 * may result on legal prosecution.
 *
 *
 * Created by <B>Eudy Contreras</B>
 *
 * @author  Eudy Contreras
 * @version 1.0
 * @since   2018-03-31
 */
data class Dimension(
    var width: Float = 0f,
    var height: Float = 0f
) {
    var padding = Padding()

    fun copy(): Dimension {
        return Dimension(width, height)
    }
}
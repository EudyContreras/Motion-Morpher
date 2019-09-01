package com.eudycontreras.motionmorpherlibrary.properties

import android.view.View

/**
 * Class which holds infromation about the bounds of a view along
 * with its paddings. See: [Paddings] and margins. See: [Margings].
 *
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

data class ViewBounds (
    private var view: View?,
    var top: Int = 0,
    var left: Int = 0,
    var right: Int = 0,
    var bottom: Int = 0
): Bounds () {

    val paddings: Paddings = Paddings(view)

    val margings: Margings = Margings(view)
}

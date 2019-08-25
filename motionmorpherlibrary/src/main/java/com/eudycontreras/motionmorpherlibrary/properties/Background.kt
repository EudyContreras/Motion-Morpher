package com.eudycontreras.motionmorpherlibrary.properties

import android.graphics.drawable.GradientDrawable
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 01 2019
 */

class Background(
    view: MorphLayout
) {

    var corners: CornerRadii = view.morphCornerRadii
    var drawable: GradientDrawable = view.mutableBackground
    var gradient: Gradient = Gradient()
    var color: Color = MutableColor()
    var shape: Shape = if (view.morphShape == MorphLayout.CIRCULAR) Shape.CIRCULAR else Shape.RECTANGULAR

    enum class Shape {
        RECTANGULAR, CIRCULAR
    }
}
package com.eudycontreras.motionmorpherlibrary.properties

import android.widget.TextView
import androidx.annotation.IdRes


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since September 01 2019
 */
 
class TextMorph(
    var view: TextView,
    var textFrom: String,
    var textTo: String
) {
    var onOffset: Float = 0.3f

    var fontSizeFrom: Float = view.textSize
    var fontSizeTo: Float = view.textSize

    var textColorFrom: Int = view.currentTextColor
    var textColorTo: Int =  view.currentTextColor

    constructor(view: TextView, @IdRes textFrom: Int, @IdRes textTo: Int): this(
        view = view,
        textFrom = view.resources.getString(textFrom),
        textTo = view.resources.getString(textTo)
    )

    constructor(view: TextView, other: TextView): this(
        view = view,
        textFrom = view.text.toString(),
        textTo = other.text.toString()
    ) {
        this.fontSizeFrom = view.textSize
        this.fontSizeTo = other.textSize

        this.textColorFrom = view.currentTextColor
        this.textColorTo = other.currentTextColor
    }
}
package com.eudycontreras.motionmorpherlibrary.properties

import android.graphics.Typeface
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.IdRes
import com.eudycontreras.motionmorpherlibrary.*
import com.eudycontreras.motionmorpherlibrary.extensions.toStateList
import com.eudycontreras.motionmorpherlibrary.interfaces.Morphable
import com.eudycontreras.motionmorpherlibrary.utilities.ColorUtility


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since September 01 2019
 */
 
class TextMorph(
    var view: TextView,
    var textTo: String = view.text.toString(),
    var textSizeTo: Float = view.textSize,
    var textColorTo: Int =  view.currentTextColor,
    var letterSpacingTo: Float = view.letterSpacing,
    var typefaceTo: Typeface = view.typeface
): Morphable {
    var onOffset: Float = MID_OFFSET

    private var changed: Boolean = false

    constructor(view: TextView, @IdRes textTo: Int): this(
        view = view,
        textTo = view.resources.getString(textTo)
    )

    override fun build() {}

    override fun reset() {
        changed = false

    }

    override fun morph(fraction: Float) {
        val outgoingFraction = mapRange(fraction, MIN_OFFSET, onOffset, MIN_OFFSET, MAX_OFFSET)
        val incomingFraction = mapRange(fraction, onOffset, MAX_OFFSET, MIN_OFFSET, MAX_OFFSET)

        val alphaFrom = lerp(MAX_OFFSET, MIN_OFFSET, outgoingFraction)
        val alphaTo = lerp(MIN_OFFSET, MAX_OFFSET, incomingFraction)

        if (alphaFrom <= MIN_OFFSET && !changed) {
            applyValues()
            changed = true
        }

        view.alpha = if (fraction <= onOffset) alphaFrom else alphaTo
    }

    private fun applyValues() {
        this.view.setText(textTo)
        this.view.setTypeface(typefaceTo)
        this.view.setTextSize(textSizeTo)
        this.view.setTextColor(textColorTo)
        this.view.letterSpacing = letterSpacingTo
    }

    override fun onEnd() { }
}
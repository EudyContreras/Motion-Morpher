package com.eudycontreras.motionmorpherlibrary.layouts

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet

/**
 * <h1>Class description!</h1>
 *
 * Unlicensed private property of the author and creator
 * unauthorized use of this class outside of the Soul Vibe project
 * may result on legal prosecution.
 *
 * Created by <B>Eudy Contreras</B>
 *
 * @author  Eudy Contreras
 * @version 1.0
 * @since   2018-03-31
 */
class MorphContainer : com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts.FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val mask: Path = Path()

    init {
        clipToOutline = true
    }


    override fun updateLayout() {
        requestLayout()
    }

    override fun dispatchDraw(canvas: Canvas) {

        val count = canvas.saveCount

        val top = 0f
        val left = 0f
        val bottom = top + morphHeight
        val right = left + morphWidth

        mask.rewind()
        mask.addRoundRect(left, top, right, bottom, morphCornerRadii.corners, Path.Direction.CCW)
        mask.close()

        canvas.clipPath(mask)

        canvas.restoreToCount(count)

        super.dispatchDraw(canvas)
    }
}
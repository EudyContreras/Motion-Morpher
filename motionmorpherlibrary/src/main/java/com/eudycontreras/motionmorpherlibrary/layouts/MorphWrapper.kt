package com.eudycontreras.motionmorpherlibrary.layouts

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.annotation.IdRes
import com.eudycontreras.motionmorpherlibrary.R
import com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts.FrameLayout


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 19 2019
 */


class MorphWrapper : FrameLayout, MorphContainer {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setUpAttributes(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setUpAttributes(attrs)
    }

    private fun setUpAttributes(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MorphWrapper)
        try {
            shape = typedArray.getInt(
                R.styleable.MorphWrapper_mw_shapeType,
                MorphLayout.RECTANGULAR
            )
            startViewId = typedArray.getResourceId(R.styleable.MorphWrapper_mw_startLayout, -1)
            endViewId = typedArray.getResourceId(R.styleable.MorphWrapper_mw_endLayout, -1)

            val radius = typedArray.getDimension(R.styleable.MorphWrapper_mw_radius, 0f)
            val topLeft = typedArray.getDimension(R.styleable.MorphWrapper_mw_topLeftCornerRadius, radius)
            val topRight = typedArray.getDimension(R.styleable.MorphWrapper_mw_topRightCornerRadius, radius)
            val bottomRight = typedArray.getDimension(R.styleable.MorphWrapper_mw_bottomRightCornerRadius, radius)
            val bottomLeft = typedArray.getDimension(R.styleable.MorphWrapper_mw_bottomLeftCornerRadius, radius)

            applyDrawable(shape, topLeft, topRight, bottomRight, bottomLeft)
        } finally {
            typedArray.recycle()
        }
    }

    private val mask: Path = Path()

    @IdRes private var startViewId: Int = -1
    @IdRes private var endViewId: Int = -1

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

    override fun getStartState(): MorphLayout {
        return this.findViewById<View>(startViewId) as MorphLayout
    }

    override fun getEndState(): MorphLayout {
        return this.findViewById<View>(endViewId) as MorphLayout
    }
}
/*
package com.eudycontreras.motionmorpherlibrary.layouts

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.annotation.IdRes
import com.eudycontreras.motionmorpherlibrary.R
import com.eudycontreras.motionmorpherlibrary.interfaces.Clipable
import com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts.ConstraintLayout
import com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts.FrameLayout


*/
/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 19 2019
 *//*


class MorphWrapperConstraint : ConstraintLayout, MorphContainer, Clipable {

    @IdRes private var startViewId: Int = -1
    @IdRes private var endViewId: Int = -1

    private val mask: Path = Path()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setUpAttributes(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setUpAttributes(attrs)
    }

    private fun setUpAttributes(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MorphWrapperConstraint)
        try {
            shape = typedArray.getInt(
                R.styleable.MorphWrapper_mw_shapeType,
                MorphLayout.RECTANGULAR
            )

            startViewId = typedArray.getResourceId(R.styleable.MorphWrapperConstraint_mwc_startLayout, -1)
            endViewId = typedArray.getResourceId(R.styleable.MorphWrapperConstraint_mwc_endLayout, -1)

            val radius = typedArray.getDimension(R.styleable.MorphWrapperConstraint_mwc_radius, 0f)
            val topLeft = typedArray.getDimension(R.styleable.MorphWrapperConstraint_mwc_topLeftCornerRadius, radius)
            val topRight = typedArray.getDimension(R.styleable.MorphWrapperConstraint_mwc_topRightCornerRadius, radius)
            val bottomRight = typedArray.getDimension(R.styleable.MorphWrapperConstraint_mwc_bottomRightCornerRadius, radius)
            val bottomLeft = typedArray.getDimension(R.styleable.MorphWrapperConstraint_mwc_bottomLeftCornerRadius, radius)

            applyDrawable(shape, topLeft, topRight, bottomRight, bottomLeft)
        } finally {
            typedArray.recycle()
        }
    }

    init {
        clipToOutline = true
    }

    override fun updateLayout() {
        requestLayout()
    }

    override fun dispatchDraw(canvas: Canvas) {

        clipChildren(mask, canvas, cornerRadii.corners, morphWidth, morphHeight)

        super.dispatchDraw(canvas)
    }

    override fun getStartState(): MorphLayout {
        return this.findViewById<View>(startViewId) as MorphLayout
    }

    override fun getEndState(): MorphLayout {
        return this.findViewById<View>(endViewId) as MorphLayout
    }

    override fun hasStartState(): Boolean {
        return startViewId != -1
    }
}*/

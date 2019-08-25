package com.eudycontreras.motionmorpherlibrary.customViews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.widget.ImageView
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii


open class RoundedImageView : ImageView {

    private var path: Path = Path()

    private var paint: Paint = Paint().apply {
        isAntiAlias = true
        color = -0xbdbdbe
    }

    var cornersChanged: Boolean = false
        private set

    var corners: CornerRadii = CornerRadii()
        private set

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setupAttributes(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        setupAttributes(attrs)
    }

    private fun setupAttributes(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, com.eudycontreras.motionmorpherlibrary.R.styleable.RoundedImageView)
        try {
            val radius = typedArray.getDimension(com.eudycontreras.motionmorpherlibrary.R.styleable.RoundedImageView_riv_radius, MIN_OFFSET)
            val topLeft = typedArray.getDimension(com.eudycontreras.motionmorpherlibrary.R.styleable.RoundedImageView_riv_topLeftCornerRadius, radius)
            val topRight = typedArray.getDimension(com.eudycontreras.motionmorpherlibrary.R.styleable.RoundedImageView_riv_topRightCornerRadius, radius)
            val bottomRight = typedArray.getDimension(com.eudycontreras.motionmorpherlibrary.R.styleable.RoundedImageView_riv_bottomRightCornerRadius, radius)
            val bottomLeft = typedArray.getDimension(com.eudycontreras.motionmorpherlibrary.R.styleable.RoundedImageView_riv_bottomLeftCornerRadius, radius)

            applyCorners(topLeft, topRight, bottomRight, bottomLeft)
        } finally {
            typedArray.recycle()
        }
    }

    private fun applyCorners(topLeft: Float = MIN_OFFSET, topRight: Float = MIN_OFFSET, bottomRight: Float = MIN_OFFSET, bottomLeft: Float = MIN_OFFSET) {
        corners.apply(topLeft, topRight, bottomRight, bottomLeft)
    }

    fun updateCornerRadii(index: Int, corner: Float) {
        corners[index] = corner
        cornersChanged = true
        //recomputeCorners()
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (drawable !is BitmapDrawable)
            return

        if (changed || cornersChanged) {

            recomputeCorners()

            cornersChanged = false
        }
    }

    private fun recomputeCorners() {
        if (width == 0 || height == 0) {
            return
        }
        val fullSizeBitmap = (drawable as BitmapDrawable).bitmap

        val scaledWidth = measuredWidth
        val scaledHeight = measuredHeight

        val scaledBitmap = if (scaledWidth == fullSizeBitmap.width && scaledHeight == fullSizeBitmap.height) {
            fullSizeBitmap
        } else {
            Bitmap.createScaledBitmap(fullSizeBitmap, scaledWidth, scaledHeight, true)
        }

        val shader = BitmapShader(scaledBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        paint.shader = shader
    }

    override fun onDraw(canvas: Canvas) {
        path.rewind()
        path.addRoundRect(MIN_OFFSET, MIN_OFFSET, measuredWidth.toFloat(), measuredHeight.toFloat(), corners.corners, Path.Direction.CCW)
        path.close()

        canvas.drawPath(path, paint)
    }
}
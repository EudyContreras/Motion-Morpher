package com.eudycontreras.motionmorpherlibrary.customViews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.widget.ImageView
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii


class RoundedImageView : ImageView {

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
            val radius = typedArray.getDimension(com.eudycontreras.motionmorpherlibrary.R.styleable.RoundedImageView_riv_radius, 0f)
            val topLeft = typedArray.getDimension(com.eudycontreras.motionmorpherlibrary.R.styleable.RoundedImageView_riv_topLeftCornerRadius, radius)
            val topRight = typedArray.getDimension(com.eudycontreras.motionmorpherlibrary.R.styleable.RoundedImageView_riv_topRightCornerRadius, radius)
            val bottomRight = typedArray.getDimension(com.eudycontreras.motionmorpherlibrary.R.styleable.RoundedImageView_riv_bottomRightCornerRadius, radius)
            val bottomLeft = typedArray.getDimension(com.eudycontreras.motionmorpherlibrary.R.styleable.RoundedImageView_riv_bottomLeftCornerRadius, radius)

            applyCorners(topLeft, topRight, bottomRight, bottomLeft)
        } finally {
            typedArray.recycle()
        }
    }

    private fun applyCorners(topLeft: Float = 0f, topRight: Float = 0f, bottomRight: Float = 0f, bottomLeft: Float = 0f) {
        corners.apply(topLeft, topRight, bottomRight, bottomLeft)
    }

    fun updateCorners(index: Int, corner: Float) {
        corners[index] = corner
        cornersChanged = true
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (changed || cornersChanged) {
            if (width == 0 || height == 0) {
                return
            }
            val fullSizeBitmap = (drawable as BitmapDrawable).bitmap

            val scaledWidth = measuredWidth
            val scaledHeight = measuredHeight

            val scaledBitmap: Bitmap = if (scaledWidth == fullSizeBitmap.width && scaledHeight == fullSizeBitmap.height) {
                fullSizeBitmap
            } else {
                Bitmap.createScaledBitmap(fullSizeBitmap, scaledWidth, scaledHeight, true)
            }

            val shader = BitmapShader(scaledBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

            paint.shader = shader

            path.rewind()
            path.addRoundRect(0f, 0f, scaledWidth.toFloat(), scaledHeight.toFloat(), corners.corners, Path.Direction.CCW)
            path.close()

            cornersChanged = false
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }
}
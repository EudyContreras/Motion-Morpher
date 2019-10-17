package com.eudycontreras.motionmorpherlibrary.drawables

import android.graphics.*
import android.graphics.drawable.Drawable
import com.eudycontreras.motionmorpherlibrary.globals.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since September 07 2019
 */

class RoundedBitmapDrawable(
    bitmap: Bitmap,
    cornerRadii: CornerRadii = CornerRadii()
) : Drawable() {

    private var path: Path = Path()

    var bitmap: Bitmap = bitmap
        private set

    var cornersChanged: Boolean = false
        private set

    var corners: CornerRadii = cornerRadii
        private set

    var viewWidth: Int = intrinsicWidth
        private set

    var viewHeight: Int = intrinsicHeight
        private set

    private var paint: Paint = Paint().apply {
        isAntiAlias = true
        color = -0xbdbdbe
    }

    constructor(
        bitmap: Bitmap,
        width: Int,
        height: Int,
        cornerRadii: CornerRadii? = CornerRadii()
    ) : this(bitmap, cornerRadii ?: CornerRadii()) {
        this.viewWidth = width
        this.viewHeight = height
        computeDimensions()
        invalidateSelf()
    }

    override fun getOpacity(): Int {
        return paint.alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.setColorFilter(colorFilter)
        invalidateSelf()
    }

    override fun getColorFilter(): ColorFilter? {
        return paint.getColorFilter()
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        if (bounds.width() != viewWidth || bounds.height() != viewHeight) {
            viewWidth = bounds.width()
            viewHeight = bounds.height()

            computeDimensions()
        }
        invalidateSelf()
    }

    fun updateCornerRadii(index: Int, corner: Float) {
        corners[index] = corner
        cornersChanged = true
        invalidateSelf()
    }

    fun updateCornerRadii(cornerRadii: CornerRadii) {
        corners = cornerRadii
        cornersChanged = true
        invalidateSelf()
    }

    override fun setAlpha(alpha: Int) {
        val oldAlpha = paint.getAlpha()
        if (alpha != oldAlpha) {
            paint.setAlpha(alpha)
            invalidateSelf()
        }
    }

    private fun computeDimensions() {
        val fullSizeBitmap = bitmap

        val scaledWidth = viewWidth
        val scaledHeight = viewHeight

        val scaledBitmap = if (scaledWidth == fullSizeBitmap.width && scaledHeight == fullSizeBitmap.height) {
            fullSizeBitmap
        } else {
            Bitmap.createScaledBitmap(fullSizeBitmap, scaledWidth, scaledHeight, false)
        }

        paint.shader = BitmapShader(scaledBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    override fun draw(canvas: Canvas) {
        path.rewind()
        path.addRoundRect(
            MIN_OFFSET,
            MIN_OFFSET, viewWidth.toFloat(), viewHeight.toFloat(), corners.corners, Path.Direction.CCW)
        path.close()

        canvas.drawPath(path, paint)
    }
}
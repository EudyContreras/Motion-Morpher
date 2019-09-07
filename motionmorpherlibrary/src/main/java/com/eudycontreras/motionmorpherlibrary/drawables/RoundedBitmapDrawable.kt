package com.eudycontreras.motionmorpherlibrary.drawables

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.extensions.dp
import com.eudycontreras.motionmorpherlibrary.interfaces.Clipable
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

    override fun getOpacity(): Int {
        return paint.alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

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

    constructor(bitmap: Bitmap, width: Int, height: Int, cornerRadii: CornerRadii = CornerRadii()) : this(bitmap, cornerRadii) {
        recomputeImage(width, height)
        invalidateSelf()
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        recomputeImage(bounds.width(), bounds.height() / 2)
        invalidateSelf()
    }

    fun updateCornerRadii(index: Int, corner: Float) {
        corners[index] = corner
        cornersChanged = true
    }

    fun updateCornerRadii(cornerRadii: CornerRadii) {
        corners = cornerRadii
        cornersChanged = true
    }


    override fun setAlpha(alpha: Int) {
        val oldAlpha = paint.getAlpha()
        if (alpha != oldAlpha) {
            paint.setAlpha(alpha)
            invalidateSelf()
        }
    }

    fun recomputeImage(width: Int, height: Int) {
        val fullSizeBitmap = bitmap

        viewWidth = width
        viewHeight = height

        if (viewWidth <= 0 || viewHeight <= 0)
            return

        val scaledBitmap = if (viewWidth == fullSizeBitmap.width && viewHeight == fullSizeBitmap.height) {
            fullSizeBitmap
        } else {
            Bitmap.createScaledBitmap(fullSizeBitmap, viewWidth, viewHeight, true)
        }
        paint.apply {
            shader = BitmapShader(scaledBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
    }

    override fun draw(canvas: Canvas) {
        path.rewind()
        path.addRoundRect(MIN_OFFSET, MIN_OFFSET, viewWidth.toFloat(), viewHeight.toFloat(), corners.corners, Path.Direction.CCW)
        path.close()

        canvas.drawPath(path, paint)
    }
}
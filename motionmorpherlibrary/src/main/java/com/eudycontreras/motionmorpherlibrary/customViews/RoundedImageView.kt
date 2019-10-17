package com.eudycontreras.motionmorpherlibrary.customViews

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import com.eudycontreras.motionmorpherlibrary.globals.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.drawables.RoundedBitmapDrawable
import com.eudycontreras.motionmorpherlibrary.extensions.toBitmap
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii
import com.eudycontreras.motionmorpherlibrary.utilities.BitmapUtility

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 20 2019
 */

open class RoundedImageView : ImageView {

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
            val radius = typedArray.getDimension(com.eudycontreras.motionmorpherlibrary.R.styleable.RoundedImageView_riv_radius,
                MIN_OFFSET
            )
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

    fun updateCornerRadii(index: Int, corner: Float) {
        if (drawable is RoundedBitmapDrawable) {
            (drawable as RoundedBitmapDrawable).updateCornerRadii(index, corner)
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        drawable?.let {
            if (it is RoundedBitmapDrawable || (measuredWidth <= 0 || measuredHeight <= 0)) {
                super.setImageDrawable(drawable)
            } else  {
                val newDrawable = BitmapUtility.asRoundedBitmap(this, it.toBitmap())
                super.setImageDrawable(newDrawable)
            }
        }
    }

    override fun setImageResource(resId: Int) {
        setImageDrawable(ContextCompat.getDrawable(context, resId))
    }

    override fun setImageBitmap(bitmap: Bitmap?) {
        bitmap?.let {
            setImageDrawable(it.toDrawable(resources))
        }
    }

    private var created: Boolean = false

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed && !created) {
            created = true
            drawable?.let {
                if (it is RoundedBitmapDrawable || (measuredWidth <= 0 || measuredHeight <= 0)) {
                    super.setImageDrawable(drawable)
                } else  {
                    val newDrawable = BitmapUtility.asRoundedBitmap(this, it.toBitmap())
                    super.setImageDrawable(newDrawable)
                }
            }
        }
    }
}
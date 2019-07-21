package com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import com.eudycontreras.motionmorpherlibrary.doWith
import com.eudycontreras.motionmorpherlibrary.drawables.MorphTransitionDrawable
import com.eudycontreras.motionmorpherlibrary.extensions.getColor
import com.eudycontreras.motionmorpherlibrary.extensions.toStateList
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout.Companion.CIRCULAR
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii
import com.eudycontreras.motionmorpherlibrary.properties.ViewBounds
import com.eudycontreras.motionmorpherlibrary.shapes.MorphShape
import com.google.android.material.floatingactionbutton.FloatingActionButton


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 19 2019
 */

class FabLayout: FloatingActionButton, MorphLayout {

    override var morphX: Float
        get() = this.x
        set(value) {
            this.x = value
        }
    override var morphY: Float
        get() = this.y
        set(value) {
            this.y = value
        }
    override var morphWidth: Float
        get() = this.width.toFloat()
        set(value) {
            this.layoutParams.width = value.toInt()
        }
    override var morphHeight: Float
        get() = this.height.toFloat()
        set(value) {
            this.layoutParams.height = value.toInt()
        }
    override var morphAlpha: Float
        get() = this.alpha
        set(value) {
            this.alpha = value
        }
    override var morphElevation: Float
        get() = this.elevation
        set(value) {
            this.elevation = value
        }
    override var morphTranslationX: Float
        get() = this.translationX
        set(value) {
            this.translationX = value
        }
    override var morphTranslationY: Float
        get() = this.translationY
        set(value) {
            this.translationY = value
        }
    override var morphTranslationZ: Float
        get() = this.translationZ
        set(value) {
            this.translationZ = value
        }
    override var morphPivotX: Float
        get() = this.pivotX
        set(value) {
            this.pivotX = value
        }
    override var morphPivotY: Float
        get() = this.pivotY
        set(value) {
            this.pivotY = value
        }
    override var morphRotation: Float
        get() = this.rotation
        set(value) {
            this.rotation = value
        }
    override var morphRotationX: Float
        get() = this.rotationX
        set(value) {
            this.rotationX = value
        }
    override var morphRotationY: Float
        get() = this.rotationY
        set(value) {
            this.rotationY = value
        }
    override var morphScaleX: Float
        get() = this.scaleX
        set(value) {
            this.scaleX = value
        }
    override var morphScaleY: Float
        get() = this.scaleY
        set(value) {
            this.scaleY = value
        }
    override var morphColor: Int
        get() = this.getColor()
        set(value) {
            this.backgroundTintList = value.toStateList()
        }
    override var morphStateList: ColorStateList?
        get() = this.backgroundTintList
        set(value) {
            this.backgroundTintList = value
        }
    override var morphCornerRadii: CornerRadii
        get() = cornerRadii
        set(value) {
            updateCorners(value)
        }
    override val morphChildCount: Int
        get() = 0

    override var morphVisibility: Int
        get() = this.visibility
        set(value) {
            this.visibility = value
        }
    override var mutateCorners: Boolean = true

    override val morphTag: Any?
        get() = this.tag

    override val windowLocationX: Int
        get() {
            this.getLocationInWindow(location)
            return location[0]
        }
    override val windowLocationY: Int
        get() {
            this.getLocationInWindow(location)
            return location[1]
        }

    override var morphBackground: Drawable
        get() = if (background == null) {
            applyDrawable(CIRCULAR)
            mutableDrawable
        } else {
            background
        }
        set(value) {
            this.background = value
        }

    override val morphShape: Int
        get() = shape

    override val viewBounds: ViewBounds
        get() {
            bounds.top = this.top
            bounds.left = this.left
            bounds.right = this.right
            bounds.bottom = this.bottom

            bounds.paddings.top = this.paddingTop
            bounds.paddings.start = this.paddingStart
            bounds.paddings.end = this.paddingEnd
            bounds.paddings.bottom = this.paddingBottom

            doWith(layoutParams as ViewGroup.MarginLayoutParams) {
                bounds.margins.top = it.topMargin
                bounds.margins.start = it.marginStart
                bounds.margins.end = it.marginEnd
                bounds.margins.bottom = it.bottomMargin
            }

            return bounds
        }

    override var animate: Boolean = true

    private var bounds: ViewBounds = ViewBounds()

    private var shape: Int = CIRCULAR

    private val location: IntArray = IntArray(2)

    private var cornerRadii: CornerRadii = CornerRadii()

    private lateinit var mutableDrawable: GradientDrawable

    constructor(context: Context) : super(context) {
        applyDrawable(shape = CIRCULAR)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        applyDrawable(shape = CIRCULAR)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        applyDrawable(shape = CIRCULAR)
    }

    override fun applyDrawable(shape: Int, topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float) {
        var drawable = GradientDrawable()

        if (background is VectorDrawable || background is BitmapDrawable) {
            return
        }

        drawable = if (background is GradientDrawable) {
            (background as GradientDrawable).mutate() as GradientDrawable
        } else {
            drawable.mutate() as GradientDrawable
        }

        if (backgroundTintList != null) {
            drawable.color = backgroundTintList
        } else {
            drawable.color = solidColor.toStateList()
        }

        drawable.shape = if (shape == MorphLayout.RECTANGULAR) {
            GradientDrawable.RECTANGLE
        } else
            GradientDrawable.OVAL

        if (shape == MorphLayout.RECTANGULAR) {
            val corners = floatArrayOf(
                topLeft, topLeft,
                topRight, topRight,
                bottomRight, bottomRight,
                bottomLeft, bottomLeft
            )

            cornerRadii = CornerRadii(corners)

            drawable.cornerRadii = cornerRadii.corners
        } else {
            mutateCorners = false
        }

        mutableDrawable = drawable
        background = drawable
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (shape == CIRCULAR) {

            if (background is RippleDrawable) {
                val corners = floatArrayOf(
                    w.toFloat(), h.toFloat(),
                    w.toFloat(), h.toFloat(),
                    w.toFloat(), h.toFloat(),
                    w.toFloat(), h.toFloat()
                )

                cornerRadii = CornerRadii(corners)

                background = drawable
                return
            }

            val drawable = (background as GradientDrawable).mutate() as GradientDrawable

            val corners = floatArrayOf(
                w.toFloat(), h.toFloat(),
                w.toFloat(), h.toFloat(),
                w.toFloat(), h.toFloat(),
                w.toFloat(), h.toFloat()
            )

            drawable.cornerRadii = corners

            cornerRadii = CornerRadii(corners)

            mutableDrawable = drawable
            background = drawable
        }
    }

    override fun getView(): View  = this

    override fun isFloatingActionButton(): Boolean = true

    override fun hasVectorDrawable(): Boolean {
        return false
    }

    override fun hasBitmapDrawable(): Boolean {
        return background is BitmapDrawable
    }

    override fun hasGradientDrawable(): Boolean {
        return background is GradientDrawable
    }

    override fun hasMorphTransitionDrawable(): Boolean {
        return background is MorphTransitionDrawable
    }

    override fun getVectorDrawable(): VectorDrawable {
        return this.drawable as VectorDrawable
    }

    override fun getGradientBackground(): GradientDrawable {
        return mutableDrawable
    }

    override fun getBitmapDrawable(): BitmapDrawable {
        return background as BitmapDrawable
    }

    override fun getMorphTransitionDrawable(): MorphTransitionDrawable {
        return background as MorphTransitionDrawable
    }

    override fun applyTransitionDrawable(transitionDrawable: MorphTransitionDrawable) {
        this.background = transitionDrawable
    }

    override fun updateCorners(cornerRadii: CornerRadii): Boolean {
        return false
    }

    override fun updateCorners(index: Int, corner: Float): Boolean {
        return false
    }

    override fun getMorphShape(): MorphShape {
        return MorphShape()
    }

    override fun updateLayout() {
        requestLayout()
    }

    override fun animator(): ViewPropertyAnimator = animate()

    override fun hasChildren(): Boolean = false

    override fun getChildViewAt(index: Int): View = this

    override fun getChildren(): Sequence<View> {
        return emptySequence()
    }

    override fun setLayer(layer: Int) {
        setLayerType(layer, null)
    }

    override fun toString(): String = tag.toString()
}

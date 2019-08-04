package com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import com.eudycontreras.motionmorpherlibrary.R
import com.eudycontreras.motionmorpherlibrary.doWith
import com.eudycontreras.motionmorpherlibrary.drawables.MorphTransitionDrawable
import com.eudycontreras.motionmorpherlibrary.extensions.getColor
import com.eudycontreras.motionmorpherlibrary.extensions.toStateList
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout.Companion.CIRCULAR
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout.Companion.RECTANGULAR
import com.eudycontreras.motionmorpherlibrary.listeners.DrawDispatchListener
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii
import com.eudycontreras.motionmorpherlibrary.properties.Margings
import com.eudycontreras.motionmorpherlibrary.properties.Paddings
import com.eudycontreras.motionmorpherlibrary.properties.ViewBounds
import com.eudycontreras.motionmorpherlibrary.shapes.MorphShape


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 19 2019
 */


class ViewLayout : View, MorphLayout {

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
            cornerRadii = value
            if (::mutableDrawable.isInitialized) {
                mutableDrawable.cornerRadii = cornerRadii.corners
            }
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

    override var mutableBackground: GradientDrawable
        get() = mutableDrawable
        set(value) {
            this.mutableDrawable = value
        }

    override var morphBackground: Drawable?
        get() = background
        set(value) {
            this.background = value
        }

    override val morphShape: Int
        get() = shape

    override val coordinates: IntArray
        get() {
            getLocationInWindow(location)
            return location
        }

    override val viewBounds: ViewBounds
        get() {
            bounds.top = top
            bounds.left = left
            bounds.right = right
            bounds.bottom = bottom

            bounds.paddings.top = paddingTop.toFloat()
            bounds.paddings.start = paddingStart.toFloat()
            bounds.paddings.end = paddingEnd.toFloat()
            bounds.paddings.bottom = paddingBottom.toFloat()

            bounds.x = coordinates[0]
            bounds.y = coordinates[1]

            bounds.width = morphWidth
            bounds.height = morphHeight

            doWith(layoutParams as ViewGroup.MarginLayoutParams) {
                bounds.margings.top = it.topMargin.toFloat()
                bounds.margings.start = it.marginStart.toFloat()
                bounds.margings.end = it.marginEnd.toFloat()
                bounds.margings.bottom = it.bottomMargin.toFloat()
            }

            return bounds
        }

    override var morphMargings: Margings
        get() = bounds.margings
        set(value) {
            bounds.margings.top = value.top
            bounds.margings.start = value.start
            bounds.margings.end = value.end
            bounds.margings.bottom = value.bottom
        }

    override var morphPaddings: Paddings
        get() = bounds.paddings
        set(value) {
            bounds.paddings.top = value.top
            bounds.paddings.start = value.start
            bounds.paddings.end = value.end
            bounds.paddings.bottom = value.bottom
        }


    override var animate: Boolean = true

    private var bounds: ViewBounds = ViewBounds(this.getView())

    private var shape: Int = RECTANGULAR

    private val location: IntArray = IntArray(2)

    private var cornerRadii: CornerRadii = CornerRadii()

    private var drawListener: DrawDispatchListener? = null

    private var dimensionSnap: MorphLayout.DimensionSnap = MorphLayout.DimensionSnap.NONE

    private lateinit var mutableDrawable: GradientDrawable

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setUpAttributes(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setUpAttributes(attrs)
    }

    private fun setUpAttributes(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ViewLayout)
        try {
            shape = typedArray.getInt(
                R.styleable.ViewLayout_vl_shapeType,
                RECTANGULAR
            )
            animate = typedArray.getBoolean(R.styleable.ViewLayout_vl_animate, true)

            val radius = typedArray.getDimension(R.styleable.ViewLayout_vl_radius, 0f)
            val topLeft = typedArray.getDimension(R.styleable.ViewLayout_vl_topLeftCornerRadius, radius)
            val topRight = typedArray.getDimension(R.styleable.ViewLayout_vl_topRightCornerRadius, radius)
            val bottomRight = typedArray.getDimension(R.styleable.ViewLayout_vl_bottomRightCornerRadius, radius)
            val bottomLeft = typedArray.getDimension(R.styleable.ViewLayout_vl_bottomLeftCornerRadius, radius)

             dimensionSnap = MorphLayout.DimensionSnap.from(typedArray.getInt(R.styleable.ViewLayout_vl_snap_dimensions, -1))

            applyDrawable(shape, topLeft, topRight, bottomRight, bottomLeft)
        } finally {
            typedArray.recycle()
        }

        bounds = ViewBounds(this.getView())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        when(dimensionSnap) {
            MorphLayout.DimensionSnap.WIDTH -> setMeasuredDimension(measuredWidth, measuredWidth)
            MorphLayout.DimensionSnap.HEIGHT -> setMeasuredDimension(measuredHeight, measuredHeight)
            MorphLayout.DimensionSnap.NONE -> setMeasuredDimension(measuredWidth, measuredHeight)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (shape == CIRCULAR && background !is VectorDrawable) {

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

    override fun isFloatingActionButton(): Boolean = false

    override fun hasVectorDrawable(): Boolean {
        return background is VectorDrawable
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
        return (background as VectorDrawable).mutate() as VectorDrawable
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return false
        }

        for (index in 0 until cornerRadii.size) {
            val corner = cornerRadii[index]
            cornerRadii[index] = corner
        }

        mutableDrawable.cornerRadii = cornerRadii.corners
        return true
    }

    override fun updateCorners(index: Int, corner: Float): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return false
        }

        cornerRadii[index] = corner
        mutableDrawable.cornerRadii = cornerRadii.corners
        return true
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

    fun setListener(listener: DrawDispatchListener) {
        this.drawListener = listener
    }
}

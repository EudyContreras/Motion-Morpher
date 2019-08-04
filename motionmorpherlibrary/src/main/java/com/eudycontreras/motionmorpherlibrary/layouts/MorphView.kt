package com.eudycontreras.motionmorpherlibrary.layouts

import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.widget.TextView
import androidx.core.view.children
import com.eudycontreras.motionmorpherlibrary.customViews.RoundedImageView
import com.eudycontreras.motionmorpherlibrary.doWith
import com.eudycontreras.motionmorpherlibrary.drawables.MorphTransitionDrawable
import com.eudycontreras.motionmorpherlibrary.extensions.getColor
import com.eudycontreras.motionmorpherlibrary.extensions.getProperties
import com.eudycontreras.motionmorpherlibrary.extensions.setProperties
import com.eudycontreras.motionmorpherlibrary.extensions.toStateList
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii
import com.eudycontreras.motionmorpherlibrary.properties.Margings
import com.eudycontreras.motionmorpherlibrary.properties.Paddings
import com.eudycontreras.motionmorpherlibrary.properties.ViewBounds
import com.eudycontreras.motionmorpherlibrary.shapes.MorphShape
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 19 2019
 */

class MorphView: MorphLayout {

    private val view: View

    override var morphX: Float
        get() = view.x
        set(value) {
            view.x = value
        }
    override var morphY: Float
        get() = view.y
        set(value) {
            view.y = value
        }
    override var morphWidth: Float
        get() = view.width.toFloat()
        set(value) {
            view.layoutParams.width = value.toInt()
            recomputeChange()
        }
    override var morphHeight: Float
        get() = view.height.toFloat()
        set(value) {
            view.layoutParams.height = value.toInt()
            recomputeChange()
        }
    override var morphAlpha: Float
        get() = view.alpha
        set(value) {
            view.alpha = value
        }
    override var morphElevation: Float
        get() = view.elevation
        set(value) {
            view.elevation = value
        }
    override var morphTranslationX: Float
        get() = view.translationX
        set(value) {
            view.translationX = value
        }
    override var morphTranslationY: Float
        get() = view.translationY
        set(value) {
            view.translationY = value
        }
    override var morphTranslationZ: Float
        get() = view.translationZ
        set(value) {
            view.translationZ = value
        }
    override var morphPivotX: Float
        get() = view.pivotX
        set(value) {
            view.pivotX = value
        }
    override var morphPivotY: Float
        get() = view.pivotY
        set(value) {
            view.pivotY = value
        }
    override var morphRotation: Float
        get() = view.rotation
        set(value) {
            view.rotation = value
        }
    override var morphRotationX: Float
        get() = view.rotationX
        set(value) {
            view.rotationX = value
        }
    override var morphRotationY: Float
        get() = view.rotationY
        set(value) {
            view.rotationY = value
        }
    override var morphScaleX: Float
        get() = view.scaleX
        set(value) {
            view.scaleX = value
        }
    override var morphScaleY: Float
        get() = view.scaleY
        set(value) {
            view.scaleY = value
        }
    override var morphColor: Int
        get() = view.getColor()
        set(value) {
            view.backgroundTintList = value.toStateList()
        }
    override var morphStateList: ColorStateList?
        get() = view.backgroundTintList
        set(value) {
            view.backgroundTintList = value
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
        get() = if (view is ViewGroup) view.childCount else 0

    override var morphVisibility: Int
        get() = view.visibility
        set(value) {
            view.visibility = value
        }
    override var mutateCorners: Boolean = true

    override val morphTag: Any?
        get() = view.tag

    override val windowLocationX: Int
        get() {
            view.getLocationInWindow(location)
            return location[0]
        }
    override val windowLocationY: Int
        get() {
            view.getLocationInWindow(location)
            return location[1]
        }

    override val morphShape: Int
        get() = shape


    override var mutableBackground: GradientDrawable
        get() = mutableDrawable
        set(value) {
            this.mutableDrawable = value
        }

    override var morphBackground: Drawable?
        get() = view.background
        set(value) {
            view.background = value
        }

    override val coordinates: IntArray
        get() {
            view.getLocationInWindow(location)
            return location
        }

    override val viewBounds: ViewBounds
        get() {
            bounds.top = view.top
            bounds.left = view.left
            bounds.right = view.right
            bounds.bottom = view.bottom

            bounds.paddings.top = view.paddingTop.toFloat()
            bounds.paddings.start = view.paddingStart.toFloat()
            bounds.paddings.end = view.paddingEnd.toFloat()
            bounds.paddings.bottom = view.paddingBottom.toFloat()

            bounds.x = coordinates[0]
            bounds.y = coordinates[1]

            bounds.width = morphWidth
            bounds.height = morphHeight

            doWith(view.layoutParams as ViewGroup.MarginLayoutParams) {
                bounds.margings.top = it.topMargin.toFloat()
                bounds.margings.start = it.marginStart.toFloat()
                bounds.margings.end = it.marginEnd.toFloat()
                bounds.margings.bottom = it.bottomMargin.toFloat()
            }

            return bounds
        }

    override var morphMargings: Margings
        get() = viewBounds.margings
        set(value) {
            bounds.margings.top = value.top
            bounds.margings.start = value.start
            bounds.margings.end = value.end
            bounds.margings.bottom = value.bottom
        }

    override var morphPaddings: Paddings
        get() = viewBounds.paddings
        set(value) {
            bounds.paddings.top = value.top
            bounds.paddings.start = value.start
            bounds.paddings.end = value.end
            bounds.paddings.bottom = value.bottom
        }

    private var isActionButton: Boolean = false

    private var isTextView: Boolean = false

    override var animate: Boolean = true

    private var bounds: ViewBounds = ViewBounds(this.getView())

    private var shape: Int = MorphLayout.RECTANGULAR

    private val location: IntArray = IntArray(2)

    private var cornerRadii: CornerRadii = CornerRadii()

    private lateinit var mutableDrawable: GradientDrawable

    constructor(view: View): this(view, MorphLayout.RECTANGULAR)

    constructor(view: View, shape: Int): this(view, shape, 0f)

    constructor(view: View, shape: Int, cornerRadius: Float) :this(view, shape, cornerRadius, cornerRadius, cornerRadius, cornerRadius)

    constructor(view: View, shape: Int, topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float) {
        this.view = view

        applyDrawable(shape, topLeft, topRight, bottomRight, bottomLeft)

        this.view.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            bounds = ViewBounds(this.getView())
        }

        if (view is RoundedImageView) {
            morphCornerRadii = view.corners

            cornerRadii.changeListener.add {
                for (index in 0 until it.size) {
                    view.corners.corners[index] = it[index]
                }
            }
        }
    }

    private fun recomputeChange() {
        if (shape == MorphLayout.CIRCULAR && morphBackground !is VectorDrawable) {

            val drawable = (morphBackground as GradientDrawable).mutate() as GradientDrawable

            val corners = floatArrayOf(
                morphWidth, morphHeight,
                morphWidth, morphHeight,
                morphWidth, morphHeight,
                morphWidth, morphHeight
            )

            drawable.cornerRadii = corners

            cornerRadii = CornerRadii(corners)

            mutableDrawable = drawable
            morphBackground = drawable
        }
    }

    override fun getView(): View = view

    override fun isFloatingActionButton(): Boolean = isActionButton

    override fun hasVectorDrawable(): Boolean {
        return view.background is VectorDrawable
    }

    override fun hasBitmapDrawable(): Boolean {
        return view.background is BitmapDrawable
    }

    override fun hasGradientDrawable(): Boolean {
        return view.background is GradientDrawable
    }

    override fun hasMorphTransitionDrawable(): Boolean {
        return view.background is MorphTransitionDrawable
    }

    override fun getVectorDrawable(): VectorDrawable {
        return (view.background as VectorDrawable).mutate() as VectorDrawable
    }

    override fun getGradientBackground(): GradientDrawable {
        return mutableDrawable
    }

    override fun getBitmapDrawable(): BitmapDrawable {
        return view.background as BitmapDrawable
    }

    override fun getMorphTransitionDrawable(): MorphTransitionDrawable {
        return view.background as MorphTransitionDrawable
    }

    override fun applyTransitionDrawable(transitionDrawable: MorphTransitionDrawable) {
        view.background = transitionDrawable
    }

    override fun updateCorners(cornerRadii: CornerRadii): Boolean {
        return updateCorners(cornerRadii.corners)
    }

    fun updateCorners(cornerRadii: FloatArray): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return false
        }

        for (index in 0 until cornerRadii.size) {
            this.cornerRadii[index] = cornerRadii[index]
        }

        mutableDrawable.cornerRadii = this.cornerRadii.corners
        return true
    }

    override fun updateCorners(index: Int, corner: Float): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return false
        }

        cornerRadii[index] = corner
        mutableDrawable.cornerRadii = cornerRadii.corners

        if (view is RoundedImageView) {
            view.updateCorners(index, corner)
        }
        return true
    }

    override fun getMorphShape(): MorphShape {
        return MorphShape()
    }

    override fun updateLayout() {
        view.requestLayout()
    }

    override fun animator(): ViewPropertyAnimator = view.animate()

    override fun getChildViewAt(index: Int): View = if (view is ViewGroup) view.getChildAt(index) else view

    override fun hasChildren(): Boolean = this.morphChildCount > 0

    override fun getChildren(): Sequence<View> {
        return if (view is ViewGroup) view.children.asSequence() else emptySequence()
    }

    override fun setLayer(layer: Int) {
        view.setLayerType(layer, null)
    }

    override fun toString(): String = view.tag.toString()

    companion object {

        fun makeMorphable(view: View): MorphLayout {
            if (view is MorphLayout) {
                return view
            }
            if (view is RoundedImageView) {
                return MorphView(view, MorphLayout.RECTANGULAR)
            }
            if (view is TextView) {
                return MorphView(view, MorphLayout.RECTANGULAR).apply { isTextView = true }
            }
            if (view is FloatingActionButton) {
                MorphView(view, MorphLayout.CIRCULAR).apply {
                    (this.view as FloatingActionButton).setProperties(view.getProperties())
                    this.view.layoutParams = view.layoutParams
                    this.view.backgroundTintList = view.backgroundTintList
                    this.view.supportImageTintList = view.supportImageTintList
                    this.view.compatElevation = view.compatElevation
                    this.view.customSize = view.customSize
                    this.isActionButton = true
                    this.applyDrawable(MorphLayout.CIRCULAR)

                    this.view.drawable?.let { this.view.setImageDrawable(it) }

   /*                 val parent = view.parent as ViewGroup

                    parent.addView(this)*/
                }
            }
            return MorphView(view)
        }
    }
}

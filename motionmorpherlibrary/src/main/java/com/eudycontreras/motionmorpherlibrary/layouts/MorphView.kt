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
import androidx.core.view.children
import com.eudycontreras.motionmorpherlibrary.drawables.MorphTransitionDrawable
import com.eudycontreras.motionmorpherlibrary.extensions.getColor
import com.eudycontreras.motionmorpherlibrary.extensions.toStateList
import com.eudycontreras.motionmorpherlibrary.listeners.DrawDispatchListener
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii
import com.eudycontreras.motionmorpherlibrary.shapes.MorphShape

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
        }
    override var morphHeight: Float
        get() = view.height.toFloat()
        set(value) {
            view.layoutParams.height = value.toInt()
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
            updateCorners(value)
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

    override var morphBackground: Drawable
        get() = view.background
        set(value) {
            view.background = value
        }

    override var animate: Boolean = true

    private var shape: Int = MorphLayout.RECTANGULAR

    private val location: IntArray = IntArray(2)

    private var cornerRadii: CornerRadii = CornerRadii()

    private var drawListener: DrawDispatchListener? = null

    private lateinit var mutableDrawable: GradientDrawable

    constructor(view: View): this(view, MorphLayout.RECTANGULAR)

    constructor(view: View, shape: Int): this(view, shape, 0f)

    constructor(view: View, shape: Int, cornerRadius: Float) :this(view, shape, cornerRadius, cornerRadius, cornerRadius, cornerRadius)

    constructor(view: View, shape: Int, topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float) {
        this.view = view
        applyDrawable(shape, topLeft, topRight, bottomRight, bottomLeft)
    }

    override fun applyDrawable(shape: Int, topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float) {
        var drawable = GradientDrawable()

        if (view.background is VectorDrawable || view.background is BitmapDrawable) {
            return
        }

        drawable = if (view.background is GradientDrawable) {
            (view.background as GradientDrawable).mutate() as GradientDrawable
        } else {
            drawable.mutate() as GradientDrawable
        }

        if (view.backgroundTintList != null) {
            drawable.color = view.backgroundTintList
        } else {
            drawable.color = view.solidColor.toStateList()
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
        view.background = drawable
    }

    override fun isFloatingActionButton(): Boolean = false

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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return false
        }

        for (index in 0 until cornerRadii.size) {
            val corner = cornerRadii[index]
            this.cornerRadii[index] = corner
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
}

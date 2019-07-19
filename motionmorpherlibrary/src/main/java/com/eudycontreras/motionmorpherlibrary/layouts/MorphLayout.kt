package com.eudycontreras.motionmorpherlibrary.layouts

import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.VectorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.eudycontreras.motionmorpherlibrary.drawables.MorphTransitionDrawable
import com.eudycontreras.motionmorpherlibrary.extensions.getProperties
import com.eudycontreras.motionmorpherlibrary.extensions.setProperties
import com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts.FabLayout
import com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts.TextLayout
import com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts.ViewLayout
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * <h1>Class description!</h1>
 *
 *
 *
 * **Note:** Unlicensed private property of the author and creator
 * unauthorized use of this class outside of the Soul Vibe project
 * may result on legal prosecution.
 *
 *
 * Created by <B>Eudy Contreras</B>
 *
 * @author  Eudy Contreras
 * @version 1.0
 * @since   2018-03-31
 */
interface MorphLayout {
    var morphX: Float
    var morphY: Float
    var morphWidth: Float
    var morphHeight: Float
    var morphAlpha: Float
    var morphElevation: Float
    var morphTranslationX: Float
    var morphTranslationY: Float
    var morphTranslationZ: Float
    var morphPivotX: Float
    var morphPivotY: Float
    var morphRotation: Float
    var morphRotationX: Float
    var morphRotationY: Float
    var morphScaleX: Float
    var morphScaleY: Float
    val morphColor: Int
    var morphStateList: ColorStateList?
    var morphCornerRadii: CornerRadii
    var morphVisibility: Int
    val morphChildCount: Int
    var mutateCorners: Boolean
    val morphShape: Int
    val morphTag: Any?
    val windowLocationX: Int
    val windowLocationY: Int
    var morphBackground: Drawable
    fun animator(): ViewPropertyAnimator
    fun updateLayout()
    fun hasChildren(): Boolean
    fun getChildViewAt(index: Int): View
    fun getChildren(): Sequence<View>
    fun hasVectorDrawable(): Boolean
    fun hasGradientDrawable(): Boolean
    fun hasBitmapDrawable(): Boolean
    fun isFloatingActionButton(): Boolean
    fun hasMorphTransitionDrawable(): Boolean
    fun getGradientBackground(): GradientDrawable
    fun getVectorDrawable(): VectorDrawable
    fun getBitmapDrawable(): BitmapDrawable
    fun getMorphTransitionDrawable(): MorphTransitionDrawable
    fun applyTransitionDrawable(transitionDrawable: MorphTransitionDrawable)
    fun applyDrawable(shape: Int = RECTANGULAR, topLeft: Float = 0f, topRight: Float = 0f, bottomRight: Float = 0f, bottomLeft: Float = 0f)
    fun updateCorners(cornerRadii: CornerRadii): Boolean
    fun updateCorners(index: Int, corner: Float): Boolean
    fun getMorphShape(): MorphShape
    fun setLayer(layer: Int)

    enum class DimensionSnap(val value: Int) {
        WIDTH(0), HEIGHT(1), NONE(-1);

        companion object {
           fun from(value: Int): DimensionSnap {
               return when (value) {
                   0 -> WIDTH
                   1 -> HEIGHT
                   else -> NONE
               }
           }
        }
    }
    companion object {
        const val CIRCULAR = 0
        const val RECTANGULAR = 1

        fun makeMorphable(viewGroup: ViewGroup): View{
            return when (viewGroup) {
                is ConstraintLayout -> {
                    viewGroup as com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts.ConstraintLayout
                    viewGroup.apply {
                        this.applyDrawable()
                    }
                }
                is FrameLayout -> {
                    viewGroup as com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts.FrameLayout
                    viewGroup.apply {
                        this.applyDrawable()
                    }
                }
                is LinearLayout -> {
                    viewGroup as com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts.LinearLayout
                    viewGroup.apply {
                        this.applyDrawable()
                    }
                }
                else -> viewGroup
            }
        }

        fun makeMorphable(view: View): View {
            if (view is ViewGroup) {
                return makeMorphable(view)
            }

            return when (view) {
                is TextView -> {
                    view as TextLayout
                    view.apply {
                        this.applyDrawable()
                    }
                }
                is FloatingActionButton -> {
                    FabLayout(view.context).apply {
                        this.setProperties(view.getProperties())
                        this.layoutParams = view.layoutParams
                        this.backgroundTintList = view.backgroundTintList
                        this.supportImageTintList = view.supportImageTintList
                        this.compatElevation = view.compatElevation
                        this.customSize = view.customSize
                        this.applyDrawable(CIRCULAR)
                        //this.setRippleColor(view.rippleColorStateList)
                        view.drawable?.let { this.setImageDrawable(it) }

                        val parent = view.parent as ViewGroup
                        parent.addView(this)
                    }
                }
                else -> {
                    view as ViewLayout
                    view.apply {
                        this.applyDrawable()
                    }
                }
            }
        }
    }
}
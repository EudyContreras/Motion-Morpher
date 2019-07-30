package com.eudycontreras.motionmorpherlibrary.layouts

import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.VectorDrawable
import android.view.View
import android.view.ViewPropertyAnimator
import com.eudycontreras.motionmorpherlibrary.drawables.MorphTransitionDrawable
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii
import com.eudycontreras.motionmorpherlibrary.properties.ViewBounds
import com.eudycontreras.motionmorpherlibrary.shapes.MorphShape

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 19 2019
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
    val morphShape: Int
    val morphTag: Any?
    var animate: Boolean
    var mutateCorners: Boolean
    val windowLocationX: Int
    val windowLocationY: Int
    val viewBounds: ViewBounds
    var morphBackground: Drawable
    fun getView(): View
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
    }
}
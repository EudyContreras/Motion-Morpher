package com.eudycontreras.motionmorpherlibrary.properties

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable

data class AnimatedProperties(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    var alpha: Float,
    val elevation: Float,
    var translationX: Float,
    var translationY: Float,
    val translationZ: Float,
    val pivotX: Float,
    val pivotY: Float,
    val rotation: Float,
    val rotationX: Float,
    val rotationY: Float,
    var scaleX: Float,
    var scaleY: Float,
    val color: Int,
    val stateList: ColorStateList?,
    var cornerRadii: CornerRadii,
    val viewBounds: ViewBounds,
    val windowLocationX: Int,
    val windowLocationY: Int,
    val background: Drawable?,
    val hasVectorBackground: Boolean,
    val hasBitmapBackground: Boolean,
    val hasGradientBackground: Boolean,
    val tag: String
    ) {
        fun getDeltaCoordinates() = Coordinates(translationX, translationY)

        fun getBounds(): Bounds {
            return Bounds(
                windowLocationX,
                windowLocationY,
                width,
                height
            )
        }
        override fun toString() = tag
    }
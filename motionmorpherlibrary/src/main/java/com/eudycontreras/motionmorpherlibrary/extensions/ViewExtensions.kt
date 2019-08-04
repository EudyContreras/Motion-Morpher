package com.eudycontreras.motionmorpherlibrary.extensions

import android.graphics.drawable.ColorDrawable
import android.view.View
import com.eudycontreras.motionmorpherlibrary.Action
import com.eudycontreras.motionmorpherlibrary.Morpher
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.properties.ViewProperties








/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 18 2019
 */

fun View.getColor(default: Int = 0x000000): Int {
    val background = this.backgroundTintList
    if (background != null) {
        return background.defaultColor
    } else {
        val drawable = this.background

        if (drawable is ColorDrawable) {
            return drawable.color
        }
    }
    return default
}

fun View.hide(duration: Long = 0L, onEnd: Action = null) {
    if (duration == 0L) {
        this.alpha = 0f
        onEnd?.invoke()
        return
    }
    this.animate()
        .alpha(0f)
        .withEndAction(onEnd)
        .setDuration(duration)
        .start()
}

fun View.show(duration: Long = 0L, onEnd: Action = null) {
    if (duration == 0L) {
        this.alpha = 1f
        onEnd?.invoke()
        return
    }
    this.animate()
        .alpha(1f)
        .withEndAction(onEnd)
        .setDuration(duration)
        .start()
}

fun MorphLayout.hide(duration: Long = 0L, onEnd: Action = null) {
    if (duration == 0L) {
        this.morphAlpha = 0f
        onEnd?.invoke()
        return
    }
    this.animator()
        .alpha(0f)
        .withEndAction(onEnd)
        .setDuration(duration)
        .start()
}

fun MorphLayout.show(duration: Long = 0L, onEnd: Action = null) {
    if (duration == 0L) {
        this.morphAlpha = 1f
        onEnd?.invoke()
        return
    }
    this.animator()
        .alpha(1f)
        .withEndAction(onEnd)
        .setDuration(duration)
        .start()
}

fun MorphLayout.getProperties(): Morpher.Properties {
    val x = this.morphX
    val y = this.morphY
    val width = this.morphWidth
    val height = this.morphHeight
    val alpha = this.morphAlpha
    val elevation = this.morphElevation
    val translationX = this.morphTranslationX
    val translationY = this.morphTranslationY
    val translationZ = this.morphTranslationZ
    val locationX = this.windowLocationX
    val locationY = this.windowLocationY
    val pivotX = this.morphPivotX
    val pivotY = this.morphPivotY
    val rotation = this.morphRotation
    val rotationX = this.morphRotationX
    val rotationY = this.morphRotationY
    val scaleX = this.morphScaleX
    val scaleY = this.morphScaleY
    val color = this.morphColor
    val stateList = this.morphStateList
    val cornerRadii = this.morphCornerRadii.getCopy()
    val background = this.morphBackground!!.constantState?.newDrawable()
    val hasVectorBackground = this.hasVectorDrawable()
    val hasBitmapBackground = this.hasBitmapDrawable()
    val hasGradientBackground = this.hasGradientDrawable()
    val tag = this.morphTag.toString()
    return Morpher.Properties(
        x,
        y,
        width,
        height,
        alpha,
        elevation,
        translationX,
        translationY,
        translationZ,
        pivotX,
        pivotY,
        rotation,
        rotationX,
        rotationY,
        scaleX,
        scaleY,
        color,
        stateList,
        cornerRadii,
        locationX,
        locationY,
        background,
        hasVectorBackground,
        hasBitmapBackground,
        hasGradientBackground,
        tag
    )
}

fun View.setProperties(properties: ViewProperties) {
    this.x = properties.x
    this.y = properties.y
    this.z = properties.z
    this.alpha = properties.alpha
    this.elevation = properties.elevation
    this.translationX = properties.translationX
    this.translationY = properties.translationY
    this.translationZ = properties.translationZ
    this.pivotX = properties.pivotX
    this.pivotY = properties.pivotY
    this.rotation = properties.rotation
    this.rotationX = properties.rotationX
    this.rotationY = properties.rotationY
    this.scaleX = properties.scaleX
    this.scaleY = properties.scaleY
    this.top = properties.top
    this.left = properties.left
    this.right = properties.right
    this.bottom = properties.bottom
    this.tag = properties.tag
}

fun View.getProperties(): ViewProperties {
    val x = this.x
    val y = this.y
    val z = this.z
    val alpha = this.alpha
    val elevation = this.elevation
    val translationX = this.translationX
    val translationY = this.translationY
    val translationZ = this.translationZ
    val pivotX = this.pivotX
    val pivotY = this.pivotY
    val rotation = this.rotation
    val rotationX = this.rotationX
    val rotationY = this.rotationY
    val scaleX = this.scaleX
    val scaleY = this.scaleY
    val top = this.top
    val left = this.left
    val right = this.right
    val bottom = this.bottom
    val tag = this.tag
    return ViewProperties(
        x,
        y,
        z,
        alpha,
        elevation,
        translationX,
        translationY,
        translationZ,
        pivotX,
        pivotY,
        rotation,
        rotationX,
        rotationY,
        scaleX,
        scaleY,
        top,
        left,
        right,
        bottom,
        tag = tag?.toString() ?: ""
    )
}
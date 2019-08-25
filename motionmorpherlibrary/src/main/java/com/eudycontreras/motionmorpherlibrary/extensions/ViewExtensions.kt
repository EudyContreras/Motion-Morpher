package com.eudycontreras.motionmorpherlibrary.extensions

import android.graphics.drawable.ColorDrawable
import android.view.View
import com.eudycontreras.motionmorpherlibrary.*
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.layouts.MorphView
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

fun View.hide(duration: Long = MIN_DURATION, onEnd: Action = null) {
    if (duration == MIN_DURATION) {
        this.alpha = MIN_OFFSET
        onEnd?.invoke()
        return
    }
    this.animate()
        .alpha(MIN_OFFSET)
        .withEndAction(onEnd)
        .setDuration(duration)
        .start()
}

fun View.show(duration: Long = MIN_DURATION, onEnd: Action = null) {
    if (duration == MIN_DURATION) {
        this.alpha = MAX_OFFSET
        onEnd?.invoke()
        return
    }
    this.animate()
        .alpha(MAX_OFFSET)
        .withEndAction(onEnd)
        .setDuration(duration)
        .start()
}

fun View.toMorphable(): MorphLayout {
    return if (this is MorphLayout) {
        this
    } else {
        MorphView.makeMorphable(this)
    }
}

fun MorphLayout.hide(duration: Long = MIN_DURATION, delay: Long = MIN_DURATION, onEnd: Action = null) {
    if (duration == MIN_DURATION) {
        morphAlpha = MIN_OFFSET
        morphVisibility = View.INVISIBLE
        onEnd?.invoke()
        return
    }
    this.animator()
        .alpha(MIN_OFFSET)
        .withEndAction {
            morphVisibility = View.INVISIBLE
            onEnd?.invoke()
        }
        .setStartDelay(delay)
        .setDuration(duration)
        .start()
}

fun MorphLayout.show(duration: Long = MIN_DURATION, delay: Long = MIN_DURATION, onEnd: Action = null) {
    if (duration == MIN_DURATION) {
        morphAlpha = MAX_OFFSET
        if (morphVisibility != View.VISIBLE) {
            morphVisibility = View.VISIBLE
        }
        onEnd?.invoke()
        return
    }
    if (morphVisibility != View.VISIBLE) {
        morphVisibility = View.VISIBLE
    }
    this.animator()
        .alpha(MAX_OFFSET)
        .withEndAction(onEnd)
        .setStartDelay(delay)
        .setDuration(duration)
        .start()
}

fun MorphLayout.applyProps(props: Morpher.Properties) {
    morphX = props.x
    morphY = props.y
    morphAlpha = props.alpha
    morphElevation = props.elevation
    morphTranslationX = props.translationX
    morphTranslationY = props.translationY
    morphTranslationZ = props.translationZ
    /*morphPivotX = morphWidth
    morphPivotY = morphHeight*/
    morphRotation = props.rotation
    morphRotationX = props.rotationX
    morphRotationY = props.rotationY
    morphScaleX = props.scaleX
    morphScaleY = props.scaleY
    morphStateList = props.stateList
    morphWidth = props.width
    morphHeight = props.height

    if (hasGradientDrawable() && mutateCorners) {
        updateCorners(0, props.cornerRadii[0])
        updateCorners(1, props.cornerRadii[1])
        updateCorners(2, props.cornerRadii[2])
        updateCorners(3, props.cornerRadii[3])
        updateCorners(4, props.cornerRadii[4])
        updateCorners(5, props.cornerRadii[5])
        updateCorners(6, props.cornerRadii[6])
        updateCorners(7, props.cornerRadii[7])
    }

    updateLayout()
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
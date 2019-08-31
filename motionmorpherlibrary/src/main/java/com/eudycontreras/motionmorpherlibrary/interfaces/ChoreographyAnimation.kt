package com.eudycontreras.motionmorpherlibrary.interfaces

import android.animation.TimeInterpolator
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import com.eudycontreras.motionmorpherlibrary.*
import com.eudycontreras.motionmorpherlibrary.Choreographer.Choreography
import com.eudycontreras.motionmorpherlibrary.enumerations.*
import com.eudycontreras.motionmorpherlibrary.extensions.clamp
import com.eudycontreras.motionmorpherlibrary.extensions.toArrayList
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.layouts.MorphView
import com.eudycontreras.motionmorpherlibrary.properties.*
import kotlin.math.abs


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 28 2019
 */
 
interface ChoreographyAnimation {

    val morphViews: Array<out MorphLayout>

    /*fun xSkewTo(amount: Float)
    fun ySkewTo(amount: Float)
    fun toVisibility(visibility: Int)
    fun fromVisibility(visibilityFrom: Int, visibilityTo: Int)
    fun animateFramesFor(layout: ImageView, frames: Array<Bitmap>)
    fun withRipple(ripple: Ripple)*/
    /**
     * Animates the morphViews of this [Choreography] to the center of the specified [MorphLayout]
     * Optionally uses the specified [TimeInterpolator] if any is present.
     * @param otherView The layout to which the choreography will animate its morphViews to the center of
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun centerIn(otherView: MorphLayout, interpolator: TimeInterpolator?)

    /**
     * Animates the morphViews of this [Choreography] to the left of the specified [MorphLayout] with
     * the specified margin. Optionally uses the specified [TimeInterpolator] if any is present.
     * @param otherView The layout to which the choreography will animate its morphViews to the left of
     * @param margin The margin to use between the choreography morphViews and the specified layout.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun toLeftOf(otherView: MorphLayout, margin: Float = MIN_OFFSET, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the right of the specified [MorphLayout] with
     * the specified margin. Optionally uses the specified [TimeInterpolator] if any is present.
     * @param otherView The layout to which the choreography will animate its morphViews to the right of
     * @param margin The margin to use between the choreography morphViews and the specified layout.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun toRightOf(otherView: MorphView, margin: Float = MIN_OFFSET, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the top of the specified [MorphLayout] with
     * the specified margin. Optionally uses the specified [TimeInterpolator] if any is present.
     * @param otherView The layout to which the choreography will animate its morphViews to the top of
     * @param margin The margin to use between the choreography morphViews and the specified layout.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun toTopOf(otherView: MorphView, margin: Float = MIN_OFFSET, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the bottm of the specified [MorphLayout] with
     * the specified margin. Optionally uses the specified [TimeInterpolator] if any is present.
     * @param otherView The layout to which the choreography will animate its morphViews to the bottom of
     * @param margin The margin to use between the choreography morphViews and the specified layout.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun toBottomOf(otherView: MorphView, margin: Float = MIN_OFFSET, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Arc animates the position of the morphViews of this [Choreography] to the specified position of the specified [Anchor].
     * in relation to the specified view: [MorphLayout]. If no arc translation control point has been specified it will
     * then been computed upon building. If a margin offset is used the the morphViews will position at the
     * anchor point with the given margin offset.
     * @param anchor The position to animate the position to
     * @param view The view to animate relative to.
     * @param margin The offset distance to add from the absolute anchor to the animated morphViews
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun anchorArcTo(anchor: Anchor, view: MorphLayout, margin: Float = MIN_OFFSET, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the position of the morphViews of this [Choreography] to the specified position of the specified [Anchor].
     * in relation to the specified view: [MorphLayout]. If a margin offset is used the the morphViews will position at the
     * anchor point with the given margin offset.
     * @param anchor The position to animate the position to
     * @param view The view to animate relative to.
     * @param margin The offset distance to add from the absolute anchor to the animated morphViews
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun anchorTo(anchor: Anchor, view: MorphLayout, margin: Float = MIN_OFFSET, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the specified bounds.
     * @param bounds The bounds to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun positionAt(bounds: Bounds, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the specified x position.
     * @param positionX the position to which the X position value is to be animated to
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun xPositionTo(positionX: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the specified X position from the specified X position.
     * @param fromValue the position from which the X position value is to be animated from
     * @param toValue the position to which the X position value is to be animated to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun xPositionFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the specified X position value property
     * @param value The property to use for animating the X value of this choreography
     * @return this choreography.
     */
    fun xPosition(value: AnimatedFloatValue): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the specified Y position.
     * @param positionY the position to which the Y position value is to be animated to
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun yPositionTo(positionY: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the specified Y position from the specified Y position.
     * @param fromValue the position from which the Y position value is to be animated from
     * @param toValue the position to which the Y position value is to be animated to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun yPositionFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the specified Y position value property.
     * The position.
     * @param value The property to use for animating the Y value of this choreography
     * @return this choreography.
     */
    fun yPosition(value: AnimatedFloatValue): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the specified points see: [FloatPoint].
     * Uses the default interpolator if any is present
     * @return this choreography
     */
    fun translateBetween(vararg values: FloatPoint): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the specified points see: [FloatPoint].
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography
     */
    fun translateBetween(interpolator: TimeInterpolator? = null, vararg values: FloatPoint): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the X translation values created
     * by mapping to the specified percentages. Int based percentages are used.
     * Ex: 0%, 50%, 120%
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography
     */
    fun xTranslateBetween(value: Float, percentages: IntArray, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the X translation values created
     * by mapping to the specified percentages. Float based percentages are used where
     * 0.5f equals 50% and so on.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography
     */
    fun xTranslateBetween(value: Float, percentages: FloatArray, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the specified X translation values.
     * Uses the default interpolator if any is present.
     * @param values the values to translate between.
     * @return this choreography.
     */
    fun xTranslateBetween(vararg values: Float): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the specified X translation values.
     * @param values the values to translate between.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun xTranslateBetween(interpolator: TimeInterpolator? = null, vararg values: Float): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the specified X translation.
     * @param translationX the position to which the X translation value is to be animated to
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun xTranslateTo(translationX: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the specified X translation from the specified X translation.
     * @param fromValue the position from which the X translation value is to be animated from
     * @param toValue the position to which the X translation value is to be animated to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun xTranslateFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the specified X translation value property
     * @param value The property to use for animating the X value of this choreography
     * @return this choreography.
     */
    fun xTranslate(value: AnimatedFloatValue): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the Y translation values created
     * by mapping to the specified percentages. Int based percentages are used.
     * Ex: 0%, 50%, 120%
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography
     */
    fun yTranslateBetween(value: Float, percentages: IntArray, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the Y translation values created
     * by mapping to the specified percentages. Float based percentages are used where
     * 0.5f equals 50% and so on.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography
     */
    fun yTranslateBetween(value: Float, percentages: FloatArray, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the specified Y translation values.
     * Uses the default interpolator if any is present.
     * @param values the values to translate between.
     * @return this choreography.
     */
    fun yTranslateBetween(vararg values: Float): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the specified Y translation values.
     * @param values the values to translate between.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun yTranslateBetween(interpolator: TimeInterpolator? = null, vararg values: Float): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the specified Y translation.
     * @param translationY the position to which the Y translation value is to be animated to
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun yTranslateTo(translationY: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the specified Y translation from the specified Y translation.
     * @param fromValue the position from which the Y translation value is to be animated from
     * @param toValue the position to which the Y translation value is to be animated to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun yTranslateFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the specified Y translation value property
     * @param value The property to use for animating the Y value of this choreography
     * @return this choreography.
     */
    fun yTranslate(value: AnimatedFloatValue): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the Z translation values created
     * by mapping to the specified percentages. Int based percentages are used.
     * Ex: 0%, 50%, 120%
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography
     */
    fun zTranslateBetween(value: Float, percentages: IntArray, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the Z translation values created
     * by mapping to the specified percentages. Float based percentages are used where
     * 0.5f equals 50% and so on.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography
     */
    fun zTranslateBetween(value: Float, percentages: FloatArray, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the specified Z translation values.
     * Uses the default interpolator if any is present.
     * @param values the values to translate between.
     * @return this choreography.
     */
    fun zTranslateBetween(vararg values: Float): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the specified Z translation values.
     * @param values the values to translate between.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun zTranslateBetween(interpolator: TimeInterpolator? = null, vararg values: Float): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the specified Z translation.
     * @param translationZ the position to which the Z translation value is to be animated to
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun zTranslateTo(translationZ: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the specified Z translation from the specified Z translation.
     * @param fromValue the position from which the Z translation value is to be animated from
     * @param toValue the position to which the Z translation value is to be animated to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun zTranslateFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the specified Z translation value property
     * @param value The property to use for animating the Z value of this choreography
     * @return this choreography.
     */
    fun zTranslate(value: AnimatedFloatValue): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the specified coordinates using arc translation
     * The control point is auto calculated if no control point has been specified.
     * @param coordinates The coordinates to arc translate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography
     */
    fun arcTranslateTo(coordinates: Coordinates, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the specified X and Y translation values using arc translation
     * The control point is auto calculated if no control point has been specified.
     * @param translationX The x translation amount to arc translate to.
     * @param translationY The x translation amount to arc translate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography
     */
    fun arcTranslateTo(translationX: Float, translationY: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the rotation values created
     * by mapping to the specified percentages. Int based percentages are used.
     * Ex: 0%, 50%, 120%
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography
     */
    fun rotateBetween(value: Float, percentages: IntArray, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the rotation values created
     * by mapping to the specified percentages. Float based percentages are used where
     * 0.5f equals 50% and so on.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography
     */
    fun rotateBetween(value: Float, percentages: FloatArray, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the specified rotation values.
     * Uses the default interpolator if any is present.
     * @param values the values to rotate between.
     * @return this choreography.
     */
    fun rotateBetween(vararg values: Float): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the specified rotation values.
     * @param values the values to rotate between.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun rotateBetween(interpolator: TimeInterpolator? = null, vararg values: Float): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the computed rotation value created
     * by adding the specified delta to the current rotation value. This causes the rotation value
     * to be increased/decreased with the specified amount.
     * @param delta The amount to add to the current rotation value
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun addRotation(delta: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the computed rotation value created
     * by multiplying the specified multiplier with the current rotation value. This causes the rotation value
     * to be increased/decreased with the specified amount.
     * @param multiplier The amount to multiply the current rotation value by
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun rotateBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the rotation value of the morphViews of this [Choreography] to the specified rotation value.
     * @param rotation The rotation value to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun rotateTo(rotation: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the rotation value of the morphViews of this [Choreography] from the specified rotation value
     * to the specified rotation value
     * @param fromValue The rotation value to animate from
     * @param toValue The rotation value to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun rotateFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the rotation value of the morphViews of this [Choreography] using the specified animated
     * rotation value property. See [AnimatedFloatValue]
     * @param value The property to use for this animation.
     * @return this choreography.
     */
    fun rotate(value: AnimatedFloatValue): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the X rotation values created
     * by mapping to the specified percentages. Int based percentages are used.
     * Ex: 0%, 50%, 120%
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography
     */
    fun xRotateBetween(value: Float, percentages: IntArray, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the X rotation values created
     * by mapping to the specified percentages. Float based percentages are used where
     * 0.5f equals 50% and so on.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography
     */
    fun xRotateBetween(value: Float, percentages: FloatArray, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the specified X rotation values.
     * Uses the default interpolator if any is present.
     * @param values the values to X rotate between.
     * @return this choreography.
     */
    fun xRotateBetween(vararg values: Float): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the specified X rotation values.
     * @param values the values to X rotate between.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun xRotateBetween(interpolator: TimeInterpolator? = null, vararg values: Float): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the computed X rotation value created
     * by adding the specified delta to the current X rotation value. This causes the X rotation value
     * to be increased/decreased with the specified amount.
     * @param delta The amount to add to the current X rotation value
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun xRotateAdd(delta: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the computed X rotation value created
     * by multiplying the specified multiplier with the current X rotation value. This causes the X rotation value
     * to be increased/decreased with the specified amount.
     * @param multiplier The amount to multiply the current X rotation value by
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun xRotateBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the X rotation value of the morphViews of this [Choreography] to the specified X rotation value.
     * @param rotationX The X rotation value to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun xRotateTo(rotationX: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the X rotation value of the morphViews of this [Choreography] from the specified X rotation value
     * to the specified X rotation value
     * @param fromValue The X rotation value to animate from
     * @param toValue The X rotation value to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun xRotateFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the X rotation value of the morphViews of this [Choreography] using the specified animated
     * X rotation value property. See [AnimatedFloatValue]
     * @param value The property to use for this animation.
     * @return this choreography.
     */
    fun xRotate(value: AnimatedFloatValue): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the Y rotation values created
     * by mapping to the specified percentages. Int based percentages are used.
     * Ex: 0%, 50%, 120%
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography
     */
    fun yRotateBetween(value: Float, percentages: IntArray, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the Y rotation values created
     * by mapping to the specified percentages. Float based percentages are used where
     * 0.5f equals 50% and so on.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography
     */
    fun yRotateBetween(value: Float, percentages: FloatArray, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the specified Y rotation values.
     * Uses the default interpolator if any is present.
     * @param values the values to Y rotate between.
     * @return this choreography.
     */
    fun yRotateBetween(vararg values: Float): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the specified Y rotation values.
     * @param values the values to Y rotate between.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun yRotateBetween(interpolator: TimeInterpolator? = null, vararg values: Float): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the computed Y rotation value created
     * by adding the specified delta to the current Y rotation value. This causes the Y rotation value
     * to be increased/decreased with the specified amount.
     * @param delta The amount to add to the current Y rotation value
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun yRotateAdd(delta: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the computed Y rotation value created
     * by multiplying the specified multiplier with the current Y rotation value. This causes the Y rotation value
     * to be increased/decreased with the specified amount.
     * @param multiplier The amount to multiply the current Y rotation value by
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun yRotateBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the Y rotation value of the morphViews of this [Choreography] to the specified Y rotation value.
     * @param rotationY The Y rotation value to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun yRotateTo(rotationY: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the Y rotation value of the morphViews of this [Choreography] from the specified Y rotation value
     * to the specified Y rotation value
     * @param fromValue The Y rotation value to animate from
     * @param toValue The Y rotation value to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun yRotateFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the Y rotation value of the morphViews of this [Choreography] using the specified animated
     * X rotation value property. See [AnimatedFloatValue]
     * @param value The property to use for this animation.
     * @return this choreography.
     */
    fun yRotate(value: AnimatedFloatValue): Choreography

    /**
     * Animates the scale value of the morphViews of this [Choreography] to the specified [Bounds] value.
     * @param bounds The bounds dimension to scale animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun scaleTo(bounds: Bounds, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the scale value of the morphViews of this [Choreography] to the specified [Dimension] value.
     * @param dimension The dimension to scale animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun scaleTo(dimension: Dimension, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the computed scale value created
     * by adding the specified delta to the current X and Y scale value. This causes the scale values
     * to be increased/decreased with the specified amount.
     * @param delta The amount to add to the current X and Y scale value
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun scaleAdd(delta: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the computed scale value created
     * by multiplying the specified delta to the current X and Y scale value. This causes the scale values
     * to be increased/decreased with the specified amount.
     * @param multiplier The amount to add to the current X and Y scale value
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun scaleBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the X and Y scale value of the morphViews of this [Choreography] to the specified scale value.
     * @param scale The scale amount to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun scaleTo(scale: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the X and Y scale value of the morphViews of this [Choreography] from the specified scale value
     * to the specified scale value
     * @param fromValue The scale amount to animate from
     * @param toValue The scale amount to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun scaleFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the scale X values created
     * by mapping to the specified percentages. Int based percentages are used.
     * Ex: 0%, 50%, 120%
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography
     */
    fun xScaleBetween(value: Float, percentages: IntArray, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the scale X values created
     * by mapping to the specified percentages. Float based percentages are used where
     * 0.5f equals 50% and so on.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography
     */
    fun xScaleBetween(value: Float, percentages: FloatArray, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the specified scale X values.
     * Uses the default interpolator if any is present.
     * @param values the values to scale X between.
     * @return this choreography.
     */
    fun xScaleBetween(vararg values: Float): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the specified scale X values.
     * @param values the values to scale X between.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun xScaleBetween(interpolator: TimeInterpolator? = null, vararg values: Float): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the computed X scale value created
     * by adding the specified delta to the current X scale value. This causes the scale value
     * to be increased/decreased with the specified amount.
     * @param delta The amount to add to the current X scale value
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun xScaleAdd(delta: Float, interpolator: TimeInterpolator?): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the computed X scale value created
     * by multiplying the specified delta to the current X scale value. This causes the scale value
     * to be increased/decreased with the specified amount.
     * @param multiplier The amount to add to the current X scale value
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun xScaleBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the X scale value of the morphViews of this [Choreography] to the specified X scale value.
     * @param scaleX The scale amount to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun xScaleTo(scaleX: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the X scale value of the morphViews of this [Choreography] from the specified X scale value
     * to the specified X scale value
     * @param fromValue The scale amount to animate from
     * @param toValue The scale amount to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun xScaleFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the X scale value of the morphViews of this [Choreography] using the specified animated
     * X scale value property. See [AnimatedFloatValue]
     * @param value The property to use for this animation.
     * @return this choreography.
     */
    fun xScale(value: AnimatedFloatValue): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the scale Y values created
     * by mapping to the specified percentages. Int based percentages are used.
     * Ex: 0%, 50%, 120%
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography
     */
    fun yScaleBetween(value: Float, percentages: IntArray, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the scale Y values created
     * by mapping to the specified percentages. Float based percentages are used where
     * 0.5f equals 50% and so on.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography
     */
    fun yScaleBetween(value: Float, percentages: FloatArray, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the specified scale Y values.
     * Uses the default interpolator if any is present.
     * @param values the values to scale Y between.
     * @return this choreography.
     */
    fun yScaleBetween(vararg values: Float): Choreography

    /**
     * Animates the morphViews of this [Choreography] between the specified scale Y values.
     * @param values the values to scale Y between.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun yScaleBetween(interpolator: TimeInterpolator? = null, vararg values: Float): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the computed Y scale value created
     * by adding the specified delta to the current Y scale value. This causes the scale value
     * to be increased/decreased with the specified amount.
     * @param delta The amount to add to the current X scale value
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun yScaleAdd(delta: Float, interpolator: TimeInterpolator?): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the computed Y scale value created
     * by multiplying the specified delta to the current Y scale value. This causes the scale value
     * to be increased/decreased with the specified amount.
     * @param multiplier The amount to add to the current Y scale value
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun yScaleBy(multiplier: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the Y scale value of the morphViews of this [Choreography] to the specified Y scale value.
     * @param scaleY The scale amount to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun yScaleTo(scaleY: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the Y scale value of the morphViews of this [Choreography] from the specified Y scale value
     * to the specified Y scale value
     * @param fromValue The scale amount to animate from
     * @param toValue The scale amount to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun yScaleFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the Y scale value of the morphViews of this [Choreography] using the specified animated
     * Y scale value property. See [AnimatedFloatValue]
     * @param value The property to use for this animation.
     * @return this choreography.
     */
    fun yScale(value: AnimatedFloatValue): Choreography


    /**
     * Animates the alpha value of the morphViews of this [Choreography] to the specified alpha value.
     * @param alpha The alpha value to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun alphaTo(alpha: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the alpha value of the morphViews of this [Choreography] from the specified alpha value
     * to the specified alpha value.
     * @param fromValue The alpha value to animate from.
     * @param toValue The alpha value to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun alphaFrom(fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the alpha value of the morphViews of this [Choreography] to the specified alpha value.
     * The alpha value is specified as a percentage where 50 is 50 percent opacity
     * @param alpha The alpha value to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun alphaTo(alpha: Int, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the alpha value of the morphViews of this [Choreography] from the specified alpha value
     * to the specified alpha value. The alpha value is specified as a percentage where 50 is 50 percent opacity.
     * @param fromValue The alpha value to animate from.
     * @param toValue The alpha value to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun alphaFrom(fromValue: Int, toValue: Int, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the alpha value of the morphViews of this [Choreography] using the specified animated
     * alpha value property. See [AnimatedFloatValue]
     * @param value The property to use for this animation.
     * @return this choreography.
     */
    fun alpha(value: AnimatedFloatValue): Choreography

    /**
     * Animates the corner radius of the morphViews of this [Choreography] to the specified [CornerRadii].
     * @param corners The corner radius value to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun cornerRadiusTo(corners: CornerRadii, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the corner radius of the specified corners of the morphViews of this [Choreography] to the specified value.
     * @param corners The corners which value is to be animated.
     * @param radius The radius to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun cornerRadiusTo(corners: CornersSet, radius: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the corner radius of the specified corners of the morphViews of this [Choreography] to the specified value.
     * @param corner The corner which value is to be animated.
     * @param radius The radius to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun cornerRadiusTo(corner: Corner = Corner.ALL, radius: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the corner radius of the specified corners of the morphViews of this [Choreography] from the specified value.
     * to the specified value
     * @param corner The corner which value is to be animated.
     * @param fromValue The radius to animate from.
     * @param toValue The radius to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun cornerRadiusFrom(corner: Corner = Corner.ALL, fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the corner radius of the specified corners of the morphViews of this [Choreography] from the specified value.
     * to the specified value
     * @param cornerValue The corner property to use fo this animation.
     * @return this choreography.
     */
    fun cornerRadius(cornerValue: AnimatedValue<CornerRadii>): Choreography

    /**
     * Animate the [Bounds] (Dimensions and Coordinates) of the morphViews of this [Choreography]
     * to the specified bounds.
     * @param bounds The bounds to animate to.
     * @param interpolator the interpolator to use for this animation
     */
    fun boundsTo(bounds: Bounds, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animate the size (Width and/or Height properties) of the morphViews of this [Choreography] using the
     * specified [Measurement] with the specified delta. The delta is the amount to be added to the dimension
     * which is to be animated.
     * @param measurement The dimension to resize.
     * @param delta The amount to add to the current size
     * @param interpolator the interpolator to use for this animation
     */
    fun addToSize(measurement: Measurement, delta: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the morphViews of this [Choreography] to the computed size (Width, Height) value created
     * by multiplying the specified delta with the current width and height values. This causes the size value
     * to be increased/decreased with the specified amount for the specified [Measurement].
     * @param measurement The dimension to resize.
     * @param multiplier The amount to add to the current Y scale value
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun resizeBy(measurement: Measurement, multiplier: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the size (Width, Height) values of the morphViews of this [Choreography] to the specified [Bounds] value.
     * @param bounds The bounds to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun resizeTo(bounds: Bounds, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the size (Width, Height) values of the morphViews of this [Choreography] to the specified [Dimension] value.
     * @param dimension The dimension to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun resizeTo(dimension: Dimension, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the size (Width and/or Height) values of the morphViews of this [Choreography] to the specified [Dimension] value
     * based on the specified [Measurement]
     * @param measurement The dimension to resize.
     * @param value The value to animate to the specified dimension to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun resizeTo(measurement: Measurement, value: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the size (Width and/or Height) values of the morphViews of this [Choreography] from the specified [Dimension] value
     * to the specified [Dimension] value based on the specified [Measurement]
     * @param measurement The dimension to resize.
     * @param fromValue The value from which to animate specified dimension from.
     * @param toValue The value to animate the specified dimension to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun resizeFrom(measurement: Measurement, fromValue: Float, toValue: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the size (Width and/or Height) values of the morphViews of this [Choreography] with the specified Size animation
     * property value based on the specified [Measurement]
     * @param measurement The dimension to resize.
     * @param value The property to use for this animation.
     * @return this choreography.
     */
    fun resize(measurement: Measurement, value: AnimatedFloatValue): Choreography

    /**
     * Animates the specified [Margin] value of the morphViews of this [Choreography] to the specified value
     * @param margin The margin to animate.
     * @param value The value to animate the specified margin to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun marginTo(margin: Margin, value: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the specified [Margin] value of the morphViews of this [Choreography] from the specified value
     * to the specified value
     * @param margin The margin to animate.
     * @param valueFrom The value to animate the specified margin from.
     * @param valueTo The value to animate the specified margin to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun marginFrom(margin: Margin, valueFrom: Float, valueTo: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the specified [Margin] value of the morphViews of this [Choreography] with the specified animation
     * value property. See [AnimatedValue]
     * @param margin The property to use for this animation.
     * @return this choreography.
     */
    fun margin(margin: AnimatedValue<Margings>): Choreography

    /**
     * Animates the specified [Padding] value of the morphViews of this [Choreography] to the specified value
     * @param padding The padding to animate.
     * @param value The value to animate the specified padding to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun paddingTo(padding: Padding, value: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the specified [Padding] value of the morphViews of this [Choreography] from the specified value
     * to the specified value
     * @param padding The padding to animate.
     * @param valueFrom The value to animate the specified padding from.
     * @param valueTo The value to animate the specified padding to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun paddingFrom(padding: Padding, valueFrom: Float, valueTo: Float, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the specified [Padding] value of the morphViews of this [Choreography] with the specified animation
     * value property. See [AnimatedValue]
     * @param padding The property to use for this animation.
     * @return this choreography.
     */
    fun padding(padding: AnimatedValue<Paddings>): Choreography

    /**
     * Animates the color value of the morphViews of this [Choreography] to the specified color value.
     * @param color The color value to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun colorTo(color: Color, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the color value of the morphViews of this [Choreography] from the specified color value
     * to the specified color value
     * @param fromValue The color to animate from
     * @param toValue The color to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun colorFrom(fromValue: Color, toValue: Color, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the color value of the morphViews of this [Choreography] using the specified animated
     * color value property. See [AnimatedValue]
     * @param value The property to use for this animation.
     * @return this choreography.
     */
    fun color(value: AnimatedValue<Color>): Choreography

    /**
     * Animates the color value of the morphViews of this [Choreography] to the specified color value.
     * @param color The color value to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun colorTo(@ColorInt color: Int, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the color value of the morphViews of this [Choreography] from the specified color value
     * to the specified color value
     * @param fromValue The color to animate from
     * @param toValue The color to animate to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun colorFrom(@ColorInt fromValue: Int, @ColorInt toValue: Int, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Animates the background of the morphViews of this [Choreography] to the specified [Background]
     * @param background the background to animate the current to.
     * @param interpolator the interpolator to use for this animation.
     * @return this choreography.
     */
    fun backgroundTo(background: Background, interpolator: TimeInterpolator? = null): Choreography

    /**
     * Specifies the pivot offset values see: [FloatPoint] to use for rotate and scale animations
     * in this [Choreography]. The pivot will be computed given the specified [Pivot] relation.
     * @param pivotPoint The offset points to use for pivoting.
     * @param type The relation to use when computing the pivot.
     * @return this choreography.
     */
    fun withPivot(pivotPoint: FloatPoint, type: Pivot = Pivot.RELATIVE_TO_SELF): Choreography

    /**
     * Specifies the pivot point X and pivot point Y to use for rotate and scale animations in.
     * this [Choreography]. The pivot will be computed given the specified [Pivot] relation.
     * @param pivotX The offset pivot X point to use.
     * @param pivotY The offset pivot Y point to use.
     * @param type The relation to use when computing the pivot.
     * @return this choreography.
     */
    fun withPivot(pivotX: Float, pivotY: Float, type: Pivot = Pivot.RELATIVE_TO_SELF): Choreography

    /**
     * Specifies the pivot point X to use for rotate and scale animations in this [Choreography].
     * The pivot will be computed given the specified [Pivot] relation.
     * @param pivotX The offset pivot X point to use.
     * @param type The relation to use when computing the pivot.
     * @return this choreography.
     */
    fun withPivotX(pivotX: Float, type: Pivot = Pivot.RELATIVE_TO_SELF): Choreography

    /**
     * Specifies the pivot point Y to use for rotate and scale animations in this [Choreography].
     * The pivot will be computed given the specified [Pivot] relation.
     * @param pivotY The offset pivot Y point to use.
     * @param type The relation to use when computing the pivot.
     * @return this choreography.
     */
    fun withPivotY(pivotY: Float, type: Pivot = Pivot.RELATIVE_TO_SELF): Choreography

    /**
     * Specifies the default [TimeInterpolator] to use for this [Choreography]. The interpolator
     * will be used when the property being animated has no defined interpolator of its own.
     * @param interpolator The interpolator to use for this choreography.
     * @return this choreography.
     */
    fun withInterpolator(interpolator: TimeInterpolator?): Choreography

    /**
     * Specifies the duration of the animation for this [Choreography]. Based on how this choreography was
     * created, if no duration is specified this choreography will use the duration of its parent.
     * In other cases the duration will be set to the default animation of the [Choreographer]
     * @param duration The duration of the choreography animation
     * @return this choreography.
     */
    fun withDuration(duration: Long): Choreography

    /**
     * Specifies a [Stretch] property to use for when animating the translation or position properties
     * of this [Choreography].
     * @param stretch The stretch property to use for stretching and squashing the morphViews
     * being animated by this choreography upon translation.
     */
    fun withStretch(stretch: Stretch): Choreography

    /**
     * Specifies the start delay of the animation for this [Choreography].
     * @param delay The delay of the choreography animation
     * @return this choreography.
     */
    fun withStartDelay(delay: Long): Choreography

    /**
     * Specifies the stagger value to use for animating through the morphViews for this [Choreography].
     * The duration of the animation will remain intact but the higher the stagger offset the faster
     * the animation for each individual view will be. See: [AnimationStagger]
     * @param offset The offset to use. The offset indicates at what point through the animation
     * of the previous view should the animation of the current view start. When incremental stagger
     * is used the value will range between a threshold.
     * @param multiplier The stagger multiplier to use. The multiplier determines the range of the offset
     * a value of 1f means full offset.
     * @param type The [Stagger] type to use.
     * @return this choreography.
     */
    fun withStagger(offset: Float = MIN_OFFSET, multiplier: Float = MAX_OFFSET, type: Stagger = Stagger.LINEAR): Choreography

    /**
     * Specifies the stagger animation see: [AnimationStagger] to use for animating through the morphViews for this [Choreography].
     * The duration of the animation will remain intact but the higher the stagger offset the faster
     * the animation for each individual view will be.
     * @param stagger The instruction to use for creating the stagger effect.
     * @return this choreography.
     */
    fun withStagger(stagger: AnimationStagger): Choreography

    /**
     * Specifies the way the arc translation control point should be computer. If arc
     * translation is used the control point will be calculated based on the specified
     * type. The available types are:
     * * [ArcType.INNER] : Arc translates across the inner path of its destination.
     * * [ArcType.OUTER] : Arc translates across the outer path of its destination.
     * @param arcType the arc path to use for the arc translation.
     * @return this choreography.
     */
    fun withArcType(arcType: ArcType): Choreography

    /**
     * Specifies whether or not arc translation should be used for translating
     * the views. When arc translation is used the views will arc translate using
     * the default or a specified control point.
     * @param useArcTranslation specifies if arc translation should be used. Default: `True`
     * @return this choreography.
     */
    fun witArchTranslation(useArcTranslation: Boolean = true): Choreography

    /**
     * Specifies the circular reveal to use for revealing the morphViews of this [Choreography]. The
     * reveal will happen with the radius and center point of the specified view.
     * @param view the view from which the reveal will happen.
     * @param interpolator the interpolator to use for this animation.
     * @param onEnd the action to perform at the end of the reveal.
     * @return this choreography.
     */
    fun revealFrom(view: View, interpolator: TimeInterpolator? = null, onEnd: Action = null): Choreography

    /**
     * Specifies the circular reveal to use for revealing the morphViews of this [Choreography]. The
     * reveal will happen with the specified center coordinates and radius.
     * @param centerX the initial horizontal center coordinate of the reveal
     * @param centerY the initial vertical center coordinate of the reveal
     * @param radius the initial radius of the reveal
     * @param interpolator the interpolator to use for this animation.
     * @param onEnd the action to perform at the end of the reveal.
     * @return this choreography.
     */
    fun revealFrom(centerX: Float, centerY: Float, radius: Float, interpolator: TimeInterpolator? = null, onEnd: Action = null): Choreography

    /**
     * Specifies the circular reveal to use for revealing the morphViews of this [Choreography]. The
     * reveal will happen with the specified center coordinates and radius.
     * @param coordinates the location from where the reveal will happen.
     * @param radius the initial radius of the reveal
     * @param interpolator the interpolator to use for this animation.
     * @param onEnd the action to perform at the end of the reveal.
     * @return this choreography.
     */
    fun revealFrom(coordinates: Coordinates, radius: Float, interpolator: TimeInterpolator? = null, onEnd: Action = null): Choreography

    /**
     * Specifies the circular reveal to use for revealing the morphViews of this [Choreography]. The
     * reveal will happen with the specified relative offsets and radius.
     * @param offsetX the x location offset within the view. 0.5f == the horizontal center of the view
     * @param offsetY the Y location offset within the view. 0.5f == the vertical center of the view
     * @param radius the initial radius of the reveal
     * @param interpolator the interpolator to use for this animation.
     * @param onEnd the action to perform at the end of the reveal.
     * @return this choreography.
     */
    fun revealWith(offsetX: Float, offsetY: Float, radius: Float, interpolator: TimeInterpolator? = null, onEnd: Action = null): Choreography

    /**
     * Specifies the circular reveal to use for revealing the morphViews of this [Choreography]. The
     * reveal will happen from the pivot location if any has been specified, otherwise the reveal will happen at
     * the center of the view being revealed.
     * @param interpolator the interpolator to use for this animation.
     * @param radius the initial radius of the reveal
     * @param onEnd the action to perform at the end of the reveal.
     * @return this choreography.
     */
    fun revealWith(radius: Float, interpolator: TimeInterpolator? = null, onEnd: Action = null): Choreography

    /**
     * Specifies the circular [Reveal] to use for revealing the morphViews of this [Choreography]
     * @param reveal contains information on how to reveal the morphViews.
     * @return this choreography.
     */
    fun withReveal(reveal: Reveal): Choreography

    /**
     * Specifies the circular conceal to use for concealing the morphViews of this [Choreography]. The
     * conceal will happen towards the radius and center point of the specified view.
     * @param view the view to which the conceal will happen.
     * @param interpolator the interpolator to use for this animation.
     * @param onEnd the action to perform at the end of the conceal.
     * @return this choreography.
     */
    fun concealFrom(view: View, interpolator: TimeInterpolator? = null, onEnd: Action = null): Choreography

    /**
     * Specifies the circular conceal to use for concealing the morphViews of this [Choreography]. The
     * conceal will happen towards the specified center coordinates and radius.
     * @param centerX the ending horizontal center coordinate of the conceal
     * @param centerY the ending vertical center coordinate of the conceal
     * @param radius the ending radius of the conceal
     * @param interpolator the interpolator to use for this animation.
     * @param onEnd the action to perform at the end of the conceal.
     * @return this choreography.
     */
    fun concealTo(centerX: Float, centerY: Float, radius: Float, interpolator: TimeInterpolator? = null, onEnd: Action = null): Choreography

    /**
     * Specifies the circular conceal to use for concealing the morphViews of this [Choreography]. The
     * conceal will happen towards the specified center coordinates and radius.
     * @param coordinates the location to where the conceal will end.
     * @param radius the ending radius of the conceal
     * @param interpolator the interpolator to use for this animation.
     * @param onEnd the action to perform at the end of the conceal.
     * @return this choreography.
     */
    fun concealFrom(coordinates: Coordinates, radius: Float, interpolator: TimeInterpolator? = null, onEnd: Action = null): Choreography

    /**
     * Specifies the circular conceal to use concealing the morphViews of this [Choreography]. The
     * conceal will happen towards the specified relative offsets and radius.
     * @param offsetX the x location offset within the view. 0.5f == the horizontal center of the view
     * @param offsetY the Y location offset within the view. 0.5f == the vertical center of the view
     * @param radius the ending radius of the conceal
     * @param interpolator the interpolator to use for this animation.
     * @param onEnd the action to perform at the end of the conceal.
     * @return this choreography.
     */
    fun concealWith(offsetX: Float, offsetY: Float, radius: Float, interpolator: TimeInterpolator? = null, onEnd: Action = null): Choreography

    /**
     * Specifies the circular conceal to use for concealing the morphViews of this [Choreography]. The
     * conceal will happen towards the pivot location if any has been specified, otherwise the conceal will happen at
     * towards the center of the view being concealed.
     * @param interpolator the interpolator to use for this animation.
     * @param radius the ending radius of the conceal
     * @param onEnd the action to perform at the end of the conceal.
     * @return this choreography.
     */
    fun concealWith(radius: Float, interpolator: TimeInterpolator? = null, onEnd: Action = null): Choreography

    /**
     * Specifies the circular [Conceal] to use for concealing the morphViews of this [Choreography]
     * @param conceal contains information on how to conceal the morphViews.
     * @return this choreography.
     */
    fun withConceal(conceal: Conceal): Choreography

    /**
     * Specifies a [TransitionProgressListener] to use for this [Choreography]. The listener
     * is notified by the progress of the animation being perform by this choreography with
     * a percent fraction from 0f to 1f
     * @param progressListener The listener to notify.
     * @return this choreography.
     */
    fun withProgressListener(progressListener: TransitionProgressListener): Choreography

    /**
     * Specifies an [OffsetTrigger] to use for this [Choreography]. The trigger will execute
     * its specified event: [OffsetTrigger.triggerAction] when the animation has reached the
     * specified trigger offset: [OffsetTrigger.percentage]. A trigger can only be activated
     * once.
     * @param offsetTrigger The trigger to use.
     * @return this choreography.
     */
    fun withOffsetTrigger(offsetTrigger: OffsetTrigger): Choreography

    /**
     * Specifies the action that should be executed upon the start of the animation of this [Choreography]
     * @param action The start action to execute.
     * @return this choreography.
     */
    fun onStart(action: ChoreographerAction): Choreography

    /**
     * Specifies the action that should be executed upon the end of the animation of this [Choreography]
     * @param action The end action to execute.
     * @return this choreography.
     */
    fun whenDone(action: ChoreographerAction): Choreography

    /**
     * Specifies that the animation perform by this [Choreography] will reverse upon finish.
     * @return this choreography.
     */
    fun thenReverse(): Choreography

    /**
     * Creates a [Choreography] for the latest given morphViews which will start at the duration
     * offset of its parent. An offset of 0.5f indicates that this choreography will play when
     * the animation of its parent is half way through. If no morphViews have been specified the
     * morphViews of the previous choreography will be used.
     * @param offset The offset at which this choreography will start animating.
     * @return this choreography.
     */
    fun after(offset: Float): Choreography

    /**
     * Creates a [Choreography] for the given view which will start at the duration
     * offset of its parent. A value of 0.5f indicates that this choreography will play when
     * the animation of its parent is half way through. If no view have been specified the
     * morphViews of the previous choreography will be used.
     * @param offset The offset at which this choreography will start animating.
     * @param views The views which will be animated by this choreography.
     * @return this choreography.
     */
    fun animateAfter(offset: Float, vararg views: View? = emptyArray()): Choreography

    /**
     * Creates a [Choreography] for the given morphViews which will start at the duration
     * offset of its parent. A value of 0.5f indicates that this choreography will play when
     * the animation of its parent is half way through. If no morphViews have been specified the
     * morphViews of the previous choreography will be used.
     * @param offset The offset at which this choreography will start animating.
     * @param morphViews The morph layouts, see: [MorphLayout] which will be animated by this choreography.
     * @return this choreography.
     */
    fun animateAfter(offset: Float, vararg morphViews: MorphLayout = this.morphViews): Choreography

    /**
     * Creates a [Choreography] for the last given morphViews which will start directly the animation of
     * its parent choreography is over. If no morphViews have been specified the morphViews of the previous
     * choreography will be used.
     * @return this choreography.
     */
    fun then(): Choreography

    /**
     * Creates a [Choreography] for the given views which will start directly the animation of
     * its parent choreography is over. If no views have been specified the morphViews of the previous
     * choreography will be used.
     * @param views The views which will be animated by this choreography.
     * @return this choreography.
     */
    fun thenAnimate(vararg views: View? = emptyArray()): Choreography

    /**
     * Creates a [Choreography] for the last given morphViews which will start directly at the same time
     * as its parent. If no morphViews have been specified the morphViews of the previous choreography will be used.
     * @return this choreography.
     */
    fun also(): Choreography

    /**
     * Creates a [Choreography] for the given views which will start directly at the same time
     * as its parent. If no views have been specified the morphViews of the previous choreography will be used.
     * @param views The views which will be animated by this choreography.
     * @return this choreography.
     */
    fun alsoAnimate(vararg views: View? = emptyArray()): Choreography

    /**
     * Creates a [Choreography] for the given morphViews which will start directly at the same time
     * as its parent. If no morphViews have been specified the morphViews of the previous choreography will be used.
     * @param morphViews The morph layouts, see: [MorphLayout] which will be animated by this choreography.
     * @return this choreography.
     */
    fun alsoAnimate(vararg morphViews: MorphLayout = this.morphViews): Choreography

    /**
     * Creates a [Choreography] for the given views which will start directly at the same time
     * as its parent with the same properties as its parent unless specified otherwise. If no views
     * have been specified the morphViews of the previous choreography will be used.
     * @param views The views which will be animated by this choreography.
     * @return this choreography.
     */
    fun andAnimate(vararg views: View? = emptyArray()): Choreography

    /**
     * Creates a [Choreography] for the given morphViews which will start directly at the same time
     * as its parent with the same properties as its parent unless specified otherwise. If no morphViews
     * have been specified the morphViews of the previous choreography will be used.
     * @param morphViews The morph layouts, see: [MorphLayout] which will be animated by this choreography.
     * @return this choreography.
     */
    fun andAnimate(vararg morphViews: MorphLayout = this.morphViews): Choreography

    /**
     * Creates a [Choreography] for the given views which will start directly after the specified duration
     * offset of its parent with the same properties as its parent unless specified otherwise. If no views
     * have been specified the morphViews of the previous choreography will be used.
     * @param views The views which will be animated by this choreography.
     * @return this choreography.
     */
    fun andAnimateAfter(offset: Float, vararg views: View? = emptyArray()): Choreography

    /**
     * Creates a [Choreography] for the given morphViews which will start directly after the specified duration
     * offset of its parent with the same properties as its parent unless specified otherwise. If no morphViews
     * have been specified the morphViews of the previous choreography will be used.
     * @param morphViews The morph layouts, see: [MorphLayout] which will be animated by this choreography.
     * @return this choreography.
     */
    fun andAnimateAfter(offset: Float, vararg morphViews: MorphLayout = this.morphViews): Choreography

    /**
     * Creates a [Choreography] for the given views which will reverse the last choreography which was
     * assign to the same views if any. If the views have not been part of a previous choreography this
     * will do nothing. The animation will play upon the end of the animation of its parent.
     * If no views have been specified the morphViews of the previous choreography will be used.
     * @param views The views which will be animated by this choreography.
     * @return this choreography.
     */
    fun reverseAnimate(vararg views: View? = emptyArray()): Choreography

    /**
     * Creates a [Choreography] for the given morphViews which will reverse the last choreography which was
     * assign to the same morphViews if any. If the morphViews have not been part of a previous choreography this
     * will do nothing. The animation will play upon the end of the animation of its parent.
     * If no morphViews have been specified the morphViews of the previous choreography will be used.
     * @param morphViews The morph layouts, see: [MorphLayout] which will be animated by this choreography.
     * @return this choreography.
     */
    fun reverseAnimate(vararg morphViews: MorphLayout = this.morphViews): Choreography

    /**
     * Creates a [Choreography] for the given views which will reverse the last choreography which was
     * assign to the same views if any. If the views have not been part of a previous choreography this
     * will do nothing. The animation will play at the same time as its parent and will clone its parents properties.
     * If no views have been specified the morphViews of the previous choreography will be used.
     * @param views The views which will be animated by this choreography.
     * @return this choreography.
     */
    fun andReverseAnimate(vararg views: View? = emptyArray()): Choreography

    /**
     * Creates a [Choreography] for the given morphViews which will reverse the last choreography which was
     * assign to the same morphViews if any. If the morphViews have not been part of a previous choreography this
     * will do nothing. The animation will play at the same time as its parent and will clone its parents properties.
     * If no morphViews have been specified the morphViews of the previous choreography will be used.
     * @param morphViews The morph layouts, see: [MorphLayout] which will be animated by this choreography.
     * @return this choreography.
     */
    fun andReverseAnimate(vararg morphViews: MorphLayout = this.morphViews): Choreography
    /**
     * Creates a [Choreography] for the given children of the specified view which will start at the duration
     * offset of its parent. A value of 0.5f indicates that this choreography will play when
     * the animation of its parent is half way through. If a stagger is specified the morphViews will be animated
     * with the specified stagger.
     * @param offset The offset at which this choreography will start animating.
     * @param view The morph layouts, see: [MorphLayout] which children will be animated by this choreography.
     * @param stagger The stagger to use when animating the children. See [AnimationStagger]
     * @return this choreography.
     */
    fun animateChildrenOfAfter(view: MorphLayout, offset: Float, stagger: AnimationStagger? = null): Choreography

    /**
     * Creates a [Choreography] for the given children of the specified view which will start when the animation
     * of the parent choreography is over. If a stagger is specified the morphViews will be animated
     * with the specified stagger.
     * @param view The morph layouts, see: [MorphLayout] which children will be animated by this choreography.
     * @param stagger The stagger to use when animating the children. See [AnimationStagger]
     * @return this choreography.
     */
    fun thenAnimateChildrenOf(view: MorphLayout, stagger: AnimationStagger? = null): Choreography
    /**
     * Creates a [Choreography] for the given children of the specified view which will start when the animation
     * of the parent choreography starts. If a stagger is specified the morphViews will be animated
     * with the specified stagger.
     * @param view The morph layouts, see: [MorphLayout] which children will be animated by this choreography.
     * @param stagger The stagger to use when animating the children. See [AnimationStagger]
     * @return this choreography.
     */
    fun alsoAnimateChildrenOf(view: MorphLayout, stagger: AnimationStagger? = null): Choreography

    /**
     * Creates a [Choreography] for the given children of the specified view which will start when the animation
     * of the parent choreography starts. The properties of the parent choreography will be used by this choreography.
     * If a stagger is specified the morphViews will be animated with the specified stagger.
     * @param view The morph layouts, see: [MorphLayout] which children will be animated by this choreography.
     * @param stagger The stagger to use when animating the children. See [AnimationStagger]
     * @return this choreography.
     */
    fun andAnimateChildrenOf(view: MorphLayout, stagger: AnimationStagger? = null): Choreography

    /**
     * A call to this function will build the current and all the previously appended choreographies.
     * This function must be called prior to starting the [Choreography] animation. Note that a
     * call to this function will not only built the current choreography but also all its predecessors.
     * A built choreography can be saved to played at a later time. The ability to build a
     * choreography helps to get rid of overhead.
     * @return the [Choreographer] which will animate this choreography.
     */
    fun build(): Choreographer

    /**
     * A call to this function will start the current and all the previously appended choreographies.
     * call to this function will not only start the current choreography but also all its predecessors.
     * If the choreographies are not yet build they will also be built.
     * choreography helps to get rid of overhead.
     */
    fun start()
}
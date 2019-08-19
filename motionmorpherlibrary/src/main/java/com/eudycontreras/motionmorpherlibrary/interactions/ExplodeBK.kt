/*
package com.eudycontreras.motionmorpherlibrary.interactions

import com.eudycontreras.motionmorpherlibrary.MAX_OFFSET
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.enumerations.AnimationType
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.mapRange
import com.eudycontreras.motionmorpherlibrary.properties.*
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.math.hypot
import kotlin.math.round


*/
/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 12 2019
 *//*


*/
/**
 * The effect should be able to have an incremental/decremental stagger effect
 * and a stretch effect which should be share or divided.
 *
 * The Stretch should work in this way: 0 means that there is no stretch
 * 0.2 stretchOffset means that the stretch should happen for 20% of the animation before the translation
 * start. The stretch should happen in the direction of the translation.
 *//*

class ExplodeBK(
    var type: Type = Type.LOOSE,
    override var duration: Long = 0L,
    override var amountMultiplier: Float = MAX_OFFSET
): Interaction() {

    private var nodesGroups: Map<Float, List<Node>> = LinkedHashMap()

    private var parentBounds: ViewBounds? = null

    private lateinit var epicenter: Coordinates

    override fun buildInteraction(
        startView: MorphLayout,
        endView: MorphLayout
    ) {
        val nodes: LinkedList<Node> = LinkedList()

        parentBounds = startView.getParentBounds()

        epicenter = Coordinates(
            startView.windowLocationX.toFloat() + startView.morphWidth / 2,
            startView.windowLocationY.toFloat() + startView.morphHeight / 2
        )

        val boundsStart: ViewBounds = startView.viewBounds
        val boundsEnd: ViewBounds = endView.viewBounds

        val widthDelta = if (type == Type.LOOSE) (boundsStart.width) else 0f
        val heightDelta = if (type == Type.LOOSE) (boundsStart.height) else 0f

        val maxX = round((boundsEnd.right - boundsStart.right) - widthDelta)
        val maxY = round((boundsEnd.bottom - boundsStart.bottom) - heightDelta)
        val minX = round((boundsEnd.left - boundsStart.left) + widthDelta)
        val minY = round((boundsEnd.top - boundsStart.top) + heightDelta)

        startView.siblings?.let {collection ->
            for (sibling in collection) {

                if (!sibling.viewBounds.overlaps(boundsEnd)) {
                    continue
                }

                val centerPoint = sibling.centerLocation

                val xDifference = centerPoint.x - epicenter.x
                val yDifference = centerPoint.y - epicenter.y

                val distance = hypot(centerPoint.x - epicenter.x, centerPoint.y - epicenter.y)

                val mappingX = AnimatedFloatValue(AnimatedValue.TRANSLATION_X, MIN_OFFSET, MIN_OFFSET)
                val mappingY = AnimatedFloatValue(AnimatedValue.TRANSLATION_Y, MIN_OFFSET, MIN_OFFSET)

                val scaleX = AnimatedFloatValue(AnimatedValue.SCALE_X, MAX_OFFSET, MAX_OFFSET)
                val scaleY = AnimatedFloatValue(AnimatedValue.SCALE_Y, MAX_OFFSET, MAX_OFFSET)

                val xDelta = if (type == Type.LOOSE) xDifference else 0f
                val yDelta = if (type == Type.LOOSE) yDifference else 0f

                if (xDifference > 0) {
                    mappingX.toValue = maxX + xDelta
                    scaleX.toValue = stretchMultiplier
                    sibling.morphPivotX = 0f
                }
                if (yDifference > 0) {
                    mappingY.toValue = maxY + yDelta
                    scaleY.toValue = stretchMultiplier
                    sibling.morphPivotY = 0f
                }
                if (xDifference < 0) {
                    mappingX.toValue = minX + xDelta
                    scaleX.toValue = stretchMultiplier
                    sibling.morphPivotX = sibling.morphWidth
                }
                if (yDifference < 0) {
                    mappingY.toValue = minY + yDelta
                    scaleY.toValue = stretchMultiplier
                    sibling.morphPivotY = sibling.morphHeight
                }

                val animationNode = Node(
                    view = sibling,
                    distance = distance,
                    translationX = mappingX,
                    translationY = mappingY,
                    scaleX = scaleX,
                    scaleY = scaleY,
                    centerLocation = centerPoint,
                    epicenter = false
                )
                nodes.add(animationNode)
            }
        }

        val animationNode = Node(
            view = endView,
            distance = 0f,
            translationX = AnimatedFloatValue(AnimatedValue.TRANSLATION_X, MIN_OFFSET, MIN_OFFSET),
            translationY = AnimatedFloatValue(AnimatedValue.TRANSLATION_X, MIN_OFFSET, MIN_OFFSET),
            scaleX = AnimatedFloatValue(AnimatedValue.SCALE_X, MAX_OFFSET, MAX_OFFSET),
            scaleY = AnimatedFloatValue(AnimatedValue.SCALE_Y, MAX_OFFSET, MAX_OFFSET),
            centerLocation = epicenter,
            epicenter = true
        )

        nodes.add(animationNode)

        nodesGroups = nodes.sortedByDescending { it.distance }.groupBy { it.distance }

        animationStaggerOut?.let {
            applyStagger(it, AnimationType.REVEAL)
        }
    }

    override fun applyStagger(animationStagger: AnimationStagger, animationType: AnimationType) {

       */
/* val inDuration = duration
        val stagger = (inDuration * animationStagger.staggerOffset)
        val durationDelta = (inDuration - stagger)
        val delayAddition = stagger / nodesGroups.size
        var delay = 0f

        if (animationType == AnimationType.CONCEAL) {
            for (nodeEntry in nodesGroups.entries.reversed()) {
                val startOffset = delay / inDuration
                val endOffset = (delay + durationDelta) / inDuration

                for(node in nodeEntry.value.reversed()) {
                    node.startOffset = startOffset
                    node.endOffset = endOffset
                }

                delay += delayAddition
            }
        } else {
            for (nodeEntry in nodesGroups.entries) {
                val startOffset = delay / inDuration
                val endOffset = (delay + durationDelta) / inDuration

                for(node in nodeEntry.value) {
                    node.startOffset = startOffset
                    node.endOffset = endOffset
                }

                delay += delayAddition
            }
        }*//*

    }

    override fun animate(fraction: Float, animationType: AnimationType) {
        for (entry in nodesGroups.entries) {
            for (node in entry.value) {

                if (fraction >= node.startOffset) {

                    val mappedRation = mapRange(fraction, node.startOffset, node.endOffset, MIN_OFFSET, MAX_OFFSET)

                    val translationX = node.translationX
                    val translationY = node.translationY

                    val scaleX = node.scaleX
                    val scaleY = node.scaleY

                    when(animationType) {
                        AnimationType.REVEAL -> {
                            val interpolatedFraction = outInterpolator?.getInterpolation(mappedRation) ?: mappedRation

                            node.view.morphTranslationX = (translationX.fromValue + (translationX.toValue - translationX.fromValue) * interpolatedFraction) * amountMultiplier
                            node.view.morphTranslationY = (translationY.fromValue + (translationY.toValue - translationY.fromValue) * interpolatedFraction) * amountMultiplier

                            node.view.morphScaleX = (scaleX.fromValue + (scaleX.toValue - scaleX.fromValue) * interpolatedFraction)
                            node.view.morphScaleY = (scaleY.fromValue + (scaleY.toValue - scaleY.fromValue) * interpolatedFraction)

                            if (node.epicenter) {
                                morphUpdater?.invoke(mappedRation)
                            }

                        }
                        AnimationType.CONCEAL -> {
                            val interpolatedFraction = inInterpolator?.getInterpolation(mappedRation) ?: mappedRation

                            node.view.morphTranslationX = (translationX.toValue + (translationX.fromValue - translationX.toValue) * interpolatedFraction)
                            node.view.morphTranslationY = (translationY.toValue + (translationY.fromValue - translationY.toValue) * interpolatedFraction)

                            node.view.morphScaleX = (scaleX.toValue + (scaleX.fromValue - scaleX.toValue) * interpolatedFraction)
                            node.view.morphScaleY = (scaleY.toValue + (scaleY.fromValue - scaleY.toValue) * interpolatedFraction)

                            if (node.epicenter) {
                                morphUpdater?.invoke(mappedRation)
                            }
                        }
                    }
                }
            }
        }
    }

    data class Node(
        val view: MorphLayout,
        val distance: Float,
        val scaleX: AnimatedFloatValue,
        val scaleY: AnimatedFloatValue,
        val translationX: AnimatedFloatValue,
        val translationY: AnimatedFloatValue,
        val centerLocation: Coordinates,
        val epicenter: Boolean
    ) {
        var startOffset: Float = MIN_OFFSET
        var endOffset: Float = MAX_OFFSET

        override fun toString(): String {
            return "Distance: $distance, ValueX: ${translationX.toValue} ValueY: ${translationY.toValue}"
        }
    }

    enum class Type {
        LOOSE, TIGHT
    }
}*/

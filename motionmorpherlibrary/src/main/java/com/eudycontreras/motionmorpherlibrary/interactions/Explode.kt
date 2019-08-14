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


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 12 2019
 */


class Explode(
    var type: Type = Type.LOOSE,
    override var duration: Long = 0L,
    override var amountMultiplier: Float = MAX_OFFSET
): Interaction() {

    private lateinit var mappingsX: Array<AnimatedFloatValue>
    private lateinit var mappingsY: Array<AnimatedFloatValue>

    private var nodesGroups: Map<Float, List<Node>> = LinkedHashMap()

    private var siblings: List<MorphLayout>? = null
        set(value) {
            field = value
            mappingsX = Array(siblings?.size ?: 0) { AnimatedFloatValue(AnimatedValue.TRANSLATION_X, MIN_OFFSET, MIN_OFFSET) }
            mappingsY = Array(siblings?.size ?: 0) { AnimatedFloatValue(AnimatedValue.TRANSLATION_Y, MIN_OFFSET, MIN_OFFSET) }
        }

    override fun createInteraction(
        startView: MorphLayout,
        endView: MorphLayout
    ) {
        siblings = startView.siblings

        val epicenter = Coordinates(
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

        siblings?.let { siblingCollection ->
            for ((index, sibling) in siblingCollection.withIndex()) {

                val centerPoint = sibling.centerLocation

                val xDifference = centerPoint.x - epicenter.x
                val yDifference = centerPoint.y - epicenter.y

                val xDelta = if (type == Type.LOOSE) xDifference else 0f
                val yDelta = if (type == Type.LOOSE) yDifference else 0f

                if (xDifference > 0) mappingsX[index].toValue = maxX + xDelta
                if (yDifference > 0) mappingsY[index].toValue = maxY + yDelta
                if (xDifference < 0) mappingsX[index].toValue = minX + xDelta
                if (yDifference < 0) mappingsY[index].toValue = minY + yDelta
            }
        }
    }

    override fun buildInteraction(
        startView: MorphLayout,
        endView: MorphLayout
    ) {
        val nodes: LinkedList<Node> = LinkedList()

        val epicenter = Coordinates(
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

                val xDelta = if (type == Type.LOOSE) xDifference else 0f
                val yDelta = if (type == Type.LOOSE) yDifference else 0f

                if (xDifference > 0) mappingX.toValue = maxX + (xDelta * amountMultiplier)
                if (yDifference > 0) mappingY.toValue = maxY + (yDelta * amountMultiplier)
                if (xDifference < 0) mappingX.toValue = minX + (xDelta * amountMultiplier)
                if (yDifference < 0) mappingY.toValue = minY + (yDelta * amountMultiplier)

                val animationNode = Node(
                    view = sibling,
                    distance = distance,
                    valueX = mappingX,
                    valueY = mappingY
                )
                nodes.add(animationNode)
            }
            collection
        }

        nodesGroups = nodes.sortedByDescending { it.distance }.groupBy { it.distance }

        animationStagger?.let {
            applyStagger(it, AnimationType.REVEAL)
        }
    }

    override fun applyStagger(animationStagger: AnimationStagger, animationType: AnimationType) {
        val inDuration = duration
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
        }
    }

    override fun animate(fraction: Float, animationType: AnimationType) {
        for (entry in nodesGroups.entries) {
            for (node in entry.value) {

                if (fraction >= node.startOffset && fraction <= node.endOffset) {

                    val mappedRation = mapRange(fraction, node.startOffset, node.endOffset, MIN_OFFSET, MAX_OFFSET)

                    val animatedX = node.valueX
                    val animatedY = node.valueY

                    when(animationType) {
                        AnimationType.REVEAL -> {
                            val interpolatedFraction = outInterpolator?.getInterpolation(mappedRation) ?: mappedRation

                            node.view.morphTranslationX = (animatedX.fromValue + (animatedX.toValue - animatedX.fromValue) * interpolatedFraction)
                            node.view.morphTranslationY = (animatedY.fromValue + (animatedY.toValue - animatedY.fromValue) * interpolatedFraction)
                        }
                        AnimationType.CONCEAL -> {
                            val interpolatedFraction = inInterpolator?.getInterpolation(mappedRation) ?: mappedRation

                            node.view.morphTranslationX = (animatedX.toValue + (animatedX.fromValue - animatedX.toValue) * interpolatedFraction)
                            node.view.morphTranslationY = (animatedY.toValue + (animatedY.fromValue - animatedY.toValue) * interpolatedFraction)
                        }
                    }
                }
            }
        }
    }

    override fun applyInteraction(fraction: Float, animationType: AnimationType) {
          val siblings = this.siblings ?: return

          for ((index, sibling) in siblings.withIndex()) {
              val animatedX = mappingsX[index]
              val animatedY = mappingsY[index]

              when(animationType) {
                  AnimationType.REVEAL -> {
                      val interpolatedFraction = outInterpolator?.getInterpolation(fraction) ?: fraction

                      sibling.morphTranslationX = (animatedX.fromValue + (animatedX.toValue - animatedX.fromValue) * interpolatedFraction) * amountMultiplier
                      sibling.morphTranslationY = (animatedY.fromValue + (animatedY.toValue - animatedY.fromValue) * interpolatedFraction) * amountMultiplier
                  }
                  AnimationType.CONCEAL -> {
                      val interpolatedFraction = inInterpolator?.getInterpolation(fraction) ?: fraction

                      sibling.morphTranslationX = (animatedX.toValue + (animatedX.fromValue - animatedX.toValue) * interpolatedFraction) * amountMultiplier
                      sibling.morphTranslationY = (animatedY.toValue + (animatedY.fromValue - animatedY.toValue) * interpolatedFraction) * amountMultiplier
                  }
              }
          }
    }

    data class Node(
        val view: MorphLayout,
        val distance: Float,
        val valueX: AnimatedFloatValue,
        val valueY: AnimatedFloatValue
    ) {
        var startOffset: Float = MIN_OFFSET
        var endOffset: Float = MAX_OFFSET

        override fun toString(): String {
            return "Distance: $distance, ValueX: ${valueX.toValue} ValueY: ${valueY.toValue}"
        }
    }

    enum class Type {
        LOOSE, TIGHT
    }
}
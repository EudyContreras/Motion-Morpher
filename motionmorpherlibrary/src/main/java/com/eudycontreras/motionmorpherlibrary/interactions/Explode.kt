package com.eudycontreras.motionmorpherlibrary.interactions

import com.eudycontreras.motionmorpherlibrary.MAX_OFFSET
import com.eudycontreras.motionmorpherlibrary.MIN_DURATION
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.enumerations.AnimationType
import com.eudycontreras.motionmorpherlibrary.enumerations.Stagger
import com.eudycontreras.motionmorpherlibrary.helpers.StretchAnimationHelper
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.mapRange
import com.eudycontreras.motionmorpherlibrary.properties.*
import java.util.*
import kotlin.math.hypot
import kotlin.math.round
import kotlin.math.roundToLong


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 12 2019
 */

/**
 * The effect should be able to have an incremental/decremental stagger effect
 * and a stretch effect which should be share or divided.
 *
 * The Stretch should work in this way: 0 means that there is no stretch
 * 0.2 stretchOffset means that the stretch should happen for 20% of the animation before the translation
 * start. The stretch should happen in the direction of the translation.
 */
class Explode(
    var type: Type = Type.LOOSE,
    override var amountMultiplier: Float = MAX_OFFSET
): Interaction() {

    var stretch: Stretch? = null

    private var animationNodes: LinkedList<AnimationNode> = LinkedList()

    private lateinit var endView: MorphLayout
    private lateinit var startView: MorphLayout

    override fun buildInteraction(
        startView: MorphLayout,
        endView: MorphLayout
    ) {
        this.endView = endView
        this.startView = startView

        val animationNodes: LinkedList<AnimationNode> = LinkedList()

        val epicenter = startView.centerLocation

        val boundsStart: ViewBounds = startView.viewBounds
        val boundsEnd: ViewBounds = endView.viewBounds

        val widthDelta = if (type == Type.LOOSE) (boundsStart.width) else MIN_OFFSET
        val heightDelta = if (type == Type.LOOSE) (boundsStart.height) else MIN_OFFSET

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

                val xDelta = if (type == Type.LOOSE) xDifference else MIN_OFFSET
                val yDelta = if (type == Type.LOOSE) yDifference else MIN_OFFSET

                if (xDifference > 0) mappingX.toValue = maxX + xDelta
                if (yDifference > 0) mappingY.toValue = maxY + yDelta
                if (xDifference < 0) mappingX.toValue = minX + xDelta
                if (yDifference < 0) mappingY.toValue = minY + yDelta

                val animationNode = AnimationNode(
                    view = sibling,
                    distance = distance,
                    translationX = mappingX,
                    translationY = mappingY
                )
                animationNodes.add(animationNode)
            }
        }

        this.animationNodes = LinkedList(animationNodes.sortedByDescending { it.distance })
    }

    override fun applyStagger(animationStagger: AnimationStagger?, animationType: AnimationType) {
        /*
         * If the stagger is null or the offset is 0 or the duration is 0 return.
                */
        if (animationStagger == null || animationStagger.staggerOffset == MIN_OFFSET || duration == MIN_DURATION) {
            val animationNode = AnimationNode(
                view = endView,
                distance = MIN_OFFSET,
                translationX = AnimatedFloatValue(AnimatedValue.TRANSLATION_X, MIN_OFFSET, MIN_OFFSET),
                translationY = AnimatedFloatValue(AnimatedValue.TRANSLATION_X, MIN_OFFSET, MIN_OFFSET),
                epicenter = true
            )
            animationNodes.addFirst(animationNode)
            return
        }

        /*
         * When an epicenter is available sort and group the list of animationNodes by distance.
         */
        val nodesGroups = when (animationType) {
            AnimationType.CONCEAL -> {
                animationNodes =
                    LinkedList(animationNodes.filter { it.distance != MIN_OFFSET }.sortedBy { it.distance })
                animationNodes.groupBy { it.distance }
            }
            AnimationType.REVEAL -> {
                animationNodes = LinkedList(animationNodes.sortedByDescending { it.distance })
                animationNodes.groupBy { it.distance }
            }
        }
        /*
         * The total amount of stagger is created by multiplying the duration by the offset of the stagger.
         * A staggerOffset of 0.5 means that the next animation should wait until the previous
         * animation is halfway in order to start.
         */
        val stagger = (duration * animationStagger.staggerOffset)

        /*
         * The duration delta is calculated by removing the total amount stagger from
         * the total amount of duration of the animation as a whole. Regardless of the amount of
         * stagger. The duration must remain constant.
         */
        val durationDelta = (duration - stagger)

        /*
         * The delay addition is the value added upon each iteration. This value is incremental
         * and is created dividing the total amount of stagger by the number of items to animate
         * if incremental fragmentation is used the delay addition must be divided by 2.
         * After each iteration upon which the stagger value is assigned to each node the delay
         * addition of the stagger will increase.
         */
        val delayAddition: Float

        /*
         * Holds the value to be added to or subtracted from the delay addition in order to
         * create the incremental or decremental effect.
         */
        val fragmentation: Float

        /*
         * The accumulator value is used for accumulating the amount of fragmentation needed
         * in order to reach the final desired delay value by the end of the iteration. This
         * accumulator is added to or subtracted from the delay addition in order to create
         * incremental or decremental stagger effects..
         */
        var accumulator: Float

        when (animationStagger.type) {
            Stagger.INCREMENTAL -> {
                delayAddition = (stagger / (nodesGroups.size - 1)) / 2
                fragmentation = (stagger / (nodesGroups.size - 1)) / (nodesGroups.size - 2)
                accumulator = MIN_OFFSET
            }
            Stagger.DECREMENTAL -> {
                delayAddition = (stagger / (nodesGroups.size - 1))
                fragmentation = (stagger / (nodesGroups.size - 1)) / (nodesGroups.size - 2)
                accumulator =  (stagger / (nodesGroups.size - 1)) / 2
            }
            Stagger.LINEAR -> {
                delayAddition = (stagger / (nodesGroups.size - 1))
                fragmentation = MIN_OFFSET
                accumulator = MIN_OFFSET
            }
        }

        /*
         * The amount of the delay added to each node. The delay is incremented upon each
         * iteration and the final value should be equal the total stagger of the animation.
         */
        var delay = MIN_OFFSET

        for (nodeEntry in nodesGroups.entries) {
            /*
             * The start offset the point within the main animation in which the current element
             * should start animating. This is used for mapping animations to a 0f to 1f fraction.
             */
            val startOffset = delay / duration
            /*
             * The end offset the point within the main animation in which the current element
             * should stop animating. This is used for mapping animations to a 0f to 1f fraction.
             */
            val endOffset = (delay + durationDelta) / duration

            for(node in nodeEntry.value) {
                node.startOffset = startOffset
                node.endOffset = endOffset
                node.stagger = delay.roundToLong()
            }

            /*
             * The amount of delay for each of the elements is calculated based on
             * the fadeType of stagger being used. With incremental stagger the later elements
             * have higher delays. With decremental stagger the later elements have lower
             * delays while with linear stagger all elements have the same amount of delay
             * between them.
             */
            when (animationStagger.type) {
                Stagger.INCREMENTAL -> {
                    delay +=  (delayAddition + accumulator)
                    accumulator += fragmentation
                }
                Stagger.DECREMENTAL -> {
                    delay +=  (delayAddition + accumulator)
                    accumulator -= fragmentation
                }
                Stagger.LINEAR -> {
                    delay +=  delayAddition
                }
            }
        }

        /*
         * The epicenter node is and added to the animation stack and used as a signaling trigger
         * for animating the view being causing the explosion.
         */
        when (animationType) {
            AnimationType.REVEAL -> {
                val last = animationNodes.last()
                val animationNode = AnimationNode(
                    view = endView,
                    distance = MIN_OFFSET,
                    translationX = AnimatedFloatValue(AnimatedValue.TRANSLATION_X, MIN_OFFSET, MIN_OFFSET),
                    translationY = AnimatedFloatValue(AnimatedValue.TRANSLATION_X, MIN_OFFSET, MIN_OFFSET),
                    epicenter = true
                )
                animationNode.copyOffsets(last)
                animationNodes.add(animationNode)
            }
            AnimationType.CONCEAL -> {
                val first = animationNodes.first()
                val animationNode = AnimationNode(
                    view = endView,
                    distance = MIN_OFFSET,
                    translationX = AnimatedFloatValue(AnimatedValue.TRANSLATION_X, MIN_OFFSET, MIN_OFFSET),
                    translationY = AnimatedFloatValue(AnimatedValue.TRANSLATION_X, MIN_OFFSET, MIN_OFFSET),
                    epicenter = true
                )
                animationNode.copyOffsets(first)
                animationNodes.addFirst(animationNode)
            }
        }
    }

    override fun animate(fraction: Float, animationType: AnimationType) {
        for (node in animationNodes) {

            if (fraction < node.startOffset || fraction > node.endOffset)
                continue

            val mappedRation = mapRange(fraction, node.startOffset, node.endOffset, MIN_OFFSET, MAX_OFFSET)

            if (node.epicenter) {
                morphUpdater?.invoke(mappedRation)
                continue
            }

            if (amountMultiplier == MIN_OFFSET)
                return

            val translationX = node.translationX
            val translationY = node.translationY

            stretch?.let {
                StretchAnimationHelper.applyStretch(node.view, translationY, it, node.view.morphTranslationY)
            }

            when(animationType) {
                AnimationType.REVEAL -> {
                    val interpolatedFraction = outInterpolator?.getInterpolation(mappedRation) ?: mappedRation

                    node.view.morphTranslationX = (translationX.fromValue + (translationX.toValue - translationX.fromValue) * interpolatedFraction) * amountMultiplier
                    node.view.morphTranslationY = (translationY.fromValue + (translationY.toValue - translationY.fromValue) * interpolatedFraction) * amountMultiplier
                }
                AnimationType.CONCEAL -> {
                    val interpolatedFraction = inInterpolator?.getInterpolation(mappedRation) ?: mappedRation

                    node.view.morphTranslationX = (translationX.toValue + (translationX.fromValue - translationX.toValue) * interpolatedFraction) * amountMultiplier
                    node.view.morphTranslationY = (translationY.toValue + (translationY.fromValue - translationY.toValue) * interpolatedFraction) * amountMultiplier
                }
            }
        }
    }

    enum class Type {
        LOOSE, TIGHT
    }
}
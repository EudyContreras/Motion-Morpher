package com.eudycontreras.motionmorpherlibrary.properties

/**
 * Class which represents an offset trigger.
 * When the value passed into the [listenTo] function
 * reaches the defined [percentage] the trigger will go
 * off and perform the specified [triggerAction] if it
 * hasnt already been triggered.
 *
 * @param percentage The percentage at which the trigger should go off.
 * @param triggerAction The action to perform when the trigger goes off.
 *
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */

data class OffsetTrigger(
    val percentage: Float,
    val triggerAction: () -> Unit
) {
    /**
     * Specifies whether the action of this trigger has been
     * performed.
     */
    var hasTriggered: Boolean = false

    fun listenTo(offset: Float) {
        if (!hasTriggered && offset >= percentage) {
            triggerAction.invoke()
            hasTriggered = true
        }
    }
}
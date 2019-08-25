package com.eudycontreras.motionmorpherlibrary.properties

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
package com.eudycontreras.motionmorpherlibrary

import com.eudycontreras.motionmorpherlibrary.properties.Bounds
import java.util.*

/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 12 2019
 */


typealias Action = (()-> Unit)?

typealias ValueChangeListener <T> = (old: T, new: T) -> Unit

typealias PropertyChangeListener <T> = (oldValue: T, newValue: T, name: String) -> Unit

typealias TransitionOffsetListener = ((percent: Float) -> Unit)?

typealias ContainerBoundsListener = ((oldBounds: Bounds, newBounds: Bounds) -> Unit)?

typealias BackgroundDimListener = ((dimAmount: Float) -> Unit)?

typealias ComputedStatesListener = ((startState: Morpher.Properties, endState: Morpher.Properties) -> Unit)?

typealias TranslationPositions = EnumSet<Morpher.TranslationPosition>

typealias MorphValuesListener = ((startValue: Morpher.MorphValues, endValues: Morpher.MorphValues) -> Unit)?
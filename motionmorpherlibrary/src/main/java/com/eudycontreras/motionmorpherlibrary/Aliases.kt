package com.eudycontreras.motionmorpherlibrary

typealias Action = (()-> Unit)?

typealias ValueChangeListener <T> = (old: T, new: T) -> Unit

typealias PropertyChangeListener <T> = (oldValue: T, newValue: T, name: String) -> Unit

typealias TransitionOffsetListener = ((percent: Float) -> Unit)?

typealias BackgroundDimListener = ((dimAmount: Float) -> Unit)?
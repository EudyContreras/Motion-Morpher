package com.eudycontreras.motionmorpherlibrary.extensions

import com.eudycontreras.motionmorpherlibrary.Morpher
import com.eudycontreras.motionmorpherlibrary.globals.TranslationPositions


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 03 2019
 */

infix fun TranslationPositions.and(other: Morpher.TranslationPosition): TranslationPositions = TranslationPositions.of(other, *this.toTypedArray())

infix fun TranslationPositions.has(other: TranslationPositions) = this.containsAll(other)

infix fun TranslationPositions.has(other: Morpher.TranslationPosition) = this.contains(other)

fun TranslationPositions.get(item: Morpher.TranslationPosition): Morpher.TranslationPosition {
    return this.first { it == item}
}
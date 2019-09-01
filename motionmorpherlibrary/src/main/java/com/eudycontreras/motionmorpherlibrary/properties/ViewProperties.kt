package com.eudycontreras.motionmorpherlibrary.properties


/**
 * Class which holds information about the animateable properties
 * of a view.
 *
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 18 2019
 */
 
data class ViewProperties(
    val x: Float,
    val y: Float,
    val z: Float,
    val alpha: Float,
    val elevation: Float,
    var translationX: Float,
    var translationY: Float,
    val translationZ: Float,
    val pivotX: Float,
    val pivotY: Float,
    val rotation: Float,
    val rotationX: Float,
    val rotationY: Float,
    val scaleX: Float,
    val scaleY: Float,
    val top: Int,
    val left: Int,
    val bottom: Int,
    val right: Int,
    val tag: String
)
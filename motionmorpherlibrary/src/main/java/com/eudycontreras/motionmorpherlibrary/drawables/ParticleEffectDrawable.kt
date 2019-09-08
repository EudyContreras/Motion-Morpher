package com.eudycontreras.motionmorpherlibrary.drawables

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.extensions.dp
import com.eudycontreras.motionmorpherlibrary.interfaces.Clipable
import com.eudycontreras.motionmorpherlibrary.particles.Particle
import com.eudycontreras.motionmorpherlibrary.particles.effects.RippleEffect
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii
import com.eudycontreras.motionmorpherlibrary.properties.Ripple


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since September 07 2019
 */

class ParticleEffectDrawable() : Drawable() {

    private lateinit var particle: Particle

    private var path: Path = Path()

    private var paint: Paint = Paint().apply {
        isAntiAlias = true
        color = -0xbdbdbe
    }

    fun setupWith(particle: Particle): ParticleEffectDrawable {
        this.particle = particle
        return this
    }

    override fun setAlpha(alpha: Int) {
        val oldAlpha = paint.getAlpha()
        if (alpha != oldAlpha) {
            paint.setAlpha(alpha)
            invalidateSelf()
        }
    }

    override fun getOpacity(): Int {
        return paint.alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) { }

    override fun draw(canvas: Canvas) {

    }

    companion object {
        fun ofRipple(ripple: Ripple): ParticleEffectDrawable{
            return ParticleEffectDrawable().apply {
                setupWith(RippleEffect(ripple))
            }
        }
    }
}
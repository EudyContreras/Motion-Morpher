package com.eudycontreras.motionmorpherlibrary.interpolators

import android.animation.TimeInterpolator
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.eudycontreras.motionmorpherlibrary.enumerations.Interpolation
import java.lang.StrictMath.pow
import kotlin.math.sin


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 18 2019
 */
 
 
class MaterialInterpolator(type: Interpolation? = null): TimeInterpolator {

    private var pathInterpolator: TimeInterpolator =
        when (type) {
            Interpolation.STANDARD -> FastOutSlowInInterpolator()
            Interpolation.OUTGOING -> FastOutLinearInInterpolator()
            Interpolation.INCOMING -> LinearOutSlowInInterpolator()

            Interpolation.QUART_OUT -> PathInterpolatorCompat.create(.165f, .84f, .44f, 1f)
            Interpolation.QUART_IN_OUT -> PathInterpolatorCompat.create(.77f, 0f, .175f, 1f)
            Interpolation.CIRC_IN -> PathInterpolatorCompat.create(.6f, .04f, .98f, .335f)
            Interpolation.CIRC_OUT -> PathInterpolatorCompat.create(.075f, .82f, .165f, 1f)
            Interpolation.CIRC_IN_OUT -> PathInterpolatorCompat.create(.785f, .135f, .15f, .86f)
            Interpolation.EXP_OUT -> PathInterpolatorCompat.create(.19f, 1f, .22f, 1f)
            Interpolation.EXP_IN_OUT -> PathInterpolatorCompat.create(1f, 0f, 0f, 1f)
            Interpolation.QUINT_IN_OUT -> PathInterpolatorCompat.create(.86f, 0f, .07f, 1f)
            Interpolation.CUBIC_IN_OUT -> PathInterpolatorCompat.create(.645f, .045f, .355f, 1f)
            Interpolation.FAST_OUT_SLOW_IN -> PathInterpolatorCompat.create(.065f, .85f, .18f, 1f)

            Interpolation.REVERSED_OUT ->  Reversed()
            Interpolation.ELASTIC_OUT ->  ElasticOut()
            Interpolation.SPRING_IN ->  Spring()

            else -> PathInterpolatorCompat.create(0.4f, 0.0f, 0.2f, 1f)
    }

    override fun getInterpolation(fraction: Float) = pathInterpolator.getInterpolation(fraction)

    class Spring: TimeInterpolator {
        val factor: Float = 0.3f
        override fun getInterpolation(fraction: Float): Float {
            return (pow(2.0, (-10 * fraction).toDouble()) * sin(2 * Math.PI * (fraction - factor / 4) / factor) + 1).toFloat()
        }
    }

    class ElasticOut: TimeInterpolator {
        val factor: Float = 0.3f
        override fun getInterpolation(fraction: Float): Float {
            return (pow(2.0, (-10 * fraction).toDouble()) * sin((fraction - factor / 4) * (2 * Math.PI) / factor) + 1).toFloat()
        }
    }

    class Reversed: TimeInterpolator {
        val factor: Float = 0.3f
        override fun getInterpolation(fraction: Float): Float {
            return 1.0f - fraction
        }
    }
}
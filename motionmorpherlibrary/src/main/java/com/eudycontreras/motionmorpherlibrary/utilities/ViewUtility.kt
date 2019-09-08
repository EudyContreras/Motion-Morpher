package com.eudycontreras.motionmorpherlibrary.utilities

import android.graphics.*
import com.eudycontreras.motionmorpherlibrary.MIN_COLOR
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 03 2019
 */
 
 
object ViewUtility {

    fun getRoundedBitmap(
        input: Bitmap,
        width: Int,
        height: Int,
        paint: Paint,
        clipPath: Path,
        cornerRadii: CornerRadii
    ): Bitmap {

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output).apply { drawARGB(MIN_COLOR, MIN_COLOR, MIN_COLOR, MIN_COLOR) }

        clipPath.rewind()
        clipPath.addRoundRect(MIN_OFFSET, MIN_OFFSET, width.toFloat(), height.toFloat(), cornerRadii.corners, Path.Direction.CCW)
        clipPath.close()

        canvas.drawPath(clipPath, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        canvas.drawBitmap(input, MIN_OFFSET, MIN_OFFSET, paint)

        return output
    }
}
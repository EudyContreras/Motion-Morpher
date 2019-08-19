package com.eudycontreras.motionmorpherlibrary.utilities

import android.graphics.*
import com.eudycontreras.motionmorpherlibrary.properties.CornerRadii


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 03 2019
 */
 
 
class ViewUtility {
    companion object {

        fun getRoundedBitmap(
            input: Bitmap,
            width: Int,
            height: Int,
            paint: Paint,
            clipPath: Path,
            cornerRadii: CornerRadii
        ): Bitmap {

            val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output).apply { drawARGB(0, 0, 0, 0) }

            clipPath.rewind()
            clipPath.addRoundRect(0f, 0f, width.toFloat(), height.toFloat(), cornerRadii.corners, Path.Direction.CCW)
            clipPath.close()

            canvas.drawPath(clipPath, paint)

            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

            canvas.drawBitmap(input, 0f, 0f, paint)

            return output
        }
    }
}
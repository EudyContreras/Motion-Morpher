package com.eudycontreras.motionmorpherlibrary.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.math.MathUtils.clamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import kotlin.math.roundToInt


/**
 * Would contain an image buffer running on a background thread. The buffer holds the n* numbers of next
 * images to be rendered on a view. The images are prepared and can then be played using a 0 to 1 fraction.
 *
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since August 31 2019
 */
class FrameAnimationHelper(var context: Context) {

    private var bufferSize: Int = 5

    private var bitmapFrames: ArrayList<WeakReference<Bitmap>> = ArrayList()

    fun prepareFrames(vararg frameIds: Int, readyListener: (List<WeakReference<Bitmap>>) -> Unit) {
        GlobalScope.launch() {
            for (frameId in frameIds) {
                bitmapFrames.add(WeakReference(BitmapFactory.decodeResource(context.getResources(), frameId)))
            }
            readyListener(bitmapFrames)
        }
    }

    fun prepareFrames(frameIds: List<Int>, readyListener: (List<WeakReference<Bitmap>>) -> Unit) {
        GlobalScope.launch() {
            for (frameId in frameIds) {
                bitmapFrames.add(WeakReference(BitmapFactory.decodeResource(context.getResources(), frameId)))
            }
            readyListener(bitmapFrames)
        }
    }


    fun playFrames(frames: List<Bitmap>, fraction: Float, frameListener: (index: Int, frame: Bitmap) -> Unit) {
        val raw = ((frames.size - 1) * fraction).roundToInt()

        val index = clamp(raw, 0, frames.size - 1)

        val frame = frames[index]

        frameListener(index, frame)
    }
}
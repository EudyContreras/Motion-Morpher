package com.eudycontreras.motionmorpherlibrary.activities

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import com.eudycontreras.motionmorpherlibrary.Morpher
import com.eudycontreras.motionmorpherlibrary.layouts.MorphContainer


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 19 2019
 */
 
 
abstract class MorphDialog : DialogFragment() {

    protected lateinit var morphView: MorphContainer
    protected lateinit var activity: MorphActivity

    protected lateinit var morpher: Morpher
    protected lateinit var layout: ViewGroup

    @LayoutRes protected var layoutId: Int = -1

    @StyleRes protected var layoutTheme: Int = -1

    protected var onShown: ((MorphDialog, MorphContainer) -> Unit)? = null

    override fun onResume() {
        super.onResume()
        dialog?.window?.let {
            val layoutParams = it.attributes
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            it.attributes = layoutParams
            it.setGravity(Gravity.CENTER)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, layoutTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(activity, theme) {
            override fun onBackPressed() {
                if (morpher.isMorphing) {
                    morpher.cancelMorph()
                    return
                }
                if (morpher.isMorphed) {
                    morpher.morphFrom(onEnd = { super.onBackPressed() })
                    return
                }

                super.onBackPressed()
            }
        }.apply {
            window?.let {
                it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                it.requestFeature(Window.FEATURE_NO_TITLE)
                it.setDimAmount(0f)
            }
        }
    }
}
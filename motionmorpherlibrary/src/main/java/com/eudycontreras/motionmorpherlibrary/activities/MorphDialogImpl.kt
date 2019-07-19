package com.eudycontreras.motionmorpherlibrary.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import com.eudycontreras.motionmorpherlibrary.Morpher
import com.eudycontreras.motionmorpherlibrary.R
import com.eudycontreras.motionmorpherlibrary.layouts.MorphContainer


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 19 2019
 */
 
 
class MorphDialogImpl: MorphDialog() {

    companion object {
        fun instance(
            activity: MorphActivity,
            morpher: Morpher,
            @LayoutRes layoutId: Int,
            @StyleRes layoutTheme: Int,
            showListener: ((MorphDialog, MorphContainer) -> Unit)? = null
        ): MorphDialogImpl {

            val fragment = MorphDialogImpl()
            fragment.activity = activity
            fragment.morpher = morpher
            fragment.layoutId = layoutId
            fragment.layoutTheme = layoutTheme
            fragment.onShown = showListener
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = inflater.inflate(R.layout.morph_dialog_container, container) as ViewGroup
        inflater.inflate(layoutId, layout, true)
        morphView = layout.getChildAt(0) as MorphContainer

        morphView.post {
            onShown?.invoke(this, morphView)
        }

        morpher.backgroundDimListener = {
            layout.background.alpha = (it * 255).toInt()
        }
        return layout
    }
}
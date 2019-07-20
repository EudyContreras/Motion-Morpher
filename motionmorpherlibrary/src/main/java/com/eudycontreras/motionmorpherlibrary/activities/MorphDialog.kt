package com.eudycontreras.motionmorpherlibrary.activities

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import com.eudycontreras.motionmorpherlibrary.Morpher
import com.eudycontreras.motionmorpherlibrary.R
import com.eudycontreras.motionmorpherlibrary.layouts.MorphWrapper


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 19 2019
 */
 
 
sealed class MorphDialog : DialogFragment() {

    lateinit var morphView: MorphWrapper

    protected lateinit var activity: MorphActivity

    protected lateinit var morpher: Morpher
    protected lateinit var layout: ViewGroup

    protected var showListener: ArrayList<((MorphWrapper) -> Unit)?> = ArrayList()

    @LayoutRes protected var layoutId: Int = -1

    @StyleRes protected var layoutTheme: Int = -1

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

    fun addShowListener(listener: ((MorphWrapper) -> Unit)? ) {
        this.showListener.add(listener)
    }

    fun show(listener: ((MorphWrapper) -> Unit)? = null ) {
        val prev = activity.supportFragmentManager.findFragmentByTag(this::class.java.simpleName)

        val fragmentTransaction = activity.supportFragmentManager.beginTransaction()

        if (activity.supportFragmentManager.fragments.contains(this) || prev != null) {
            fragmentTransaction.remove(prev!!)
        }

        fragmentTransaction.addToBackStack(null)

        listener?.let {
            addShowListener(it)
        }

        this.show(fragmentTransaction, this::class.java.simpleName)
    }

    companion object {
        fun instance(
            activity: MorphActivity,
            morpher: Morpher,
            @LayoutRes layoutId: Int,
            @StyleRes layoutTheme: Int,
            showListener: ((MorphWrapper) -> Unit)? = null
        ): MorphDialogImpl {

            val fragment = MorphDialogImpl()
            fragment.activity = activity
            fragment.morpher = morpher
            fragment.layoutId = layoutId
            fragment.layoutTheme = layoutTheme
            fragment.addShowListener(showListener)
            return fragment
        }

    }

    class MorphDialogImpl: MorphDialog() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            layout = inflater.inflate(R.layout.morph_dialog_container, container) as ViewGroup
            inflater.inflate(layoutId, layout, true)
            morphView = layout.getChildAt(0) as MorphWrapper

            morphView.post {
                morpher.endView = morphView
                showListener.forEach { it?.invoke(morphView) }
            }

            morpher.backgroundDimListener = {
                layout.background.alpha = (it * 255).toInt()
            }
            return layout
        }
    }
}
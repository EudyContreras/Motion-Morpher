package com.eudycontreras.motionmorpherlibrary.activities

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import com.eudycontreras.motionmorpherlibrary.Action
import com.eudycontreras.motionmorpherlibrary.MIN_OFFSET
import com.eudycontreras.motionmorpherlibrary.Morpher
import com.eudycontreras.motionmorpherlibrary.R
import com.eudycontreras.motionmorpherlibrary.layouts.MorphLayout
import com.eudycontreras.motionmorpherlibrary.layouts.morphLayouts.ConstraintLayout
import java.util.*


/**
 * @Project MotionMorpher
 * @author Eudy Contreras.
 * @since July 19 2019
 */
 
 
sealed class MorphDialog : DialogFragment() {

    lateinit var morphView: ConstraintLayout

    protected lateinit var activity: MorphActivity

    protected lateinit var morpher: Morpher
    protected lateinit var layout: ViewGroup

    protected var createListener: LinkedList<((MorphLayout) -> Unit)?> = LinkedList()
    protected var dismissRequestListener: LinkedList<Action> = LinkedList()

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
                    dismissRequestListener.forEach {
                        it?.invoke()
                    }
                    return
                }
                super.onBackPressed()
            }
        }.apply {
            window?.let {
                it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                it.requestFeature(Window.FEATURE_NO_TITLE)
                it.setDimAmount(MIN_OFFSET)
            }
        }
    }

    fun addCreateListener(listener: ((MorphLayout) -> Unit)? ) {
        this.createListener.add(listener)
    }

    fun addDismissRequestListener(listener: Action ) {
        this.dismissRequestListener.add(listener)
    }

    fun requestDismiss() {
        dialog?.onBackPressed()
    }

    override fun dismiss() {
        if (morpher.isMorphing) {
            return
        }
        if (morpher.isMorphed) {
            return
        }
        super.dismiss()
    }

    fun show(listener: ((MorphLayout) -> Unit)? = null ) {
        val prev = activity.supportFragmentManager.findFragmentByTag(this::class.java.simpleName)

        val fragmentTransaction = activity.supportFragmentManager.beginTransaction()

        if (activity.supportFragmentManager.fragments.contains(this) || prev != null) {
            fragmentTransaction.remove(prev!!)
        }

        fragmentTransaction.addToBackStack(null)

        listener?.let {
            addCreateListener(it)
        }

        this.show(fragmentTransaction, this::class.java.simpleName)
    }

    companion object {
        fun instance(
            activity: MorphActivity,
            morpher: Morpher,
            @LayoutRes layoutId: Int,
            @StyleRes layoutTheme: Int,
            showListener: ((MorphLayout) -> Unit)? = null
        ): MorphDialogImpl {

            val fragment = MorphDialogImpl()
            fragment.activity = activity
            fragment.morpher = morpher
            fragment.layoutId = layoutId
            fragment.layoutTheme = layoutTheme
            fragment.addCreateListener(showListener)
            return fragment
        }
    }

    class MorphDialogImpl: MorphDialog() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            layout = inflater.inflate(R.layout.morph_dialog_container, container) as ViewGroup
            layoutInflater.inflate(layoutId, layout, true)

            morphView = layout.getChildAt(0) as ConstraintLayout
            morphView.morphAlpha = MIN_OFFSET
            morphView.post {
                morpher.endView = morphView
                createListener.forEach { it?.invoke(morphView) }
            }
            layout.background.alpha = 0
            morpher.backgroundDimListener = {
                layout.background.alpha = (it * 255).toInt()
            }
            return layout
        }
    }
}
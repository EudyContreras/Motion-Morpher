package com.eudycontreras.motionmorpher.examples.demo1

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.eudycontreras.motionmorpher.R
import com.eudycontreras.motionmorpherlibrary.Morpher
import com.eudycontreras.motionmorpherlibrary.activities.MorphActivity
import com.eudycontreras.motionmorpherlibrary.activities.MorphDialog
import com.eudycontreras.motionmorpherlibrary.extensions.openDialog
import com.eudycontreras.motionmorpherlibrary.layouts.MorphWrapper
import kotlinx.android.synthetic.main.activity_demo1.*


class ActivityDemo1 : MorphActivity() {

    lateinit var morpher: Morpher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo1)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_menu)

        morpher = Morpher(this)

        morpher.useArcTranslator = false

        morpher.morphIntoDuration = 1500
        morpher.morphFromDuration = 1500

        morpher.morphIntoInterpolator = FastOutSlowInInterpolator()
        morpher.morphFromInterpolator = FastOutSlowInInterpolator()

        morpher.endStateMorphIntoDescriptor.animateOnOffset = 0f
        morpher.endStateMorphFromDescriptor.animateOnOffset = 0f

        morpher.startView = toolbarMenuBor as MorphWrapper

        fab.setOnClickListener {
            val dialog = MorphDialog.instance(this, morpher, R.layout.activity_demo1_details, R.style.AppTheme_Dialog) { dialog, morphView ->

                val details = DetailsDemo1(this, dialog, morphView)

                morpher.endView = morphView
                morpher.morphInto()
            }
            openDialog(dialog)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_demo_1, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun getRoot(): ViewGroup {
        return this.findViewById(R.id.root)
    }
}

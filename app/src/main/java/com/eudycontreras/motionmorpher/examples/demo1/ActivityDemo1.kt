package com.eudycontreras.motionmorpher.examples.demo1

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.eudycontreras.motionmorpher.R
import com.eudycontreras.motionmorpherlibrary.Morpher
import com.eudycontreras.motionmorpherlibrary.layouts.MorphContainer
import kotlinx.android.synthetic.main.activity_demo1.*


class ActivityDemo1 : AppCompatActivity() {

    private lateinit var morpher: Morpher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo1)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_menu)

        morpher = Morpher(this)

        morpher.useArcTranslator = false

        morpher.morphIntoInterpolator = FastOutSlowInInterpolator()
        morpher.morphFromInterpolator = FastOutSlowInInterpolator()

        morpher.endStateMorphIntoDescriptor.animateOnOffset = 0f

        morpher.startView = toolbarMenuBor as MorphContainer
        morpher.endView = detailsLayout as MorphContainer

        fab.setOnClickListener  {
            morpher.morphInto(
                duration = 10000
            )
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

}

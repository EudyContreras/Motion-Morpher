package com.eudycontreras.motionmorpher.examples.demo2

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.eudycontreras.motionmorpher.R
import com.eudycontreras.motionmorpherlibrary.Morpher
import com.eudycontreras.motionmorpherlibrary.activities.MorphActivity

class ActivityDemo2 : MorphActivity() {

    lateinit var morpher: Morpher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo2)
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

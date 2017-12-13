package com.example.samanthamorrison.xmasify

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.RelativeLayout



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val grid = findViewById<GridLayout>(R.id.grid_layout)
        val childCount = grid.childCount

        for (i in 0 until childCount) {
            val container = grid.getChildAt(i) as ImageView
            container.setOnClickListener {
                val intent = Intent (this, SpecificDayActivity::class.java)
                intent.putExtra("EXTRA", grid.id)
                startActivity(intent)
            }
        }

    }



}



package com.example.samanthamorrison.xmasify

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageView1 = findViewById<View>(R.id.gift1) as ImageView
        val imageView2 = findViewById<View>(R.id.gift2) as ImageView
        val imageView3 = findViewById<View>(R.id.gift3) as ImageView
        val imageView4 = findViewById<View>(R.id.gift4) as ImageView
        val imageView5 = findViewById<View>(R.id.gift5) as ImageView
        val imageView6 = findViewById<View>(R.id.gift6) as ImageView
        val imageView7 = findViewById<View>(R.id.gift7) as ImageView
        val imageView8 = findViewById<View>(R.id.gift8) as ImageView
        val imageView9 = findViewById<View>(R.id.gift9) as ImageView
        val imageView10 = findViewById<View>(R.id.gift10) as ImageView
        val imageView11 = findViewById<View>(R.id.gift11) as ImageView
        val imageView12 = findViewById<View>(R.id.gift12) as ImageView
        val imageView13 = findViewById<View>(R.id.gift13) as ImageView
        val imageView14 = findViewById<View>(R.id.gift14) as ImageView
        val imageView15 = findViewById<View>(R.id.gift15) as ImageView
        val imageView16 = findViewById<View>(R.id.gift16) as ImageView
        val imageView17 = findViewById<View>(R.id.gift17) as ImageView
        val imageView18 = findViewById<View>(R.id.gift18) as ImageView
        val imageView19 = findViewById<View>(R.id.gift19) as ImageView
        val imageView20 = findViewById<View>(R.id.gift20) as ImageView
        val imageView21 = findViewById<View>(R.id.gift21) as ImageView
        val imageView22 = findViewById<View>(R.id.gift22) as ImageView
        val imageView23 = findViewById<View>(R.id.gift23) as ImageView
        val imageView24 = findViewById<View>(R.id.gift24) as ImageView

        imageView1.setOnClickListener {
            val intent = Intent (this, SpecificDayActivity::class.java)
            startActivity(intent)
        }

        imageView14.setOnClickListener {
            val intent = Intent (this, SpecificDayActivity::class.java)
            startActivity(intent)
        }
    }


    /*
    fun selectWindow(view: View) {



       /* when (view.id){
            R.id.gift1 -> setGun1
        }*/
    }*/
}

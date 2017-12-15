package com.example.samanthamorrison.xmasify

class SDKHelper : Any() {


    val TEST_SONG_URI = "spotify:track:5rgStA2VSw8K6jragj6yBx"
    val CLIENT_ID = "9edc6aaf7f66422f9cbef9e093fa5aed"
    val REDIRECT_URI = "xmasify://callback"

    val TRACK_1 = "spotify:track:5rgStA2VSw8K6jragj6yBx"
    val TRACK_3 = "spotify:track:7brBzTFzUzxwKNXbyS5sN2"


    /**
     * Request code that will be passed together with authentication result to the onAuthenticationResult
     */
    val REQUEST_CODE = 1337

    /**
     * UI controls which may only be enabled after the player has been initialized,
     * (or effectively, after the user has logged in).
     */
    val REQUIRES_INITIALIZED_STATE = intArrayOf(R.id.play_track_button, R.id.pause_button)

    val REQUIRES_PLAYING_STATE = intArrayOf(R.id.gift1, R.id.gift2, R.id.gift3, R.id.gift4, R.id.gift5, R.id.gift6, R.id.gift7, R.id.gift8, R.id.gift9, R.id.gift10)



}



/*var gridId = getIntent().getStringExtra("EXTRA")

    var id

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

*/
package com.example.samanthamorrison.xmasify

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse
import com.spotify.sdk.android.player.Config
import com.spotify.sdk.android.player.ConnectionStateCallback
import com.spotify.sdk.android.player.Connectivity
import com.spotify.sdk.android.player.Error
import com.spotify.sdk.android.player.Metadata
import com.spotify.sdk.android.player.PlaybackBitrate
import com.spotify.sdk.android.player.PlaybackState
import com.spotify.sdk.android.player.Player
import com.spotify.sdk.android.player.PlayerEvent
import com.spotify.sdk.android.player.Spotify
import com.spotify.sdk.android.player.SpotifyPlayer


class SpecificDayActivity : AppCompatActivity() {


    private var mPlayer: SpotifyPlayer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specific_day)''
    }
}
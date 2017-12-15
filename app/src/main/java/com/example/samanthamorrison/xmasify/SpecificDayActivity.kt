package com.example.samanthamorrison.xmasify

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView

import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse
import com.spotify.sdk.android.player.Config
import com.spotify.sdk.android.player.ConnectionStateCallback
import com.spotify.sdk.android.player.Connectivity
import com.spotify.sdk.android.player.Error
import com.spotify.sdk.android.player.Metadata
import com.spotify.sdk.android.player.PlaybackState
import com.spotify.sdk.android.player.Player
import com.spotify.sdk.android.player.PlayerEvent
import com.spotify.sdk.android.player.Spotify
import com.spotify.sdk.android.player.SpotifyPlayer


class SpecificDayActivity : Activity(), Player.NotificationCallback, ConnectionStateCallback {

    var sdkHelper = SDKHelper()

    private var player: SpotifyPlayer? = null
    private var currentPlaybackState: PlaybackState? = null
    private var networkStateReceiver: BroadcastReceiver? = null

    /**
     * Used to scroll the [.mStatusText] to the bottom after updating text.
     */
    private var mMetadata: Metadata? = null

    private val operationCallback = object : Player.OperationCallback {
        override fun onSuccess() {
            Log.d("TAG", "OK")
        }

        override fun onError(error: Error) {
            Log.d("TAG", "ERROR:" + error)
        }
    }

    val isLoggedIn: Boolean
        get() = player != null && player!!.isLoggedIn



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specific_day)

        updateView()
        Log.d("TAG", "Ready")
    }

    override fun onResume() {
        super.onResume()

        // Set up the broadcast receiver for network events. Note that we also unregister
        // this receiver again in onPause().
        networkStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (player != null) {
                    val connectivity = getNetworkConnectivity(baseContext)
                    Log.d("TAG", "Network state changed: " + connectivity.toString())
                    player!!.setConnectivityStatus(operationCallback, connectivity)
                }
            }
        }

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkStateReceiver, filter)

        if (player != null) {
            player!!.addNotificationCallback(this@SpecificDayActivity)
            player!!.addConnectionStateCallback(this@SpecificDayActivity)
        }
    }

    /**
     * Registering for connectivity changes in Android does not actually deliver them to
     * us in the delivered intent.
     *
     * @param context Android context
     * @return Connectivity state to be passed to the SDK
     */
    private fun getNetworkConnectivity(context: Context): Connectivity {
        val connectivityManager: ConnectivityManager
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return if (activeNetwork != null && activeNetwork.isConnected) {
            Connectivity.fromNetworkType(activeNetwork.type)
        } else {
            Connectivity.OFFLINE
        }
    }


    private fun openLoginWindow() {
        val request = AuthenticationRequest.Builder(sdkHelper.CLIENT_ID, AuthenticationResponse.Type.TOKEN, sdkHelper.REDIRECT_URI)
                .setScopes(arrayOf("user-read-private", "playlist-read", "playlist-read-private", "streaming"))
                .build()

        AuthenticationClient.openLoginActivity(this, sdkHelper.REQUEST_CODE, request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
        super.onActivityResult(requestCode, resultCode, intent)

        // Check if result comes from the correct activity
        if (requestCode == sdkHelper.REQUEST_CODE) {
            val response = AuthenticationClient.getResponse(resultCode, intent)
            when (response.type) {
            // Response was successful and contains auth token
                AuthenticationResponse.Type.TOKEN -> onAuthenticationComplete(response)

            // Auth flow returned an error
                AuthenticationResponse.Type.ERROR -> Log.d("TAG", "Auth error: " + response.error)

            // Most likely auth flow was cancelled
                else -> Log.d("TAG", "Auth result: " + response.type)
            }
        }
    }

    private fun onAuthenticationComplete(authResponse: AuthenticationResponse) {
        // Once we have obtained an authorization token, we can proceed with creating a SDKHelper.
        Log.d("TAG", "Got authentication token")
        if (player == null) {
            val playerConfig = Config(applicationContext, authResponse.accessToken, sdkHelper.CLIENT_ID)
            // Since the SDKHelper is a static singleton owned by the Spotify class, we pass "this" as
            // the second argument in order to refcount it properly. Note that the method
            // Spotify.destroyPlayer() also takes an Object argument, which must be the same as the
            // one passed in here. If you pass different instances to Spotify.getPlayer() and
            // Spotify.destroyPlayer(), that will definitely result in resource leaks.
            player = Spotify.getPlayer(playerConfig, this, object : SpotifyPlayer.InitializationObserver {
                override fun onInitialized(player: SpotifyPlayer) {
                    Log.d("TAG", "-- SDKHelper initialized --")
                    this@SpecificDayActivity.player!!.setConnectivityStatus(operationCallback, getNetworkConnectivity(this@SpecificDayActivity))
                    this@SpecificDayActivity.player!!.addNotificationCallback(this@SpecificDayActivity)
                    this@SpecificDayActivity.player!!.addConnectionStateCallback(this@SpecificDayActivity)
                    // Trigger UI refresh
                    updateView()
                }

                override fun onError(error: Throwable) {
                    Log.d("TAG", "Error in initialization: " + error.message)
                }
            })
        } else {
            player!!.login(authResponse.accessToken)
        }
    }


    private fun updateView() {
        val loggedIn = isLoggedIn

        // Login button should be the inverse of the logged in state
        val loginButton = findViewById<View>(R.id.login_button) as Button
        loginButton.setText(if (loggedIn) R.string.logout_button_label else R.string.login_button_label)

        // Set enabled for all widgets which depend on initialized state
        for (id in sdkHelper.REQUIRES_INITIALIZED_STATE) {
            findViewById<View>(id).isEnabled = loggedIn
        }

        // Same goes for the playing state
        val playing = loggedIn && currentPlaybackState != null && currentPlaybackState!!.isPlaying
        /*for (id in sdkHelper.REQUIRES_PLAYING_STATE) {
            findViewById<ImageView>(id).isEnabled = playing
        }*/

        if (mMetadata != null) {
            findViewById<View>(R.id.pause_button).isEnabled = mMetadata!!.currentTrack != null
        }


    }

    fun onLoginButtonClicked(view: View) {
        if (!isLoggedIn) {
            Log.d("TAG", "Logging in")
            openLoginWindow()
        } else {
            player!!.logout()
        }
    }


  fun onPlayButtonClicked(view: View) {

        val uri: String
        when (view.id) {
            R.id.play_track_button -> uri = sdkHelper.TRACK_1
            else -> throw IllegalArgumentException("View ID does not have an associated URI to play")
        }

        Log.d("TAG", "Starting playback for " + uri)
        player!!.playUri(operationCallback, uri, 0, 0)
    }


    fun onPauseButtonClicked(view: View) {
        if (currentPlaybackState != null && currentPlaybackState!!.isPlaying) {
            player!!.pause(operationCallback)
        } else {
            player!!.resume(operationCallback)
        }
    }


    override fun onLoggedIn() {
        Log.d("TAG", "Login complete")
        updateView()
    }

    override fun onLoggedOut() {
        Log.d("TAG", "Logout complete")
        updateView()
    }

    override fun onLoginFailed(error: Error) {
        Log.d("TAG", "Login error " + error)
    }

    override fun onTemporaryError() {
        Log.d("TAG", "Temporary error occurred")
    }

    override fun onConnectionMessage(message: String) {
        Log.d("TAG", "Incoming connection message: " + message)
    }



    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkStateReceiver)

        // Note that calling Spotify.destroyPlayer() will also remove any callbacks on whatever
        // instance was passed as the refcounted owner. So in the case of this particular example,
        // it's not strictly necessary to call these methods, however it is generally good practice
        // and also will prevent your application from doing extra work in the background when
        // paused.
        if (player != null) {
            player!!.removeNotificationCallback(this@SpecificDayActivity)
            player!!.removeConnectionStateCallback(this@SpecificDayActivity)
        }
    }

    override fun onDestroy() {
        // *** ULTRA-IMPORTANT ***
        // ALWAYS call this in your onDestroy() method, otherwise you will leak native resources!
        // This is an unfortunate necessity due to the different memory management models of
        // Java's garbage collector and C++ RAII.
        // For more information, see the documentation on Spotify.destroyPlayer().
        Spotify.destroyPlayer(this)
        super.onDestroy()
    }

    override fun onPlaybackEvent(event: PlayerEvent) {
        // Remember kids, always use the English locale when changing case for non-UI strings!
        // Otherwise you'll end up with mysterious errors when running in the Turkish locale.
        // See: http://java.sys-con.com/node/46241
        Log.d("TAG", "Event: " + event)
        currentPlaybackState = player!!.playbackState
        mMetadata = player!!.metadata
        Log.i(TAG, "SDKHelper state: " + currentPlaybackState!!)
        Log.i(TAG, "Metadata: " + mMetadata!!)
        updateView()
    }

    override fun onPlaybackError(error: Error) {
        Log.d("TAG", "Error: " + error)
    }

}

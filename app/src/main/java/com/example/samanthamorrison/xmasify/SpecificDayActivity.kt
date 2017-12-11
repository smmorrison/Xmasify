package com.example.samanthamorrison.xmasify

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

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


class SpecificDayActivity : Activity(), Player.NotificationCallback, ConnectionStateCallback {

    /**
     * The player used by this activity. There is only ever one instance of the player,
     * which is owned by the [com.spotify.sdk.android.player.Spotify] class and refcounted.
     * This means that you may use the Player from as many Fragments as you want, and be
     * assured that state remains consistent between them.
     *
     *
     * However, each fragment, activity, or helper class **must** call
     * [com.spotify.sdk.android.player.Spotify.destroyPlayer] when they are no longer
     * need that player. Failing to do so will result in leaked resources.
     */
    private var mPlayer: SpotifyPlayer? = null

    private var mCurrentPlaybackState: PlaybackState? = null

    /**
     * Used to get notifications from the system about the current network state in order
     * to pass them along to
     * [SpotifyPlayer.setConnectivityStatus]
     * Note that this implies <pre>android.permission.ACCESS_NETWORK_STATE</pre> must be
     * declared in the manifest. Not setting the correct network state in the SDK may
     * result in strange behavior.
     */
    private var mNetworkStateReceiver: BroadcastReceiver? = null

    /**
     * Used to log messages to a [android.widget.TextView] in this activity.
     */
    private var mStatusText: TextView? = null

    private var mMetadataText: TextView? = null

    private var mSeekEditText: EditText? = null

    /**
     * Used to scroll the [.mStatusText] to the bottom after updating text.
     */
    private var mStatusTextScrollView: ScrollView? = null
    private var mMetadata: Metadata? = null

    private val mOperationCallback = object : Player.OperationCallback {
        override fun onSuccess() {
            logStatus("OK!")
        }

        override fun onError(error: Error) {
            logStatus("ERROR:" + error)
        }
    }

    private val isLoggedIn: Boolean
        get() = mPlayer != null && mPlayer!!.isLoggedIn




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

        // Get a reference to any UI widgets that we'll need to use later
        mStatusText = findViewById<View>(R.id.status_text) as TextView
        mMetadataText = findViewById<View>(R.id.metadata) as TextView
        mSeekEditText = findViewById<View>(R.id.seek_edittext) as EditText
        mStatusTextScrollView = findViewById<View>(R.id.status_text_container) as ScrollView

        updateView()
        logStatus("Ready")
    }

    override fun onResume() {
        super.onResume()

        // Set up the broadcast receiver for network events. Note that we also unregister
        // this receiver again in onPause().
        mNetworkStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (mPlayer != null) {
                    val connectivity = getNetworkConnectivity(baseContext)
                    logStatus("Network state changed: " + connectivity.toString())
                    mPlayer!!.setConnectivityStatus(mOperationCallback, connectivity)
                }
            }
        }

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(mNetworkStateReceiver, filter)

        if (mPlayer != null) {
            mPlayer!!.addNotificationCallback(this@SpecificDayActivity)
            mPlayer!!.addConnectionStateCallback(this@SpecificDayActivity)
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
        val request = AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                .setScopes(arrayOf("user-read-private", "playlist-read", "playlist-read-private", "streaming"))
                .build()

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
        super.onActivityResult(requestCode, resultCode, intent)

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            val response = AuthenticationClient.getResponse(resultCode, intent)
            when (response.type) {
            // Response was successful and contains auth token
                AuthenticationResponse.Type.TOKEN -> onAuthenticationComplete(response)

            // Auth flow returned an error
                AuthenticationResponse.Type.ERROR -> logStatus("Auth error: " + response.error)

            // Most likely auth flow was cancelled
                else -> logStatus("Auth result: " + response.type)
            }
        }
    }

    private fun onAuthenticationComplete(authResponse: AuthenticationResponse) {
        // Once we have obtained an authorization token, we can proceed with creating a Player.
        logStatus("Got authentication token")
        if (mPlayer == null) {
            val playerConfig = Config(applicationContext, authResponse.accessToken, CLIENT_ID)
            // Since the Player is a static singleton owned by the Spotify class, we pass "this" as
            // the second argument in order to refcount it properly. Note that the method
            // Spotify.destroyPlayer() also takes an Object argument, which must be the same as the
            // one passed in here. If you pass different instances to Spotify.getPlayer() and
            // Spotify.destroyPlayer(), that will definitely result in resource leaks.
            mPlayer = Spotify.getPlayer(playerConfig, this, object : SpotifyPlayer.InitializationObserver {
                override fun onInitialized(player: SpotifyPlayer) {
                    logStatus("-- Player initialized --")
                    mPlayer!!.setConnectivityStatus(mOperationCallback, getNetworkConnectivity(this@SpecificDayActivity))
                    mPlayer!!.addNotificationCallback(this@SpecificDayActivity)
                    mPlayer!!.addConnectionStateCallback(this@SpecificDayActivity)
                    // Trigger UI refresh
                    updateView()
                }

                override fun onError(error: Throwable) {
                    logStatus("Error in initialization: " + error.message)
                }
            })
        } else {
            mPlayer!!.login(authResponse.accessToken)
        }
    }


    private fun updateView() {
        val loggedIn = isLoggedIn

        // Login button should be the inverse of the logged in state
        val loginButton = findViewById<View>(R.id.login_button) as Button
        loginButton.setText(if (loggedIn) R.string.logout_button_label else R.string.login_button_label)

        // Set enabled for all widgets which depend on initialized state
        for (id in REQUIRES_INITIALIZED_STATE) {
            findViewById<View>(id).isEnabled = loggedIn
        }

        // Same goes for the playing state
        val playing = loggedIn && mCurrentPlaybackState != null && mCurrentPlaybackState!!.isPlaying
        for (id in REQUIRES_PLAYING_STATE) {
            findViewById<View>(id).isEnabled = playing
        }

        if (mMetadata != null) {
            findViewById<View>(R.id.skip_next_button).isEnabled = mMetadata!!.nextTrack != null
            findViewById<View>(R.id.skip_prev_button).isEnabled = mMetadata!!.prevTrack != null
            findViewById<View>(R.id.pause_button).isEnabled = mMetadata!!.currentTrack != null
        }


    }

    fun onLoginButtonClicked(view: View) {
        if (!isLoggedIn) {
            logStatus("Logging in")
            openLoginWindow()
        } else {
            mPlayer!!.logout()
        }
    }




    /**
    Gör en when() (switchsats) här som spelar upp olika låtar beroende på vilken lucka som öppnats
     */
    fun onPlayButtonClicked(view: View) {

        val uri: String
        when (view.id) {
            R.id.play_track_button -> uri = TEST_SONG_URI
            R.id.play_mono_track_button -> uri = TEST_SONG_MONO_URI
            R.id.play_48khz_track_button -> uri = TEST_SONG_48kHz_URI
            R.id.play_playlist_button -> uri = TEST_PLAYLIST_URI
            R.id.play_album_button -> uri = TEST_ALBUM_URI
            else -> throw IllegalArgumentException("View ID does not have an associated URI to play")
        }

        logStatus("Starting playback for " + uri)
        mPlayer!!.playUri(mOperationCallback, uri, 0, 0)
    }

    fun onPauseButtonClicked(view: View) {
        if (mCurrentPlaybackState != null && mCurrentPlaybackState!!.isPlaying) {
            mPlayer!!.pause(mOperationCallback)
        } else {
            mPlayer!!.resume(mOperationCallback)
        }
    }



    override fun onLoggedIn() {
        logStatus("Login complete")
        updateView()
    }

    override fun onLoggedOut() {
        logStatus("Logout complete")
        updateView()
    }

    override fun onLoginFailed(error: Error) {
        logStatus("Login error " + error)
    }

    override fun onTemporaryError() {
        logStatus("Temporary error occurred")
    }

    override fun onConnectionMessage(message: String) {
        logStatus("Incoming connection message: " + message)
    }





    /**
     * Print a status message from a callback (or some other place) to the TextView in this
     * activity
     *
     * @param status Status message
     */
    private fun logStatus(status: String) {
        Log.i(TAG, status)
        if (!TextUtils.isEmpty(mStatusText!!.text)) {
            mStatusText!!.append("\n")
        }
        mStatusText!!.append(">>>" + status)
        mStatusTextScrollView!!.post {
            // Scroll to the bottom
            mStatusTextScrollView!!.fullScroll(View.FOCUS_DOWN)
        }
    }




    override fun onPause() {
        super.onPause()
        unregisterReceiver(mNetworkStateReceiver)

        // Note that calling Spotify.destroyPlayer() will also remove any callbacks on whatever
        // instance was passed as the refcounted owner. So in the case of this particular example,
        // it's not strictly necessary to call these methods, however it is generally good practice
        // and also will prevent your application from doing extra work in the background when
        // paused.
        if (mPlayer != null) {
            mPlayer!!.removeNotificationCallback(this@SpecificDayActivity)
            mPlayer!!.removeConnectionStateCallback(this@SpecificDayActivity)
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
        logStatus("Event: " + event)
        mCurrentPlaybackState = mPlayer!!.playbackState
        mMetadata = mPlayer!!.metadata
        Log.i(TAG, "Player state: " + mCurrentPlaybackState!!)
        Log.i(TAG, "Metadata: " + mMetadata!!)
        updateView()
    }

    override fun onPlaybackError(error: Error) {
        logStatus("Err: " + error)
    }

    companion object {




        private val CLIENT_ID = "089d841ccc194c10a77afad9e1c11d54"
        private val REDIRECT_URI = "testschema://callback"

        private val TEST_SONG_URI = "spotify:track:0JoLc8rgQBJhDMolSCuRuw" //<-Jingle Bell Rock
        private val TEST_SONG_MONO_URI = "spotify:track:1FqY3uJypma5wkYw66QOUi"
        private val TEST_SONG_48kHz_URI = "spotify:track:3wxTNS3aqb9RbBLZgJdZgH"
        private val TEST_PLAYLIST_URI = "spotify:user:spotify:playlist:2yLXxKhhziG2xzy7eyD4TD"
        private val TEST_ALBUM_URI = "spotify:album:2lYmxilk8cXJlxxXmns1IU"
        private val TEST_QUEUE_SONG_URI = "spotify:track:5EEOjaJyWvfMglmEwf9bG3"

        /**
         * Request code that will be passed together with authentication result to the onAuthenticationResult
         */
        private val REQUEST_CODE = 1337

        /**
         * UI controls which may only be enabled after the player has been initialized,
         * (or effectively, after the user has logged in).
         */
        private val REQUIRES_INITIALIZED_STATE = intArrayOf(R.id.play_track_button, R.id.play_mono_track_button, R.id.play_48khz_track_button, R.id.play_album_button, R.id.play_playlist_button, R.id.pause_button, R.id.seek_button, R.id.low_bitrate_button, R.id.normal_bitrate_button, R.id.high_bitrate_button, R.id.seek_edittext)

        /**
         * UI controls which should only be enabled if the player is actively playing.
         */
        private val REQUIRES_PLAYING_STATE = intArrayOf(R.id.skip_next_button, R.id.skip_prev_button, R.id.queue_song_button, R.id.toggle_shuffle_button, R.id.toggle_repeat_button)
        val TAG = "SpotifySdkDemo"
    }
}

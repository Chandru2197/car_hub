package com.personal.carhub

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/** Full-screen video player for one of your saved streams. */
class PlayerActivity : Activity() {

    private var player: ExoPlayer? = null
    private lateinit var view: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        view = PlayerView(this)
        setContentView(view)
    }

    override fun onStart() {
        super.onStart()
        val id = intent.getStringExtra(EXTRA_STREAM_ID)
        val stream = id?.let { StreamStore(this).find(it) }
        if (stream == null) {
            finish()
            return
        }
        val exo = ExoPlayer.Builder(this).build()
        exo.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(
                    this@PlayerActivity,
                    "Can't play this stream: ${error.errorCodeName}",
                    Toast.LENGTH_LONG,
                ).show()
            }
        })
        exo.setMediaItem(MediaItem.fromUri(stream.url))
        exo.prepare()
        exo.playWhenReady = true
        view.player = exo
        player = exo
    }

    override fun onStop() {
        super.onStop()
        view.player = null
        player?.release()
        player = null
    }

    companion object {
        const val EXTRA_STREAM_ID = "stream_id"
    }
}

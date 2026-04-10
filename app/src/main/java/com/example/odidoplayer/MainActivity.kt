package com.example.odidoplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.LocalMediaDrmCallback
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val playerView = PlayerView(this)
        setContentView(playerView)

        val streamUrl = "PASTE_YOUR_FULL_MPD_URL_HERE"
        val userAgent = "Mozila"
        val kid = "ef34ae91b4f2415e8439b2ad105e7488"
        val key = "243248d8de1ff8c7c587ee2057317523"

        // DRM Setup
        val drmJson = ClearKeyUtils.createClearKeyJson(kid, key)
        val drmCallback = LocalMediaDrmCallback(drmJson.toByteArray())
        val drmSessionManager = DefaultDrmSessionManager.Builder()
            .setUuidAndExoMediaDrmProvider(C.CLEARKEY_UUID) { it }
            .build(drmCallback)

        // Data Source
        val dataSourceFactory = DefaultHttpDataSource.Factory().setUserAgent(userAgent)

        // Media Source
        val mediaSource = DashMediaSource.Factory(dataSourceFactory)
            .setDrmSessionManagerProvider { drmSessionManager }
            .createMediaSource(MediaItem.Builder()
                .setUri(streamUrl)
                .setDrmConfiguration(MediaItem.DrmConfiguration.Builder(C.CLEARKEY_UUID).build())
                .build())

        player = ExoPlayer.Builder(this).build().apply {
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
        }
        playerView.player = player
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}

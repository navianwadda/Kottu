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
import androidx.media3.exoplayer.drm.FrameworkMediaDrm
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val playerView = PlayerView(this)
        setContentView(playerView)

        // The raw link with pipes
        val rawUrl = "https://mag03.tvx.prd.tv.odido.nl/wh7f454c46tw75168188_-627298088/PLTV/86/224/3221241590/3221241590.mpd?zoneoffset=0&devkbps=1-7000&servicetype=1&icpid=86&accounttype=1&limitflux=-1&limitdur=-1&tenantId=3103&accountinfo=%7E%7EV2.0%7EqbcsJh_jU5C9BcZc959e_wae44b4867b3417aa76b5db2da20fe46c%7EKZzTWjB8qD1zdgbJjRPVLJX-tV0qiN9RBHC_iseGrsmTSRjj06oGDtGlpSCRGOwF3626cf085c08d024c7e4aafc18c32440%7EExtInfo5Ro3VppWiUusj2ippqUPkQ%3D%3D4a2d2c8ce133f43026d0e31b822b8474%3A20240601012829%3AUTC%2C10001003329222%2C87.212.140.171%2C20240601012829%2C3103_SP1S%2C10001003329222%2C-1%2C0%2C1%2C%2C%2C2%2C3103_Sport1%2C%2C%2C2%2C10000044444303%2C0%2C10000025050255%2CNDEzODg2NTY3MzEwMzI2NzMwNjMwNTY%3D%2C%2C%2C5%2C1%2CEND&GuardEncType=2&RTS=1717205309&from=11&hms_devid=1008&online=1717205309&mag_hms=1008,311,305&_=1717205322621|user-agent=Mozila|drmScheme=clearkey|drmLicense=ef34ae91b4f2415e8439b2ad105e7488:243248d8de1ff8c7c587ee2057317523"

        // 1. Clean the URL (remove everything after the first '|')
        val cleanUrl = rawUrl.substringBefore("|")

        // 2. Extract specific metadata from the string
        val userAgentFromUrl = if (rawUrl.contains("user-agent=")) 
            rawUrl.substringAfter("user-agent=").substringBefore("|") else "Mozilla/5.0"
        
        val drmLicense = if (rawUrl.contains("drmLicense=")) 
            rawUrl.substringAfter("drmLicense=").substringBefore("|") else ""

        // 3. Setup DRM using the extracted keys
        val (kid, key) = if (drmLicense.contains(":")) {
            drmLicense.split(":").let { it[0] to it[1] }
        } else {
            "ef34ae91b4f2415e8439b2ad105e7488" to "243248d8de1ff8c7c587ee2057317523"
        }

        val drmJson = ClearKeyUtils.createClearKeyJson(kid, key)
        val drmCallback = LocalMediaDrmCallback(drmJson.toByteArray())
        val drmSessionManager = DefaultDrmSessionManager.Builder()
            .setUuidAndExoMediaDrmProvider(C.CLEARKEY_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
            .build(drmCallback)

        // 4. Setup DataSource with the extracted User-Agent
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgentFromUrl)
            .setAllowCrossProtocolRedirects(true)

        // 5. Build MediaSource
        val mediaSource = DashMediaSource.Factory(dataSourceFactory)
            .setDrmSessionManagerProvider { drmSessionManager }
            .createMediaSource(MediaItem.Builder()
                .setUri(cleanUrl)
                .setDrmConfiguration(MediaItem.DrmConfiguration.Builder(C.CLEARKEY_UUID).build())
                .build())

        player = ExoPlayer.Builder(this).build().apply {
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
        }
        playerView.player = player
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
}

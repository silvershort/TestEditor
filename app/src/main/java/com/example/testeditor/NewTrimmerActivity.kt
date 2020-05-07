package com.example.testeditor

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ClippingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import idv.luchafang.videotrimmer.VideoTrimmerView
import kotlinx.android.synthetic.main.activity_new_trimmer.*
import java.io.File

class NewTrimmerActivity : AppCompatActivity(), VideoTrimmerView.OnSelectedRangeChangedListener {

    private val trackSelector: TrackSelector = DefaultTrackSelector()
    private val dataFactory: DefaultDataSourceFactory by lazy {
        DefaultDataSourceFactory(
            this,
            Util.getUserAgent(this, getString(R.string.app_name))
        )
    }
    private val player: SimpleExoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(this, trackSelector).also {
            it.repeatMode = Player.REPEAT_MODE_ALL
            trim_playerView.player = it
        }
    }

    private val path: String by lazy { intent.getStringExtra("path") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_trimmer)

        setSupportActionBar(trim_toolbar)
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.setDisplayShowHomeEnabled(true)

        trim_videoTrimmerView
            .setVideo(File(path))
            .setMaxDuration(30_000)
            .setMinDuration(3_000)
            .setFrameCountInWindow(1)
            .setExtraDragSpace(dpToPx(2f))
            .setOnSelectedRangeChangedListener(this)
            .show()

        playVideo(path, 1000L, 10000L)

        Log.d("로그", "path $path")
    }

    override fun onSelectRangeStart() {
        player.playWhenReady = false
    }

    override fun onSelectRange(startMillis: Long, endMillis: Long) {
        showDuration(startMillis, endMillis)
    }

    override fun onSelectRangeEnd(startMillis: Long, endMillis: Long) {
        showDuration(startMillis, endMillis)
        playVideo(path, startMillis, endMillis)
    }

    private fun playVideo(path: String, startMillis: Long, endMillis: Long) {
        if (path.isBlank()) return

        val source = ExtractorMediaSource.Factory(dataFactory)
            .createMediaSource(Uri.parse(path))
            .let {
                ClippingMediaSource(
                    it,
                    startMillis * 1000L,
                    endMillis * 1000L
                )
            }
        player.playWhenReady = true
        player.prepare(source)
    }

    private fun showDuration(startMillis: Long, endMillis: Long) {
        val duration = (endMillis - startMillis) / 1000L
        trim_durationView.text = "$duration seconds selected"
    }

    private fun dpToPx(dp: Float): Float {
        val density = resources.displayMetrics.density
        return dp * density
    }
}

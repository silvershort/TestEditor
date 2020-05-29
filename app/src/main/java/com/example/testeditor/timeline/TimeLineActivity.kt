package com.example.testeditor.timeline

import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.example.testeditor.R
import com.example.testeditor.util.BitmapObject
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import idv.luchafang.videotrimmer.VideoTrimmerView
import kotlinx.android.synthetic.main.activity_time_line.*
import kotlinx.android.synthetic.main.activity_trimmer.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.roundToInt

class TimeLineActivity : AppCompatActivity(), VideoTrimmerView.OnSelectedRangeChangedListener {

    private val TAG = "!!!TimeLineActivity!!!"

    private val trackSelector: TrackSelector = DefaultTrackSelector()
    private val dataFactory: DefaultDataSourceFactory by lazy {
        DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)))
    }
    private val player: SimpleExoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(this, trackSelector).also {
            it.repeatMode = Player.REPEAT_MODE_ALL
            timeline_playerView.player = it
        }
    }
    private val path: String by lazy { intent.getStringExtra("path") }
    private var currentPosition: Long = 0
    private var startTimeLine: Long = 0
    private var endTimeLine: Long = 0

    private var timelineJob: Job? = null

    override fun onResume() {
        super.onResume()
        player?.playWhenReady = true
    }

    override fun onPause() {
        player.playWhenReady = false
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_line)

        setSupportActionBar(timeline_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        timeline_sticker.setImageBitmap(BitmapObject.bitmap)

        GlobalScope.launch {
            while (true) {
                delay(100L)
                // ExoPlayer의 현재 재생 위치를 저장
                currentPosition = player.currentPosition
                Log.d(TAG, "타임라인 : $currentPosition")

                // 현재 재생 위치가 지정한 범위 안에 있는지 확인 후 스티커 visible 상태 변경
                if (currentPosition in startTimeLine..endTimeLine) {
                    runOnUiThread { timeline_sticker.visibility = View.VISIBLE }
                } else {
                    runOnUiThread { timeline_sticker.visibility = View.GONE }
                }
            }
        }

        Handler().postDelayed({
            timeline_trimmerview
                .setVideo(File(path))
                .setMaxDuration(30_000)
                .setMinDuration(1_000)
                .setFrameCountInWindow(10)
                .setExtraDragSpace(dpToPx(2f))
                .setOnSelectedRangeChangedListener(this)
                .show()
        }, 100)

        playVideo(path)
    }

    override fun onSelectRange(startMillis: Long, endMillis: Long) {
        showDuration(startMillis, endMillis)
    }

    override fun onSelectRangeStart() {
        player.playWhenReady = false
    }

    override fun onSelectRangeEnd(startMillis: Long, endMillis: Long) {
        showDuration(startMillis, endMillis)
        startTimeLine = startMillis
        endTimeLine = endMillis
        player.seekTo(startMillis)
        player.playWhenReady = true
    }

    private fun playVideo(path: String) {
        path?: return

        val source = ExtractorMediaSource.Factory(dataFactory)
            .createMediaSource(Uri.parse(path))
        player.playWhenReady = true
        player.prepare(source)
    }

    private fun showDuration(startMillis: Long, endMillis: Long) {
//        val duration: Long = (endMillis - startMillis) / 1000L
        val df = DecimalFormat("#.#")
        timeline_tv_duration.text = "${df.format((endMillis - startMillis) / 1000.0)} ${getString(R.string.trim_selected)}"
        timeline_tv_start.text = "${df.format(startMillis / 1000.0)} ${getString(R.string.common_seconds)}"
        timeline_tv_end.text = "${df.format(endMillis / 1000.0)} ${getString(R.string.common_seconds)}"
    }

    private fun dpToPx(dp: Float): Float {
        val density = resources.displayMetrics.density
        return dp * density
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                player.release()
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        player.release()
        super.onBackPressed()
    }

    inner class ProgressThread : Thread() {
        override fun run() {
            while(true) {
                currentPosition = player.currentPosition
                sleep(100);
            }
        }
    }
}

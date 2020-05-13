package com.example.testeditor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.daasuu.mp4compose.composer.Mp4Composer
import com.example.testeditor.dialog.CustomProgressDialog
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
import kotlinx.android.synthetic.main.activity_trimmer.*
import org.jetbrains.anko.toast
import java.io.File
import java.lang.Exception

class TrimmerActivity : AppCompatActivity(), VideoTrimmerView.OnSelectedRangeChangedListener{

    private val TAG = "!!!TrimmerActivity!!!"

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
    private var sMillis: Long = 0
    private var eMillis: Long = 0

    private val proDialog: CustomProgressDialog = CustomProgressDialog()
    private lateinit var mp4Composer: Mp4Composer

    override fun onResume() {
        super.onResume()
        if (player != null) {
            player.playWhenReady = true
        }
    }

    override fun onPause() {
        super.onPause()
        if(player != null) {
            player.playWhenReady = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimmer)

        setSupportActionBar(trim_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Handler().postDelayed({
            trim_videoTrimmerView
                .setVideo(File(path))
                .setMaxDuration(30_000)
                .setMinDuration(1_000)
                .setFrameCountInWindow(10)
                .setExtraDragSpace(dpToPx(2f))
                .setOnSelectedRangeChangedListener(this)
                .show()
        }, 100)

        trim_tv_complete.setOnClickListener(View.OnClickListener {
            proDialog.show(supportFragmentManager, "progressDialog")
            proDialog.setDialogResultInterface(object: CustomProgressDialog.OnDialogResult{
                override fun finish() {
                    mp4Composer.cancel()
                }
            })

            mp4Composer = Mp4Composer(path, "${cacheDir.canonicalPath}/temp.mp4")
                .trim(sMillis, eMillis)
                .listener(object: Mp4Composer.Listener{
                    override fun onFailed(exception: Exception?) {
                        Log.d(TAG, "변환 실패")
                        proDialog.dismiss()
                    }

                    override fun onProgress(progress: Double) {
                        proDialog.setText(progress)
                    }

                    override fun onCanceled() {
                        Log.d(TAG, "변환 취소")
                    }

                    override fun onCompleted() {
                        proDialog.dismiss()
                        Log.d(TAG, "변환 완료")
                        releaseVideo()
                        val intent = Intent()
                        intent.putExtra("path", "${cacheDir.canonicalPath}/temp.mp4")
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                })
            mp4Composer.start()
        })
    }

    override fun onSelectRangeStart() {
        player.playWhenReady = false
    }

    override fun onSelectRange(startMillis: Long, endMillis: Long) {
        showDuration(startMillis, endMillis)
    }

    override fun onSelectRangeEnd(startMillis: Long, endMillis: Long) {
        sMillis = startMillis
        eMillis = endMillis
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

    private fun releaseVideo() {
        player.release()
    }

    private fun showDuration(startMillis: Long, endMillis: Long) {
        val duration: Long = (endMillis - startMillis) / 1000L
        trim_durationView.text = "$duration ${getString(R.string.trim_selected)}"
    }

    private fun dpToPx(dp: Float): Float {
        val density = resources.displayMetrics.density
        return dp * density
    }

/*    private fun timeConverter(rawLong: Long): String{
        val sec = rawLong % 1000
        val mSec = rawLong / 1000
        return String.format("%02d:%02d:%02d.%d", sec / 60 / 60, sec / 60 % 60, sec % 60, mSec)
    }*/

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                releaseVideo()
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        Log.d(TAG, "백버튼 눌림")
        releaseVideo()
        super.onBackPressed()
    }
}

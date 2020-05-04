package com.example.testeditor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var simpleExoPlayer: SimpleExoPlayer
    lateinit var dataFactory: DefaultDataSourceFactory
    lateinit var renderersFactory: DefaultRenderersFactory
    lateinit var trackSelector: DefaultTrackSelector
    lateinit var loadControl: DefaultLoadControl
    lateinit var mediaSource: MediaSource

    var path: String? = null
    var uri: Uri? = null

    private val RECORD_REQUEST_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupPermissions()

        main_ib_add.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_PICK);
            intent.data = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            intent.type = "video/*"
            startActivityForResult(intent, 1111)
        })

        main_ib_trim.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, TrimmerActivity::class.java)
            intent.putExtra("path", path)
            startActivityForResult(intent, 2222)
        })

        main_ib_crop.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, CropActivity::class.java)
            intent.putExtra("path", path)
            startActivityForResult(intent, 2222)
        })
    }

    private fun setupPermissions() {
        //스토리지 읽기 퍼미션을 permission 변수에 담는다
        val permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("로그", "Permission to record denied")
            makeRequest()
        }
    }
    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            RECORD_REQUEST_CODE)
    }

    private fun getRealPathFromURI(contentUri: Uri): String? {
        var columnIndex = 0
        val proj = arrayOf(
            MediaStore.Images.Media.DATA
        )
        val cursor =
            contentResolver.query(contentUri, proj, null, null, null)
        if (cursor!!.moveToFirst()) {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        }
        return cursor.getString(columnIndex)
    }

    private fun setPlayer(path: String) {
        dataFactory = DefaultDataSourceFactory(
            this,
            Util.getUserAgent(this, getString(R.string.app_name))
        )
        renderersFactory = DefaultRenderersFactory(this)
        trackSelector = DefaultTrackSelector()
        loadControl = DefaultLoadControl()
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(
            renderersFactory, trackSelector, loadControl
        )
        mediaSource =
            ExtractorMediaSource.Factory(dataFactory).createMediaSource(Uri.parse(path))
        simpleExoPlayer.prepare(mediaSource)
        main_playerview.player = simpleExoPlayer
        main_playerview.player.playWhenReady = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1111 -> {
                // 1번방법
                if (data != null) {
                    uri = data.data
                    path = getRealPathFromURI(uri!!)
                    setPlayer(path!!)
                } else {
                    return
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when(requestCode){
            RECORD_REQUEST_CODE ->{
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                }else{
                }
                return
            }
        }
    }

}

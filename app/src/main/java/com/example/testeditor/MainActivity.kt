package com.example.testeditor

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.daasuu.gpuv.composer.GPUMp4Composer
import com.daasuu.gpuv.egl.filter.GlFilter
import com.daasuu.gpuv.player.GPUPlayerView
import com.example.testeditor.dialog.CustomProgressDialog
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.main_tv_complete
import org.jetbrains.anko.toast
import java.io.File
import androidx.core.view.isVisible as isVisible

class MainActivity : AppCompatActivity() {

    private val trackSelector: TrackSelector = DefaultTrackSelector()
    private val dataFactory: DefaultDataSourceFactory by lazy {
        DefaultDataSourceFactory(
            this,
            Util.getUserAgent(this, getString(R.string.app_name))
        )
    }

    private lateinit var simpleExoPlayer: SimpleExoPlayer
    lateinit var mediaSource: MediaSource
    lateinit var gpuPlayerView: GPUPlayerView

    lateinit var glFilter: GlFilter
    lateinit var filterName: String
    lateinit var gpuMp4Composer: GPUMp4Composer
    private val proDialog: CustomProgressDialog = CustomProgressDialog()

    var path: String? = null
    var fileName: String? = null
    var uri: Uri? = null
    var savePath: String? = null

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    val bottomUp: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.bottom_up) }
    val bottomDown: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.bottom_down) }

    private val GALLERY_REQUEST_CODE = 1000
    private val TRIM_REQUEST_CODE = 1001
    private val CROP_REQUEST_CODE = 1002

    override fun onResume() {
        super.onResume()
        try {
            simpleExoPlayer.playWhenReady = true
        } catch (e: Exception) {
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            simpleExoPlayer.playWhenReady = false
        } catch (e: Exception) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupPermissions()

        setSupportActionBar(main_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val filterAdapter = FilterAdapter(FilterType.createFilterList())
        main_recycler_filter.adapter = filterAdapter
        main_recycler_filter.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        filterAdapter.setOnFilterListener(object: OnFilterClickListener{
            override fun onFilterClick(holder: FilterAdapter.FilterHolder, position: Int) {
                filterName = holder.filter_tv.text.toString();
                Log.d("로그", filterName)
                glFilter = FilterType.createGlFilter(FilterType.valueOf(filterName), applicationContext)
                gpuPlayerView.setGlFilter(glFilter)
            }
        })

        main_ib_add.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_PICK);
            intent.data = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            intent.type = "video/*"
            startActivityForResult(intent, 1111)
        })

        /*main_ib_trim.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, TrimmerActivity::class.java)
            intent.putExtra("path", path)
            startActivityForResult(intent, 2222)
        })*/

        main_ib_new_trim.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, NewTrimmerActivity::class.java)
            intent.putExtra("path", path)
            startActivityForResult(intent, 2222)
        })

        main_ib_crop.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, CropActivity::class.java)
            intent.putExtra("path", path)
            startActivityForResult(intent, 3333)
        })

        main_ib_filter.setOnClickListener(View.OnClickListener {
            if (main_recycler_filter.isVisible) {
                main_recycler_filter.startAnimation(bottomDown)
                bottomDown.setAnimationListener(object: Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        main_recycler_filter.visibility = View.GONE
                    }

                    override fun onAnimationStart(animation: Animation?) {
                    }
                })
            } else {
                main_recycler_filter.startAnimation(bottomUp)
                main_recycler_filter.visibility = View.VISIBLE
            }
        })

        main_tv_complete.setOnClickListener(View.OnClickListener {
            Log.d("로그", "완료 버튼이 눌림")
            Log.d("로그", "원본 경로 : $path")
            Log.d("로그", "저장 경로 : $savePath")
            proDialog.show(supportFragmentManager, "progressDialog")
            proDialog.setDialogResultInterface(object: CustomProgressDialog.OnDialogResult{
                override fun finish() {
                    gpuMp4Composer.cancel()
                }
            })

            val directory = File(Environment.getExternalStorageDirectory().absolutePath + "/" + Environment.DIRECTORY_DCIM + "/TEST")
            if (!directory.exists()) {
                Log.d("로그", "path: $directory")
                Log.d("로그", "path: $path")
                Log.d("로그", "folder create")
                directory.mkdir()
            }

            gpuMp4Composer = GPUMp4Composer(path, savePath)
                .filter(FilterType.createGlFilter(FilterType.valueOf(filterName), applicationContext))
                .listener(object: GPUMp4Composer.Listener {
                    override fun onFailed(exception: Exception?) {
                        Log.d("로그", "변환 실패 : ${exception.toString()}")
                        proDialog.dismiss()
                    }

                    override fun onProgress(progress: Double) {
                        Log.d("로그", "변환 중 : $progress")
                        proDialog.setText(progress)
                    }

                    override fun onCanceled() {
                        Log.d("로그", "변환 취소")
                        toast("취소되었습니다")
                    }

                    override fun onCompleted() {
                        proDialog.dismiss()
                        Log.d("로그", "변환 성공")
                        Log.d("로그", "uri : " + Uri.parse(savePath))

//                        contentResolver.update(Uri.parse(savePath), ContentValues(), null, null)

                        MediaScannerConnection.scanFile(applicationContext,
                            arrayOf(savePath), null, object: MediaScannerConnection.OnScanCompletedListener{
                                override fun onScanCompleted(path: String?, uri: Uri?) {

                                }
                            })
                    }
                })
                gpuMp4Composer.start()
        })
    }

    private fun setupPermissions() {
        var rejectedPermissionList = ArrayList<String>()

        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                rejectedPermissionList.add(permission)
            }
        }

        if (rejectedPermissionList.isNotEmpty()) {
            val array = arrayOfNulls<String>(rejectedPermissionList.size)
            ActivityCompat.requestPermissions(this, rejectedPermissionList.toArray(array), GALLERY_REQUEST_CODE)
        }
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

    /*private fun videoPlay() {
        simpleExoPlayer.playWhenReady = true
    }

    private fun videoPause() {
        simpleExoPlayer.playWhenReady = false
    }*/

    private fun setPlayer(path: String) {
        main_playerview.removeAllViews()

        mediaSource =
            ExtractorMediaSource.Factory(dataFactory).createMediaSource(Uri.parse(path))
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        simpleExoPlayer.repeatMode = Player.REPEAT_MODE_ONE

        simpleExoPlayer.playWhenReady = true
        simpleExoPlayer.prepare(mediaSource)

        gpuPlayerView = GPUPlayerView(this);
        gpuPlayerView.setSimpleExoPlayer(simpleExoPlayer)
        gpuPlayerView.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        main_playerview.addView(gpuPlayerView)
        gpuPlayerView.onResume()
    }

    private fun setFilePath(rawPath: String?) {
        path = rawPath
        if (rawPath != null) {
            fileName = File(path).name
            Log.d("로그", "파일이름 : $fileName")
            savePath = Environment.getExternalStorageDirectory().absolutePath + "/" + Environment.DIRECTORY_DCIM + "/TEST/modify_" + fileName
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1111 -> {
                // 1번방법
                if (data != null) {
                    uri = data.data
                    setFilePath(getRealPathFromURI(uri!!))
                    setPlayer(path!!)
                } else {
                    return
                }
            }
            2222 -> {
                if (data != null) {
                    path = data.getStringExtra("path")
                    Log.d("로그", "result path : $path")
                    setFilePath(path)
                    setPlayer(path!!)
                } else {
                    return
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when(requestCode) {
            GALLERY_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                }else{
                }
                return
            }
            TRIM_REQUEST_CODE -> {

            }
            CROP_REQUEST_CODE -> {

            }
        }
    }
}

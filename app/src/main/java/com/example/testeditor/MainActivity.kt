package com.example.testeditor

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.FFprobe
import com.arthenica.mobileffmpeg.StatisticsCallback
import com.daasuu.gpuv.composer.FillMode
import com.daasuu.gpuv.composer.GPUMp4Composer
import com.daasuu.gpuv.egl.filter.GlFilter
import com.daasuu.gpuv.player.GPUPlayerView
import com.example.testeditor.dialog.CircleProgressDialog
import com.example.testeditor.dialog.CustomProgressDialog
import com.example.testeditor.dialog.TextEditorDialogFragment
import com.example.testeditor.dialog.TextStickerEditDialog
import com.example.testeditor.mp4filter.FilterAdapter
import com.example.testeditor.mp4filter.FilterType
import com.example.testeditor.sound.SoundActivity
import com.example.testeditor.sticker.utils.FontProvider
import com.example.testeditor.sticker.viewmodel.Font
import com.example.testeditor.sticker.viewmodel.Layer
import com.example.testeditor.sticker.viewmodel.TextLayer
import com.example.testeditor.sticker.widget.FontAdapter
import com.example.testeditor.sticker.widget.MotionView
import com.example.testeditor.sticker.widget.MotionView.MotionViewCallback
import com.example.testeditor.sticker.widget.entity.ImageEntity
import com.example.testeditor.sticker.widget.entity.MotionEntity
import com.example.testeditor.sticker.widget.entity.TextEntity
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity(), TextEditorDialogFragment.OnTextLayerCallback{

    private val TAG = "!!!MainActivity!!!"

    //    Exoplayer 관련 변수
    private val trackSelector: TrackSelector = DefaultTrackSelector()
    private val dataFactory: DefaultDataSourceFactory by lazy {
        DefaultDataSourceFactory(
            this,
            Util.getUserAgent(this, getString(R.string.app_name))
        )
    }
    private lateinit var simpleExoPlayer: SimpleExoPlayer
    lateinit var mediaSource: MediaSource

    //    뷰 및 필터 변수
    val gpuPlayerView: GPUPlayerView by lazy {
        GPUPlayerView(this)
    }
    lateinit var glFilter: GlFilter
    lateinit var filterName: String
    lateinit var gpuMp4Composer: GPUMp4Composer

    // 프로그래스 다이얼로그
    lateinit var proDialog: CustomProgressDialog

    // 스티커 변수
    var stickerIvList = mutableListOf<String>()
    var stickertvList = mutableListOf<String>()
    val imgStickerList = arrayListOf(
        R.drawable.test_sticker1,
        R.drawable.test_sticker2,
        R.drawable.test_sticker3,
        R.drawable.test_sticker4,
        R.drawable.test_sticker5,
        R.drawable.test_sticker6,
        R.drawable.test_sticker7,
        R.drawable.test_sticker8
    )

    val fontProvider by lazy { FontProvider(resources) }
    val textStickerEditDialog: TextStickerEditDialog = TextStickerEditDialog()
    lateinit var motionView: MotionView
    var textEntityEditPanel: View? = null

    private val motionViewCallback: MotionViewCallback = object : MotionViewCallback {
        override fun onEntitySelected(entity: MotionEntity?) {
            if (entity is TextEntity) {
                textEntityEditPanel?.visibility = View.VISIBLE;
            } else {
                textEntityEditPanel?.visibility = View.GONE;
            }
        }

        override fun onEntityDoubleTap(entity: MotionEntity) {
            startTextEntityEditing()
        }
    }


    // 파일 변환을 위한 변수
    var overlayImage: Bitmap? = null
    var rootPath: String? = null
    var path: String? = null
    var imgPath: String? = null
    var fileName: String = FilterType.DEFAULT.name
    var uri: Uri? = null
    var savePath: String? = null

    var duration: Long = 0
    var videoWidth: Int = 0
    var videoHeight: Int = 0

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    // 사운드 관련 변수
    lateinit var main_tv_sound: TextView
    var soundPath: String? = null

    val bottomUp: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.bottom_up) }
    val bottomDown: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.bottom_down) }

    private val GALLERY_REQUEST_CODE = 1000
    private val TRIM_REQUEST_CODE = 1001
    private val SOUND_REQUEST_CODE = 1002

    override fun onResume() {
        // 다시 메인 화면으로 돌아왔을때 영상을 재생시켜준다
        super.onResume()
        try {
            simpleExoPlayer.playWhenReady = true
        } catch (e: Exception) {
        }
    }

    override fun onPause() {
        // 앱을 최소화하거나 다른 액티비티로 진입했을 경우 영상을 멈춰준다
        super.onPause()
        try {
            simpleExoPlayer.playWhenReady = false
        } catch (e: Exception) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 권한 체크
        setupPermissions()

        // 툴바를 액션바로 지정하고 기본 타이틀을 숨김
        setSupportActionBar(main_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 사운드 텍스트에 흐르는 글자 효과 적용
        main_tv_sound = findViewById(R.id.main_tv_sound)
        main_tv_sound.isSingleLine = true
        main_tv_sound.ellipsize = TextUtils.TruncateAt.MARQUEE
        main_tv_sound.isSelected = true

        motionView = findViewById(R.id.main_layout_sticker)
        motionView.setMotionViewCallback(motionViewCallback)
        textEntityEditPanel = findViewById(R.id.main_layout_text_edit)

        // 필터 이름에 기본값을 넣음
        filterName = FilterType.DEFAULT.name

        // 필터 리사이클러뷰에 어댑터를 연결
        val filterAdapter =
            FilterAdapter(FilterType.createFilterList())
        main_recycler_filter.adapter = filterAdapter
        main_recycler_filter.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // 스티커 리사이클러뷰에 어댑터를 연결
        val imgStickerAdapter = ImgStickerAdapter(imgStickerList)
        main_recycler_sticker.adapter = imgStickerAdapter
        main_recycler_sticker.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        initTextEntitiesListeners();

        main_toolbar.setOnClickListener(View.OnClickListener {
            Log.d(TAG, "툴바가 선택됨")
            main_layout_sticker.unselectEntity()
        })

        // 필터아이템 선택시 리스너
        filterAdapter.setOnFilterListener(object : OnFilterClickListener {
            override fun onFilterClick(holder: FilterAdapter.FilterHolder, position: Int) {
                filterName = holder.filter_tv.text.toString();
                Log.d(TAG, filterName)
                glFilter = FilterType.createGlFilter(
                    FilterType.valueOf(filterName), applicationContext, overlayImage
                )
                gpuPlayerView.setGlFilter(glFilter)
            }
        })

        // 스티커 아이탬 선택시 리스너
        imgStickerAdapter.setOnImgStickerClickListener(object : OnImgStickerClickListener {
            override fun onStickerClick(holder: ImgStickerAdapter.ImgStickerHolder, position: Int) {
                Log.d(TAG, "스티커 아이템 눌림 : $position")

                val layer = Layer()
                val bitmap = BitmapFactory.decodeResource(resources, imgStickerList[position])

                val entity = ImageEntity(layer, bitmap, motionView.width, motionView.height)
                motionView.addEntityAndPosition(entity)
            }
        })

        // 갤러리에서 파일 불러오는 리스너
        main_ib_add.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_PICK);
            intent.data = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            intent.type = "video/*"
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        })

        // 트리밍 기능
        main_ib_new_trim.setOnClickListener(View.OnClickListener {
            if (path == null) {
                toast("영상을 선택해주세요")
                return@OnClickListener
            }
            val intent = Intent(this, TrimmerActivity::class.java)
            intent.putExtra("path", path)
            startActivityForResult(intent, TRIM_REQUEST_CODE)
        })

        // 필터 기능
        main_ib_filter.setOnClickListener(View.OnClickListener {
            if (main_recycler_filter.isVisible) {
                main_recycler_filter.startAnimation(bottomDown)
                bottomDown.setAnimationListener(object : Animation.AnimationListener {
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
                main_recycler_sticker.visibility = View.GONE
            }
        })

        // 스티커 기능
        main_ib_sticker.setOnClickListener(View.OnClickListener {
            if (main_recycler_sticker.isVisible) {
                main_recycler_sticker.startAnimation(bottomDown)
                bottomDown.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        main_recycler_sticker.visibility = View.GONE
                    }

                    override fun onAnimationStart(animation: Animation?) {
                    }

                })
            } else {
                main_recycler_sticker.startAnimation(bottomUp)
                main_recycler_sticker.visibility = View.VISIBLE
                main_recycler_filter.visibility = View.GONE
            }
        })

        // 텍스트 스티커 기능
        main_ib_text.setOnClickListener(View.OnClickListener {
            addTextSticker()
        })

//        // 메인 레이아웃 클릭 리스너
//        main_layout_main.setOnClickListener(View.OnClickListener {
//            for (x in stickerIvList) {
//                x.setControlItemsHidden(true)
//            }
//            for (x in stickertvList) {
//                x.setControlItemsHidden(true)
//            }
//        })
//
//        // 스티커 클릭 리스너
//        main_layout_sticker.setOnClickListener(View.OnClickListener {
//            for (x in stickerIvList) {
//                x.setControlItemsHidden(true)
//            }
//            for (x in stickertvList) {
//                x.setControlItemsHidden(true)
//            }
//        })

        // 오디오 추가
        main_ib_audio.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, SoundActivity::class.java)
            if (!soundPath.isNullOrEmpty()) {
                intent.putExtra("path", soundPath)
            }
            startActivityForResult(intent, SOUND_REQUEST_CODE)
        })

        // 완료 버튼
        main_tv_complete.setOnClickListener(View.OnClickListener {
            Log.d(TAG, "완료 버튼이 눌림")
            Log.d(TAG, "원본 경로 : $path")
            Log.d(TAG, "저장 경로 : $savePath")

            val directory =
                File(Environment.getExternalStorageDirectory().absolutePath + "/" + Environment.DIRECTORY_DCIM + "/TEST")
            if (!directory.exists()) {
                Log.d(TAG, "path: $directory")
                Log.d(TAG, "path: $path")
                Log.d(TAG, "folder create")
                directory.mkdir()
            }

            executeVideo(0)

            /*if (filterName != FilterType.DEFAULT.name) {
                Log.d(TAG, "완료 : 필터 변경")
                setFilter(isFFmpeg())
            } else {
                if (isFFmpeg()) {
                    Log.d(TAG, "완료 : 필터 변경 없음")
                    executeVideo(0)
                } else {
                    Log.d(TAG, "아무 동작 안함")
                    runOnUiThread { toast("변경된 사항이 없습니다.") }
                }
            }*/
        })
    }

    private fun initTextEntitiesListeners() {
        findViewById<Button>(R.id.text_entity_font_size_increase).setOnClickListener(View.OnClickListener {
            increaseTextEntitySize()
        })
        findViewById<Button>(R.id.text_entity_font_size_decrease).setOnClickListener(View.OnClickListener {
            decreaseTextEntitySize()
        })
        findViewById<Button>(R.id.text_entity_color_change).setOnClickListener(View.OnClickListener {
            changeTextEntityColor()
        })
        findViewById<Button>(R.id.text_entity_font_change).setOnClickListener(View.OnClickListener {
            changeTextEntityFont()
        })
        findViewById<Button>(R.id.text_entity_edit).setOnClickListener(View.OnClickListener {
            startTextEntityEditing()
        })
        findViewById<Button>(R.id.text_entity_delete).setOnClickListener(View.OnClickListener {
            deleteTextEntity()
        })
    }

    fun increaseTextEntitySize() {
        val textEntity = currentTextEntity() ?: return
        textEntity.layer.font.increaseSize(TextLayer.Limits.FONT_SIZE_STEP)
        textEntity.updateEntity()
        motionView.invalidate()
    }

    fun decreaseTextEntitySize() {
        val textEntity = currentTextEntity() ?: return
        textEntity.layer.font.decreaseSize(TextLayer.Limits.FONT_SIZE_STEP)
        textEntity.updateEntity()
        motionView.invalidate()
    }

    fun changeTextEntityColor() {
        val textEntity = currentTextEntity() ?: return
        val initialColor = textEntity.layer.font.color

        ColorPickerDialogBuilder
            .with(this@MainActivity)
            .setTitle("색상선택")
            .initialColor(initialColor)
            .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
            .density(8)
            .setPositiveButton("완료"
            ) { d, lastSelectedColor, allColors ->
                val textEntity = currentTextEntity()
                if (textEntity != null) {
                    textEntity.layer.font.color = lastSelectedColor
                    textEntity.updateEntity()
                    motionView.invalidate()
                }
            }.setNegativeButton("취소") {
                dialog, which ->

            }.build().show()
    }

    fun changeTextEntityFont() {
        val fonts: MutableList<String> = fontProvider.fontNames
        val fontAdapter = FontAdapter(this@MainActivity, fonts, fontProvider)
        AlertDialog.Builder(this@MainActivity)
            .setTitle("폰트 선택")
            .setAdapter(fontAdapter) { dialog, which ->
                val textEntity = currentTextEntity()
                if (textEntity != null) {
                    textEntity.layer.font.typeface = fonts[which]
                    textEntity.updateEntity()
                    motionView.invalidate()
                }
            }.show()
    }

    fun startTextEntityEditing() {
        val textEntity = currentTextEntity()
        if (textEntity != null) {
            val fragment = TextEditorDialogFragment.getInstance(textEntity.layer.text)
            fragment.show(supportFragmentManager, "textStickerDialog")
        }
    }

    private fun deleteTextEntity() {
        val textEntity = currentTextEntity()
        if (textEntity != null) {
            motionView.deleteEntity(textEntity)
        }
    }

    fun currentTextEntity() =
        if (motionView != null && motionView.selectedEntity is TextEntity)
            motionView.selectedEntity as TextEntity
        else null

    fun addTextSticker() {
        val textLayer = createTextLayer()
        val textEntity = TextEntity(textLayer, motionView.width, motionView.height, fontProvider)
        motionView.addEntityAndPosition(textEntity)

        val center = textEntity.absoluteCenter()
        center.y = center.y * 0.5F
        textEntity.moveCenterTo(center)

        motionView.invalidate()

        startTextEntityEditing();
    }

    fun createTextLayer() : TextLayer {
        val textLayer = TextLayer()
        val font = Font()

        font.color = TextLayer.Limits.INITIAL_FONT_COLOR
        font.size = TextLayer.Limits.INITIAL_FONT_SIZE
        font.typeface = fontProvider.defaultFontName

        textLayer.font = font

        if (BuildConfig.DEBUG) {
            textLayer.text = "Hello, world"
        }

        return textLayer
    }

    private fun setFilter() {
        proDialog = CustomProgressDialog(getString(R.string.progress_dialog_title_filter))
        proDialog.show(supportFragmentManager, "progressDialog")
        proDialog.setDialogResultInterface(object : CustomProgressDialog.OnDialogResult {
            override fun finish() {
                gpuMp4Composer.cancel()
            }
        })

        gpuMp4Composer = GPUMp4Composer(path, savePath)
            .filter(
                FilterType.createGlFilter(
                    FilterType.valueOf(filterName), applicationContext, overlayImage
                )
            )
            .fillMode(FillMode.PRESERVE_ASPECT_FIT)
            .listener(object : GPUMp4Composer.Listener {
                override fun onFailed(exception: Exception?) {
                    Log.d(TAG, "변환 실패 : ${exception.toString()}")
                    runOnUiThread { toast(getString(R.string.conversion_failed)) }
                    proDialog.dismiss()
                }

                override fun onProgress(progress: Double) {
                    Log.d(TAG, "변환 중 : $progress")
                    proDialog.setText(progress)
                }

                override fun onCanceled() {
                    Log.d(TAG, "변환 취소")
                    runOnUiThread { toast(getString(R.string.conversion_cancel)) }
                }

                override fun onCompleted() {
                    proDialog.dismiss()
                    Log.d(TAG, "변환 성공")
                    Log.d(TAG, "uri : " + Uri.parse(savePath))
                    scanSaveFile()
                }
            })
        gpuMp4Composer.start()
    }

    private fun isFilter(): Boolean {
        return filterName != FilterType.DEFAULT.name
    }

    private fun scanSaveFile() {
        MediaScannerConnection.scanFile(applicationContext,
            arrayOf(savePath), null, object : MediaScannerConnection.OnScanCompletedListener {
                override fun onScanCompleted(path: String?, uri: Uri?) {
                }
            })
        runOnUiThread { toast(getString(R.string.conversion_complete)) }
    }

    private fun executeVideo(type: Int) {

        if (true) {
            Log.d(TAG, "스티커 변경")
            combineSticker()

            val outPath = if (isFilter()) "${cacheDir.canonicalPath}/temp.mp4" else savePath

            val cmd = when (type) {
//            2-> "-i $path -vf curves=psfile=${tempFilterFile.absolutePath} -y $outPath"
//            2 -> "-i $path -i $imgPath -filter_complex '[0:v][1:v]overlay=0:0, curves=psfile=${tempFilterFile.absolutePath}' -y $outPath"
//            else-> "-i $path -vf curves=lighter -c:a copy -y $outPath"
                else -> "-i $path -i $imgPath -filter_complex '[0:v][1:v]overlay=0:0'  -y $outPath"
            }

            Log.d (TAG, "cmd : $cmd")
            Thread(Runnable {
                val rc = FFmpeg.execute(cmd)

                if (rc == Config.RETURN_CODE_SUCCESS) {
                    proDialog.dismiss()
                    if (isFilter()) {
                        setFilePath(outPath)
                        setFilter()
                    } else {
                        scanSaveFile()
                    }
                } else {
                    runOnUiThread { toast(getString(R.string.conversion_failed)) }
                }
            }).start()

            proDialog = CustomProgressDialog(getString(R.string.progress_dialog_title))
            proDialog.show(supportFragmentManager, "proDialog")
            proDialog.setDialogResultInterface(object : CustomProgressDialog.OnDialogResult {
                override fun finish() {
                    FFmpeg.cancel()
                    runOnUiThread { toast(getString(R.string.conversion_cancel)) }
                }
            })

            // FFmpeg 프로그래스
            Config.enableStatisticsCallback(StatisticsCallback {
                val progress = (it.time / duration.toDouble())
                Log.d(TAG, String.format("frame: ${it.videoFrameNumber} time: ${it.time}"))
                Log.d(TAG, String.format("progress: $progress"))
                Log.d(TAG, "rc : ${Config.getLastReturnCode()} suc: ${Config.RETURN_CODE_SUCCESS}")
                proDialog.setText(progress)
            })
        } else {
            Log.d(TAG, "변경사항이 없습니다.")

            if (isFilter()) {
                toast(getString(R.string.common_no_change))
            } else {
                setFilter()
            }
        }

        /*val inputStream = resources.openRawResource(R.raw.aurora);
        val tempFilterPath = getDir("tempFilter", Context.MODE_PRIVATE)
        val tempFilterFile = File(tempFilterPath, "${filterName.toLowerCase()}.acv")

        try {
            val outputStream = FileOutputStream(tempFilterFile)

            val fileReader = ByteArray(4096)
            var bytesRead = 0

            while (true) {
                val read = inputStream.read(fileReader)

                if (read == -1) {
                    break
                }

                outputStream.write(fileReader, 0, read)
                bytesRead += read
            }

            outputStream.flush()

            inputStream.close()
            outputStream.close()

        } catch (e: IOException) {
            Log.d(TAG, e.printStackTrace().toString())
        }*/
    }

    private fun soundCombine() {
        val circlePro = CircleProgressDialog()
        circlePro.show(supportFragmentManager, "circlePro")

        val soundInfo = FFprobe.getMediaInformation(soundPath)

        Log.d(TAG, "사운드 길이 : ${soundInfo.duration} 영상 길이 : ${duration}")

        val cmd = if (soundInfo.duration > duration) {
            "-i '$rootPath' -i ${cacheDir.canonicalPath}/temp_merge.mp3 -map 0:v -map 1:a -c copy -shortest -y \"${cacheDir.canonicalPath}/temp.mp4\""
        } else {
            "-i '$rootPath' -i ${cacheDir.canonicalPath}/temp_merge.mp3 -map 0:v -map 1:a -c copy -y \"${cacheDir.canonicalPath}/temp.mp4\""
        }

        Log.d (TAG, "cmd : $cmd")
        Thread(Runnable {
            FFmpeg.execute("-i '$rootPath' -y ${cacheDir.canonicalPath}/temp.mp3")
            FFmpeg.execute("-i ${cacheDir.canonicalPath}/temp.mp3 -i $soundPath -filter_complex amerge -c:a libmp3lame -q:a 4 -y ${cacheDir.canonicalPath}/temp_merge.mp3")
            val rc = FFmpeg.execute(cmd)

            if (rc == Config.RETURN_CODE_SUCCESS) {
                Log.d(TAG, "변환 완료")
                scanSaveFile()
                setFilePath("${cacheDir.canonicalPath}/temp.mp4")
                runOnUiThread { setPlayer(path!!) }
            } else {
                runOnUiThread { toast(getString(R.string.conversion_failed)) }
            }

            runOnUiThread { circlePro.dismiss() }
        }).start()
    }

    private fun combineSticker() {
        /*for (item in stickerIvList) {
            item.setControlItemsHidden(true)
        }
        for (item in stickertvList) {
            item.setControlItemsHidden(true)
        }*/

        main_layout_sticker.isDrawingCacheEnabled = true
        main_layout_sticker.buildDrawingCache()

        val imgWidth = main_layout_sticker.width
        val imgHeight = main_layout_sticker.height
        Log.d(TAG, "img_width : $imgWidth")
        Log.d(TAG, "img_height : $imgHeight")

        overlayImage = Bitmap.createScaledBitmap(
            main_layout_sticker.drawingCache,
            videoWidth, videoHeight,true)
        Log.d(TAG, overlayImage.toString())
        val storage = File(
            Environment.getExternalStorageDirectory()
                .absolutePath + "/" + Environment.DIRECTORY_DCIM + "/TEST"
        )
        val fileName = "temp.png"
        val tempFile = File(storage, fileName)
        imgPath = tempFile.absolutePath

        try {
            Log.d("로그", "변환 : " + tempFile.absolutePath)
            tempFile.createNewFile()
            val out = FileOutputStream(tempFile)
            overlayImage!!.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun setupPermissions() {
        var rejectedPermissionList = ArrayList<String>()

        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                rejectedPermissionList.add(permission)
            }
        }

        if (rejectedPermissionList.isNotEmpty()) {
            val array = arrayOfNulls<String>(rejectedPermissionList.size)
            ActivityCompat.requestPermissions(
                this,
                rejectedPermissionList.toArray(array),
                GALLERY_REQUEST_CODE
            )
        }
    }

    private fun getRealPathFromURI(contentUri: Uri): String? {
        var columnIndex = 0
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor =
            contentResolver.query(contentUri, proj, null, null, null)
        if (cursor!!.moveToFirst()) {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        }
        return cursor.getString(columnIndex)
    }

    private fun setPlayer(path: String) {
        main_playerview.removeAllViews()

        mediaSource =
            ExtractorMediaSource.Factory(dataFactory).createMediaSource(Uri.parse(path))
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        simpleExoPlayer.repeatMode = Player.REPEAT_MODE_ONE

        simpleExoPlayer.playWhenReady = true
        simpleExoPlayer.prepare(mediaSource)

        gpuPlayerView.setSimpleExoPlayer(simpleExoPlayer)
        gpuPlayerView.layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        main_playerview.addView(gpuPlayerView)
        gpuPlayerView.onResume()

        val info = FFprobe.getMediaInformation(path)
        duration = info.duration
        val streamInfo = info.streams
        videoWidth = streamInfo[0].width.toInt()
        videoHeight = streamInfo[0].height.toInt()
        Log.d(TAG, "width : $videoWidth")
        Log.d(TAG, "height : $videoHeight")

        simpleExoPlayer.addListener(object : Player.EventListener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
            }

            override fun onSeekProcessed() {
            }

            override fun onTracksChanged(
                trackGroups: TrackGroupArray?,
                trackSelections: TrackSelectionArray?
            ) {
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
            }

            override fun onLoadingChanged(isLoading: Boolean) {
            }

            override fun onPositionDiscontinuity(reason: Int) {
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            }

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            }
        })
    }

    private fun setFilePath(rawPath: String?) {
        path = rawPath
        if (rawPath != null) {
            fileName = File(rootPath).name
            Log.d(TAG, "파일이름 : $fileName")
            savePath =
                Environment.getExternalStorageDirectory().absolutePath + "/" + Environment.DIRECTORY_DCIM + "/TEST/modify_" + fileName
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // 갤러리에서 파일을 받아왔을때
            GALLERY_REQUEST_CODE -> {
                // 1번방법
                if (data != null) {
                    uri = data.data
                    rootPath = getRealPathFromURI(uri!!)
                    setFilePath(getRealPathFromURI(uri!!))
                    setPlayer(path!!)
                } else {
                    return
                }
            }
            // 영상 편집을 완료했을때
            TRIM_REQUEST_CODE -> {
                if (data != null) {
                    path = data.getStringExtra("path")
                    Log.d(TAG, "result path : $path")
                    setFilePath(path)
                    setPlayer(path!!)
                } else {
                    return
                }
            }
            // 사운드를 가져 왔을때
            SOUND_REQUEST_CODE -> {
                if (data != null) {
                    val soundTitle = data.getStringExtra("title")

                    if (soundTitle == "empty") {
                        setFilePath(rootPath)
                        setPlayer(rootPath!!)
                        soundPath = null
                    } else {
                        main_tv_sound.text = soundTitle
                        soundPath = data.getStringExtra("path")
                        soundCombine()
                        Log.d(TAG, "SOUND REQUEST : ${data.getStringExtra("title")} path : $soundPath")
                    }
                } else {
                    return
                }
            }
        }
    }

    override fun textChanged(text: String) {
        val textEntity = currentTextEntity() ?: return
        val textLayer = textEntity.layer
        if (text != textLayer.text) {
            textLayer.text = text
            textEntity.updateEntity()
            motionView.invalidate()
        }
    }
}

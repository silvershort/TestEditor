package com.example.testeditor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.arthenica.mobileffmpeg.*
import com.daasuu.gpuv.composer.FillMode
import com.daasuu.gpuv.composer.GPUMp4Composer
import com.daasuu.gpuv.egl.filter.GlFilter
import com.daasuu.gpuv.player.GPUPlayerView
import com.example.testeditor.dialog.CircleProgressDialog
import com.example.testeditor.dialog.CustomProgressDialog
import com.example.testeditor.dialog.TextStickerEditDialog
import com.example.testeditor.mp4filter.FilterAdapter
import com.example.testeditor.mp4filter.FilterType
import com.example.testeditor.sound.SoundActivity
import com.example.testeditor.sticker.DrawableSticker
import com.example.testeditor.sticker.Sticker
import com.example.testeditor.sticker.StickerView
import com.example.testeditor.sticker.TextSticker
import com.example.testeditor.timeline.TimeLineActivity
import com.example.testeditor.util.BitmapObject
import com.example.testeditor.util.KeyBoardUtil
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import petrov.kristiyan.colorpicker.ColorPicker
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Runnable

class MainActivity : AppCompatActivity() {

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

    // 레이아웃
    lateinit var textEditer: ConstraintLayout

    // 인클루드 레이아웃
    lateinit var text_sticker_tv_done: TextView
    lateinit var text_sticker_et_input: EditText
    lateinit var text_sticker_color: ImageButton
    lateinit var text_sticker_gothic: TextView
    lateinit var text_sticker_pen: TextView
    lateinit var text_sticker_brush: TextView
    lateinit var text_sticker_myeogjo: TextView
    lateinit var text_sticker_square: TextView

    var selectColor: Int = -1
    lateinit var selectTypeface: Typeface

    // 스티커 변수
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
    val textStickerEditDialog: TextStickerEditDialog = TextStickerEditDialog()
    var stickerIndex = 0

    // 파일 변환을 위한 변수
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

        // 레이아웃을 받아옴
        textEditer = findViewById(R.id.main_include_text_sticker)

        text_sticker_tv_done = textEditer.findViewById(R.id.text_sticker_tv_done)
        text_sticker_et_input = textEditer.findViewById(R.id.text_sticker_et_input)
        text_sticker_color = textEditer.findViewById(R.id.text_sticker_color)
        text_sticker_gothic = textEditer.findViewById(R.id.text_sticker_gothic)
        text_sticker_pen = textEditer.findViewById(R.id.text_sticker_pen)
        text_sticker_brush = textEditer.findViewById(R.id.text_sticker_brush)
        text_sticker_myeogjo = textEditer.findViewById(R.id.text_sticker_myeogjo)
        text_sticker_square = textEditer.findViewById(R.id.text_sticker_square)

        // 사운드 텍스트에 흐르는 글자 효과 적용
        main_tv_sound = findViewById(R.id.main_tv_sound)
        main_tv_sound.isSingleLine = true
        main_tv_sound.ellipsize = TextUtils.TruncateAt.MARQUEE
        main_tv_sound.isSelected = true

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

        // 필터아이템 선택시 리스너
        filterAdapter.setOnFilterListener(object : OnFilterClickListener {
            override fun onFilterClick(holder: FilterAdapter.FilterHolder, position: Int) {
                filterName = holder.filter_tv.text.toString();
                Log.d(TAG, filterName)
                glFilter = FilterType.createGlFilter(
                    FilterType.valueOf(filterName), applicationContext, null
                )
                gpuPlayerView.setGlFilter(glFilter)
            }
        })

        // 스티커 아이탬 선택시 리스너
        imgStickerAdapter.setOnImgStickerClickListener(object : OnImgStickerClickListener {
            override fun onStickerClick(holder: ImgStickerAdapter.ImgStickerHolder, position: Int) {
                val imgSticker = DrawableSticker(ContextCompat.getDrawable(this@MainActivity, imgStickerList[position]))
                main_layout_sticker.addSticker(imgSticker)
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
            val textSticker = TextSticker(this)
                .setText("테스트 텍스트")
                .setMaxTextSize(14F)
                .setTextColor(Color.BLUE)
                .setTypeface(Typeface.MONOSPACE)
                .resizeText()
            main_layout_sticker.addSticker(textSticker, Sticker.Position.TOP)
        })

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

        // 스티커 리스너
        main_layout_sticker.onStickerOperationListener = object : StickerView.OnStickerOperationListener{
            override fun onStickerZoomFinished(sticker: Sticker) {
            }

            override fun onStickerTimeClicked(sticker: Sticker) {
                Log.d(TAG, "타임라인 편집 버튼이 눌렸습니다")
                val intent = Intent(this@MainActivity, TimeLineActivity::class.java)
                intent.putExtra("path", path)

                main_layout_sticker.toggle()
                BitmapObject.bitmap = stickerDraw()
                main_layout_sticker.toggle()
                startActivity(intent)
            }

            override fun onStickerClicked(sticker: Sticker) {
                Log.d(TAG, "x : ${main_layout_sticker.currentSticker!!.matrix}")
            }

            override fun onStickerTouchedDown(sticker: Sticker) {

            }

            override fun onStickerDoubleTapped(sticker: Sticker) {
                if (sticker !is TextSticker) return
                if (!textEditer.isVisible) {
                    textEditer.visibility = View.VISIBLE
                    uiVisibility(false)

                    text_sticker_et_input.requestFocus()

                    KeyBoardUtil.focusAndShowKeyboard(this@MainActivity, text_sticker_et_input)
                    /*val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(text_sticker_et_input, InputMethodManager.SHOW_IMPLICIT)*/
                }
            }

            override fun onStickerDragFinished(sticker: Sticker) {
            }

            override fun onStickerFlipped(sticker: Sticker) {
            }

            override fun onStickerDeleted(sticker: Sticker) {
            }

            override fun onStickerAdded(sticker: Sticker) {
            }
        }

        // 컬러 선택 리스너
        text_sticker_color.setOnClickListener {
            val colorPicker = ColorPicker(this)
            colorPicker.show()
            colorPicker.setOnChooseColorListener(object : ColorPicker.OnChooseColorListener {
                override fun onChooseColor(position: Int, color: Int) {
                    selectColor = color
                    text_sticker_et_input.setTextColor(color)
                }

                override fun onCancel() {

                }
            })
        }

        text_sticker_gothic.setOnClickListener {
            selectTypeface = ResourcesCompat.getFont(this, R.font.nanum_gothic)!!
            text_sticker_et_input.typeface = selectTypeface
        }

        text_sticker_pen.setOnClickListener {
            selectTypeface = ResourcesCompat.getFont(this, R.font.nanum_pen)!!
            text_sticker_et_input.typeface = selectTypeface
        }

        text_sticker_brush.setOnClickListener {
            selectTypeface = ResourcesCompat.getFont(this, R.font.nanum_brush)!!
            text_sticker_et_input.typeface = selectTypeface
        }

        text_sticker_myeogjo.setOnClickListener {
            selectTypeface = ResourcesCompat.getFont(this, R.font.nanum_myeongjo)!!
            text_sticker_et_input.typeface = selectTypeface
        }

        text_sticker_square.setOnClickListener {
            selectTypeface = ResourcesCompat.getFont(this, R.font.nanum_square_ac)!!
            text_sticker_et_input.typeface = selectTypeface
        }

        // 텍스트 스티커 에디터 완료
        text_sticker_tv_done.setOnClickListener(View.OnClickListener {
            val tempSticker = main_layout_sticker.currentSticker as TextSticker

            val temp = text_sticker_et_input.text.toString()

            KeyBoardUtil.unfocusAndHideKeyboard(this@MainActivity, text_sticker_et_input)
            textEditer.visibility = View.GONE
            uiVisibility(true)

            tempSticker.text = text_sticker_et_input.text.toString()
            if (selectColor != -1) tempSticker.setTypeface(selectTypeface)
            if (selectColor != -1) tempSticker.setTextColor(selectColor)
            tempSticker.resizeText()
        })
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
                    FilterType.valueOf(filterName), applicationContext, null
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

        if (main_layout_sticker.isNoneSticker) {
            Log.d(TAG, "스티커 변경")
            combineSticker()


            val outPath = if (isFilter()) "${cacheDir.canonicalPath}/temp.mp4" else savePath

            val cmd = when (type) {
//            2-> "-i $path -vf curves=psfile=${tempFilterFile.absolutePath} -y $outPath"
//            2 -> "-i $path -i $imgPath -filter_complex '[0:v][1:v]overlay=0:0, curves=psfile=${tempFilterFile.absolutePath}' -y $outPath"
//            else-> "-i $path -vf curves=psfile=${Environment.getExternalStorageDirectory().absolutePath + "/" + Environment.DIRECTORY_DCIM}/TEST/afterglow.acv -c:v libx264 -preset ultrafast -c:a copy -y $outPath"
//                else -> "-y -i $path -i $imgPath -filter_complex '[0:v][1:v]overlay=0:0, colorspace=smpte240m' -c:v libx264 -preset ultrafast -c:a copy $outPath"
                else -> "-y -i $path -i $imgPath -filter_complex '[0:v][1:v]overlay=0:0, colorspace=smpte240m' -c:v libx264 -preset ultrafast -c:a copy $outPath"
//                else -> "-y -i $path -i $filterPath -filter_complex '[0]split[m][a];[m][a]alphamerge[keyed];[1][keyed]overlay=eof_action=endall' -shortest $outPath"
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

    private fun stickerDraw(): Bitmap? {
        val viewBitmap = Bitmap.createBitmap(main_layout_sticker.width, main_layout_sticker.height, Bitmap.Config.ARGB_8888);
        val canvas = Canvas(viewBitmap)
        main_layout_sticker.draw(canvas)

        return Bitmap.createScaledBitmap(
            viewBitmap, main_layout_sticker.width, main_layout_sticker.height,false)
    }

    private fun combineSticker() {
        val viewBitmap = Bitmap.createBitmap(main_layout_sticker.width, main_layout_sticker.height, Bitmap.Config.ARGB_8888);
        val canvas = Canvas(viewBitmap)
        main_layout_sticker.draw(canvas)

        var imgWidth: Int = 0
        var imgHeight: Int = 0

        if (main_layout_sticker.width > main_layout_sticker.height && videoWidth < videoHeight) {
            imgWidth = main_layout_sticker.height
            imgHeight = main_layout_sticker.width
        } else {
            imgWidth = main_layout_sticker.width
            imgHeight = main_layout_sticker.height
        }

        Log.d(TAG, "img_width : $imgWidth")
        Log.d(TAG, "img_height : $imgHeight")
        Log.d(TAG, "view_width : ${main_layout_sticker.width}")
        Log.d(TAG, "view_height : ${main_layout_sticker.height}")

        val overlayImage = Bitmap.createScaledBitmap(
            viewBitmap, imgWidth, imgHeight,false)

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
            overlayImage!!.compress(Bitmap.CompressFormat.PNG, 100, out)
            overlayImage?.recycle()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun uiVisibility(visible: Boolean) {
        if (visible) {
            main_toolbar.visibility = View.VISIBLE
            main_layout_button.visibility = View.VISIBLE
        } else {
            main_toolbar.visibility = View.INVISIBLE
            main_layout_button.visibility = View.INVISIBLE
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
                    main_layout_sticker.removeAllStickers()
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
}

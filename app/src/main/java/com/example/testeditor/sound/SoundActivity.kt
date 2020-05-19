package com.example.testeditor.sound

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testeditor.R
import com.example.testeditor.api.APIClient
import com.example.testeditor.dialog.CircleProgressDialog
import com.example.testeditor.dialog.CustomProgressDialog
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class SoundActivity : AppCompatActivity() {

    private val TAG = "!!!SoundActivity!!!"
    private val EMPTY_RESULT = -1

    private var soundPath: String? = null

    private lateinit var sound_layout_selected: ConstraintLayout
    private lateinit var sound_recyclerview: RecyclerView
    private lateinit var sound_tv_current: TextView
    private lateinit var sound_tv_cannel: TextView

    private val circleDialog: CircleProgressDialog = CircleProgressDialog()
    private lateinit var rootPath: String
    private lateinit var directory: File
    private lateinit var file: File

    private var soundEmpty: Boolean = false

    private val soundList = arrayListOf<SoundData>(
        SoundData(
            "Blippy_Trance______________________________________________",
            "music/royalty-free/mp3-royaltyfree/Blippy%20Trance.mp3"
        ),
        SoundData(
            "A_Very_Brady_Special",
            "music/royalty-free/mp3-royaltyfree/A%20Very%20Brady%20Special.mp3"
        ),
        SoundData(
            "Frogs_Legs_Rag",
            "music/royalty-free/mp3-royaltyfree/Frogs%20Legs%20Rag.mp3"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sound)

        soundPath = intent?.getStringExtra("path")

        sound_layout_selected = findViewById(R.id.sound_layout_selected)
        sound_recyclerview = findViewById(R.id.sound_recyclerview)
        sound_tv_current = findViewById(R.id.sound_tv_current)
        sound_tv_cannel = findViewById(R.id.sound_tv_cannel)

        sound_tv_current.isSingleLine = true
        sound_tv_current.ellipsize = TextUtils.TruncateAt.MARQUEE
        sound_tv_current.isSelected = true

        if (!soundPath.isNullOrEmpty()) {
            sound_layout_selected.visibility = View.VISIBLE
            sound_tv_current.text = soundPath
        }

        val soundSelectAdapter = SoundSelectAdapter(soundList)
        sound_recyclerview.adapter = soundSelectAdapter
        sound_recyclerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        sound_tv_cannel.setOnClickListener(View.OnClickListener {

        })

        sound_tv_cannel.setOnClickListener(View.OnClickListener {
            soundEmpty = true
            sound_tv_current.text = ""
            sound_layout_selected.visibility = View.GONE
        })

        soundSelectAdapter.setOnSoundSelectClickListener(object : OnSoundSelectClickListener {
            override fun playButtonClick(holder: SoundSelectAdapter.SoundHolder, position: Int) {
            }

            override fun selectButtonClick(holder: SoundSelectAdapter.SoundHolder, position: Int) {
                soundEmpty = false

                circleDialog.show(supportFragmentManager, "circleDialog")

                rootPath = Environment.getExternalStorageDirectory().absolutePath + "/" + Environment.DIRECTORY_DCIM + "/TEST/"
                directory = File(rootPath)

                if (!directory.exists()) {
                    Log.d(TAG, "path: $directory")
                    Log.d(TAG, "folder create")
                    directory.mkdir()
                }

                file = File(rootPath, soundList[position].title + ".mp3")
                Log.d(TAG, file.absolutePath)

                if (file.exists()) {
                    Log.d(TAG, "이미 파일이 존재합니다.")
                    setResultFisish(position)
                } else {
                    APIClient.getAPIInterface().getAudioDownload(soundList[position].url).enqueue(
                        object : retrofit2.Callback<ResponseBody> {
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                circleDialog.dismiss()
                                Log.d(TAG, "다운로드에 오류가 발생")
                            }

                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                val result = writeResponseBodyToDisk(response.body()!!, position)
                                circleDialog.dismiss()
                                if (result) {
                                    setResultFisish(position)
                                } else {
                                    Log.d(TAG, "다운로드에 오류가 발생")
                                }
                            }
                        }
                    )
                }
            }
        })
    }

    fun setResultFisish(position: Int) {
        val intent = Intent()

        if (position == EMPTY_RESULT) {
            intent.putExtra("path", "empty")
            intent.putExtra("title", "empty")
        } else {
            Log.d(TAG, "인텐트로 넘기기 직전 ${file.absolutePath}")
            intent.putExtra("path", file.absoluteFile.toString())
            intent.putExtra("title", soundList[position].title)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun writeResponseBodyToDisk(body: ResponseBody, position: Int): Boolean {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        val fileReader = ByteArray(4096)

        val fileSize = body.contentLength()
        var fileSizeDownloaded = 0;

        inputStream = body.byteStream()
        outputStream = FileOutputStream(file)

        while(true) {
            val read = inputStream.read(fileReader)

            if (read == -1) {
                break
            }

            outputStream.write(fileReader, 0, read)
            fileSizeDownloaded += read
            Log.d(TAG, "file download: ${fileSizeDownloaded / fileSize.toDouble()}")
        }

        outputStream.flush()

        outputStream.close()
        inputStream.close()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (soundEmpty) setResultFisish(-1)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (soundEmpty) setResultFisish(-1)
        super.onBackPressed()
    }
}

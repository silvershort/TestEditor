package com.example.testeditor

/*
import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import com.video.trimmer.interfaces.OnTrimVideoListener
import kotlinx.android.synthetic.main.activity_trimmer.*
import java.io.File

class TrimmerActivity : AppCompatActivity(), OnTrimVideoListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimmer)

        setSupportActionBar(trim_toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        supportActionBar?.setHomeButtonEnabled(true)

        val path:String? = intent.getStringExtra("path")
        Log.d("로그", "path $path")

        trim_trimmer.setTextTimeSelectionTypeface(Typeface.SANS_SERIF)
            .setOnTrimVideoListener(this)
            .setVideoURI(Uri.parse(path))
            .setVideoInformationVisibility(true)
            .setMaxDuration(10)
            .setMinDuration(2)
            .setDestinationPath(cacheDir.absolutePath + "/temp")

        trim_tv_complete.setOnClickListener(View.OnClickListener {
            trim_trimmer.onSaveClicked()
            finish()
        })
    }

    override fun cancelAction() {
        Log.d("로그", "취소됨")
    }

    override fun getResult(uri: Uri) {
        Log.d("로그", "변환 성공 : $uri")
    }

    override fun onError(message: String) {
        Log.d("로그", "에러 발생 $message")
    }

    override fun onTrimStarted() {
        Log.d("로그", "저장 시작")
    }
}
*/

package com.example.testeditor

import android.graphics.Typeface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import com.video.trimmer.interfaces.OnTrimVideoListener
import kotlinx.android.synthetic.main.activity_trimmer.*
import java.io.File

class TrimmerActivity : AppCompatActivity(), OnTrimVideoListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimmer)

        val path:String? = intent.getStringExtra("path")

        trim_trimmer.setTextTimeSelectionTypeface(Typeface.SANS_SERIF)
            .setOnTrimVideoListener(this)
            .setVideoURI(Uri.parse(path))
            .setVideoInformationVisibility(true)
            .setMaxDuration(10)
            .setMinDuration(2)
            .setDestinationPath(Environment.getExternalStorageDirectory().toString() + File.separator + "temp")
    }

    override fun cancelAction() {
        TODO("Not yet implemented")
    }

    override fun getResult(uri: Uri) {
        TODO("Not yet implemented")
    }

    override fun onError(message: String) {
        TODO("Not yet implemented")
    }

    override fun onTrimStarted() {
        TODO("Not yet implemented")
    }
}

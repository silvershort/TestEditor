package com.example.testeditor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_GALLERY = 0x01
        internal const val EXTRA_VIDEO_PATH = "EXTRA_VIDEO_PATH"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun printlog() {
        Log.d("로그", "로그가 출력되었습니다.")
    }

    private fun testFun(first: () -> Unit) {
        first()
    }

    private fun pickFromGallery(intentCode: Int) {
    }
}

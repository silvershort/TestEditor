package com.example.testeditor

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.white.progressview.HorizontalProgressView

class CustomProgressDialog : DialogFragment() {

    lateinit var progress: HorizontalProgressView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.custom_progress_dialog, container)
        dialog!!.setCanceledOnTouchOutside(false)
        progress = view.findViewById(R.id.progress_pro_view)
        return view
    }

    fun setText(percent: Double) {
        progress.setProgress((percent * 100).toInt(), true)
    }
}
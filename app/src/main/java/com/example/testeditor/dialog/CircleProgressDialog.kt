package com.example.testeditor.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.testeditor.R

class CircleProgressDialog : DialogFragment() {

    private val TAG = "!!!CircleProgressDialog!!!"

    private lateinit var circle_progress: ProgressBar
    private lateinit var circle_tv_title: TextView
    private lateinit var dialogResult: OnDialogResult

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.circle_progress_dialog, container)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        circle_progress = view.findViewById(R.id.circle_progress)
        circle_tv_title = view.findViewById(R.id.circle_tv_title)

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(activity!!, theme) {
            override fun onBackPressed() {
                Log.d(TAG, "백버튼이 다이얼로그에서 눌림")
/*                if (dialogResult != null) {
                    dialogResult.finish()
                }*/
//                dismiss()
            }
        }
    }

    fun setDialogResultInterface(dialogResult: OnDialogResult) {
        this.dialogResult = dialogResult
    }

    interface OnDialogResult {
        fun finish()
    }
}
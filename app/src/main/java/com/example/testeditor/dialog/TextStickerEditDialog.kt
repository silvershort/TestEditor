package com.example.testeditor.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.testeditor.R

class TextStickerEditDialog : DialogFragment() {

    private val TAG = "!!!TextStickerEditDialog!!!"

    private lateinit var dialogResult: OnDialogResult
    private lateinit var inputText: EditText
    private lateinit var negativeButton: Button
    private lateinit var positiveButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.text_sticker_edit_dialog, container)
        val layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()

        dialog?.setCanceledOnTouchOutside(false)

        inputText = view.findViewById(R.id.textsticker_et_inputtext)
        negativeButton = view.findViewById(R.id.textsticker_btn_negative)
        positiveButton = view.findViewById(R.id.textsticker_btn_positive)

        inputText.requestFocus()

        Handler().postDelayed(Runnable {
            val imm: InputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(inputText, InputMethodManager.SHOW_IMPLICIT)
        }, 150)

        negativeButton.setOnClickListener(View.OnClickListener {
            dismiss()
        })

        positiveButton.setOnClickListener(View.OnClickListener {
            dialogResult?.finish(inputText.text.toString())
            dismiss()
        })

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(activity!!, theme) {
            override fun onBackPressed() {
                Log.d(TAG, "백버튼이 다이얼로그에서 눌림")
                dismiss()
            }
        }
    }

    fun setDialogResultInterface(dialogResult: OnDialogResult) {
        this.dialogResult = dialogResult
    }

    interface OnDialogResult {
        fun finish(text: String)
    }
}
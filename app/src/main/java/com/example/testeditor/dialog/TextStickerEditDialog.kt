package com.example.testeditor.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.testeditor.R

class TextStickerEditDialog : DialogFragment() {

    private val TAG = "!!!TextStickerEditDialog!!!"

    private lateinit var dialogResult: OnDialogResult
    private lateinit var inputText: EditText
    private lateinit var complete: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        dialog?.setCanceledOnTouchOutside(false)

        val view = inflater.inflate(R.layout.text_sticker_edit_dialog, container)

        inputText = view.findViewById(R.id.textsticker_et_input)
        complete = view.findViewById(R.id.textsticker_tv_done)

        inputText.requestFocus()

        Handler().postDelayed(Runnable {
            val imm: InputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(inputText, InputMethodManager.SHOW_IMPLICIT)
        }, 150)

        complete.setOnClickListener(View.OnClickListener {
            Log.d(TAG, "완료 버튼이 눌렸습니다")
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
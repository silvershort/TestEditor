package com.example.testeditor.util

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

object KeyBoardUtil {

    lateinit var imm: InputMethodManager

    fun unfocusAndHideKeyboard(context: Context, editText: EditText) {
        imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        editText.clearFocus()
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    fun focusAndShowKeyboard(context: Context, editText: EditText) {
        imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        editText.requestFocus()
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }
}
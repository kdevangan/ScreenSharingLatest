package com.ezmall.screensharinglatest_agora.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

object CommonUtil {

     fun hideInputBoard(activity: Activity, editText: EditText) {
         val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
         imm.hideSoftInputFromWindow(editText.windowToken, 0)
     }
}
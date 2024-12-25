package com.yuepeng.demo.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

// 隐藏键盘的工具
fun View.hideKeyboard(content:Context){
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}
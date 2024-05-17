package com.zhouz.turntablelib

import android.graphics.Bitmap
import android.graphics.Matrix


/**
 * @author:zhouz
 * @date: 2024/5/17 14:55
 * description：图标位置
 */
data class IconData(
    var icon: Int = 0,
    var bitmap: Bitmap? = null,
    val matrix: Matrix = Matrix()
)
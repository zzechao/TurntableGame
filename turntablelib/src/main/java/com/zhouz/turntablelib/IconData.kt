package com.zhouz.turntablelib

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Matrix
import android.graphics.Shader
import android.view.View


/**
 * @author:zhouz
 * @date: 2024/5/17 14:55
 * description：图标绘制信息
 */
data class IconData(
    var icon: Int = 0,
    var bitmap: Bitmap? = null,
    val matrix: Matrix = Matrix()
) {

    var bitmapShader: BitmapShader? = null

    private var isSettingMatrix: Boolean = false

    fun setMatrix(view: View, scale: Float, builder: (() -> Unit)? = null) {
        if (isSettingMatrix) return
        isSettingMatrix = true
        bitmap?.let {
            matrix.postScale(scale, scale)
            val left = (view.measuredWidth.toFloat() - it.width.toFloat() * scale) / 2f
            val top = (view.measuredHeight.toFloat() - it.height.toFloat() * scale) / 2f
            matrix.postTranslate(left, top)
            builder?.invoke()
        }
    }

    fun initShader() {
        bitmapShader = bitmap?.let {
            BitmapShader(it, Shader.TileMode.MIRROR, Shader.TileMode.REPEAT)
        }
    }
}
package com.zhouz.turntablelib

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.view.View


/**
 * @author:zhouz
 * @date: 2024/5/17 14:55
 * description：图标位置
 */
data class IconData(
    var icon: Int = 0,
    var bitmap: Bitmap? = null,
    val matrix: Matrix = Matrix()
) {

    private var isSettingMatrix: Boolean = false
    fun setMatrix(view: View) {
        if (isSettingMatrix) return
        isSettingMatrix = true
        bitmap?.let {
            val src = RectF(0f, 0f, it.width.toFloat(), it.height.toFloat())
            val dst = RectF(0f, 0f, view.measuredWidth.toFloat(), view.measuredHeight.toFloat())
            matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER)
            matrix.postScale(
                it.width.toFloat() / view.measuredWidth.toFloat(),
                it.height.toFloat() / view.measuredHeight.toFloat()
            )
            val left = (view.measuredWidth.toFloat() - it.width.toFloat()) / 2f
            val top = (view.measuredHeight.toFloat() - it.height.toFloat()) / 2f
            matrix.postTranslate(left, top)
        }
    }
}
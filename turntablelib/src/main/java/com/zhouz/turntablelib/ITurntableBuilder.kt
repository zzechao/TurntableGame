package com.zhouz.turntablelib

import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.graphics.Bitmap
import android.view.View


/**
 * @author:zhouz
 * @date: 2024/5/16 16:47
 * description：声明式构建类
 */
interface ITurntableBuilder {
    var turntableBg: Int
    var turntableNeedleBg: Int
    var turntableNeedleIcon: Int
    var turntableNeedleTop: Int

    var numberPart: Int
    var mMinTimes: Int
    var mDurationTime: Long
    var startAngle: Float

    var dividingLineColor: Int
    var dividingLineSize: Float
    var dividingLineWidth: Float

    var photoLoader: (suspend (Any) -> Bitmap?)?

    var animatorUpdateListener: AnimatorUpdateListener?

    fun partyChildBuild(child: IPartyChild.() -> Unit)

    fun build(finish: (() -> Unit)? = null)
}


interface IPartyChild {
    var partyChild: (Int) -> View?

    var centerChild: View?

    var turntableBgView: View?
}
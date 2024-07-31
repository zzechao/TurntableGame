package com.zhouz.turntablelib

import android.animation.ValueAnimator.AnimatorUpdateListener
import android.graphics.Bitmap
import android.view.View
import android.view.animation.Interpolator


/**
 * @author:zhouz
 * @date: 2024/5/16 16:47
 * description：声明式构建类
 */
interface ITurntableBuilder {
    // 转盘背景
    var turntableBg: Int

    // 转盘中心背景
    var turntableNeedleBg: Int

    // 转盘指针图标
    var turntableNeedleIcon: Int

    // 转盘指针图标距离 中点 的上间距
    var turntableNeedleTop: Int

    // 转盘份数
    var numberPart: Int

    // 最少转动次数
    var mMinTimes: Int

    // 装懂时长
    var mDurationMillisecond: Long

    // 开始角度
    var startAngle: Float

    // 分割线颜色
    var dividingLineColor: Int

    // 分割线图标
    var dividingLineIcon: Int

    // 分割线高度
    var dividingLineSize: Float

    // 分割线长度
    var dividingLineWidth: Float

    // 是否显示分割线的序号
    var dividingNumberShow: Boolean

    // 插值器
    var interpolator: Interpolator

    // 图片加载类（图标或者图片加载返回上层构造）
    var photoLoader: (suspend (Any) -> Bitmap?)?

    // 插值器监听
    var animatorUpdateListener: AnimatorUpdateListener?

    // 每部分的样式构造
    fun partyChildBuild(child: IPartyChild.() -> Unit)

    // 构建
    fun build(finish: (() -> Unit)? = null)
}

/**
 * 每个item子布局view的绘制
 */
interface IPartyChild {
    // 每部分view样式1123

    var partyChild: (Int) -> View?

    // 中心view样式
    var centerChild: View?
}
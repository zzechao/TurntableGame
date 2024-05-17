package com.zhouz.turntablelib

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

    var numberPart: Int
    var mMinTimes: Int
    var mDurationTime: Long

    var photoLoader: (suspend (Any) -> Bitmap?)?

    fun partyChildBuild(childBuilder: IPartyChild.() -> Unit)

    fun build()
}


interface IPartyChild {
    var partyChild: (Int) -> View?

    var centerChild: View?
}
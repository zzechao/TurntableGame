package com.zhouz.turntablelib

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


/**
 * @author:zhouz
 * @date: 2024/5/16 16:46
 * description：转盘view
 */

private const val TAG = "TurntableView"

@SuppressLint("CustomViewStyleable")
class TurntableView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), ITurntableBuilder {


    private var anim: ObjectAnimator? = null
    private var center: Float = 0f
    private val defaultSize = 800

    private var mPaint: Paint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
        isDither = true
    }

    override var turntableBg: Int = 0
    private val bgIconData = IconData()

    override var turntableNeedleBg: Int = 0
    private val bgNeedleIconData = IconData()

    override var turntableNeedleIcon: Int = 0
    private val iconTurntableNeedle = IconData()

    override var numberPart: Int = 8
    private val partViews = mutableListOf<View>()
    private var mAngle = 0f


    override var mMinTimes: Int = 6
    override var mDurationTime: Long = 2000L


    private var centerView: View? = null


    override var photoLoader: (suspend (Any) -> Bitmap?)? = null

    private var currAngle = 0f

    private val childBuilder: IPartyChild = object : IPartyChild {
        override var partyChild: (Int) -> View? = {
            null
        }
        override var centerChild: View? = null
    }


    override fun partyChildBuild(child: IPartyChild.() -> Unit) {
        child.invoke(childBuilder)
    }

    private var viewScope: CoroutineScope? = CoroutineScope(SupervisorJob(getCurrentLifeCycleOwner()?.lifecycleScope?.coroutineContext?.job) + CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "throwable happen!", throwable)
    })

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.customTurntable)
            try {
                turntableBg = typedArray.getResourceId(R.styleable.customTurntable_CTBackground, 0)
            } finally {
                typedArray.recycle()
            }
        }
        setWillNotDraw(false)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> heightSize.coerceAtMost(widthSize)
            MeasureSpec.AT_MOST -> min(defaultSize, heightSize.coerceAtMost(widthSize))
            else -> defaultSize
        }

        center = width / 2f

        //MUST CALL THIS
        setMeasuredDimension(width, width)
    }


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        bgIconData.bitmap?.let {
            val src = RectF(0f, 0f, it.width.toFloat(), it.height.toFloat())
            val dst = RectF(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
            bgIconData.matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER)
            canvas?.drawBitmap(it, bgIconData.matrix, mPaint)
        }

        // 计算初始角度
        // 从最上面开始绘制扇形会好看一点
        var startAngle = -mAngle / 2 - 90
        val radius = measuredWidth / 2f - 105f
        for (i in 0 until numberPart) {
            mPaint.setColor(Color.WHITE)
            mPaint.alpha = 100
            mPaint.strokeWidth = 2f

            //画一个扇形
            val angleLine = Math.toRadians(startAngle.toDouble())
            val lineX: Float = center + (radius * cos(angleLine)).toFloat()
            val lineY: Float = center + (radius * sin(angleLine)).toFloat()
            canvas?.drawLine(center, center, lineX, lineY, mPaint)


            val angle = Math.toRadians((startAngle + mAngle / 2).toDouble())
            //确定图片在圆弧中 中心点的位置
            val x: Float = (center + (radius - 100) * cos(angle)).toFloat()
            val y: Float = (center + (radius - 100) * sin(angle)).toFloat()

            val partView = partViews.getOrNull(i)
            partView?.let {
                it.layout(
                    (x - it.measuredWidth / 2f).toInt(), (y - it.measuredHeight / 2f).toInt(), (x + it.measuredWidth / 2f).toInt(), (y + it.measuredHeight / 2f).toInt()
                )
            }

            startAngle += mAngle
        }

        mPaint.alpha = 255

        iconTurntableNeedle.bitmap?.let {
            val src = RectF(0f, 0f, it.width.toFloat(), it.height.toFloat())
            val dst = RectF(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
            iconTurntableNeedle.matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER)
            iconTurntableNeedle.matrix.postScale(0.12f, 0.12f)
            val left = (measuredWidth.toFloat() - it.width.toFloat()) / 2f
            val top = (measuredHeight.toFloat() - it.height.toFloat()) / 2.85f
            iconTurntableNeedle.matrix.postTranslate(left, top)
            canvas?.drawBitmap(it, iconTurntableNeedle.matrix, mPaint)
        }

        bgNeedleIconData.bitmap?.let {
            val src = RectF(0f, 0f, it.width.toFloat(), it.height.toFloat())
            val dst = RectF(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
            bgNeedleIconData.matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER)
            bgNeedleIconData.matrix.postScale(0.31f, 0.31f)
            val left = (measuredWidth.toFloat() - it.width.toFloat()) / 2f
            val top = (measuredHeight.toFloat() - it.height.toFloat()) / 2f
            bgNeedleIconData.matrix.postTranslate(left, top)
            canvas?.drawBitmap(it, bgNeedleIconData.matrix, mPaint)
        }

        centerView?.let {
            val left = (measuredWidth - it.measuredWidth) / 2
            val top = (measuredHeight - it.measuredHeight) / 2
            it.layout(left, top, left + it.measuredWidth, top + it.measuredHeight)
        }
    }

    fun setting(builder: ITurntableBuilder.() -> Unit) {
        Log.i(TAG, "setting")
        builder.invoke(this)
        build()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewScope = CoroutineScope(SupervisorJob(getCurrentLifeCycleOwner()?.lifecycleScope?.coroutineContext?.job))
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewScope?.cancel()
        viewScope = null
    }

    override fun build() {
        Log.i(TAG, "build viewScope:$viewScope")
        viewScope?.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                partViews.forEach {
                    removeView(it)
                }
                partViews.clear()
            }

            val bitmapBg = photoLoader?.invoke(turntableBg) ?: return@launch
            bgIconData.icon = turntableBg
            bgIconData.bitmap = bitmapBg

            val bitmap = photoLoader?.invoke(turntableNeedleBg) ?: return@launch
            bgNeedleIconData.icon = turntableNeedleBg
            bgNeedleIconData.bitmap = bitmap

            val needleBitmap = photoLoader?.invoke(turntableNeedleIcon) ?: return@launch
            iconTurntableNeedle.icon = turntableNeedleIcon
            iconTurntableNeedle.bitmap = needleBitmap

            //每一个扇形的角度
            mAngle = 360f / numberPart
            Log.i(TAG, "build bitmap:$bitmapBg")
            withContext(Dispatchers.Main) {
                for (i in 0 until numberPart) {
                    val partView = childBuilder.partyChild.invoke(i)
                    partView?.let {
                        val width = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.AT_MOST)
                        val height = MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.AT_MOST)
                        partView.measure(width, height)
                        addView(it)
                        partViews.add(partView)
                    }
                }

                if (centerView?.parent == null) {
                    centerView = childBuilder.centerChild
                    centerView?.let {
                        val width = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.AT_MOST)
                        val height = MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.AT_MOST)
                        it.measure(width, height)
                        addView(it)
                    }
                }

                invalidate()
            }
        }
    }

    fun start(pos: Int) {
        //最低圈数是mMinTimes圈
        anim?.cancel()
        val newAngle: Int = (360 * mMinTimes + (pos - 1) * mAngle + currAngle).toInt()
        //计算目前的角度划过的扇形份数
        val num: Int = ((newAngle - currAngle) / mAngle).toInt()
        anim = ObjectAnimator.ofFloat(this, "rotation", currAngle, newAngle.toFloat())
        currAngle = newAngle.toFloat()
        // 动画的持续时间，执行多久？
        anim?.setDuration(mDurationTime)
        anim?.addUpdateListener {   //将动画的过程态回调给调用者
        }
        val f = floatArrayOf(0f)
        anim?.interpolator = TimeInterpolator { t ->
            f[0] = (cos((t + 1) * Math.PI) / 2.0f).toFloat() + 0.5f
            f[0]
        }
        anim?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
            }
        })
        // 正式开始启动执行动画
        anim?.start()
    }

    private fun View.findFragmentOfGivenView(): Fragment? {
        try {
            return FragmentManager.findFragment(this)
        } catch (_: Throwable) {
        }
        return null
    }

    private fun View.getCurrentLifeCycleOwner(): LifecycleOwner? {
        return findFragmentOfGivenView() ?: getFragmentActivity()
    }

    private fun View.getFragmentActivity(): FragmentActivity? {
        var context = this.context
        while (context is ContextWrapper) {
            if (context is FragmentActivity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }
}
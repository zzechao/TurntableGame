package com.zhouz.turntablelib

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
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


    private var center: Float = 0f
    private val defaultSize = 800

    private var mPaint: Paint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
        isDither = true
    }

    private val colors = intArrayOf(0xFF6142C6.toInt(), 0xFF6D50CE.toInt())

    override var turntableBg: Int = 0
    private val bgIconData = IconData()

    override var turntableNeedleBg: Int = 0
    private val bgNeedleIconData = IconData()

    override var turntableNeedleIcon: Int = 0
    private val iconTurntableNeedle = IconData()

    override var numberPart: Int = 8
    private val partViews = mutableListOf<View>()
    private var mAngle = 0f

    private var centerView: View? = null


    override var photoLoader: (suspend (Any) -> Bitmap?)? = null


    private val childBuilder: IPartyChild = object : IPartyChild {
        override var partyChild: (Int) -> View? = {
            null
        }
        override var centerChild: View? = null
    }


    override fun partyChildBuild(child: IPartyChild.() -> Unit) {
        child.invoke(childBuilder)
    }

    private var viewScope: CoroutineScope? = CoroutineScope(SupervisorJob(getCurrentLifeCycleOwner()?.lifecycleScope?.coroutineContext?.job) +
            CoroutineExceptionHandler { _, throwable ->
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
            mPaint.setColor(colors[i % colors.size])
            //画一个扇形
            val rect = RectF(center - radius, center - radius - 10, center + radius, center + radius - 1)
            canvas?.drawArc(rect, startAngle, mAngle, true, mPaint)

            val angle = Math.toRadians((startAngle + mAngle / 2).toDouble())
            //确定图片在圆弧中 中心点的位置
            val x: Float = (width / 2f + (radius - 100) * cos(angle)).toFloat()
            val y: Float = (height / 2f + (radius - 100) * sin(angle)).toFloat()

            val partView = partViews.getOrNull(i)
            partView?.let {
                it.layout(
                    (x - it.measuredWidth / 2f).toInt(),
                    (y - it.measuredHeight / 2f).toInt(),
                    (x + it.measuredWidth / 2f).toInt(),
                    (y + it.measuredHeight / 2f).toInt()
                )
            }

            startAngle += mAngle
        }

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

                centerView = childBuilder.centerChild
                centerView?.let {
                    val width = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.AT_MOST)
                    val height = MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.AT_MOST)
                    it.measure(width, height)
                    addView(it)
                }

                invalidate()
            }
        }
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
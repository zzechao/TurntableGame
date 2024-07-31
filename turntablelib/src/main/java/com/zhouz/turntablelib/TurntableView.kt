package com.zhouz.turntablelib

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.FrameLayout
import androidx.core.graphics.withSave
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.base.animation.AnimView
import com.base.animation.item.BitmapDisplayItem
import com.base.animation.xml.AnimDecoder2
import com.base.animation.xml.AnimEncoder
import com.base.animation.xml.buildAnimNode
import com.base.animation.xml.node.coder.InterpolatorEnum
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


class TurntableView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var mAnimView: AnimView? = null
    private var anim: ValueAnimator? = null
    private var center: Float = 0f
    private val defaultSize by lazy { context.resources.getDimensionPixelOffset(R.dimen.turn_view_size) }

    private var isTurning = false

    private var mPaint: Paint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
        isDither = true
    }

    private val textPaint: TextPaint by lazy {
        TextPaint().apply {
            style = Paint.Style.FILL_AND_STROKE
            isAntiAlias = true
            isDither = true
            setColor(Color.BLACK)
            textSize = 40f
        }
    }

    // 每一部分的view
    private val partViews = mutableListOf<View>()

    // 每一部分的角度
    private var mAngle = 0f

    // 中间的view
    private var centerView: View? = null

    // 当前的角度，转动需要
    private var currAngle = 0f

    // 背景信息
    private val bgIconData = IconData()

    // 指针背景信息
    private val bgNeedleIconData = IconData()

    // 指针信息
    private val iconTurntableNeedle = IconData()

    // 分割线信息
    private val iconTurntableDividingLine = IconData()

    private val childBuilder: IPartyChild by lazy {
        object : IPartyChild {
            override var partyChild: (Int) -> View? = {
                null
            }
            override var centerChild: View? = null
        }
    }

    // 初始化部分信息
    private val turntableBuilder: ITurntableBuilder by lazy {
        object : ITurntableBuilder {
            override var turntableBg: Int = 0
            override var turntableNeedleBg: Int = 0

            override var turntableNeedleIcon: Int = 0
            override var turntableNeedleTop: Int = context.resources.getDimensionPixelOffset(R.dimen.turn_needle_top)
            override var numberPart: Int = 8
            override var mMinTimes: Int = 6
            override var mDurationMillisecond: Long = 2000L
            override var startAngle: Float = 0f

            override var dividingLineColor: Int = 0x88FFFFFF.toInt()
            override var dividingLineIcon: Int = 0
            override var dividingLineSize: Float = 2f
            override var dividingLineWidth: Float = context.resources.getDimension(R.dimen.turn_dividing_width)
            override var dividingNumberShow: Boolean = true

            override var interpolator: Interpolator = AccelerateDecelerateInterpolator()

            override var photoLoader: (suspend (Any) -> Bitmap?)? = null
            override var animatorUpdateListener: ValueAnimator.AnimatorUpdateListener? = null

            override fun partyChildBuild(child: IPartyChild.() -> Unit) {
                child.invoke(childBuilder)
            }

            // 开始构建需要view 和属性信息
            override fun build(finish: (() -> Unit)?) {
                Log.i(TAG, "build viewScope:$viewScope")
                viewScope?.launch(Dispatchers.Main) {
                    partViews.forEach {
                        removeView(it)
                    }
                    partViews.clear()

                    withContext(Dispatchers.IO) {
                        photoLoader?.invoke(turntableBg)?.let {
                            bgIconData.icon = turntableBg
                            bgIconData.bitmap = it
                        } ?: kotlin.run {
                            bgIconData.icon = 0
                            bgIconData.bitmap = null
                        }


                        photoLoader?.invoke(turntableNeedleBg)?.let {
                            bgNeedleIconData.icon = turntableNeedleBg
                            bgNeedleIconData.bitmap = it
                        } ?: kotlin.run {
                            bgNeedleIconData.icon = 0
                            bgNeedleIconData.bitmap = null
                        }


                        photoLoader?.invoke(turntableNeedleIcon)?.let {
                            iconTurntableNeedle.icon = turntableNeedleIcon
                            iconTurntableNeedle.bitmap = it
                        } ?: kotlin.run {
                            iconTurntableNeedle.icon = 0
                            iconTurntableNeedle.bitmap = null
                        }

                        photoLoader?.invoke(dividingLineIcon)?.let {
                            iconTurntableDividingLine.icon = dividingLineIcon
                            iconTurntableDividingLine.bitmap = it
                            iconTurntableDividingLine.initShader()
                        } ?: kotlin.run {
                            iconTurntableDividingLine.icon = 0
                            iconTurntableDividingLine.bitmap = null
                        }
                    }

                    // 每一个扇形的角度
                    withContext(Dispatchers.Main) {
                        mAngle = 360f / numberPart // 计算出那个部分所占角度
                        currAngle = 0f
                        Log.i(TAG, "build mAngle:$mAngle numberPart:$numberPart")
                        for (i in 0 until numberPart) {
                            val partView = childBuilder.partyChild.invoke(i)
                            partView?.let {
                                val width = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.AT_MOST)
                                val height = MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.AT_MOST)
                                partView.measure(width, height)
                                partViews.add(partView)
                                if (partView.parent == null) {
                                    addView(partView, 0)
                                    DrawListener(partView).listener()
                                }
                            }
                        }

                        if (centerView?.parent == null) {
                            centerView = childBuilder.centerChild
                            centerView?.let {
                                val width = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.AT_MOST)
                                val height = MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.AT_MOST)
                                it.measure(width, height)
                                addView(it, 0)
                            }
                        }

                        invalidate()

                        finish?.invoke()
                    }
                }
            }
        }
    }


    private var viewScope: CoroutineScope? = CoroutineScope(SupervisorJob(getCurrentLifeCycleOwner()?.lifecycleScope?.coroutineContext?.job) + CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "throwable happen!", throwable)
    })

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.customTurntable)
            try {
                //turntableBg = typedArray.getResourceId(R.styleable.customTurntable_CTBackground, 0)
            } finally {
                typedArray.recycle()
            }
        }
        setWillNotDraw(false)
    }

    init {
        mAnimView = AnimView(context)
        addView(mAnimView)
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

        mAnimView?.measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        )

        center = width / 2f

        //MUST CALL THIS
        setMeasuredDimension(width, width)
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val scale = measuredWidth.toFloat() / defaultSize
        bgIconData.bitmap?.let {
            bgIconData.setMatrix(this, scale)
            bgIconData.matrix.postRotate(currAngle, center, center)
            canvas?.drawBitmap(it, bgIconData.matrix, mPaint)
        }

        // 计算初始角度
        // 从最上面开始绘制扇形会好看一点
        var startAngle = -turntableBuilder.startAngle
        val radius = turntableBuilder.dividingLineWidth
        startAngle += currAngle
        for (i in 0 until turntableBuilder.numberPart) {
            mPaint.strokeWidth = turntableBuilder.dividingLineSize

            //画一个扇形
            iconTurntableDividingLine.bitmapShader?.let {
                mPaint.shader = iconTurntableDividingLine.bitmapShader
            } ?: kotlin.run {
                mPaint.color = turntableBuilder.dividingLineColor
            }

            canvas?.withSave {
                this.translate(center, center)
                this.rotate(startAngle - 180f)
                canvas.drawLine(0f, 0f, 0f, radius * scale, mPaint)
                if (turntableBuilder.dividingNumberShow) {
                    textPaint.textSize *= scale
                    canvas.drawText("$i", 0f, radius * scale, textPaint)
                }
                mPaint.shader = null
            }


            val viewAngle = startAngle + mAngle / 2 - 90f
            val angle = Math.toRadians((viewAngle).toDouble())

            //确定图片在圆弧中 中心点的位置
            val x: Float = (center + scale * (radius - context.resources.getDimension(R.dimen.turn_seat_radius_width)) * cos(angle)).toFloat()
            val y: Float = (center + scale * (radius - context.resources.getDimension(R.dimen.turn_seat_radius_width)) * sin(angle)).toFloat()
            val partView = partViews.getOrNull(i)
            partView?.let {
                it.scaleX = scale
                it.scaleY = scale
                it.layout(
                    (x - it.measuredWidth / 2f).toInt(), (y - it.measuredHeight / 2f).toInt(),
                    (x + it.measuredWidth / 2f).toInt(), (y + it.measuredHeight / 2f).toInt()
                )
            }

            startAngle += mAngle
        }

        mPaint.alpha = 255

        iconTurntableNeedle.bitmap?.let {
            iconTurntableNeedle.setMatrix(this, scale) {
                iconTurntableNeedle.matrix.postTranslate(0f, -turntableBuilder.turntableNeedleTop * scale)
            }
            canvas?.drawBitmap(it, iconTurntableNeedle.matrix, mPaint)
        }

        bgNeedleIconData.bitmap?.let {
            bgNeedleIconData.setMatrix(this, scale)
            canvas?.drawBitmap(it, bgNeedleIconData.matrix, mPaint)
        }

        centerView?.let {
            it.scaleX = scale
            it.scaleY = scale
            val left = (measuredWidth - it.measuredWidth) / 2
            val top = (measuredHeight - it.measuredHeight) / 2
            it.layout(left, top, left + it.measuredWidth, top + it.measuredHeight)
        }
    }

    /**
     * 设置转盘属性信息
     */
    fun setting(
        finish: (() -> Unit)? = null,
        builder: ITurntableBuilder.() -> Unit
    ) {
        builder.invoke(turntableBuilder)
        turntableBuilder.build(finish)
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

    /**
     * 取消转动
     */
    fun cancelTurn() {
        anim?.cancel()
    }

    /**
     * 开始转动
     * @param pos 转动位置
     * @param onStart 开始回调
     * @param onEnd 结束回调
     * @param onCancel 取消回调
     */
    fun startTurn(
        pos: Int,
        onStart: (() -> Unit)? = null,
        onEnd: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null
    ) {
        if (isTurning) return
        isTurning = true
        //最低圈数是mMinTimes圈
        currAngle = 0f
        val newAngle: Int = (360f * turntableBuilder.mMinTimes +
                (turntableBuilder.numberPart - pos) * mAngle - mAngle / 2f +
                currAngle).toInt()

        Log.i(TAG, "startTurn pos:$pos newAngle:${newAngle} mAngle:$mAngle ")

        //计算目前的角度划过的扇形份数
        anim?.cancel()
        anim = ValueAnimator.ofFloat(currAngle, newAngle.toFloat())

        // 动画的持续时间，执行多久？
        anim?.setDuration(turntableBuilder.mDurationMillisecond)
        anim?.addUpdateListener {   //将动画的过程态回调给调用者
            currAngle = it.animatedValue as Float
            turntableBuilder.animatorUpdateListener?.onAnimationUpdate(it)
            invalidate()
        }
        anim?.interpolator = turntableBuilder.interpolator
        anim?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                onStart?.invoke()
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                isTurning = false
                onEnd?.invoke()
            }

            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
                isTurning = false
                onCancel?.invoke()
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

    /**
     * 获取对应位置view信息
     */
    fun getPartyView(index: Int): View? {
        return partViews.getOrNull(index)
    }

    /**
     * 送礼动画
     * @param start 开始坐标
     * @param end 结束坐标
     * @param url 链接
     * @param duringMillisecond 动画时长
     * @param displayHeightSize 高度
     * @param callback 构建bitmap
     */
    fun showAnim(
        start: PointF, end: PointF, url: String, duringMillisecond: Long = 200,
        displayHeightSize: Int = 80, callback: suspend () -> Bitmap
    ) {
        val scale = measuredWidth.toFloat() / defaultSize
        AnimEncoder().buildAnimNode {
            imageNode {
                this.url = url
                this.displayHeightSize = displayHeightSize
                startNode {
                    point = start
                    scaleX = scale
                    scaleY = scale
                    alpha = 255
                    endNode {
                        point = end
                        scaleX = scale
                        scaleY = scale
                        alpha = 30
                        durTime = duringMillisecond
                        interpolator = InterpolatorEnum.Linear.type
                    }
                }
            }
        }.apply {
            viewScope?.launch(Dispatchers.IO) {
                val animView = mAnimView ?: return@launch
                AnimDecoder2.suspendPlayAnimWithAnimNode(animView, this@apply) { _, displayItem ->
                    when (displayItem) {
                        is BitmapDisplayItem -> {
                            displayItem.mBitmap = callback.invoke()
                        }
                    }
                    displayItem
                }
            }
        }
    }

    /**
     * 等待绘制前显示
     */
    inner class DrawListener(private val view: View) : ViewTreeObserver.OnPreDrawListener, OnAttachStateChangeListener {
        private var isAdd = false

        init {
            view.addOnAttachStateChangeListener(this)
        }

        fun listener() {
            if (!isAdd) {
                isAdd = true
                view.viewTreeObserver.addOnPreDrawListener(this)
            }
        }

        override fun onViewAttachedToWindow(v: View) {
        }

        override fun onViewDetachedFromWindow(v: View) {
            if (isAdd) {
                view.viewTreeObserver.removeOnPreDrawListener(this)
            }
        }

        override fun onPreDraw(): Boolean {
            if ((view.left == 0 && view.top == 0) ||
                (view.right == 0 && view.top == 0)
            ) {
                view.visibility = View.INVISIBLE
            } else {
                view.visibility = View.VISIBLE
            }
            return true
        }
    }
}
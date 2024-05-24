package com.zhouz.turntablegame

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.base.animation.AnimationEx
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.bumptech.glide.integration.webp.decoder.WebpDrawableTransformation
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.tencent.qgame.animplayer.util.ALog
import com.tencent.qgame.animplayer.util.IALog
import com.zhouz.turntablelib.TurntableView
import okio.Buffer
import okio.buffer
import okio.source
import java.io.InputStream
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    @SuppressLint("InflateParams", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AnimationEx.init(this.application, 200, 2)
        setContentView(R.layout.activity_main)
        var userSize = 0
        var giftSize = 0

        val userViews = mutableListOf<View>()
        var position = 0

        ALog.log = object : IALog {
            override fun i(tag: String, msg: String) {
                super.i(tag, msg)
                Log.i(TAG, msg)
            }

            override fun d(tag: String, msg: String) {
                super.d(tag, msg)
                Log.d(TAG, msg)
            }

            override fun e(tag: String, msg: String) {
                super.e(tag, msg)
                Log.e(TAG, msg)
            }

            override fun e(tag: String, msg: String, tr: Throwable) {
                super.e(tag, msg, tr)
                Log.e(TAG, msg, tr)
            }
        }

        val imageView = findViewById<ImageView>(R.id.icon)
        //imageView.setBackgroundColor(Color.WHITE)
        val circleCrop: Transformation<Bitmap> = CircleCrop()
        Glide.with(imageView)
            .load(R.mipmap.bg01)
            .optionalTransform(circleCrop)
            .optionalTransform(WebpDrawable::class.java, WebpDrawableTransformation(circleCrop))
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    (resource as WebpDrawable).apply { loopCount = Int.MAX_VALUE }
                    return false
                }
            }).into(object : CustomViewTarget<ImageView, Drawable>(imageView) {
                override fun onLoadFailed(errorDrawable: Drawable?) {
                }

                override fun onResourceCleared(placeholder: Drawable?) {
                }

                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    imageView.setImageDrawable(resource)
                    (resource as WebpDrawable).start()

                    (resource as WebpDrawable).stop()
                    Log.i(TAG, "${resource.frameIndex}")
                }
            })
        //(imageView.drawable as WebpDrawable).stop()

        //val vap = AnimView(this)

        val turn = findViewById<TurntableView>(R.id.view)
        turn.setting {
            photoLoader = object : (suspend (Any) -> Bitmap?) {
                override suspend fun invoke(p1: Any): Bitmap? {
                    return if (p1 is Int) {
                        BitmapFactory.decodeResource(this@MainActivity.resources, p1)
                    } else {
                        null
                    }
                }
            }

            animatorUpdateListener = ValueAnimator.AnimatorUpdateListener {
                //imageView.rotation = it.animatedValue as Float
            }

            //turntableBg = R.mipmap.bg03
            turntableNeedleBg = R.mipmap.bg02
            turntableNeedleIcon = R.mipmap.jiantou
            numberPart = 8

            partyChildBuild {
                partyChild = { index ->
                    userViews.getOrNull(index) ?: LayoutInflater.from(this@MainActivity).inflate(R.layout.layout_part_view, null).apply {
                        Log.i("zzc", "setting index:$index this:$this")
                        this.findViewById<TextView>(R.id.index).text = "$index"
                        this.setOnClickListener {
                            position = index
                            Log.i("zzc", "click index:$index it:$it")
                            it ?: return@setOnClickListener
                            userViews.add(it)
                            userSize++
                            this.findViewById<ImageView>(R.id.head).setImageResource(R.mipmap.head)
                            //Toast.makeText(this@MainActivity, "index:$index", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                centerChild = LayoutInflater.from(this@MainActivity).inflate(R.layout.layout_center_part, null).apply {
                    this.findViewById<TextView>(R.id.size).text = "x$giftSize"
                    this.setOnClickListener {
                        this.findViewById<ImageView>(R.id.gift).setImageResource(R.mipmap.head)
                        giftSize++
                        this.findViewById<TextView>(R.id.size).text = "x$giftSize"

                        val start = turn.getPartyView(position)
                        val end = it
                        val startwidth = start?.measuredWidth ?: 0
                        val startheight = start?.measuredHeight ?: 0
                        val starttop = start?.top ?: 0
                        val startleft = start?.left ?: 0

                        val endwidth = end?.measuredWidth ?: 0
                        val endheight = end?.measuredHeight ?: 0
                        val endtop = end?.top ?: 0
                        val endleft = end?.left ?: 0

                        turn.showAnim(
                            PointF(startleft + startwidth / 2f, starttop + startheight / 2f),
                            PointF(endleft + endwidth / 2f, endtop + endheight / 2f), "${R.mipmap.head}"
                        ) {
                            BitmapFactory.decodeResource(this@MainActivity.resources, R.mipmap.head)
                        }
                    }
                }
            }
        }

        findViewById<Button>(R.id.mStart).setOnClickListener {
            if (userSize > 1) {
                turn.setting({
                    position = 0
                    val index = Random.nextInt(userSize)
                    Toast.makeText(this@MainActivity, "position:$index", Toast.LENGTH_LONG).show()
                    turn.startTurn(index, onStart = {
                        (imageView.drawable as? WebpDrawable)?.start()
                    }, onEnd = {
                        (imageView.drawable as? WebpDrawable)?.apply {
                            stop()
                            Log.i(TAG, "${this.frameIndex}")
                        }
                    }, onCancel = {
                        (imageView.drawable as? WebpDrawable)?.stop()
                    })
                }) {
                    numberPart = userSize
                }
            }
        }
    }

    private fun InputStream.okioReadByteArray(): ByteArray {
        val buffer = ByteArray(1024)
        var len = 0
        try {
            this.source().buffer().use { source ->
                Buffer().use {
                    while (len != -1) {
                        len = source.read(buffer)
                        it.write(buffer)
                    }
                    return it.readByteArray()
                }
            }
        } catch (ignore: Throwable) {
        }
        return ByteArray(0)
    }
}

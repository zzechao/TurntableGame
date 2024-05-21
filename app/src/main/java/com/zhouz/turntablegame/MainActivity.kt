package com.zhouz.turntablegame

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
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
import com.zhouz.turntablelib.TurntableView
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
            turntableBg = R.mipmap.bg01
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
                            PointF(endleft + endwidth / 2f, endtop + endheight / 2f)
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
                    turn.startTurn(index)
                }) {
                    numberPart = userSize
                }
            }
        }
    }
}

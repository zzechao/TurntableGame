package com.zhouz.turntablegame

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zhouz.turntablelib.TurntableView


class MainActivity : AppCompatActivity() {

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TurntableView>(R.id.view).setting {
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


            var size = 0

            partyChildBuild {
                partyChild = { index ->
                    LayoutInflater.from(this@MainActivity).inflate(R.layout.layout_part_view, null).apply {
                        this.setOnClickListener {
                            this.findViewById<ImageView>(R.id.head).setImageResource(R.mipmap.head)
                            Toast.makeText(this@MainActivity, "index:$index", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                centerChild = LayoutInflater.from(this@MainActivity).inflate(R.layout.layout_center_part, null).apply {
                    this.setOnClickListener {
                        this.findViewById<ImageView>(R.id.gift).setImageResource(R.mipmap.head)
                        size++
                        this.findViewById<TextView>(R.id.size).text = "x$size"
                    }
                }
            }
        }
    }
}

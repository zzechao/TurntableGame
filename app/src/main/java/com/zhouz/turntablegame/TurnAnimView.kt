package com.zhouz.turntablegame

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import com.tencent.qgame.animplayer.AnimConfig
import com.tencent.qgame.animplayer.AnimView
import com.tencent.qgame.animplayer.file.StreamContainer
import com.tencent.qgame.animplayer.inter.IAnimListener
import okio.Buffer
import okio.buffer
import okio.source
import java.io.InputStream


/**
 * @author:zhouz
 * @date: 2024/5/22 14:15
 * description：TODO
 */

const val TAG = "TurnAnimView"

class TurnAnimView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AnimView(context, attrs) {

    init {
        setAnimListener(object : IAnimListener {
            override fun onFailed(errorType: Int, errorMsg: String?) {
                Log.i(TAG, "onFailed errorType:$errorType errorMsg:$errorMsg")
            }

            override fun onVideoComplete() {
                Log.i(TAG, "onVideoComplete")
            }

            override fun onVideoDestroy() {
                Log.i(TAG, "onVideoDestroy")
            }

            override fun onVideoRender(frameIndex: Int, config: AnimConfig?) {
                Log.i(TAG, "onVideoRender frameIndex:$frameIndex ")
            }

            override fun onVideoStart() {
                Log.i(TAG, "onVideoStart")
            }
        })
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.i(TAG, "onSizeChanged w:$w h:$h oldw:$oldw oldh:$oldh")
        Handler(Looper.getMainLooper()).post {
            setFps(60)
            val manager = context.resources.assets ?: return@post
            val endBuffer = manager.open("turnbg.mp4").okioReadByteArray()
            setLoop(Int.MAX_VALUE)
            startPlay(StreamContainer(endBuffer))
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
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
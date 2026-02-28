package org.crazyromteam.qmgstore.ui.qmgpreview

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color.BLACK
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.util.Log
import android.view.Surface
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import org.crazyromteam.qmgstore.qmg.DecodeQmg
import org.crazyromteam.qmgstore.qmg.utils.Color
import org.crazyromteam.qmgstore.qmg.utils.QmgHeader
import java.nio.ByteBuffer

class QmgPreviewViewModel : ViewModel() {
    private var job: Job? = null

    fun startBootAnimation(surface: Surface, introData: ByteArray, loopData: ByteArray) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.Default) {
            // 1. Play Intro
            if (introData.isNotEmpty()) {
                val header = QmgHeader(introData)
                if (header.isValid) {
                    playQmg(introData, header, surface, repeat = false)
                }
            }

            // 2. Play Loop
            if (isActive && loopData.isNotEmpty()) {
                val header = QmgHeader(loopData)
                if (header.isValid) {
                    playQmg(loopData, header, surface, repeat = true)
                }
            }
        }
    }

    private suspend fun playQmg(data: ByteArray, header: QmgHeader, surface: Surface, repeat: Boolean) {
        val decoder = DecodeQmg(data, header.width, header.height, header.frames, header.color)
        val bitmap = createBitmap(header.width, header.height, Bitmap.Config.ARGB_8888)
        val dstRect = Rect(0, 0, header.width, header.height)
        val paint = Paint()

        try {
            do {
                decoder.reset()
                for (i in 0 until header.frames) {
                    if (!currentCoroutineContext().isActive) break

                    val raw = decoder.nextFrame() ?: break
                    bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(raw))

                    val canvas: Canvas? = try {
                        surface.lockCanvas(null)
                    } catch (e: Exception) {
                        Log.e("QMG_Canvas", "Error locking canvas: ", e)
                        break
                    }

                    if (canvas != null) {
                        try {
                            canvas.drawColor(BLACK, PorterDuff.Mode.SRC)
                            canvas.drawBitmap(bitmap, null, dstRect, paint)
                        } finally {
                            surface.unlockCanvasAndPost(canvas)
                        }
                    }
                    delay(33L) // 30 FPS
                }
            } while (currentCoroutineContext().isActive && (repeat || header.repeat))
        } finally {
            decoder.release()
        }
    }

    fun startQmg(
        qmgData: ByteArray,
        width: Int,
        height: Int,
        frames: Int,
        duration: Int,
        repeat: Boolean,
        color: Color,
        surface: Surface
    ) {
        if (qmgData.isEmpty()) return
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.Default) {
            // Create a pseudo header to reuse playQmg
            val header = QmgHeader(qmgData)
            playQmg(qmgData, header, surface, repeat)
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}

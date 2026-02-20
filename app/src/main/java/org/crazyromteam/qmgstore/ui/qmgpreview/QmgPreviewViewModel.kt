package org.crazyromteam.qmgstore.ui.qmgpreview

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.util.Log
import android.view.Surface
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.crazyromteam.qmgstore.qmg.DecodeQmg
import java.nio.ByteBuffer

class QmgPreviewViewModel : ViewModel() {
    private var decoder: DecodeQmg? = null
    private var job: Job? = null

    fun startQmg(
        qmgData: ByteArray,
        width: Int,
        height: Int,
        frames: Int,
        color: org.crazyromteam.qmgstore.qmg.utils.Color,
        surface: Surface
    ) {
        if (qmgData.isEmpty()) return

        decoder = DecodeQmg(qmgData, width, height, frames, color)

        Log.d(
            "QMG_Start",
            "started decoding qmg: width=$width, height=$height, frames=$frames, color=$color"
        )

        job?.cancel()
        job = viewModelScope.launch(Dispatchers.Default) {
            val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val dstRect = Rect(0, 0, width, height)
            val paint = Paint()

            while (isActive) {
                val raw = decoder?.nextFrame() ?: break
                bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(raw))

                val canvas: Canvas? = try {
                    surface.lockCanvas(null)
                } catch (e: Exception) {
                    Log.e("QMG_Canvas", "Error locking canvas: ", e)
                    break
                }

                if (canvas != null) {
                    try {
                        // Clear the canvas before drawing the new frame
                        canvas.drawColor(Color.BLACK, PorterDuff.Mode.SRC)
                        canvas.drawBitmap(bitmap, null, dstRect, paint)
                    } finally {
                        surface.unlockCanvasAndPost(canvas)
                    }
                }

                delay(33) // ~30 FPS
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
        decoder?.release()
        decoder = null
    }
}
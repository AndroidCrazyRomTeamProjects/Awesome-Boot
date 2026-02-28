package org.crazyromteam.qmgstore.qmg

import android.util.Log
import org.crazyromteam.qmgstore.qmg.utils.Color
import org.crazyromteam.qmgstore.qmg.utils.rgb565ToArgb8888
import org.crazyromteam.qmgstore.qmg.utils.splitAlpha

class DecodeQmg(
    private val qmgData: ByteArray?,
    private val width: Int,
    private val height: Int,
    private val frames: Int,
    private val color: Color
) {
    private val outBuf = ByteArray(width * height * 4)
    private var aniPtr: Long = 0L
    private var curFrame = 0

    init {
        aniPtr = LibQmg.CreateAniInfo(qmgData, 1).also {
            Log.d("QMG_DEBUG", "CreateAniInfo returned pointer: $it")
        }
    }

    fun reset() {
        curFrame = 0
        if (aniPtr != 0L) {
            LibQmg.DestroyAniInfo(aniPtr)
        }
        aniPtr = LibQmg.CreateAniInfo(qmgData, 1)
    }

    fun nextFrame(): ByteArray? {
        if (curFrame >= frames || aniPtr == 0L) return null
        curFrame++
        LibQmg.DecodeAniFrame(aniPtr, outBuf)

        val pixelCount = width * height

        return when (color) {
            Color.RGB565 -> rgb565ToArgb8888(outBuf)

            Color.RGB888 -> {
                val out = ByteArray(pixelCount * 4)
                var src = 0
                var dst = 0
                repeat(pixelCount) {
                    out[dst++] = outBuf[src++] // R
                    out[dst++] = outBuf[src++] // G
                    out[dst++] = outBuf[src++] // B
                    out[dst++] = 0xFF.toByte() // A
                }
                out
            }

            Color.BGR888 -> {
                val out = ByteArray(pixelCount * 4)
                var src = 0
                var dst = 0
                repeat(pixelCount) {
                    out[dst++] = outBuf[src + 2] // R
                    out[dst++] = outBuf[src + 1] // G
                    out[dst++] = outBuf[src]     // B
                    out[dst++] = 0xFF.toByte()   // A
                    src += 3
                }
                out
            }

            Color.RGB5658 -> {
                val (rgb, alpha) = splitAlpha(outBuf, pixelCount, 3, false)
                rgb565ToArgb8888(rgb, alpha)
            }

            Color.RGB8565 -> {
                val (rgb, alpha) = splitAlpha(outBuf, pixelCount, 3, true)
                rgb565ToArgb8888(rgb, alpha)
            }

            Color.ARGB8888 -> {
                val out = ByteArray(pixelCount * 4)
                var i = 0
                repeat(pixelCount) {
                    out[i + 0] = outBuf[i + 1] // R
                    out[i + 1] = outBuf[i + 2] // G
                    out[i + 2] = outBuf[i + 3] // B
                    out[i + 3] = outBuf[i + 0] // A
                    i += 4
                }
                out
            }

            Color.RGBA8888 -> {
                outBuf.copyOf(pixelCount * 4)
            }

            Color.BGRA8888 -> {
                val out = ByteArray(pixelCount * 4)
                var i = 0
                repeat(pixelCount) {
                    out[i + 0] = outBuf[i + 2] // R
                    out[i + 1] = outBuf[i + 1] // G
                    out[i + 2] = outBuf[i + 0] // B
                    out[i + 3] = outBuf[i + 3] // A
                    i += 4
                }
                out
            }

            else -> null
        }
    }

    fun release() {
        if (aniPtr != 0L) {
            LibQmg.DestroyAniInfo(aniPtr)
            aniPtr = 0L
        }
    }
}

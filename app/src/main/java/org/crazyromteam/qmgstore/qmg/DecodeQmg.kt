package org.crazyromteam.qmgstore.qmg

import android.util.Log
import org.crazyromteam.qmgstore.qmg.utils.Color


class DecodeQmg(
    val qmgData: ByteArray?,
    val width: Int,
    val height: Int,
    val frames: Int,
    val bppType: Int
) {
    val color = Color()

    // A large buffer to receive data from the native decoder.
    private val outBuf = ByteArray(width * height * 4)

    private val aniPtr: Long = LibQmg.CreateAniInfo(qmgData, 1).also {
        Log.d("QMG_DEBUG", "CreateAniInfo returned pointer: $it")
    }

    private var curFrame = 0

    fun nextFrame(): ByteArray? {
        if (curFrame >= frames || aniPtr == 0L) return null
        curFrame++

        LibQmg.DecodeAniFrame(aniPtr, outBuf)

        val pixelCount = width * height

        return when (bppType) {

            // RGB565
            0 -> {
                color.rgb565ToArgb8888(outBuf)
            }

            // RGB888
            1 -> {
                val out = ByteArray(pixelCount * 4)
                var src = 0
                var dst = 0
                repeat(pixelCount) {
                    out[dst++] = 0xFF.toByte()
                    out[dst++] = outBuf[src++]
                    out[dst++] = outBuf[src++]
                    out[dst++] = outBuf[src++]
                }
                out
            }

            // BGR888
            2 -> {
                val out = ByteArray(pixelCount * 4)
                var src = 0
                var dst = 0
                repeat(pixelCount) {
                    out[dst++] = 0xFF.toByte()
                    out[dst++] = outBuf[src + 2]
                    out[dst++] = outBuf[src + 1]
                    out[dst++] = outBuf[src]
                    src += 3
                }
                out
            }

            // RGB565 + Alpha (5658)
            3 -> {
                val (rgb, alpha) =
                    color.splitAlphaRGB5658(outBuf, pixelCount)
                color.rgb565ToArgb8888(rgb, alpha)
            }

            // Alpha + RGB565 (8565)
            4 -> {
                val (rgb, alpha) =
                    color.splitAlphaRGB8565(outBuf, pixelCount)
                color.rgb565ToArgb8888(rgb, alpha)
            }

            // ARGB8888
            5 -> {
                outBuf.copyOf(pixelCount * 4)
            }

            // RGBA8888
            6 -> {
                val out = ByteArray(pixelCount * 4)
                var i = 0
                repeat(pixelCount) {
                    out[i + 0] = outBuf[i + 3] // A
                    out[i + 1] = outBuf[i + 0] // R
                    out[i + 2] = outBuf[i + 1] // G
                    out[i + 3] = outBuf[i + 2] // B
                    i += 4
                }
                out
            }

            // BGRA8888
            7 -> {
                val out = ByteArray(pixelCount * 4)
                var i = 0
                repeat(pixelCount) {
                    out[i + 0] = outBuf[i + 3]
                    out[i + 1] = outBuf[i + 2]
                    out[i + 2] = outBuf[i + 1]
                    out[i + 3] = outBuf[i + 0]
                    i += 4
                }
                out
            }

            else -> {
                null
            }
        }
    }


    fun release() {
        if (aniPtr != 0L) {
            LibQmg.DestroyAniInfo(aniPtr)
        }
    }
}





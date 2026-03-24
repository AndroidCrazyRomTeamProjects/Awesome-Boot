package org.crazyromteam.qmgstore.qmg

import android.util.Log
import org.crazyromteam.qmgstore.qmg.utils.Color
import org.crazyromteam.qmgstore.qmg.utils.rgb565AlphaInterleavedToArgb8888
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
    private val reusableOut = ByteArray(width * height * 4)
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
            Color.RGB565 -> rgb565ToArgb8888(outBuf, null, reusableOut, pixelCount)

            Color.RGB888 -> {
                val out = reusableOut
                var src = 0
                var dst = 0

                // ⚡ Bolt Optimization: Extracted array accesses into local variables and assigned values
                // via index offsets rather than repetitive sequential increments. Helps the JIT compiler with
                // Bounds Check Elimination (BCE) and better instruction pipelining in highly iterated paths.
                repeat(pixelCount) {
                    val r = outBuf[src]
                    val g = outBuf[src + 1]
                    val b = outBuf[src + 2]

                    out[dst] = r // R
                    out[dst + 1] = g // G
                    out[dst + 2] = b // B
                    out[dst + 3] = 0xFF.toByte() // A

                    dst += 4
                    src += 3
                }
                out
            }

            Color.BGR888 -> {
                val out = reusableOut
                var src = 0
                var dst = 0

                // ⚡ Bolt Optimization: Extracted array accesses into local variables and assigned values
                // via index offsets rather than repetitive sequential increments. Helps the JIT compiler with
                // Bounds Check Elimination (BCE) and better instruction pipelining in highly iterated paths.
                repeat(pixelCount) {
                    val b = outBuf[src]
                    val g = outBuf[src + 1]
                    val r = outBuf[src + 2]

                    out[dst] = r // R
                    out[dst + 1] = g // G
                    out[dst + 2] = b // B
                    out[dst + 3] = 0xFF.toByte() // A

                    dst += 4
                    src += 3
                }
                out
            }

            Color.RGB5658 -> {
                rgb565AlphaInterleavedToArgb8888(outBuf, pixelCount, false, reusableOut)
            }

            Color.RGB8565 -> {
                rgb565AlphaInterleavedToArgb8888(outBuf, pixelCount, true, reusableOut)
            }

            Color.ARGB8888 -> {
                val out = reusableOut
                var src = 0
                var dst = 0

                // ⚡ Bolt Optimization: Extracted array accesses into local variables and assigned values
                // via index offsets rather than repetitive sequential increments. Helps the JIT compiler with
                // Bounds Check Elimination (BCE) and better instruction pipelining in highly iterated paths.
                repeat(pixelCount) {
                    val a = outBuf[src]
                    val r = outBuf[src + 1]
                    val g = outBuf[src + 2]
                    val b = outBuf[src + 3]

                    out[dst] = r // R
                    out[dst + 1] = g // G
                    out[dst + 2] = b // B
                    out[dst + 3] = a // A

                    src += 4
                    dst += 4
                }
                out
            }

            Color.RGBA8888 -> {
                System.arraycopy(outBuf, 0, reusableOut, 0, pixelCount * 4)
                reusableOut
            }

            Color.BGRA8888 -> {
                val out = reusableOut
                var src = 0
                var dst = 0

                // ⚡ Bolt Optimization: Extracted array accesses into local variables and assigned values
                // via index offsets rather than repetitive sequential increments. Helps the JIT compiler with
                // Bounds Check Elimination (BCE) and better instruction pipelining in highly iterated paths.
                repeat(pixelCount) {
                    val b = outBuf[src]
                    val g = outBuf[src + 1]
                    val r = outBuf[src + 2]
                    val a = outBuf[src + 3]

                    out[dst] = r // R
                    out[dst + 1] = g // G
                    out[dst + 2] = b // B
                    out[dst + 3] = a // A

                    src += 4
                    dst += 4
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

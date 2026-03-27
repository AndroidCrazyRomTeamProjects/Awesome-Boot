package org.crazyromteam.qmgstore.qmg

import org.crazyromteam.qmgstore.qmg.utils.Color
import org.crazyromteam.qmgstore.qmg.utils.rgb565ToArgb8888
import org.crazyromteam.qmgstore.qmg.utils.splitAlpha
import org.junit.Test
import kotlin.system.measureTimeMillis

class DecodeQmgBenchmark {

    class MockDecodeQmg(
        private val width: Int,
        private val height: Int,
        private val frames: Int,
        private val color: Color
    ) {
        private val outBuf = ByteArray(width * height * 4) { it.toByte() }
        private val reusableOut = ByteArray(width * height * 4)
        private var splitAlphaPixels: ByteArray? = null
        private var splitAlphaValues: ByteArray? = null
        private var curFrame = 0

        fun nextFrame(): ByteArray? {
            if (curFrame >= frames) return null
            curFrame++

            val pixelCount = width * height

            return when (color) {
                Color.RGB565 -> rgb565ToArgb8888(outBuf, null, reusableOut, pixelCount)

                Color.RGB888 -> {
                    val out = reusableOut
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
                    val out = reusableOut
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
                    if (splitAlphaPixels == null) splitAlphaPixels = ByteArray(pixelCount * 2)
                    if (splitAlphaValues == null) splitAlphaValues = ByteArray(pixelCount)
                    val (rgb, alpha) = splitAlpha(outBuf, pixelCount, 3, false, splitAlphaPixels, splitAlphaValues)
                    rgb565ToArgb8888(rgb, alpha, reusableOut, pixelCount)
                }

                Color.RGB8565 -> {
                    if (splitAlphaPixels == null) splitAlphaPixels = ByteArray(pixelCount * 2)
                    if (splitAlphaValues == null) splitAlphaValues = ByteArray(pixelCount)
                    val (rgb, alpha) = splitAlpha(outBuf, pixelCount, 3, true, splitAlphaPixels, splitAlphaValues)
                    rgb565ToArgb8888(rgb, alpha, reusableOut, pixelCount)
                }

                Color.ARGB8888 -> {
                    val out = reusableOut
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
                    outBuf
                }

                Color.BGRA8888 -> {
                    val out = reusableOut
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
    }

    @Test
    fun runBenchmark() {
        val iterations = 500
        val width = 1080
        val height = 2400
        val colors = listOf(Color.RGB888, Color.ARGB8888, Color.RGB565, Color.RGBA8888)

        println("Starting benchmark...")

        for (color in colors) {
            System.gc()
            Thread.sleep(100)
            val time = measureTimeMillis {
                val decoder = MockDecodeQmg(width, height, iterations, color)
                var framesProcessed = 0
                while (true) {
                    val frame = decoder.nextFrame() ?: break
                    framesProcessed++
                }
            }
            println("Color: ${color}, Time for ${iterations} frames: ${time} ms")
        }
    }
}

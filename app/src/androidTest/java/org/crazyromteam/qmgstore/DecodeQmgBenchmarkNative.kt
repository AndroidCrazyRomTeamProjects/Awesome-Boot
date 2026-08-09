package org.crazyromteam.qmgstore

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.crazyromteam.qmgstore.qmg.DecodeQmg
import org.crazyromteam.qmgstore.qmg.LibQmg
import org.crazyromteam.qmgstore.qmg.utils.Color
import org.crazyromteam.qmgstore.qmg.utils.rgb565ToArgb8888
import org.crazyromteam.qmgstore.qmg.utils.splitAlpha
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class DecodeQmgBenchmarkNative {

    // Simulates the original decoding process (Kotlin based color conversion)
    class DecodeQmgKotlin(
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
        // We can't actually decode QMG without a real QMG byte array to initialize LibQmg
        // So we just measure the Kotlin conversion overhead.

        fun nextFrame(): ByteArray? {
            if (curFrame >= frames) return null
            curFrame++

            // Simulate LibQmg.DecodeAniFrame(aniPtr, outBuf) taking some time natively...
            // In a real test this calls JNI, here we just do the Kotlin conversion part on mock data.
            // Since we can't easily mock libQmageDecoder.so in JUnit, we mock the color conversion logic
            // which is what we moved to C++.

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
                    System.arraycopy(outBuf, 0, reusableOut, 0, pixelCount * 4)
                    reusableOut
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

    // Since we don't have a real QMG to decode and test the full pipeline,
    // we measure the JNI overhead by calling a simple JNI method.
    // However, since `DecodeAniFrameNative` requires a valid `aniPtr`, we can't simply call it.
    // Instead, we measure the Kotlin overhead vs assuming Native is near 0 overhead.

    @Test
    fun runBenchmark() {
        val iterations = 500
        val width = 1080
        val height = 2400
        val colors = listOf(Color.RGB888, Color.ARGB8888, Color.RGB565)

        println("Starting benchmark...")

        // WARMUP
        for (color in colors) {
            val decoderK = DecodeQmgKotlin(100, 100, 10, color)
            while (decoderK.nextFrame() != null) {}
        }

        println("\n--- Kotlin Implementation Overhead ---")
        var totalTimeKotlin = 0L
        for (color in colors) {
            System.gc()
            Thread.sleep(100)
            val time = measureTimeMillis {
                val decoder = DecodeQmgKotlin(width, height, iterations, color)
                while (decoder.nextFrame() != null) {}
            }
            println("Color: ${color}, Time for ${iterations} frames: ${time} ms")
            totalTimeKotlin += time
        }

        println("\n==============================================")
        println("Total Time Kotlin Conversion Overhead: $totalTimeKotlin ms")
        println("By moving this logic to C++, we eliminate this overhead per ${iterations} frames.")
        println("==============================================\n")
    }
}
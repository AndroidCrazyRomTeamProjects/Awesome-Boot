package org.crazyromteam.qmgstore.qmg.utils

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class ColorUtilsTest {

    @Test
    fun testSplitAlpha_AlphaFirst() {
        // Pixel size = 3, pixelCount = 2
        // Format: Alpha, R, G
        // Data: [A1, R1, G1, A2, R2, G2]
        val data = byteArrayOf(10, 11, 12, 20, 21, 22)
        val pixelCount = 2
        val pixelSize = 3
        val alphaFirst = true

        val (pixels, alpha) = splitAlpha(data, pixelCount, pixelSize, alphaFirst)

        assertArrayEquals("Extracted pixels should match", byteArrayOf(11, 12, 21, 22), pixels)
        assertArrayEquals("Extracted alpha should match", byteArrayOf(10, 20), alpha)
    }

    @Test
    fun testSplitAlpha_AlphaLast() {
        // Pixel size = 4, pixelCount = 2
        // Format: R, G, B, Alpha
        // Data: [R1, G1, B1, A1, R2, G2, B2, A2]
        val data = byteArrayOf(10, 20, 30, 40, 50, 60, 70, 80)
        val pixelCount = 2
        val pixelSize = 4
        val alphaFirst = false

        val (pixels, alpha) = splitAlpha(data, pixelCount, pixelSize, alphaFirst)

        assertArrayEquals("Extracted pixels should match", byteArrayOf(10, 20, 30, 50, 60, 70), pixels)
        assertArrayEquals("Extracted alpha should match", byteArrayOf(40, 80), alpha)
    }

    @Test
    fun testSplitAlpha_EmptyArray() {
        val data = byteArrayOf()
        val pixelCount = 0
        val pixelSize = 4
        val alphaFirst = false

        val (pixels, alpha) = splitAlpha(data, pixelCount, pixelSize, alphaFirst)

        assertArrayEquals("Extracted pixels should be empty", byteArrayOf(), pixels)
        assertArrayEquals("Extracted alpha should be empty", byteArrayOf(), alpha)
    }
}

package org.crazyromteam.qmgstore.qmg.utils

class Color {

    fun splitAlphaRGB5658(
        data: ByteArray,
        pixelCount: Int
    ): Pair<ByteArray, ByteArray> {

        val pixels = ByteArray(pixelCount * 2)
        val alpha = ByteArray(pixelCount)

        var src = 0
        var p = 0
        var a = 0

        repeat(pixelCount) {
            pixels[p++] = data[src]
            pixels[p++] = data[src + 1]
            alpha[a++] = data[src + 2]
            src += 3
        }

        return pixels to alpha
    }

    fun splitAlphaRGB8565(
        data: ByteArray,
        pixelCount: Int
    ): Pair<ByteArray, ByteArray> {

        val pixels = ByteArray(pixelCount * 2)
        val alpha = ByteArray(pixelCount)

        var src = 0
        var p = 0
        var a = 0

        repeat(pixelCount) {
            alpha[a++] = data[src]
            pixels[p++] = data[src + 1]
            pixels[p++] = data[src + 2]
            src += 3
        }

        return pixels to alpha
    }

    fun rgb565ToArgb8888(
        rgb565: ByteArray,
        alpha: ByteArray? = null
    ): ByteArray {

        val pixels = rgb565.size / 2
        val out = ByteArray(pixels * 4)

        var src = 0
        var dst = 0

        repeat(pixels) {
            val value =
                ((rgb565[src + 1].toInt() and 0xFF) shl 8) or
                        (rgb565[src].toInt() and 0xFF)

            val r = ((value shr 11) and 0x1F) * 255 / 31
            val g = ((value shr 5) and 0x3F) * 255 / 63
            val b = (value and 0x1F) * 255 / 31
            val a = alpha?.get(it)?.toInt() ?: 255

            out[dst++] = a.toByte()
            out[dst++] = r.toByte()
            out[dst++] = g.toByte()
            out[dst++] = b.toByte()

            src += 2
        }

        return out
    }
}
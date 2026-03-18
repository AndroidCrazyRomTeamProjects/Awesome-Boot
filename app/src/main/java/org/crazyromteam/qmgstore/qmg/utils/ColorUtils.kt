package org.crazyromteam.qmgstore.qmg.utils

fun rgb565ToArgb8888(
    rgb565: ByteArray,
    alpha: ByteArray? = null,
    out: ByteArray? = null,
    pixelCount: Int = rgb565.size / 2
): ByteArray {
    val result = out ?: ByteArray(pixelCount * 4)
    var src = 0
    var dst = 0

    // Fast path without alpha branching inside the loop
    // Using bit shifting instead of multiplication and division for performance
    if (alpha == null) {
        repeat(pixelCount) {
            val value = ((rgb565[src + 1].toInt() and 0xFF) shl 8) or (rgb565[src].toInt() and 0xFF)
            val r = (value shr 11) and 0x1F
            val g = (value shr 5) and 0x3F
            val b = value and 0x1F

            result[dst++] = ((r shl 3) or (r shr 2)).toByte()
            result[dst++] = ((g shl 2) or (g shr 4)).toByte()
            result[dst++] = ((b shl 3) or (b shr 2)).toByte()
            result[dst++] = -1 // 255 in byte
            src += 2
        }
    } else {
        repeat(pixelCount) {
            val value = ((rgb565[src + 1].toInt() and 0xFF) shl 8) or (rgb565[src].toInt() and 0xFF)
            val r = (value shr 11) and 0x1F
            val g = (value shr 5) and 0x3F
            val b = value and 0x1F

            result[dst++] = ((r shl 3) or (r shr 2)).toByte()
            result[dst++] = ((g shl 2) or (g shr 4)).toByte()
            result[dst++] = ((b shl 3) or (b shr 2)).toByte()
            result[dst++] = alpha[it]
            src += 2
        }
    }
    return result
}

fun splitAlpha(
    data: ByteArray,
    pixelCount: Int,
    pixelSize: Int,
    alphaFirst: Boolean,
    outPixels: ByteArray? = null,
    outAlpha: ByteArray? = null
): Pair<ByteArray, ByteArray> {
    val pixels = outPixels ?: ByteArray(pixelCount * (pixelSize - 1))
    val alpha = outAlpha ?: ByteArray(pixelCount)
    var src = 0
    var p = 0
    var a = 0

    // Avoid System.arraycopy overhead for small sizes in a tight loop
    if (pixelSize == 3) {
        if (alphaFirst) {
            repeat(pixelCount) {
                alpha[a++] = data[src]
                pixels[p++] = data[src + 1]
                pixels[p++] = data[src + 2]
                src += 3
            }
        } else {
            repeat(pixelCount) {
                pixels[p++] = data[src]
                pixels[p++] = data[src + 1]
                alpha[a++] = data[src + 2]
                src += 3
            }
        }
    } else {
        repeat(pixelCount) {
            if (alphaFirst) {
                alpha[a++] = data[src]
                System.arraycopy(data, src + 1, pixels, p, pixelSize - 1)
            } else {
                System.arraycopy(data, src, pixels, p, pixelSize - 1)
                alpha[a++] = data[src + pixelSize - 1]
            }
            src += pixelSize
            p += pixelSize - 1
        }
    }
    return pixels to alpha
}

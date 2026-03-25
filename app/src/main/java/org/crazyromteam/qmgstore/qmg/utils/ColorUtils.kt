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
            val v0 = rgb565[src].toInt() and 0xFF
            val v1 = rgb565[src + 1].toInt() and 0xFF
            val value = (v1 shl 8) or v0
            val r = (value ushr 11)
            val g = (value ushr 5) and 0x3F
            val b = value and 0x1F

            result[dst] = ((r shl 3) or (r ushr 2)).toByte()
            result[dst + 1] = ((g shl 2) or (g ushr 4)).toByte()
            result[dst + 2] = ((b shl 3) or (b ushr 2)).toByte()
            result[dst + 3] = -1 // 255 in byte

            dst += 4
            src += 2
        }
    } else {
        repeat(pixelCount) {
            val v0 = rgb565[src].toInt() and 0xFF
            val v1 = rgb565[src + 1].toInt() and 0xFF
            val value = (v1 shl 8) or v0
            val r = (value ushr 11)
            val g = (value ushr 5) and 0x3F
            val b = value and 0x1F

            result[dst] = ((r shl 3) or (r ushr 2)).toByte()
            result[dst + 1] = ((g shl 2) or (g ushr 4)).toByte()
            result[dst + 2] = ((b shl 3) or (b ushr 2)).toByte()
            result[dst + 3] = alpha[it]

            dst += 4
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
        // ⚡ Bolt Optimization: Loop unswitching.
        // Hoisting the loop-invariant `alphaFirst` check outside the loop eliminates
        // `pixelCount` branch evaluations, reducing execution time.
        if (alphaFirst) {
            repeat(pixelCount) {
                alpha[a++] = data[src]
                System.arraycopy(data, src + 1, pixels, p, pixelSize - 1)
                src += pixelSize
                p += pixelSize - 1
            }
        } else {
            repeat(pixelCount) {
                System.arraycopy(data, src, pixels, p, pixelSize - 1)
                alpha[a++] = data[src + pixelSize - 1]
                src += pixelSize
                p += pixelSize - 1
            }
        }
    }
    return pixels to alpha
}

fun rgb565AlphaInterleavedToArgb8888(
    data: ByteArray,
    pixelCount: Int,
    alphaFirst: Boolean,
    out: ByteArray? = null
): ByteArray {
    val result = out ?: ByteArray(pixelCount * 4)
    var src = 0
    var dst = 0

    // ⚡ Bolt Optimization: Loop unswitching.
    // Hoisting the loop-invariant `alphaFirst` check outside the loop eliminates
    // `pixelCount` branch evaluations, significantly speeding up the decoding process.
    if (alphaFirst) {
        repeat(pixelCount) {
            val alpha = data[src]
            val v1 = data[src + 1].toInt() and 0xFF
            val v2 = data[src + 2].toInt() and 0xFF
            val rgbValue = (v2 shl 8) or v1

            val r = (rgbValue ushr 11)
            val g = (rgbValue ushr 5) and 0x3F
            val b = rgbValue and 0x1F

            result[dst] = ((r shl 3) or (r ushr 2)).toByte()
            result[dst + 1] = ((g shl 2) or (g ushr 4)).toByte()
            result[dst + 2] = ((b shl 3) or (b ushr 2)).toByte()
            result[dst + 3] = alpha

            dst += 4
            src += 3
        }
    } else {
        repeat(pixelCount) {
            val v0 = data[src].toInt() and 0xFF
            val v1 = data[src + 1].toInt() and 0xFF
            val rgbValue = (v1 shl 8) or v0
            val alpha = data[src + 2]

            val r = (rgbValue ushr 11)
            val g = (rgbValue ushr 5) and 0x3F
            val b = rgbValue and 0x1F

            result[dst] = ((r shl 3) or (r ushr 2)).toByte()
            result[dst + 1] = ((g shl 2) or (g ushr 4)).toByte()
            result[dst + 2] = ((b shl 3) or (b ushr 2)).toByte()
            result[dst + 3] = alpha

            dst += 4
            src += 3
        }
    }

    return result
}

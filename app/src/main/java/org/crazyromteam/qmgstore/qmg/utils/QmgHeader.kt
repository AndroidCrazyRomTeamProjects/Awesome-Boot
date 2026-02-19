package org.crazyromteam.qmgstore.qmg.utils

import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

class QmgHeader(qmgData: ByteArray) {

    val TAG = "QmgHeader"

    val width: Int
    val height: Int
    val frames: Int
    val bppType: Int

    init {
        require(qmgData.size >= 0x20) { "File too small to be QMG" }

        val buffer = ByteBuffer.wrap(qmgData)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        // header[:0x2]
        val magic0 = qmgData[0].toInt().toChar()
        val magic1 = qmgData[1].toInt().toChar()
        val magic = "$magic0$magic1"

        require(magic == "QM" || magic == "IM") { "Not a valid QMG format" }
        Log.d(TAG, "qmg valid...")

        // width & height
        if (magic == "IM") {
            buffer.position(0x2)
        } else {
            buffer.position(0x6)
        }

        width = buffer.short.toInt() and 0xFFFF
        height = buffer.short.toInt() and 0xFFFF

        // frames
        if (magic == "IM") {
            buffer.position(0xA)
        } else {
            buffer.position(0x10)
        }

        frames = buffer.short.toInt() and 0xFFFF

        Log.i(TAG, "wxh=${width}x${height}, frames=$frames")

        // bppType logic
        var tmpBpp = qmgData[3].toInt() and 0xFF

        if (magic == "IM") {
            tmpBpp = 0
        }

        when (qmgData[7].toInt() and 0xFF) {
            0x5A, 0x5B -> {
                if (((qmgData[6].toInt() shr 6) and 1) == 1) {
                    tmpBpp = 6
                }
            }

            0x5C, 0x5D -> {
                if (((qmgData[6].toInt() shr 6) and 1) == 1) {
                    tmpBpp = 6
                } else if (((qmgData[8].toInt() shr 6) and 1) == 1) {
                    tmpBpp = 3
                }
            }

            else -> {
                Log.e(TAG, "Unkown bpp type $qmgData[6]")
            }
        }

        bppType = tmpBpp
    }
}

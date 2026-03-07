package org.crazyromteam.qmgstore.qmg.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder

class QmgHeader(
    val magic: String,
    val width: Int,
    val height: Int,
    val frames: Int,
    val color: Color,
    val isValid: Boolean,
    val duration: Int,
    val repeat: Boolean
) {
    companion object {
        operator fun invoke(headerBytes: ByteArray): QmgHeader {
            require(headerBytes.size >= 32) { "Header byte array must be at least 32 bytes" }

            val buffer = ByteBuffer.wrap(headerBytes).order(ByteOrder.LITTLE_ENDIAN)

            val m2 = String(headerBytes.copyOfRange(0, 2), Charsets.US_ASCII)
            val m4 = String(headerBytes.copyOfRange(0, 4), Charsets.US_ASCII)

            return when {
                m4 == "IFEG" -> IfegFormatStrategy.parse(headerBytes, buffer)
                m2 == "IM" -> ImFormatStrategy.parse(headerBytes, buffer)
                m2 == "QM" -> QmFormatStrategy.parse(headerBytes, buffer)
                else -> throw Exception("Unknown animation type in QMG header")
            }
        }
    }

    override fun toString(): String {
        return "QmgHeader(magic='$magic', width=$width, height=$height, frames=$frames, duration=$duration, repeat=$repeat color=$color, isValid=$isValid)"
    }
}

interface QmgFormatStrategy {
    fun parse(headerBytes: ByteArray, buffer: ByteBuffer): QmgHeader
}

object IfegFormatStrategy : QmgFormatStrategy {
    override fun parse(headerBytes: ByteArray, buffer: ByteBuffer): QmgHeader {
        val flagB = headerBytes[0xb].toInt() and 0xFF
        val isValid = (flagB and 0x15) == 0x15

        val codecType = headerBytes[0x9].toInt() and 0xFF
        require(codecType in 0..1) { "Invalid codec type in IFEG header" }

        val width = buffer.getShort(4).toInt() and 0xFFFF
        val height = buffer.getShort(6).toInt() and 0xFFFF

        val header0x10 = headerBytes[0x10].toInt() and 0xFF
        val header0x8 = headerBytes[0x8].toInt() and 0xFF

        val color = if (header0x10 == 0x0) {
            when (header0x8) {
                0x1 -> Color.RGB565
                0x2 -> Color.RGB888
                0x3 -> Color.RGBA8888
                else -> Color.RGB565
            }
        } else {
            Color.RGB5658
        }

        return QmgHeader(
            magic = "IFEG",
            width = width,
            height = height,
            frames = 1,
            color = color,
            isValid = isValid,
            duration = 0,
            repeat = false
        )
    }
}

object ImFormatStrategy : QmgFormatStrategy {
    override fun parse(headerBytes: ByteArray, buffer: ByteBuffer): QmgHeader {
        val width = buffer.getShort(2).toInt() and 0xFFFF
        val height = buffer.getShort(4).toInt() and 0xFFFF
        val frames = buffer.getShort(10).toInt() and 0xFFFF
        val duration = buffer.getShort(12).toInt() and 0xFFFF
        val repeat = buffer.getShort(14).toInt() and 0xFFFF != 0

        val flags = headerBytes[6].toInt() and 0xFF
        val version = headerBytes[7].toInt() and 0xFF
        val flag8 = headerBytes[8].toInt() and 0xFF

        var bppType = 0
        if (version == 0x5a || version == 0x5b) {
            if ((flags shr 6) and 1 == 1) {
                bppType = 6
            }
        } else if (version == 0x5c || version == 0x5d) {
            if ((flags shr 6) and 1 == 1) {
                bppType = 6
            } else if ((flag8 shr 6) and 1 == 1) {
                bppType = 3
            }
        } else {
            throw IllegalArgumentException("Unknown IM version type: 0x${version.toString(16)}")
        }

        return QmgHeader(
            magic = "IM",
            width = width,
            height = height,
            frames = frames,
            color = Color.fromInt(bppType),
            isValid = true,
            duration = duration,
            repeat = repeat
        )
    }
}

object QmFormatStrategy : QmgFormatStrategy {
    override fun parse(headerBytes: ByteArray, buffer: ByteBuffer): QmgHeader {
        val width = buffer.getShort(6).toInt() and 0xFFFF
        val height = buffer.getShort(8).toInt() and 0xFFFF
        val frames = buffer.getShort(16).toInt() and 0xFFFF
        val duration = buffer.getShort(20).toInt() and 0xFFF
        val repeat = buffer.getShort(22).toInt() and 0xFFFF != 0

        val bppType = headerBytes[3].toInt() and 0xFF

        return QmgHeader(
            magic = "QM",
            width = width,
            height = height,
            frames = frames,
            color = Color.fromInt(bppType),
            isValid = true,
            duration = duration,
            repeat = repeat
        )
    }
}

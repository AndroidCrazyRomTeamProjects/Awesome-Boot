package org.crazyromteam.qmgstore.qmg.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder

class QmgHeader(headerBytes: ByteArray) {
    val magic: String
    val width: Int
    val height: Int
    val frames: Int
    val color: Color
    val isValid: Boolean

    init {
        require(headerBytes.size >= 32) { "Header byte array must be at least 32 bytes" }

        val buffer = ByteBuffer.wrap(headerBytes).order(ByteOrder.LITTLE_ENDIAN)

        val m2 = String(headerBytes.copyOfRange(0, 2), Charsets.US_ASCII)
        val m4 = String(headerBytes.copyOfRange(0, 4), Charsets.US_ASCII)

        when {
            m4 == "IFEG" -> {
                // IFEG Format parsing based on ifgDecode
                val flagB = headerBytes[0xb].toInt() and 0xFF
                isValid = (flagB and 0x15) == 0x15

                val codecType = headerBytes[0x9].toInt() and 0xFF
                require(codecType in 0..1) { "Invalid codec type in IFEG header" }

                magic = "IFEG"
                width = buffer.getShort(4).toInt() and 0xFFFF
                height = buffer.getShort(6).toInt() and 0xFFFF
                frames = 1 // IFEG parses as a single frame in the provided script

                // IFEG color mapping from script
                val header0x10 = headerBytes[0x10].toInt() and 0xFF
                val header0x8 = headerBytes[0x8].toInt() and 0xFF

                color = if (header0x10 == 0x0) {
                    when (header0x8) {
                        0x1 -> Color.RGB565
                        0x2 -> Color.RGB888
                        0x3 -> Color.RGBA8888
                        else -> Color.RGB565
                    }
                } else {
                    Color.RGB5658
                }
            }
            m2 == "IM" -> {
                // IM Format parsing
                magic = "IM"
                width = buffer.getShort(2).toInt() and 0xFFFF
                height = buffer.getShort(4).toInt() and 0xFFFF
                frames = buffer.getShort(10).toInt() and 0xFFFF

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

                color = Color.fromInt(bppType)
                isValid = true
            }
            m2 == "QM" -> {
                // QM Format parsing
                magic = "QM"
                width = buffer.getShort(6).toInt() and 0xFFFF
                height = buffer.getShort(8).toInt() and 0xFFFF
                frames = buffer.getShort(16).toInt() and 0xFFFF

                val bppType = headerBytes[3].toInt() and 0xFF
                color = Color.fromInt(bppType)
                isValid = true
            }
            else -> {
                // Invalid or unsupported format
                magic = "UNKNOWN"
                width = 0
                height = 0
                frames = 0
                color = Color.UNKNOWN
                isValid = false
            }
        }
    }

    override fun toString(): String {
        return "QmgHeader(magic='$magic', width=$width, height=$height, frames=$frames, color=$color, isValid=$isValid)"
    }
}
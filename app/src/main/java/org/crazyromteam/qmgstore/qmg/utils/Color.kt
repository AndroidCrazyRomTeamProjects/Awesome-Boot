package org.crazyromteam.qmgstore.qmg.utils


/**
 * Represents the specific Color/BPP formats extracted from the QMG/IM headers.
 * Mapped from the Python PIL format equivalents.
 */
enum class Color(val bppType: Int) {
    RGB565(0),     // BGR;16
    RGB888(1),     // RGB
    BGR888(2),     // BGR
    RGB5658(3),    // Alpha + RGB565
    RGB8565(4),    // Alpha + RGB565 diff order
    ARGB8888(5),   // ARGB
    RGBA8888(6),   // RGBA
    BGRA8888(7);   // BGRA

    companion object {
        fun fromInt(value: Int): Color {
            return entries.find { it.bppType == value } ?: throw Exception("Unknown BPP type: $value")
        }
    }
}
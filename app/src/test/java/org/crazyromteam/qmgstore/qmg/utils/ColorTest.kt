package org.crazyromteam.qmgstore.qmg.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ColorTest {

    @Test
    fun testFromIntValid() {
        assertEquals(Color.RGB565, Color.fromInt(0))
        assertEquals(Color.RGB888, Color.fromInt(1))
        assertEquals(Color.BGR888, Color.fromInt(2))
        assertEquals(Color.RGB5658, Color.fromInt(3))
        assertEquals(Color.RGB8565, Color.fromInt(4))
        assertEquals(Color.ARGB8888, Color.fromInt(5))
        assertEquals(Color.RGBA8888, Color.fromInt(6))
        assertEquals(Color.BGRA8888, Color.fromInt(7))
    }

    @Test
    fun testFromIntInvalid() {
        var exception = assertThrows(Exception::class.java) {
            Color.fromInt(-1)
        }
        assertEquals("Unknown BPP type: -1", exception.message)

        exception = assertThrows(Exception::class.java) {
            Color.fromInt(8)
        }
        assertEquals("Unknown BPP type: 8", exception.message)
    }
}

package org.crazyromteam.qmgstore.qmg.utils

import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class QmgHeaderBenchmark {

    @Test
    fun benchmarkHeaderParse() {
        val ifegHeader = ByteArray(32) { 0 }
        ifegHeader[0] = 'I'.code.toByte()
        ifegHeader[1] = 'F'.code.toByte()
        ifegHeader[2] = 'E'.code.toByte()
        ifegHeader[3] = 'G'.code.toByte()
        ifegHeader[0xb] = 0x15.toByte() // isValid
        ifegHeader[0x9] = 1.toByte() // codecType

        val imHeader = ByteArray(32) { 0 }
        imHeader[0] = 'I'.code.toByte()
        imHeader[1] = 'M'.code.toByte()
        imHeader[7] = 0x5a.toByte() // version

        val qmHeader = ByteArray(32) { 0 }
        qmHeader[0] = 'Q'.code.toByte()
        qmHeader[1] = 'M'.code.toByte()

        // Warm up
        for (i in 0 until 10000) {
            parseString(ifegHeader)
            parseString(imHeader)
            parseString(qmHeader)

            parseBytes(ifegHeader)
            parseBytes(imHeader)
            parseBytes(qmHeader)
        }

        System.gc()
        Thread.sleep(100)

        val iterations = 1_000_000

        val startString = System.nanoTime()
        for (i in 0 until iterations) {
            parseString(ifegHeader)
            parseString(imHeader)
            parseString(qmHeader)
        }
        val endString = System.nanoTime()

        System.gc()
        Thread.sleep(100)

        val startBytes = System.nanoTime()
        for (i in 0 until iterations) {
            parseBytes(ifegHeader)
            parseBytes(imHeader)
            parseBytes(qmHeader)
        }
        val endBytes = System.nanoTime()

        println("String parse time: ${(endString - startString) / 1_000_000.0} ms")
        println("Bytes parse time: ${(endBytes - startBytes) / 1_000_000.0} ms")
        println("Speedup: ${(endString - startString).toDouble() / (endBytes - startBytes)}x")
    }

    private fun parseString(headerBytes: ByteArray) {
        val m2 = String(headerBytes.copyOfRange(0, 2), Charsets.US_ASCII)
        val m4 = String(headerBytes.copyOfRange(0, 4), Charsets.US_ASCII)

        when {
            m4 == "IFEG" -> { /* do nothing for bench */ }
            m2 == "IM" -> { }
            m2 == "QM" -> { }
            else -> throw Exception("Unknown")
        }
    }

    private fun parseBytes(headerBytes: ByteArray) {
        val b0 = headerBytes[0]
        val b1 = headerBytes[1]

        when {
            b0 == 0x49.toByte() && b1 == 0x46.toByte() && headerBytes[2] == 0x45.toByte() && headerBytes[3] == 0x47.toByte() -> { }
            b0 == 0x49.toByte() && b1 == 0x4D.toByte() -> { }
            b0 == 0x51.toByte() && b1 == 0x4D.toByte() -> { }
            else -> throw Exception("Unknown")
        }
    }
}

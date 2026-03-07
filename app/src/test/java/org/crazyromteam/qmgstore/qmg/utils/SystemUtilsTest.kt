package org.crazyromteam.qmgstore.qmg.utils

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import java.io.File

class SystemUtilsTest {
    @Test
    fun testReadSystemFile(): Unit = runBlocking {
        val tempFile = File.createTempFile("test", ".txt")
        val content = "Hello, World!".toByteArray()
        tempFile.writeBytes(content)

        val systemUtils = SystemUtils()
        val startTime = System.currentTimeMillis()
        val result = systemUtils.readSystemFile(tempFile.absolutePath)
        val endTime = System.currentTimeMillis()

        println("Execution time: ${endTime - startTime}ms")
        assertArrayEquals(content, result)

        tempFile.delete()
    }
}

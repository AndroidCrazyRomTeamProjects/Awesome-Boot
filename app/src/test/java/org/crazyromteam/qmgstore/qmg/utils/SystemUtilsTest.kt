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

    @Test
    fun testReadSystemFile_nonExistentFile(): Unit = runBlocking {
        val systemUtils = SystemUtils()
        val nonExistentPath = "/path/to/some/completely/non/existent/file_${System.currentTimeMillis()}.txt"
        val result = systemUtils.readSystemFile(nonExistentPath)

        assertArrayEquals(ByteArray(0), result)
    }

    @Test
    fun testIsValidSystemPath(): Unit = runBlocking {
        val systemUtils = SystemUtils()

        // Valid path
        assert(systemUtils.isValidSystemPath("/system/media/test.qmg"))

        // Invalid extension
        assert(!systemUtils.isValidSystemPath("/system/media/test.txt"))

        // Invalid directory
        assert(!systemUtils.isValidSystemPath("/data/local/tmp/test.qmg"))

        // Path traversal attempt
        assert(!systemUtils.isValidSystemPath("/system/media/../../etc/passwd.qmg"))

        // Another traversal attempt
        assert(!systemUtils.isValidSystemPath("/system/media/subdir/../../../etc/passwd.qmg"))

        // Null-byte injection or similar (if handled by File)
        assert(!systemUtils.isValidSystemPath("/system/media/test.qmg\u0000.txt"))
    }
}

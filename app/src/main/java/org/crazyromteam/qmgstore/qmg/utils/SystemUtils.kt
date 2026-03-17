package org.crazyromteam.qmgstore.qmg.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SystemUtils {
    suspend fun readSystemFile(path: String): ByteArray = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("cat", "--", path))
            process.inputStream.readBytes().also {
                process.waitFor()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ByteArray(0)
        }
    }

    fun isValidSystemPath(path: String): Boolean {
        return try {
            val file = File(path)
            val canonicalPath = file.canonicalPath
            canonicalPath.startsWith("/system/media/") && canonicalPath.endsWith(".qmg")
        } catch (e: Exception) {
            false
        }
    }
}

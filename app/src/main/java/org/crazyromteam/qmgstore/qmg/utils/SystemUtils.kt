package org.crazyromteam.qmgstore.qmg.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SystemUtils {
    suspend fun readSystemFile(path: String): ByteArray = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("cat", path))
            process.inputStream.readBytes().also {
                process.waitFor()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ByteArray(0)
        }
    }
}
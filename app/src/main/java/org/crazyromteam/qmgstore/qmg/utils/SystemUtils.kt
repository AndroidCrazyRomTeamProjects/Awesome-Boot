package org.crazyromteam.qmgstore.qmg.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SystemUtils {
    suspend fun readSystemFile(path: String): ByteArray = withContext(Dispatchers.IO) {
        try {
            // ⚡ Bolt Optimization: Replace slow shell process execution (`cat`)
            // with standard `java.io` file reading. Spawning a new process on Android
            // is extremely expensive and blocks significantly longer than a direct file read.
            java.io.File(path).readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
            ByteArray(0)
        }
    }
}
package org.crazyromteam.qmgstore.qmg.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SystemUtils {
    suspend fun readSystemFile(path: String): ByteArray = withContext(Dispatchers.IO) {
        try {
            // ⚡ Bolt Optimization: Replaced `Runtime.getRuntime().exec("cat ...")` with direct file I/O.
            // Spawning a new shell process via Runtime.exec() is extremely slow and memory-intensive.
            // Native Java File reading is magnitudes faster and avoids the overhead of process creation.
            File(path).readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
            ByteArray(0)
        }
    }
}
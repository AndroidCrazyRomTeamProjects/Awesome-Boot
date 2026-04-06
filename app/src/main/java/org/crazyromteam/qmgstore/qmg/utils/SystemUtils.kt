package org.crazyromteam.qmgstore.qmg.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.io.File

class SystemUtils {
    // ⚡ Bolt Optimization: Replaced `Runtime.getRuntime().exec("cat")` with `File(path).readBytes()`.
    // Forking a shell process is heavily expensive (creates new VM, allocates memory, IPC overhead).
    // Native API direct file I/O is exponentially faster and removes blocking `process.waitFor()` bottlenecks.
    suspend fun readSystemFile(path: String): ByteArray = withContext(Dispatchers.IO) {
        try {
            File(path).readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
            ByteArray(0)
        }
    }
}
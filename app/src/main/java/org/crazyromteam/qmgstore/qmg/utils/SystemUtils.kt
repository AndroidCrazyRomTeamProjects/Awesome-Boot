package org.crazyromteam.qmgstore.qmg.utils

class SystemUtils {
    fun readSystemFile(path: String): ByteArray {
        return try {
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
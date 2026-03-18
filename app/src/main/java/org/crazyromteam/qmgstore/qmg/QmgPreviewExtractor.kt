package org.crazyromteam.qmgstore.qmg

import android.graphics.Bitmap
import android.util.Log
import org.crazyromteam.qmgstore.qmg.utils.QmgHeader
import java.nio.ByteBuffer

object QmgPreviewExtractor {
    private const val TAG = "QmgPreviewExtractor"

    /**
     * Decodes only the first frame of a QMG file and returns it as a Bitmap.
     * This is useful for generating previews without decoding the entire animation.
     *
     * @param qmgData The raw bytes of the QMG file.
     * @return A Bitmap of the first frame, or null if decoding fails.
     */
    fun getFirstFrame(qmgData: ByteArray): Bitmap? {
        return try {
            val header = QmgHeader(qmgData)
            Log.d(TAG, "Header parsed: ${header.width}x${header.height}, frames=${header.frames}, color=${header.color}")
            
            val decoder = DecodeQmg(
                qmgData = qmgData,
                width = header.width,
                height = header.height,
                frames = header.frames,
                color = header.color
            )
            
            val frameData = decoder.nextFrame()
            decoder.release()

            if (frameData != null) {
                Log.d(TAG, "Frame decoded successfully, size: ${frameData.size}")
                val bitmap = Bitmap.createBitmap(header.width, header.height, Bitmap.Config.ARGB_8888)
                bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(frameData))
                bitmap
            } else {
                Log.e(TAG, "Failed to decode the first frame")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting first frame", e)
            null
        }
    }
}

package org.crazyromteam.qmgstore.qmg

import android.graphics.Bitmap
import org.crazyromteam.qmgstore.qmg.utils.QmgHeader
import java.nio.ByteBuffer

object QmgPreviewExtractor {
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
                val bitmap = Bitmap.createBitmap(header.width, header.height, Bitmap.Config.ARGB_8888)
                bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(frameData))
                bitmap
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

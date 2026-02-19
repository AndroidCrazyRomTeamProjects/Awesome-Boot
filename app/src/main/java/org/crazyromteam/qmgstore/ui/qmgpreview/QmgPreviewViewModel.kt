package org.crazyromteam.qmgstore.ui.qmgpreview

import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.createBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.crazyromteam.qmgstore.qmg.DecodeQmg
import java.nio.ByteBuffer

class QmgPreviewViewModel : ViewModel() {
    private var decoder: DecodeQmg? = null
    private var job: Job? = null

    private val _frame = MutableLiveData<Bitmap>()
    val frame: LiveData<Bitmap> = _frame

    fun startQmg(qmgData: ByteArray, width: Int, height: Int, frames: Int, bppType: Int) {
        // If the QMG data is empty, there's nothing to decode.
        if (qmgData.isEmpty()) {
            return
        }

        decoder = DecodeQmg(qmgData, width, height, frames, bppType)

        Log.d(
            "QMG_Start",
            "started decoding qmg: width=$width, height=$height, frames=$frames, bppType=$bppType"
        )

        job?.cancel() // Cancel any previous job
        job = viewModelScope.launch(Dispatchers.Default) {
            // Use isActive to ensure the loop is cancellable
            while (isActive) {
                // nextFrame() will return null if the native decoder isn't initialized
                // or the animation ends. This will break the loop.
                val raw = decoder?.nextFrame() ?: break

                val bmp = createBitmap(width, height)

                // If 'raw' is full of zeros, the bitmap will be transparent.
                bmp.copyPixelsFromBuffer(ByteBuffer.wrap(raw))

                withContext(Dispatchers.Main) {
                    _frame.value = bmp
                }

                delay(33) // ~30 FPS (bootanimation-like)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
        decoder = null
    }
}

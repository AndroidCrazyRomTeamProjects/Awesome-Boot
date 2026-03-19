package org.crazyromteam.qmgstore.ui.qmgpreview

import android.view.Surface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.crazyromteam.qmgstore.qmg.utils.SystemUtils

class BootAnimationPreview(
    private val surface: Surface,
    private val vm: QmgPreviewViewModel,
    private val scope: CoroutineScope
) {
    private val systemUtils = SystemUtils()
    
    fun playAnimation() {
        scope.launch {
            // ⚡ Bolt Optimization: Read the required QMG files concurrently to reduce latency.
            // Instead of waiting for `bootsamsung.qmg` to be read from disk before reading `bootsamsungloop.qmg`,
            // we read both simultaneously using `async`. This significantly speeds up the boot animation load time.
            val bootAnimDataDeferred = async(Dispatchers.IO) { systemUtils.readSystemFile("/system/media/bootsamsung.qmg") }
            val bootAnimLoopDataDeferred = async(Dispatchers.IO) { systemUtils.readSystemFile("/system/media/bootsamsungloop.qmg") }

            val bootAnimData = bootAnimDataDeferred.await()
            val bootAnimLoopData = bootAnimLoopDataDeferred.await()

            vm.startBootAnimation(surface, bootAnimData, bootAnimLoopData)
        }
    }
}

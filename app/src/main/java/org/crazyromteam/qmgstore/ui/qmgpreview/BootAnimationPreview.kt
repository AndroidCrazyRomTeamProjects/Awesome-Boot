package org.crazyromteam.qmgstore.ui.qmgpreview

import android.view.Surface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
            val bootAnimData = systemUtils.readSystemFile("/system/media/bootsamsung.qmg")
            val bootAnimLoopData = systemUtils.readSystemFile("/system/media/bootsamsungloop.qmg")
            vm.startBootAnimation(surface, bootAnimData, bootAnimLoopData)
        }
    }
}

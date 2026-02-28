package org.crazyromteam.qmgstore.ui.qmgpreview

import android.view.Surface
import org.crazyromteam.qmgstore.qmg.utils.SystemUtils

class BootAnimationPreview(
    private val surface: Surface,
    private val vm: QmgPreviewViewModel
) {
    private val systemUtils = SystemUtils()
    
    private val bootAnimData = systemUtils.readSystemFile("/system/media/bootsamsung.qmg")
    private val bootAnimLoopData = systemUtils.readSystemFile("/system/media/bootsamsungloop.qmg")
    
    fun playAnimation() {
        vm.startBootAnimation(surface, bootAnimData, bootAnimLoopData)
    }
}

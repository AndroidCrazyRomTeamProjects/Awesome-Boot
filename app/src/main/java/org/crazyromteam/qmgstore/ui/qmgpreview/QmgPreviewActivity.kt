package org.crazyromteam.qmgstore.ui.qmgpreview

import android.content.Intent
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import org.crazyromteam.qmgstore.R
import org.crazyromteam.qmgstore.qmg.utils.QmgHeader
import org.crazyromteam.qmgstore.qmg.utils.SystemUtils

class QmgPreviewActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var vm: QmgPreviewViewModel
    private var qmgData: ByteArray? = null
    
    private var systemUtils = SystemUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qmg_preview)

        vm = ViewModelProvider(this).get(QmgPreviewViewModel::class.java)
        findViewById<SurfaceView>(R.id.qmg_surface_view).holder.addCallback(this)

        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            contentResolver.openInputStream(intent.data!!)?.use {
                qmgData = it.readBytes()
            }
        } else {
            val qmgPath = intent.getStringExtra("qmgPath")
            if (qmgPath != null) {
                qmgData = systemUtils.readSystemFile(qmgPath)
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (intent.getBooleanExtra("bootanimation", false)) {
            val bootAnimationPreview = BootAnimationPreview(holder.surface, vm)
            bootAnimationPreview.playAnimation()
        } else {
            qmgData?.let {
                if (it.isNotEmpty()) {
                    val header = QmgHeader(it)
                    if (header.isValid) {
                        vm.startQmg(
                            it,
                            header.width,
                            header.height,
                            header.frames,
                            header.duration,
                            header.repeat,
                            header.color,
                            holder.surface
                        )
                    }
                }
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}
}

package org.crazyromteam.qmgstore.ui.qmgpreview

import android.content.Intent
import android.os.Bundle
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.crazyromteam.qmgstore.R
import org.crazyromteam.qmgstore.api.RetrofitClient
import org.crazyromteam.qmgstore.qmg.utils.QmgHeader
import org.crazyromteam.qmgstore.qmg.utils.SystemUtils

class QmgPreviewActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var vm: QmgPreviewViewModel
    private var qmgData: ByteArray? = null
    private var introData: ByteArray? = null
    private var loopData: ByteArray? = null
    private var currentSurface: Surface? = null
    
    private var systemUtils = SystemUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qmg_preview)

        // Make it full screen
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        vm = ViewModelProvider(this).get(QmgPreviewViewModel::class.java)
        findViewById<SurfaceView>(R.id.qmg_surface_view).apply {
            holder.addCallback(this@QmgPreviewActivity)
            setOnClickListener { finish() } // Exit on tap
        }

        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            contentResolver.openInputStream(intent.data!!)?.use {
                qmgData = it.readBytes()
            }
        } else if (intent.hasExtra("intro_data") || intent.hasExtra("loop_data")) {
            introData = intent.getByteArrayExtra("intro_data")
            loopData = intent.getByteArrayExtra("loop_data")
        } else if (intent.hasExtra("qmg_data")) {
            qmgData = intent.getByteArrayExtra("qmg_data")
        } else if (intent.hasExtra("theme_id") && intent.hasExtra("file_name")) {
            val themeId = intent.getStringExtra("theme_id")!!
            val fileName = intent.getStringExtra("file_name")!!
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val response = RetrofitClient.apiService.downloadFile(themeId, fileName)
                    if (response.isSuccessful) {
                        qmgData = response.body()?.bytes()
                        withContext(Dispatchers.Main) {
                            checkAndStartQmg()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            finish()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        finish()
                    }
                }
            }
        } else {
            val qmgPath = intent.getStringExtra("qmgPath")
            if (qmgPath != null) {
                lifecycleScope.launch {
                    qmgData = systemUtils.readSystemFile(qmgPath)
                    checkAndStartQmg()
                }
            }
        }
    }

    private fun checkAndStartQmg() {
        val surface = currentSurface ?: return
        
        if (introData != null || loopData != null) {
            vm.startBootAnimation(surface, introData ?: byteArrayOf(), loopData ?: byteArrayOf())
        } else if (intent.getBooleanExtra("bootanimation", false)) {
            val bootAnimationPreview = BootAnimationPreview(surface, vm, lifecycleScope)
            bootAnimationPreview.playAnimation()
        } else {
            qmgData?.let {
                if (it.isNotEmpty()) {
                    val header = QmgHeader(it)
                    if (header.isValid) {
                        vm.startQmg(
                            it,
                            header.repeat,
                            surface
                        )
                    }
                }
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        currentSurface = holder.surface
        checkAndStartQmg()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        currentSurface = null
    }
}

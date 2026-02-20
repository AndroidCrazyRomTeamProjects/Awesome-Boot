package org.crazyromteam.qmgstore.ui.qmgpreview

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import org.crazyromteam.qmgstore.R
import org.crazyromteam.qmgstore.qmg.utils.QmgHeader


class QmgPreviewActivity : AppCompatActivity() {

    private lateinit var vm: QmgPreviewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        val intent = getIntent()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qmg_preview)

        vm = ViewModelProvider(this).get(QmgPreviewViewModel::class.java)

        val image = findViewById<ImageView>(R.id.qmgPreview)

        vm.frame.observe(this) {
            image.setImageBitmap(it)
        }

        var qmgData: ByteArray? = null
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            val isValid = contentResolver.openInputStream(intent.data!!)?.use { inputStream ->
                val header = ByteArray(2)
                val read = inputStream.read(header)
                if (read < 2) return@use false
                val magic = String(header)
                magic == "QM" || magic == "IM"
            } ?: false
            if (!isValid) {
                Log.e("QMG", "Invalid QMG file")
                finish()
                return
            }

            contentResolver.openInputStream(intent.data!!)?.use {
                qmgData = it.readBytes()
            }
        } else {
            val qmgPath = intent.getStringExtra("qmgPath")
            if (qmgPath != null) {
                qmgData = readSystemFile(qmgPath)
            }
        }

        qmgData?.let {
            if (it.isNotEmpty()) {
                val header = QmgHeader(it)
                vm.startQmg(
                    it,
                    header.width,
                    header.height,
                    header.frames,
                    header.bppType
                )
            }
        }
    }

    private fun readSystemFile(path: String): ByteArray {
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

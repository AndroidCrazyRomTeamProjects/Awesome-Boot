package org.crazyromteam.qmgstore.ui.qmgpreview

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import org.crazyromteam.qmgstore.R
import org.crazyromteam.qmgstore.qmg.utils.QmgHeader

class QmgPreviewActivity : AppCompatActivity() {

    private lateinit var vm: QmgPreviewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qmg_preview)

        vm = ViewModelProvider(this).get(QmgPreviewViewModel::class.java)

        val image = findViewById<ImageView>(R.id.qmgPreview)

        vm.frame.observe(this) {
            image.setImageBitmap(it)
        }

        val qmgPath = intent.getStringExtra("qmgPath")
        if (qmgPath != null) {
            val qmgData = readSystemFile(qmgPath)
            if (qmgData.isNotEmpty()) {
                val header = QmgHeader(qmgData)
                vm.startQmg(
                    qmgData,
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

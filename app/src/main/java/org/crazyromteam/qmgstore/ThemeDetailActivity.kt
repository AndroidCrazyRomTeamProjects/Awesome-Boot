package org.crazyromteam.qmgstore

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import org.crazyromteam.qmgstore.api.RetrofitClient
import org.crazyromteam.qmgstore.databinding.ActivityThemeDetailBinding
import org.crazyromteam.qmgstore.qmg.QmgPreviewExtractor
import org.crazyromteam.qmgstore.ui.qmgpreview.QmgPreviewActivity

class ThemeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityThemeDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThemeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val themeId = intent.getStringExtra("themeid")
        val themeName = intent.getStringExtra("themename")
        val themeCreator = intent.getStringExtra("themecreator")

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_theme_details)

        binding.detailName.text = themeName
        binding.detailCreator.text = themeCreator

        if (themeId != null) {
            checkAndSetupPreviews(themeId)
        }
    }

    private fun checkAndSetupPreviews(themeId: String) {
        lifecycleScope.launch {
            try {
                val bootFile = when {
                    isFileAvailable(themeId, "bootsamsung.qmg") -> "bootsamsung.qmg"
                    isFileAvailable(themeId, "bootsamsungloop.qmg") -> "bootsamsungloop.qmg"
                    else -> null
                }
                val shutdownFile = if (isFileAvailable(themeId, "shutdown.qmg")) "shutdown.qmg" else null

                if (bootFile != null) {
                    binding.bootPreviewCard.visibility = View.VISIBLE
                    loadFirstFrame(themeId, bootFile) { bitmap ->
                        binding.bootPreviewImage.setImageBitmap(bitmap)
                    }
                    binding.bootPreviewCard.setOnClickListener {
                        startFullscreenPreview(themeId, bootFile)
                    }
                }

                if (shutdownFile != null) {
                    binding.shutdownPreviewCard.visibility = View.VISIBLE
                    loadFirstFrame(themeId, shutdownFile) { bitmap ->
                        binding.shutdownPreviewImage.setImageBitmap(bitmap)
                    }
                    binding.shutdownPreviewCard.setOnClickListener {
                        startFullscreenPreview(themeId, shutdownFile)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadFirstFrame(themeId: String, fileName: String, onBitmapReady: (Bitmap) -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Request only the first 256KB to quickly decode the first frame
                val response = RetrofitClient.apiService.downloadFileRange("bytes=0-262143", themeId, fileName)
                if (response.isSuccessful) {
                    val bytes = response.body()?.bytes() ?: return@launch
                    val bitmap = QmgPreviewExtractor.getFirstFrame(bytes)
                    
                    if (bitmap != null) {
                        withContext(Dispatchers.Main) {
                            onBitmapReady(bitmap)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startFullscreenPreview(themeId: String, fileName: String) {
        val intent = Intent(this@ThemeDetailActivity, QmgPreviewActivity::class.java)
        intent.putExtra("theme_id", themeId)
        intent.putExtra("file_name", fileName)
        startActivity(intent)
    }

    private suspend fun isFileAvailable(themeId: String, fileName: String): Boolean {
        return try {
            val response = RetrofitClient.apiService.checkFileExists(themeId, fileName)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

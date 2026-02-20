package org.crazyromteam.qmgstore.ui.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import org.crazyromteam.qmgstore.R
import org.crazyromteam.qmgstore.ui.qmgpreview.QmgPreviewActivity

class MenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_menu, container, false)

        val bootanimpreviewButton: Button = root.findViewById(R.id.boot_preview_button)
        bootanimpreviewButton.setOnClickListener {
            val intent = Intent(activity, QmgPreviewActivity::class.java)
            intent.putExtra("qmgPath", "/system/media/bootsamsung.qmg")
            startActivity(intent)
        }
        val shutdownpreviewButton: Button = root.findViewById(R.id.shutdown_preview_button)
        shutdownpreviewButton.setOnClickListener {
            val intent = Intent(activity, QmgPreviewActivity::class.java)
            intent.putExtra("qmgPath", "/system/media/shutdown.qmg")
            startActivity(intent)
        }


        return root
    }
}

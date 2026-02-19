package org.crazyromteam.qmgstore.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.crazyromteam.qmgstore.R
import org.crazyromteam.qmgstore.databinding.FragmentHotBinding
import org.crazyromteam.qmgstore.ui.hotPics.HotPicsViewModel

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHotBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val hotPicsViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHotBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        hotPicsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }
    }

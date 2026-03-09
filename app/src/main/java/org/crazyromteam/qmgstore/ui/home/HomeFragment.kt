package org.crazyromteam.qmgstore.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import org.crazyromteam.qmgstore.R
import org.crazyromteam.qmgstore.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        val themeAdapter = ThemeAdapter()
        binding.themesRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = themeAdapter
        }

        binding.retryBtn.setOnClickListener {
            homeViewModel.fetchThemes()
        }

        homeViewModel.themes.observe(viewLifecycleOwner) { themes ->
            if (!themes.isNullOrEmpty()) {
                themeAdapter.updateData(themes)
                binding.themesRecyclerView.visibility = View.VISIBLE
                binding.errorContainer.visibility = View.GONE
            }
        }

        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) {
                binding.errorContainer.visibility = View.GONE
            }
        }

        homeViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                binding.errorContainer.visibility = View.VISIBLE
                binding.errorTextView.text = getString(R.string.error_loading_themes, errorMessage)
                binding.themesRecyclerView.visibility = View.GONE
            } else {
                binding.errorContainer.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

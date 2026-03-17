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
import android.content.Intent
import org.crazyromteam.qmgstore.ThemeDetailActivity

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

        val themeAdapter = ThemeAdapter({ startActivity(Intent(activity, ThemeDetailActivity::class.java)) })
        binding.themesRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = themeAdapter
        }

        binding.retryBtn.setOnClickListener {
            homeViewModel.fetchThemes()
        }

        fun updateUIState() {
            val isLoading = homeViewModel.isLoading.value == true
            val error = homeViewModel.error.value
            val themes = homeViewModel.themes.value

            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

            if (!error.isNullOrEmpty()) {
                binding.errorContainer.visibility = View.VISIBLE
                binding.errorTextView.text = getString(R.string.error_loading_themes, error)
                binding.themesRecyclerView.visibility = View.GONE
                binding.emptyContainer.visibility = View.GONE
            } else {
                binding.errorContainer.visibility = View.GONE

                if (!isLoading) {
                    if (themes.isNullOrEmpty()) {
                        binding.emptyContainer.visibility = View.VISIBLE
                        binding.themesRecyclerView.visibility = View.GONE
                    } else {
                        binding.emptyContainer.visibility = View.GONE
                        binding.themesRecyclerView.visibility = View.VISIBLE
                    }
                } else {
                    binding.emptyContainer.visibility = View.GONE
                    binding.themesRecyclerView.visibility = View.GONE
                }
            }
        }

        homeViewModel.themes.observe(viewLifecycleOwner) { themes ->
            themes?.let { themeAdapter.updateData(it) }
            updateUIState()
        }

        homeViewModel.isLoading.observe(viewLifecycleOwner) {
            updateUIState()
        }

        homeViewModel.error.observe(viewLifecycleOwner) {
            updateUIState()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

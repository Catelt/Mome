package com.catelt.mome.ui.home


import androidx.navigation.fragment.findNavController
import com.catelt.mome.R
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment<FragmentHomeBinding>(
    FragmentHomeBinding::inflate
) {
    init {
        isFullScreen = true
    }

    override fun setUpViews() {
        binding.apply {
            btnSearch.setOnClickListener{
                findNavController().navigate(R.id.searchFragment)
            }
        }
    }
}
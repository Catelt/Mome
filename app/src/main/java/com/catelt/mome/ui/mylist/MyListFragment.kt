package com.catelt.mome.ui.mylist

import androidx.navigation.fragment.findNavController
import com.catelt.mome.R
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.databinding.FragmentMyListBinding

class MyListFragment : BaseFragment<FragmentMyListBinding>(
    FragmentMyListBinding::inflate
) {
    override fun setUpViews() {
        binding.apply {
            btnSearch.setOnClickListener {
                findNavController().navigate(R.id.searchFragment)
            }
        }
    }
}
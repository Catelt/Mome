package com.catelt.mome.ui.home


import com.catelt.mome.core.BaseFragment
import com.catelt.mome.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment<FragmentHomeBinding>(
    FragmentHomeBinding::inflate
) {
    init {
        isFullScreen = true
    }
}
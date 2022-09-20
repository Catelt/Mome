package com.catelt.mome.ui.new

import com.catelt.mome.core.BaseFragment
import com.catelt.mome.databinding.FragmentNewHotBinding

class NewHotFragment : BaseFragment<FragmentNewHotBinding>(FragmentNewHotBinding::inflate) {
    private val adapter= UpComingAdapter()

    override fun setUpViews() {
        binding.apply {
            recyclerViewNewHotList.adapter = adapter
        }
    }
}
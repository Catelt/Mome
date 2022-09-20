package com.catelt.mome.ui.search


import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.catelt.mome.adapter.ListGridAdapter
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.databinding.FragmentSearchBinding

class SearchFragment : BaseFragment<FragmentSearchBinding>(FragmentSearchBinding::inflate) {
    private val topSearchAdapter = TopSearchAdapter()
    private val resultSearchAdapter = ListGridAdapter()

    override fun setUpViews() {
        binding.apply {
            recyclerListTopSearch.adapter = topSearchAdapter
            recyclerListResult.adapter = resultSearchAdapter

            layoutSearch.handleTextChange = {
                val isEmpty = it.isNullOrEmpty()
                layoutWhenEmptySearch.isVisible = isEmpty
                layoutWhenSearch.isVisible = !isEmpty
            }

            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }
}
package com.catelt.mome.ui.categories

import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.data.model.Genre
import com.catelt.mome.databinding.FragmentItemPickerBinding
import com.catelt.mome.utils.BUNDLE_ID_GENRE
import com.catelt.mome.utils.REQUEST_KEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ItemPickerFragment : BaseFragment<FragmentItemPickerBinding>(
    FragmentItemPickerBinding::inflate
) {
    override var isHideBottom = true
    override var isFullScreen = false
    override val viewModel: ItemPickerViewModel by viewModels()

    private val adapter = ItemPickerAdapter()

    override fun setUpViews() {
        binding.apply {
            recyclerViewItem.adapter = adapter
            adapter.onClicked = {
                lifecycleScope.launch {
                    sendData(it)
                    btnClose.callOnClick()
                }
            }
            btnClose.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    override fun setUpViewModel() {
        viewModel.apply {
            genreId.observe(viewLifecycleOwner) {
                it?.let {
                    adapter.select = it
                    adapter.notifyItemRangeChanged(0, adapter.itemCount)
                }
            }

            lifecycleScope.launch {
                moviesGenres.collectLatest {
                    if (it?.isNotEmpty() == true) {
                        val newList = it.toMutableList()
                        newList.add(
                            0, Genre(
                                0,
                                "All categories"
                            )
                        )
                        adapter.submitList(newList)
                    }
                }
            }
        }
    }

    private fun sendData(genreId: Int){
        setFragmentResult(REQUEST_KEY, bundleOf(BUNDLE_ID_GENRE to genreId))

    }
}
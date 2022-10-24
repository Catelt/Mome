package com.catelt.mome.ui.mylist

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.catelt.mome.R
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.data.model.MediaType
import com.catelt.mome.databinding.FragmentMyListBinding
import com.catelt.mome.ui.bottomsheet.MediaDetailsBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyListFragment : BaseFragment<FragmentMyListBinding>(
    FragmentMyListBinding::inflate
) {
    override val viewModel: MyListViewModel by viewModels()
    private val adapter =  MyListAdapter()


    override fun setUpViews() {
        binding.apply {
            recyclerViewMyList.adapter = adapter
            adapter.onMovieClicked ={ id, type ->
                MediaDetailsBottomSheet.newInstance(id,type == MediaType.Movie)
                    .show(requireActivity().supportFragmentManager, id.toString())
            }

            btnGoHome.setOnClickListener {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }

            btnProfile.setOnClickListener {
                findNavController().navigate(R.id.profileFragment)
            }

            btnSearch.setOnClickListener {
                findNavController().navigate(R.id.searchFragment)
            }
        }
    }

    override fun setUpViewModel() {
        viewModel.apply {
            lifecycleScope.launch {
                launch {
                    imageUrlParser.collectLatest { imageParser ->
                        adapter.imageUrlParser = imageParser
                    }
                }

                launch {
                    uiState.collectLatest { myListUIState ->
                        myListUIState.myList.collectLatest {
                            adapter.submitData(it)
                        }
                    }
                }

                launch {
                    adapter.loadStateFlow.collectLatest {
                        if (it.prepend is LoadState.NotLoading && it.prepend.endOfPaginationReached) {
                            binding.layoutEmpty.visibility = setVisionView(adapter.itemCount < 1)
                        }
                    }
                }
            }
        }
    }
}
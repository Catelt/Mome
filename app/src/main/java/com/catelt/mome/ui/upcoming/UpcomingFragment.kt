package com.catelt.mome.ui.upcoming

import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.catelt.mome.R
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.databinding.FragmentUpcomingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UpcomingFragment : BaseFragment<FragmentUpcomingBinding>(
    FragmentUpcomingBinding::inflate
) {
    override val viewModel: UpcomingViewModel by viewModels()

    private val adapter = UpcomingAdapter()

    override fun setUpViews() {
        binding.apply {
            shimmerLayout.startShimmer()
            recyclerViewNewHotList.adapter = adapter
            btnProfile.setOnClickListener {
                findNavController().navigate(R.id.profileFragment)
            }
        }
    }

    override fun setUpViewModel() {
        viewModel.apply {
            lifecycleScope.launch {
                launch {
                    imageUrlParser.collectLatest {
                        adapter.imageUrlParser = it
                    }
                }

                launch {
                    moviesGenres.collectLatest {
                        adapter.genres = it
                    }
                }

                launch {
                    adapter.loadStateFlow.collectLatest {
                        if (it.prepend is LoadState.NotLoading && it.prepend.endOfPaginationReached) {
                            binding.shimmerLayout.visibility = View.GONE
                        }
                    }
                }

                launch {
                    uiState.collectLatest { newHotUIState ->
                        newHotUIState.newHotState.upcoming.collectLatest {
                            adapter.submitData(it)
                        }
                    }
                }
            }
        }
    }
}